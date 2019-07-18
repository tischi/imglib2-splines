package de.embl.cba.splines.controlpoints;

import net.imglib2.realtransform.AffineTransform3D;
import net.imglib2.ui.OverlayRenderer;
import net.imglib2.ui.TransformListener;

import java.awt.*;
import java.awt.event.KeyListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.MouseMotionListener;
import java.awt.geom.Ellipse2D;


public class ControlPointsOverlay implements OverlayRenderer, TransformListener< AffineTransform3D >
{
	private static final double DISTANCE_TOLERANCE = 20.;

	private static final double HANDLE_RADIUS = DISTANCE_TOLERANCE / 2.;
	private final ControlPoints controlPoints;

	public interface HighlightedPointListener
	{
		void highlightedPointChanged();
	}

	private final Stroke normalStroke = new BasicStroke();

	private final Color backColor = new Color( 0x00994499 );

	private final Color frontColor = Color.GREEN;

	private final Color highlightedPointColor = Color.BLUE;

	private final AffineTransform3D viewerTransform;

	private final AffineTransform3D transform;

	final RenderPointsHelper renderPointsHelper;

	private final PointHighlighter pointHighlighter;

	private double sourceSize = 5000;

	private double perspective = 0.5;

	private int canvasWidth;

	private int canvasHeight;

	private int pointId = -1;

	private boolean allHighlighted = false;

	private ControlPointsOverlay.HighlightedPointListener highlightedPointListener;

	public ControlPointsOverlay( final ControlPoints controlPoints )
	{
		this.controlPoints = controlPoints;

		viewerTransform = new AffineTransform3D();
		transform = new AffineTransform3D();
		renderPointsHelper = new RenderPointsHelper(this.controlPoints.getPoints().size());
		pointHighlighter = new PointHighlighter( DISTANCE_TOLERANCE );
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

	@Override
	public void drawOverlays( final Graphics g )
	{
		final Graphics2D graphics = ( Graphics2D ) g;

		final double ox = canvasWidth / 2;
		final double oy = canvasHeight / 2;

		synchronized ( viewerTransform )
		{
			controlPoints.getTransform(transform);
			transform.preConcatenate( viewerTransform );
		}

		//renderPointsHelper.setPerspectiveProjection( perspective > 0 );
		renderPointsHelper.setDepth( perspective * sourceSize );
		renderPointsHelper.setOrigin( ox, oy );
		renderPointsHelper.renderPoints( controlPoints.getPoints(), transform );

		graphics.setRenderingHint( RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON );

		graphics.setStroke( normalStroke );
		graphics.setPaint( frontColor );

		for(int i=0; i<controlPoints.getPoints().size(); i++){
			final double[] p = renderPointsHelper.projectedPoints[i];
			final Ellipse2D pointHandle = new Ellipse2D.Double(
					p[0] - HANDLE_RADIUS,
					p[1] - HANDLE_RADIUS,
					2 * HANDLE_RADIUS, 2 * HANDLE_RADIUS);
			final double z = renderPointsHelper.transformedPoints[i][2];
			final Color pointColor = (z > 0) ? backColor : frontColor;

			if(i==pointId || allHighlighted)
				graphics.setColor(highlightedPointColor);
			else
				graphics.setColor(pointColor);
			graphics.fill(pointHandle);
			graphics.setColor(pointColor.darker().darker());
			graphics.draw(pointHandle);
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
	 * Get the transformation from the local coordinate frame of the
	 * {@link ControlPoints} to viewer coordinates.
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
	 * Get the index of the highlighted point (if any).
	 *
	 * @return point index or {@code -1} if no point is highlighted
	 */
	public int getHighlightedPointIndex()
	{
		return pointId;
	}

	public boolean allHighlighted()
	{
		return allHighlighted;
	}

	/**
	 * Returns a {@code MouseMotionListener} that can be installed into a bdv
	 * (see {@code ViewerPanel.getDisplay().addHandler(...)}). If installed, it
	 * will notify a {@code HighlightedPointListener} (see
	 * {@link #setHighlightedPointListener(ControlPointsOverlay.HighlightedPointListener)}) when
	 * the mouse is over a point (with some tolerance)/
	 *
	 * @return a {@code MouseMotionListener} implementing mouse-over for box
	 *         corners
	 */
	public MouseMotionListener getPointHighlighter()
	{
		return pointHighlighter;
	}

	public void setHighlightedPointListener( final HighlightedPointListener highlightedPointListener )
	{
		this.highlightedPointListener = highlightedPointListener;
	}

	/**
	 * Set the index of the highlighted point.
	 *
	 * @param id
	 *            corner index, {@code -1} means that no corner is highlighted.
	 */
	private void setHighlightedPoint( final int id )
	{
		final int oldId = pointId;
		pointId = ( id >= 0 && id < controlPoints.getPoints().size() ) ? id : -1;
		if ( pointId != oldId && highlightedPointListener != null )
			highlightedPointListener.highlightedPointChanged();
	}

	private void setAllHighlighted( final boolean highlight )
	{
		if ( allHighlighted!=highlight ) {
			allHighlighted=highlight;
			highlightedPointListener.highlightedPointChanged();
		}
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

	private class PointHighlighter extends MouseMotionAdapter implements KeyListener
	{
		private final double squTolerance;

		PointHighlighter( final double tolerance )
		{
			squTolerance = tolerance * tolerance;
		}

		@Override
		public void mouseMoved( final MouseEvent e )
		{
			final int x = e.getX();
			final int y = e.getY();

			if(!allHighlighted) {
				final int numPoints = renderPointsHelper.numPoints;
				for (int i = 0; i < numPoints; i++) {
					final double[] point = renderPointsHelper.projectedPoints[i];
					final double dx = x - point[0];
					final double dy = y - point[1];
					final double dr2 = dx * dx + dy * dy;
					if (dr2 < squTolerance) {
						setHighlightedPoint(i);
						return;
					}
				}
			}
				setHighlightedPoint(-1);
		}

		@Override
		public void keyPressed(KeyEvent e) {
			if(e.getKeyCode() == KeyEvent.VK_SHIFT && pointId>=0)
				setAllHighlighted(true);
		}

		@Override
		public void keyReleased(KeyEvent e) {
			if(e.getKeyCode() == KeyEvent.VK_SHIFT)
				setAllHighlighted(false);
		}

		@Override
		public void keyTyped(KeyEvent e) {
		}
	}
}
