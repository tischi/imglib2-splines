package de.embl.cba.splines.utils;

import net.imglib2.RealPoint;

import java.util.ArrayList;

public class ExponentialSplineSurface {
    private int Ms;
    private int Mt;
    private RealPoint[] controlPoints;

    private int nDim=3;
    private double halfSupport=SplineBasis.ESPLINE3SUPPORT/2.0;
    // TODO: would be nice to have the basis function is an argument here so it is more generic

    private final double PIMs;
    private final double PIMt;
    private final double scale;
    private final double scaleMs;
    private final double scaleMt;

    public ExponentialSplineSurface(int Mt, int Ms){
        this.Mt = Mt;
        if (Mt < 3) {
            throw new IllegalArgumentException("Error: M needs to be equal or larger than 3 for longitudes.");
        }
        this.Ms = Ms;
        if (Ms < 4) {
            throw new IllegalArgumentException("Error: M needs to be equal or larger than 4 for latitudes.");
        }

        PIMt = Math.PI / Mt;
        PIMs = Math.PI / (Ms - 1);

        controlPoints = new RealPoint[Mt * (Ms - 2) + 6];
        scale = 1.0 / ((Ms-1) * SplineBasis.ESpline3Prime(1.0, PIMs));
        scaleMs = 2.0 * (1.0 - Math.cos(PIMs)) / (Math.cos(PIMs / 2) - Math.cos(3.0 * PIMs / 2));
        scaleMt = 2.0 * (1.0 - Math.cos(2.0 * PIMt)) / (Math.cos(PIMt) - Math.cos(3.0 * PIMt));
    }

    public int getMt()
    {
        return Mt;
    }

    public int getMs()
    {
        return Ms;
    }

    public void initializeDefaultShape( double radius, RealPoint center) {
        double x0 = center.getDoublePosition(0);
        double y0 = center.getDoublePosition(1);
        double z0 = center.getDoublePosition(2);

        for (int k = 0; k < Mt; k++) {
            for (int l = 1; l <= Ms - 2; l++) {
                double theta = PIMs * l;
                double phi = 2.0 * PIMt * k;

                controlPoints[k + (l - 1) * Mt] = new RealPoint(x0 + radius * scaleMs * scaleMt * Math.sin(theta) * Math.cos(phi),
                        y0 + radius * scaleMs * scaleMt * Math.sin(theta) * Math.sin(phi), z0 + radius * scaleMs * Math.cos(theta));
            }
        }
        // north pole
        controlPoints[Mt * (Ms - 2)] = new RealPoint(x0, y0, z0 + radius);
        // south pole
        controlPoints[Mt * (Ms - 2) + 1] = new RealPoint(x0, y0, z0 - radius);
        // north tangent plane
        controlPoints[Mt * (Ms - 2) + 2] = new RealPoint(x0 + Math.PI * radius, y0, z0 + radius);
        controlPoints[Mt * (Ms - 2) + 3] = new RealPoint(x0, y0 + Math.PI * radius, z0 + radius);
        // south tangent plane
        controlPoints[Mt * (Ms - 2) + 4] = new RealPoint(x0 + Math.PI * radius, y0, z0 - radius);
        controlPoints[Mt * (Ms - 2) + 5] = new RealPoint(x0, y0 + Math.PI * radius, z0 - radius);
    }

    public void initializeDefaultShape( double width, double height, double depth){
        initializeDefaultShape( Math.min(Math.min(width / 5.0, height / 5.0),
                depth / 5.0), new RealPoint (width / 2.0, height / 2.0, depth / 2.0));
    }

    public ArrayList<RealPoint> getControlPoints(){
        ArrayList<RealPoint> controlPointsArray= new ArrayList<RealPoint>();
        for (int k = 0; k < Mt; k++) {
            for (int l = 1; l <= Ms - 2; l++) {
                controlPointsArray.add(controlPoints[k + (l - 1) * Mt]);
            }
        }
        controlPointsArray.add(controlPoints[Mt * (Ms - 2)]);
        controlPointsArray.add(controlPoints[Mt * (Ms - 2) + 1]);

        // return new ArrayList<>(Arrays.asList(controlPoints));
        return controlPointsArray;
    }

    public ArrayList<RealPoint> getSampledSurface(){
        // TODO: come up with an adaptive way to sample depending on the size of the patch (compute arclength)
        int samplingRateS=2;
        int samplingRateT=2;

        ArrayList<RealPoint> surface= new ArrayList<RealPoint>();
        for (int nt = 0; nt < Mt*samplingRateT; nt++) {
            for (int ns = 0; ns <= (Ms-1)*samplingRateS; ns++) {
                final double t = ( double ) nt / ( double ) samplingRateT;
                final double s = ( double ) ns / ( double ) samplingRateS;
                surface.add(parametersToWorld( t, s ));
            }
        }
        return surface;
    }

    public RealPoint parametersToWorld(double t, double s){
        RealPoint point = new RealPoint(0.0,0.0,0.0);

        addNonPoleContributions( t, s, point);
        addNorthPoleContribution( t, s, point);
        addSouthPoleContribution( t, s, point);

        return point;
    }

    private void addSouthPoleContribution( double t, double s, RealPoint point)
    {
        // South tangent plane
        double[] SouthV1=new double[nDim];
        double[] SouthV2=new double[nDim];
        for(int d=0; d<nDim; d++) {
            SouthV1[d] = controlPoints[Mt * (Ms - 2) + 4].getDoublePosition(d) - controlPoints[Mt * (Ms - 2) + 1].getDoublePosition(d);
            SouthV2[d] = controlPoints[Mt * (Ms - 2) + 5].getDoublePosition(d) - controlPoints[Mt * (Ms - 2) + 1].getDoublePosition(d);
        }

        // l = M+1
        double sVal=s-(Ms-1)-1;
        if (sVal > -halfSupport && sVal < halfSupport) {
            for (int k = 0; k < Mt; k++) {
                double tVal=wrapIndexT(t, k);
                if (tVal > -halfSupport && tVal < halfSupport) {
                    // compute c[k,M+1]
                    double[] ckMplus=new double[nDim];
                    for(int d=0; d<nDim; d++)
                        ckMplus[d] = (controlPoints[k + (Ms - 3) * Mt].getDoublePosition(d)
                            + scale * scaleMt * (Math.cos(2*PIMt*k) * SouthV1[d] + Math.sin(2*PIMt*k) * SouthV2[d]));

                    double basisFactor = SplineBasis.ESpline3(sVal, PIMs) * SplineBasis.ESpline3(tVal,2.0*PIMt);
                    double[] pointValue=new double[nDim];
                    for(int d=0; d<nDim;d++)
                        pointValue[d] = point.getDoublePosition(d) + (ckMplus[d] * basisFactor);
                    point.setPosition(pointValue);
                }
            }
        }

        // l = M
        sVal=s-(Ms-1);
        if (sVal > -halfSupport && sVal < halfSupport) {
            for (int k = 0; k < Mt; k++) {
                double tVal=wrapIndexT(t, k);
                if (tVal > -halfSupport && tVal < halfSupport) {
                    // compute c[k,M+1]
                    double[] ckMplus=new double[nDim];
                    for(int d=0; d<nDim; d++)
                        ckMplus[d] = (controlPoints[k + (Ms - 3) * Mt].getDoublePosition(d)
                            + scale * scaleMt * (Math.cos(2 * PIMt * k) * SouthV1[d] + Math.sin(2 * PIMt * k) * SouthV2[d]));

                    // compute c[k,M]
                    double[] ckM=new double[nDim];
                    for(int d=0; d<nDim; d++)
                        ckM[d] = (controlPoints[Mt * (Ms - 2) + 1].getDoublePosition(d)
                            - SplineBasis.ESpline3(1, PIMs) * (ckMplus[d] + controlPoints[k + (Ms - 3) * Mt].getDoublePosition(d)))
                            / SplineBasis.ESpline3(0, PIMs);

                    double basisFactor = SplineBasis.ESpline3(sVal, PIMs) * SplineBasis.ESpline3(tVal, 2.0 * PIMt);
                    double[] pointValue=new double[nDim];
                    for(int d=0; d<nDim;d++)
                        pointValue[d] = point.getDoublePosition(d) + (ckM[d] * basisFactor);
                    point.setPosition(pointValue);
                }
            }
        }
    }

    private void addNorthPoleContribution( double t, double s, RealPoint point)
    {
        // North tangent plane
        double[] NorthV1=new double[nDim];
        double[] NorthV2=new double[nDim];
        for(int d=0; d<nDim; d++){
            NorthV1[d] = controlPoints[Mt * (Ms - 2) + 2].getDoublePosition(d) - controlPoints[Mt * (Ms - 2)].getDoublePosition(d);
            NorthV2[d] = controlPoints[Mt * (Ms - 2) + 3].getDoublePosition(d) - controlPoints[Mt * (Ms - 2)].getDoublePosition(d);
        }

        // l = -1
        double sVal = s+1.0;
        if (sVal > -halfSupport && sVal < halfSupport ) {
            for (int k = 0; k < Mt; k++) {
                double tVal=wrapIndexT(t, k);
                if (tVal > -halfSupport && tVal < halfSupport) {
                    // compute c[k,-1]
                    double[] ckminus1=new double[nDim];
                    for(int d=0; d<nDim; d++)
                        ckminus1[d] = (controlPoints[k].getDoublePosition(d) + scale * scaleMt * (Math.cos(2*PIMt*k) * NorthV1[d] + Math.sin(2*PIMt*k) * NorthV2[d]));

                    double basisFactor = SplineBasis.ESpline3(sVal,PIMs) * SplineBasis.ESpline3(tVal,2.0*PIMt);
                    double[] pointValue=new double[nDim];
                    for(int d=0; d<nDim;d++)
                        pointValue[d] = point.getDoublePosition(d) + (ckminus1[d] * basisFactor);
                    point.setPosition(pointValue);
                }
            }
        }

        // l = 0
        sVal=s;
        if (sVal > -halfSupport && sVal < halfSupport) {
            for (int k = 0; k < Mt; k++) {
                double tVal=wrapIndexT(t, k);
                if (tVal > -halfSupport && tVal < halfSupport) {
                    // compute c[k,-1]
                    double[] ckminus1=new double[nDim];
                    for(int d=0; d<nDim; d++)
                        ckminus1[d] = (controlPoints[k].getDoublePosition(d) + scale * scaleMt * (Math.cos(2*PIMt*k) * NorthV1[d] + Math.sin(2*PIMt*k) * NorthV2[d]));

                    // compute c[k,0]
                    double[] ck0=new double[nDim];
                    for(int d=0; d<nDim; d++)
                        ck0[d] = (controlPoints[Mt * (Ms - 2)].getDoublePosition(d)
                            - SplineBasis.ESpline3(1,PIMs) * (ckminus1[d] + controlPoints[k].getDoublePosition(d)))
                            / SplineBasis.ESpline3(0,PIMs);

                    double basisFactor = SplineBasis.ESpline3(sVal,PIMs) * SplineBasis.ESpline3(tVal,2.0*PIMt);
                    double[] pointValue=new double[nDim];
                    for(int d=0; d<nDim;d++)
                        pointValue[d] = point.getDoublePosition(d) + (ck0[d] * basisFactor);
                    point.setPosition(pointValue);
                }
            }
        }
    }

    private void addNonPoleContributions( double t, double s, RealPoint point )
    {
        // Everything but the poles
        for (int l = 1; l <= Ms - 2; l++) {
            double sVal = s-l;
            if (sVal > -halfSupport && sVal < halfSupport) {
                for (int k = 0; k < Mt; k++) {
                    double tVal=wrapIndexT(t, k);
                    if (tVal > -halfSupport && tVal < halfSupport){
                        double[] pointValue=new double[nDim];
                        for(int d=0; d<nDim;d++)
                            pointValue[d] = point.getDoublePosition(d) + controlPoints[k + (l - 1) * Mt].getDoublePosition(d) * SplineBasis.ESpline3(sVal,PIMs) * SplineBasis.ESpline3(tVal,2.0*PIMt);
                        point.setPosition(pointValue);
                    }
                }
            }
        }
    }

    private double wrapIndexT(double t, int k){
        double tVal=t-k;
        if (k < t-halfSupport) {
            if (k + Mt >= t - halfSupport && k + Mt <= t + halfSupport)
                tVal = t - (k + Mt);
        } else if (k > t+halfSupport) {
            if (k - Mt >= t - halfSupport && k - Mt <= t + halfSupport)
                tVal = t - (k - Mt);
        }
        return tVal;
    }
}
