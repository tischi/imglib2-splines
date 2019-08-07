package de.embl.cba.splines.utils;

import net.imglib2.RealPoint;

import java.util.ArrayList;

public class FirstOrderHermiteSplineSurface extends AbstractSplineSurface{
    private final double PIMs;
    private final double PIMt;

    public FirstOrderHermiteSplineSurface(int Mt, int Ms){
        super(Mt,Ms,SplineBasis.BASIS.HSPLINE);

        PIMt = Math.PI / Mt;
        PIMs = Math.PI / (Ms-1);

        controlPoints = new RealPoint[(2 * (Mt * Ms)) + (2 * ((Mt*(Ms-2)) + 2))];
    }

    public void initializeDefaultShape( double radius, RealPoint center) {
        double x0 = center.getDoublePosition(0);
        double y0 = center.getDoublePosition(1);
        double z0 = center.getDoublePosition(2);

        for (int k = 0; k < Mt; k++) {
            for (int l = 1; l <= Ms - 2; l++) {
                double theta = PIMs * l;
                double phi = 2.0 * PIMt * k;
                controlPoints[k + ((l - 1) * Mt) + 1] = new RealPoint(x0 + radius * Math.cos(phi) * Math.sin(theta), y0 + radius * Math.sin(phi) * Math.sin(theta), z0 + radius * Math.cos(theta));
                controlPoints[Mt * (Ms - 2) + 2 + k + (l * Mt)] = new RealPoint(radius * Math.cos(phi) * Math.PI * Math.cos(theta), radius * Math.sin(phi) * Math.PI * Math.cos(theta), -radius * Math.PI * Math.sin(theta));
                controlPoints[(Mt * (Ms - 2)) + (Mt * Ms) + 2 + k + ((l - 1) * Mt) + 1] = new RealPoint( -radius * (2.0 * Math.PI) * Math.sin(phi) * Math.sin(theta), radius * (2.0 * Math.PI) * Math.cos(phi) * Math.sin(theta), 0.0);
                controlPoints[(2 * (Mt * (Ms - 2))) + (Mt * Ms) + 4 + k + (l * Mt)] = new RealPoint(-radius * (2.0 * Math.PI) * Math.sin(phi) * Math.PI * Math.cos(theta), radius * (2.0 * Math.PI) * Math.cos(phi) * Math.PI * Math.cos(theta), 0.0);
            }
        }

        // north pole
        controlPoints[0] = new RealPoint(x0, y0, z0 + radius);
        controlPoints[(Mt*(Ms-2)) + (Mt*Ms) + 2] = new RealPoint(0.0, 0.0, 0.0);

        // south pole
        controlPoints[Mt * (Ms - 2) + 1] = new RealPoint(x0, y0, z0 - radius);
        controlPoints[(2*(Mt*(Ms-2))) + (Mt*Ms) + 3] = new RealPoint(0.0, 0.0, 0.0);

        for (int k = 0; k < Mt; k++) {
            double phi = 2.0 * PIMt * k;
            // north pole
            controlPoints[Mt * (Ms - 2) + 2 + k] = new RealPoint(radius * Math.PI * Math.cos(phi), radius * Math.PI * Math.sin(phi), 0.0);
            controlPoints[(2 * (Mt * (Ms - 2))) + (Mt * Ms) + 4 + k] = new RealPoint(-radius * (2.0 * Math.PI) * Math.PI * Math.sin(phi), radius * (2.0 * Math.PI) * Math.PI * Math.cos(phi), 0.0);

            // south pole
            controlPoints[Mt * (Ms - 2) + 2 + k + ((Ms - 1) * Mt)] = new RealPoint(-radius * Math.PI * Math.cos(phi), -radius * Math.PI * Math.sin(phi), 0.0);
            controlPoints[(2 * (Mt * (Ms - 2))) + (Mt * Ms) + 4 + k + ((Ms - 1) * Mt)] = new RealPoint(radius * (2.0 * Math.PI) * Math.PI * Math.sin(phi), -radius * (2.0 * Math.PI) * Math.PI * Math.cos(phi), 0.0);
        }
    }

    public ArrayList<RealPoint> getControlPoints(){
        ArrayList<RealPoint> allPointsArray=new ArrayList<RealPoint>();
        allPointsArray.addAll(getSurfaceControlPoints());
        //allPointsArray.addAll(getTangentPlaneControlPoints());
        //allPointsArray.addAll(getTwistControlPoints());
        return allPointsArray;
    }

    public ArrayList<RealPoint> getSurfaceControlPoints(){
        ArrayList<RealPoint> controlPointsArray= new ArrayList<RealPoint>();
        for (int k = 0; k < Mt; k++) {
            for (int l = 1; l <= Ms - 2; l++) {
                controlPointsArray.add(controlPoints[k + ((l - 1) * Mt) + 1]);
            }
        }
        controlPointsArray.add(controlPoints[0]);
        controlPointsArray.add(controlPoints[Mt * (Ms - 2) + 1]);

        // return new ArrayList<>(Arrays.asList(controlPoints));
        return controlPointsArray;
    }

    public ArrayList<RealPoint> getTangentPlaneControlPoints(){
        ArrayList<RealPoint> tangentPlaneControlPointsArray= new ArrayList<RealPoint>();
        for (int k = 0; k < Mt; k++) {
            for (int l = 1; l <= Ms - 2; l++) {
                tangentPlaneControlPointsArray.add(controlPoints[Mt * (Ms - 2) + 2 + k + (l * Mt)]);
                tangentPlaneControlPointsArray.add(controlPoints[(Mt * (Ms - 2)) + (Mt * Ms) + 2 + k + ((l - 1) * Mt) + 1]);
            }
        }
        tangentPlaneControlPointsArray.add(controlPoints[(Mt*(Ms-2)) + (Mt*Ms) + 2]);
        tangentPlaneControlPointsArray.add(controlPoints[(2*(Mt*(Ms-2))) + (Mt*Ms) + 3]);
        for (int k = 0; k < Mt; k++) {
            tangentPlaneControlPointsArray.add(controlPoints[Mt * (Ms - 2) + 2 + k]);
            tangentPlaneControlPointsArray.add(controlPoints[Mt * (Ms - 2) + 2 + k + ((Ms-1) * Mt)]);
        }

        // return new ArrayList<>(Arrays.asList(controlPoints));
        return tangentPlaneControlPointsArray;
    }

    public ArrayList<RealPoint> getTwistControlPoints(){
        ArrayList<RealPoint> twistControlPointsArray= new ArrayList<RealPoint>();
        for (int k = 0; k < Mt; k++) {
            for (int l = 0; l < Ms; l++) {
                twistControlPointsArray.add(controlPoints[(2 * (Mt * (Ms - 2))) + (Mt * Ms) + 4 + k + (l * Mt)]);
            }
        }

        // return new ArrayList<>(Arrays.asList(controlPoints));
        return twistControlPointsArray;
    }

    public ArrayList<RealPoint> getSampledSurface(){
        // TODO: come up with an adaptive way to sample depending on the size of the patch (compute arc length)
        int samplingRateS=20;
        int samplingRateT=20;

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

        for (int l = 0; l < Ms; l++) {
            double sVal = s - l;
            if (sVal > -halfSupport && sVal < halfSupport) {
                for (int k = 0; k < Mt; k++) {
                    double tVal=wrapIndex(t, k, Mt);
                    if (tVal > -halfSupport && tVal < halfSupport){
                        double[] pointValue=new double[nDim];
                        for(int d=0; d<nDim;d++) {
                            double phi1t=SplineBasis.EH1Spline1(tVal, Mt);
                            double phi2t=(1.0/(double)Mt)*SplineBasis.EH1Spline2(tVal, Mt);
                            double phi1s=SplineBasis.EH1Spline1(sVal, 2.0*(Ms-1.0));
                            double phi2s=(1.0/(Ms-1.0))*SplineBasis.EH1Spline2(sVal, 2.0*(Ms-1.0));

                            double c11Contribution, c21Contribution;
                            if(l==0) {
                                c11Contribution=controlPoints[0].getDoublePosition(d) * phi1t * phi1s;
                                c21Contribution=controlPoints[(Mt*(Ms-2)) + (Mt*Ms) + 2].getDoublePosition(d) * phi2t * phi1s;
                            }else if(l==Ms-1) {
                                c11Contribution=controlPoints[Mt * (Ms - 2) + 1].getDoublePosition(d) * phi1t * phi1s;
                                c21Contribution=controlPoints[(2*(Mt*(Ms-2))) + (Mt*Ms) + 3].getDoublePosition(d) * phi2t * phi1s;
                            }else{
                                c11Contribution=controlPoints[k + ((l - 1) * Mt) + 1].getDoublePosition(d) * phi1t * phi1s;
                                c21Contribution=controlPoints[(Mt * (Ms - 2)) + (Mt * Ms) + 2 + k + ((l - 1) * Mt) + 1].getDoublePosition(d) * phi2t * phi1s;
                            }

                            double c12Contribution = controlPoints[Mt * (Ms - 2) + 2 + k + (l * Mt)].getDoublePosition(d) * phi1t * phi2s;
                            double c22Contribution = controlPoints[(2 * (Mt * (Ms - 2))) + (Mt * Ms) + 4 + k + (l * Mt)].getDoublePosition(d)* phi2t * phi2s;

                            pointValue[d] = point.getDoublePosition(d) + c11Contribution + c21Contribution + c12Contribution + c22Contribution;
                        }
                        point.setPosition(pointValue);
                    }
                }
            }
        }
        return point;
    }
}
