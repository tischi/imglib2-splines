package de.embl.cba.splines.controlpoints;

import bdv.tools.boundingbox.TransformedBox;
import net.imglib2.realtransform.AffineTransform3D;
import net.imglib2.ui.OverlayRenderer;
import net.imglib2.ui.TransformListener;
import org.scijava.listeners.ChangeListener;
import org.scijava.listeners.ListenableVar;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.MouseMotionListener;
import java.awt.geom.Ellipse2D;
import java.awt.geom.GeneralPath;

import static bdv.tools.boundingbox.TransformedBoxOverlay.BoxDisplayMode.FULL;


public class ControlPointsOverlay implements OverlayRenderer, TransformListener< AffineTransform3D >
{
	private static final double DISTANCE_TOLERANCE = 20.;

	private static final double HANDLE_RADIUS = DISTANCE_TOLERANCE / 2.;
	private final ControlPoints controlPoints;

	/**
	 * Specifies whether to show 3D wireframe box ({@code FULL}), or only
	 * intersection with viewer plane ({@code SECTION}).
	 */
	public enum BoxDisplayMode
	{
		FULL, SECTION;
	}

	public interface HighlightedCornerListener
	{
		void highlightedCornerChanged();
	}

	private final Color backColor = new Color( 0x00994499 );

	private final Color frontColor = Color.GREEN;

	private final Stroke normalStroke = new BasicStroke();

	private final Stroke intersectionStroke = new BasicStroke( 1f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 1f, new float[] { 10f, 10f }, 0f );

	private final Color intersectionColor = Color.WHITE.darker();

	private Color intersectionFillColor = new Color( 0x88994499, true );

	private final AffineTransform3D viewerTransform;

	private final AffineTransform3D transform;

	final RenderPointsHelper renderPointsHelper;

	private final PointsHighlighter pointsHighlighter;

	private double sourceSize = 5000;

	private double perspective = 0.5;

	private int canvasWidth;

	private int canvasHeight;

	private final ListenableVar< BoxDisplayMode, ChangeListener > displayMode = ListenableVar.create( FULL );

	private boolean showCornerHandles = true;

	private boolean fillIntersection = true;

	private int cornerId = -1;

	private bdv.tools.boundingbox.TransformedBoxOverlay.HighlightedCornerListener highlightedCornerListener;

	public ControlPointsOverlay( final ControlPoints controlPoints )
	{
		this.controlPoints = controlPoints;

		viewerTransform = new AffineTransform3D();
		transform = new AffineTransform3D();
		renderPointsHelper = new RenderPointsHelper();
		pointsHighlighter = new PointsHighlighter( DISTANCE_TOLERANCE );
	}

	/**
	 * Sets the perspective value. {@code perspective < 0} means parallel
	 * projection.
	 *
	 * @param perspective
	 *            the perspective value.
	 */
	public void setPerspective( final double perspective )
	{
		this.perspective = perspective;
	}

	public void showCornerHandles( final boolean showCornerHandles )
	{
		this.showCornerHandles = showCornerHandles;
	}

	public void fillIntersection( final boolean fillIntersection )
	{
		this.fillIntersection = fillIntersection;
	}

	public boolean getFillIntersection()
	{
		return fillIntersection;
	}

	public void setIntersectionFillColor( final Color intersectionFillColor )
	{
		this.intersectionFillColor = intersectionFillColor;
	}

	@Override
	public void drawOverlays( final Graphics g )
	{
		final Graphics2D graphics = ( Graphics2D ) g;

		final GeneralPath front = new GeneralPath();
		final GeneralPath back = new GeneralPath();
		final GeneralPath intersection = new GeneralPath();

		final double ox = canvasWidth / 2;
		final double oy = canvasHeight / 2;

		synchronized ( viewerTransform )
		{
			transform.preConcatenate( viewerTransform );
		}

		renderPointsHelper.setPerspectiveProjection( perspective > 0 );
		renderPointsHelper.setDepth( perspective * sourceSize );
		renderPointsHelper.setOrigin( ox, oy );
		renderPointsHelper.setScale( 1 );
		renderPointsHelper.renderPoints( controlPoints.getPoints(), transform, front, back, intersection );

		graphics.setRenderingHint( RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON );

		if ( displayMode.get() == FULL )
		{
			graphics.setStroke( normalStroke );
			graphics.setPaint( backColor );
			graphics.draw( back );
		}

		if ( fillIntersection )
		{
			graphics.setPaint( intersectionFillColor );
			graphics.fill( intersection );
		}

		graphics.setPaint( intersectionColor );
		graphics.setStroke( intersectionStroke );
		graphics.draw( intersection );

		if ( displayMode.get() == FULL )
		{
			graphics.setStroke( normalStroke );
			graphics.setPaint( frontColor );
			graphics.draw( front );

			if ( showCornerHandles )
			{
				final int id = getHighlightedPointIndex();
				if ( id >= 0 )
				{
					final double[] p = renderPointsHelper.projectedPoints[ id ];
					final Ellipse2D cornerHandle = new Ellipse2D.Double(
							p[ 0 ] - HANDLE_RADIUS,
							p[ 1 ] - HANDLE_RADIUS,
							2 * HANDLE_RADIUS, 2 * HANDLE_RADIUS );
					final double z = renderPointsHelper.transformedPoints[ cornerId ][ 2 ];
					final Color cornerColor = ( z > 0 ) ? backColor : frontColor;

					graphics.setColor( cornerColor );
					graphics.fill( cornerHandle );
					graphics.setColor( cornerColor.darker().darker() );
					graphics.draw( cornerHandle );
				}
			}
		}
	}

	@Override
	public void setCanvasSize( final int width, final int height )
	{
		this.canvasWidth = width;
		this.canvasHeight = height;
	}

	@Override
	public void transformChanged( final AffineTransform3D t )
	{
		synchronized ( viewerTransform )
		{
			viewerTransform.set( t );
		}
	}

	/**
	 * Get/set {@code BoxDisplayMode}, which specifies whether to show 3D
	 * wireframe box ({@code FULL}), or only intersection with viewer plane
	 * ({@code SECTION}).
	 */
	public ListenableVar< bdv.tools.boundingbox.TransformedBoxOverlay.BoxDisplayMode, ChangeListener > boxDisplayMode()
	{
		return displayMode;
	}

	/**
	 * Get the transformation from the local coordinate frame of the
	 * {@link TransformedBox} to viewer coordinates.
	 *
	 * @param t is set to the box-to-viewer transform.
	 */
	public void getPointsToViewerTransform( final AffineTransform3D t )
	{
		synchronized ( viewerTransform ) // not a typo, all transform modifications synchronize on viewerTransform
		{
			t.set( transform );
		}
	}

	/**
	 * Get the index of the highlighted corner (if any).
	 *
	 * @return corner index or {@code -1} if no corner is highlighted
	 */
	public int getHighlightedPointIndex()
	{
		return cornerId;
	}

	/**
	 * Returns a {@code MouseMotionListener} that can be installed into a bdv
	 * (see {@code ViewerPanel.getDisplay().addHandler(...)}). If installed, it
	 * will notify a {@code HighlightedCornerListener} (see
	 * {@link #setHighlightedCornerListener(bdv.tools.boundingbox.TransformedBoxOverlay.HighlightedCornerListener)}) when
	 * the mouse is over a corner of the box (with some tolerance)/
	 *
	 * @return a {@code MouseMotionListener} implementing mouse-over for box
	 *         corners
	 */
	public MouseMotionListener getPointsHighlighter()
	{
		return pointsHighlighter;
	}

	public void setHighlightedCornerListener( final bdv.tools.boundingbox.TransformedBoxOverlay.HighlightedCornerListener highlightedCornerListener )
	{
		this.highlightedCornerListener = highlightedCornerListener;
	}

	/**
	 * Set the index of the highlighted corner.
	 *
	 * @param id
	 *            corner index, {@code -1} means that no corner is highlighted.
	 */
	private void setHighlightedCorner( final int id )
	{
		final int oldId = cornerId;
		cornerId = ( id >= 0 && id < RenderPointsHelper.numPoints ) ? id : -1;
		if ( cornerId != oldId && highlightedCornerListener != null )
			highlightedCornerListener.highlightedCornerChanged();
	}

	/**
	 * Sets the source size (estimated maximum dimension) in global coordinates.
	 * Used for setting up perspective projection.
	 *
	 * @param sourceSize
	 *            estimated source size in global coordinates
	 */
	public void setSourceSize( final double sourceSize )
	{
		this.sourceSize = sourceSize;
	}

	private class PointsHighlighter extends MouseMotionAdapter
	{
		private final double squTolerance;

		PointsHighlighter( final double tolerance )
		{
			squTolerance = tolerance * tolerance;
		}

		@Override
		public void mouseMoved( final MouseEvent e )
		{
			final int x = e.getX();
			final int y = e.getY();

			// TODO
			final int numPoints = renderPointsHelper.transformedPoints.length;

			for ( int i = 0; i < numPoints; i++ )
			{
				final double[] point = renderPointsHelper.projectedPoints[ i ];
				final double dx = x - point[ 0 ];
				final double dy = y - point[ 1 ];
				final double dr2 = dx * dx + dy * dy;
				if ( dr2 < squTolerance )
				{
					setHighlightedCorner( i );
					return;
				}
			}
			setHighlightedCorner( -1 );
		}
	}
}
