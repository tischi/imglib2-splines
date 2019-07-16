package de.embl.cba.splines.controlpoints;

import net.imglib2.FinalRealInterval;
import net.imglib2.RealInterval;
import net.imglib2.RealPoint;
import net.imglib2.realtransform.AffineTransform3D;
import org.scijava.ui.behaviour.DragBehaviour;

final class DragSingleControlPointBehaviour implements DragBehaviour
{
	private final ControlPointsOverlay pointsOverlay;

	private boolean moving = false;

	private final AbstractControlPointsModel model;

	private final AffineTransform3D transform = new AffineTransform3D();

	private int pointId;
	private RealPoint realPoint;

	public DragSingleControlPointBehaviour(final ControlPointsOverlay pointsOverlay, final AbstractControlPointsModel model )
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

		model.setPointPosition(gPos, pointId);
	}

	@Override
	public void end( final int x, final int y )
	{
		moving = false;
	}
}
