package de.embl.cba.splines.controlpoints;

import net.imglib2.RealPoint;
import net.imglib2.realtransform.AffineTransform3D;
import org.scijava.ui.behaviour.DragBehaviour;

public class DragAllControlPointsBehaviour implements DragBehaviour {

    private final ControlPointsOverlay pointsOverlay;

    private boolean moving = false;

    private final AbstractControlPointsModel model;

    private final AffineTransform3D transform = new AffineTransform3D();

    private RealPoint origin;

    public DragAllControlPointsBehaviour(final ControlPointsOverlay pointsOverlay, final AbstractControlPointsModel model )
    {
        this.pointsOverlay = pointsOverlay;
        this.model = model;
    }

    @Override
    public void init( final int x, final int y )
    {
        origin=new RealPoint(x,y);
        moving = true;
    }

    @Override
    public void drag( final int x, final int y )
    {
        if ( !moving )
            return;

        RealPoint displacementVector=new RealPoint(x-origin.getDoublePosition(0),y-origin.getDoublePosition(1));

        pointsOverlay.getPointsToViewerTransform( transform );

        for(int i=0; i<model.getPoints().size(); i++) {
            RealPoint realPoint=model.getPoints().get(i);
            final double[] gPos = new double[realPoint.numDimensions()];
            final double[] position = new double[realPoint.numDimensions()];
            for (int d = 0; d < realPoint.numDimensions(); d++)
                position[d] = realPoint.getDoublePosition(d);
            transform.apply(position, gPos);
            final double[] lPos = pointsOverlay.renderPointsHelper.reproject( gPos[0]+displacementVector.getDoublePosition(0), gPos[1]+displacementVector.getDoublePosition(1), gPos[ 2 ] );
            transform.applyInverse(gPos, lPos);
            model.setPointPosition(gPos, i);
        }

        origin=new RealPoint(x,y);
    }

    @Override
    public void end( final int x, final int y )
    {
        moving = false;
    }
}
