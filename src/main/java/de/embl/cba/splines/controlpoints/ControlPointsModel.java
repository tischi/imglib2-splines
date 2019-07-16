package de.embl.cba.splines.controlpoints;

import net.imglib2.RealPoint;
import net.imglib2.realtransform.AffineTransform3D;

import java.util.List;

public class ControlPointsModel extends AbstractControlPointsModel {
    private final List<RealPoint> points;

    public ControlPointsModel(
            final List<RealPoint> points,
            final AffineTransform3D transform) {
        super(transform);
        this.points = points;
    }

    @Override
    public List<RealPoint> getPoints() {
        return points;
    }

    @Override
    public void setPoints(final List< RealPoint > points) {
        if(points.size()!=this.points.size())
            return;
        else{
            for(int i=0; i<points.size(); i++)
                this.points.set(i,points.get(i));
            notifyPointsChanged();
        }

    }

    @Override
    public void setPointPosition(double[] position, final int index) {
        this.points.get(index).setPosition(position);
        notifyPointsChanged();
    }
}