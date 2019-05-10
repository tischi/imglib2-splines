package de.embl.cba.splines.controlpoints;

import net.imglib2.FinalRealInterval;
import net.imglib2.RealInterval;
import net.imglib2.RealPoint;
import net.imglib2.realtransform.AffineTransform3D;
import org.scijava.ui.behaviour.DragBehaviour;

final class DragControlPointBehaviour implements DragBehaviour
{
	private final ControlPointsOverlay pointsOverlay;

	private boolean moving = false;

	private final AbstractControlPointsModel model;

	private final double[] initMin = new double[ 3 ];

	private final double[] initMax = new double[ 3 ];

	private final double[] initPoint = new double[ 3 ];

	private int pointId;
	private RealPoint realPoint;

	public DragControlPointBehaviour( final ControlPointsOverlay pointsOverlay, final AbstractControlPointsModel model )
	{
		this.pointsOverlay = pointsOverlay;
		this.model = model;
	}

	@Override
	public void init( final int x, final int y )
	{
		pointId = pointsOverlay.getHighlightedPointIndex();
		if ( pointId < 0 )
			return;

		realPoint = model.getPoints().get( pointId );

		moving = true;
	}

	private final AffineTransform3D transform = new AffineTransform3D();

	@Override
	public void drag( final int x, final int y )
	{
		if ( !moving )
			return;

		pointsOverlay.getPointsToViewerTransform( transform );

		final double[] gPos = new double[ realPoint.numDimensions() ];
		final double[] position = new double[ realPoint.numDimensions() ];
		for ( int d = 0; d < realPoint.numDimensions() ; d++ )
			position[ d ] = realPoint.getDoublePosition(d);
		transform.apply( position, gPos );

		final double[] lPos = pointsOverlay.renderPointsHelper.reproject( x, y, gPos[ 2 ] );
		transform.applyInverse( gPos, lPos );

		RealPoint newPoint=new RealPoint();
		realPoint.setPosition(gPos);
		model.getPoints().set( pointId, newPoint);
	}

	@Override
	public void end( final int x, final int y )
	{
		moving = false;
	}
}
