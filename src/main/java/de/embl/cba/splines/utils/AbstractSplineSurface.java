package de.embl.cba.splines.utils;

import net.imglib2.RealPoint;

import java.util.ArrayList;

public abstract class AbstractSplineSurface {
    protected int Ms;
    protected int Mt;

    protected RealPoint[] controlPoints;

    protected int nDim=3;
    protected double support;
    protected double halfSupport;

    public AbstractSplineSurface(int Mt, int Ms, SplineBasis.BASIS basis){
        switch(basis){
            case LINEARBSPLINE:
                support=SplineBasis.LINEARBSPLINESUPPORT;
                break;
            case QUADRATICBSPLINE:
                support=SplineBasis.QUADRATICBSPLINESUPPORT;
                break;
            case CUBICBSPLINE:
                support=SplineBasis.CUBICBSPLINESUPPORT;
                break;
            case ESPLINE3:
                support=SplineBasis.ESPLINE3SUPPORT;
                break;
            case HSPLINE:
                support=SplineBasis.HSPLINESUPPORT;
                break;
        }

        halfSupport=support/2.0;

        if (Mt < support) {
            throw new IllegalArgumentException("Error: M needs to be equal or larger than "+support+" for longitudes.");
        }
        this.Mt=Mt;
        if (Ms < support+1) {
            throw new IllegalArgumentException("Error: M needs to be equal or larger than "+(support+1)+" for latitudes.");
        }
        this.Ms=Ms;
    }

    public int getMt()
    {
        return Mt;
    }

    public int getMs()
    {
        return Ms;
    }

    public void initializeDefaultShape( double width, double height, double depth){
        initializeDefaultShape( Math.min(Math.min(width / 5.0, height / 5.0),depth / 5.0), new RealPoint (width / 2.0, height / 2.0, depth / 2.0)); //
    }

    public abstract void initializeDefaultShape( double radius, RealPoint center);

    public abstract ArrayList<RealPoint> getControlPoints();

    public abstract ArrayList<RealPoint> getSampledSurface();

    public abstract RealPoint parametersToWorld(double t, double s);

    protected double wrapIndex(double t, int k, int M){
        double tVal=t-k;
        if (k < t-halfSupport) {
            if (k + M >= t - halfSupport && k + M <= t + halfSupport)
                tVal = t - (k + M);
        } else if (k > t+halfSupport) {
            if (k - M >= t - halfSupport && k - M <= t + halfSupport)
                tVal = t - (k - M);
        }
        return tVal;
    }
}
