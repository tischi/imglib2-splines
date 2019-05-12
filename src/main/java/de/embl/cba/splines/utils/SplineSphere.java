package de.embl.cba.splines.utils;

import net.imglib2.RealPoint;
import net.imglib2.realtransform.RealTransform;

import java.util.ArrayList;
import java.util.Arrays;

public class SplineSphere {
    private int M;
    private RealPoint[] controlPoints;

    private int nDim=3;
    private double halfSupport=SplineBasis.ESPLINE3SUPPORT/2.0;
    // TODO: would be nice to have the basis function is an argument here so it is more generic

    private final double PIM;
    private final double scale;
    private final double scaleM;
    private final double scale2M;

    public SplineSphere(int M){
        this.M = M;
        PIM = Math.PI / M;
        controlPoints = new RealPoint[M * (M - 1) + 6];
        scale = 1.0 / (M * SplineBasis.ESpline3Prime(1.0,PIM));
        scaleM = 2.0 * (1.0 - Math.cos(2.0 * PIM)) / (Math.cos(PIM) - Math.cos(3.0 * PIM));
        scale2M = 2.0 * (1.0 - Math.cos(PIM)) / (Math.cos(PIM / 2) - Math.cos(3.0 * PIM / 2));
    }

    public int getM()
    {
        return M;
    }

    public void initializeDefaultShape( double radius, RealPoint center) {
        double x0 = center.getDoublePosition(0);
        double y0 = center.getDoublePosition(1);
        double z0 = center.getDoublePosition(3);

        for (int k = 0; k < M; k++) {
            for (int l = 1; l <= M - 1; l++) {
                double theta = PIM * l;
                double phi = 2.0 * PIM * k;
                controlPoints[k + (l - 1) * M] = new RealPoint(x0 + radius * scale2M * scaleM * Math.sin(theta) * Math.cos(phi),
                        y0 + radius * scale2M * scaleM * Math.sin(theta) * Math.sin(phi), z0 + radius * scale2M * Math.cos(theta));
            }
        }
        // north pole
        controlPoints[M * (M - 1)] = new RealPoint(x0, y0, z0 + radius);
        // south pole
        controlPoints[M * (M - 1) + 1] = new RealPoint(x0, y0, z0 - radius);
        // north tangent plane
        controlPoints[M * (M - 1) + 2] = new RealPoint(x0 + Math.PI * radius, y0, z0 + radius);
        controlPoints[M * (M - 1) + 3] = new RealPoint(x0, y0 + Math.PI * radius, z0 + radius);
        // south tangent plane
        controlPoints[M * (M - 1) + 4] = new RealPoint(x0 + Math.PI * radius, y0, z0 - radius);
        controlPoints[M * (M - 1) + 5] = new RealPoint(x0, y0 + Math.PI * radius, z0 - radius);
    }

    public void initializeDefaultShape( double width, double height, double depth){
        initializeDefaultShape( Math.min(Math.min(width / 5.0, height / 5.0),
                depth / 5.0), new RealPoint (width / 2.0, height / 2.0, depth / 2.0));
    }

    public ArrayList<RealPoint> getControlPoints(){
        ArrayList<RealPoint> controlPointsArray= new ArrayList<RealPoint>();
        for (int k = 0; k < M; k++) {
            for (int l = 1; l <= M - 1; l++) {
                controlPointsArray.add(controlPoints[k + (l - 1) * M]);
            }
        }
        controlPointsArray.add(controlPoints[M * (M - 1)]);
        controlPointsArray.add(controlPoints[M * (M - 1) + 1]);

        // return new ArrayList<>(Arrays.asList(controlPoints));
        return controlPointsArray;
    }

    public ArrayList<RealPoint> getSampledSurface(){
        // TODO: come up with an adaptive way to sample depending on the size of the patch (compute arclength)
        int samplingRateS=10;
        int samplingRateT=20;

        ArrayList<RealPoint> surface= new ArrayList<RealPoint>();
        for (int nt = 0; nt < M*samplingRateT; nt++) {
            for (int ns = 0; ns <= M*samplingRateS; ns++) {
                final double t = ( double ) nt / ( double ) samplingRateT;
                final double s = ( double ) ns / ( double ) samplingRateS;
                surface.add(parametersToWorld( t, s ));
            }
        }
        return surface;
    }

    public RealPoint parametersToWorld(double t, double s){
        RealPoint point = new RealPoint(0.0,0.0,0.0);

        addNonPoleContributions( t, s, point );
        addNorthPoleContribution( t, s, point, scale, scaleM );
        addSouthPoleContribution( t, s, point, scale, scaleM );

        return point;
    }

    private void addSouthPoleContribution( double t, double s, RealPoint point, double scale, double scaleM )
    {
        // South tangent plane
        double[] SouthV1=new double[nDim];
        double[] SouthV2=new double[nDim];
        for(int d=0; d<nDim; d++) {
            SouthV1[d] = controlPoints[M * (M - 1) + 4].getDoublePosition(d) - controlPoints[M * (M - 1) + 1].getDoublePosition(d);
            SouthV2[d] = controlPoints[M * (M - 1) + 5].getDoublePosition(d) - controlPoints[M * (M - 1) + 1].getDoublePosition(d);
        }

        // l = M+1
        double sVal=s-M-1;
        if (sVal > -halfSupport && sVal < halfSupport) {
            for (int k = 0; k < M; k++) {
                double tVal=wrapIndex(t, k);
                if (tVal > -halfSupport && tVal < halfSupport) {
                    // compute c[k,M+1]
                    double[] ckMplus=new double[nDim];
                    for(int d=0; d<nDim; d++)
                        ckMplus[d] = (controlPoints[k + (M - 2) * M].getDoublePosition(d)
                            + scale * scaleM * (Math.cos(2*PIM*k) * SouthV1[d] + Math.sin(2*PIM*k) * SouthV2[d]));

                    double basisFactor = SplineBasis.ESpline3(sVal,PIM) * SplineBasis.ESpline3(tVal,2.0*PIM);
                    double[] pointValue=new double[nDim];
                    for(int d=0; d<nDim;d++)
                        pointValue[d] = point.getDoublePosition(d) + (ckMplus[d] * basisFactor);
                    point.setPosition(pointValue);
                }
            }
        }

        // l = M
        sVal=s-M;
        if (sVal > -halfSupport && sVal < halfSupport) {
            for (int k = 0; k < M; k++) {
                double tVal=wrapIndex(t, k);
                if (tVal > -SplineBasis.ESPLINE3SUPPORT / 2.0 && tVal < SplineBasis.ESPLINE3SUPPORT / 2.0) {
                    // compute c[k,M+1]
                    double[] ckMplus=new double[nDim];
                    for(int d=0; d<nDim; d++)
                        ckMplus[d] = (controlPoints[k + (M - 2) * M].getDoublePosition(d)
                            + scale * scaleM * (Math.cos(2 * PIM * k) * SouthV1[d] + Math.sin(2 * PIM * k) * SouthV2[d]));

                    // compute c[k,M]
                    double[] ckM=new double[nDim];
                    for(int d=0; d<nDim; d++)
                        ckM[d] = (controlPoints[M * (M - 1) + 1].getDoublePosition(d)
                            - SplineBasis.ESpline3(1, PIM) * (ckMplus[d] + controlPoints[k + (M - 2) * M].getDoublePosition(d)))
                            / SplineBasis.ESpline3(0, PIM);

                    double basisFactor = SplineBasis.ESpline3(sVal, PIM) * SplineBasis.ESpline3(tVal, 2.0 * PIM);
                    double[] pointValue=new double[nDim];
                    for(int d=0; d<nDim;d++)
                        pointValue[d] = point.getDoublePosition(d) + (ckM[d] * basisFactor);
                    point.setPosition(pointValue);
                }
            }
        }
    }

    private void addNorthPoleContribution( double t, double s, RealPoint point, double scale, double scaleM )
    {
        // North tangent plane
        double[] NorthV1=new double[nDim];
        double[] NorthV2=new double[nDim];
        for(int d=0; d<nDim; d++){
            NorthV1[d] = controlPoints[M * (M - 1) + 2].getDoublePosition(d) - controlPoints[M * (M - 1)].getDoublePosition(d);
            NorthV2[d] = controlPoints[M * (M - 1) + 3].getDoublePosition(d) - controlPoints[M * (M - 1)].getDoublePosition(d);
        }

        // l = -1
        double sVal = s+1.0;
        if (sVal > -halfSupport && sVal < halfSupport ) {
            for (int k = 0; k < M; k++) {
                double tVal=wrapIndex(t, k);
                if (tVal > -halfSupport && tVal < halfSupport) {
                    // compute c[k,-1]
                    double[] ckminus1=new double[nDim];
                    for(int d=0; d<nDim; d++)
                        ckminus1[d] = (controlPoints[k].getDoublePosition(d) + scale * scaleM * (Math.cos(2*PIM*k) * NorthV1[d] + Math.sin(2*PIM*k) * NorthV2[d]));

                    double basisFactor = SplineBasis.ESpline3(sVal,PIM) * SplineBasis.ESpline3(tVal,2.0*PIM);
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
            for (int k = 0; k < M; k++) {
                double tVal=wrapIndex(t, k);
                if (tVal > -halfSupport && tVal < halfSupport) {
                    // compute c[k,-1]
                    double[] ckminus1=new double[nDim];
                    for(int d=0; d<nDim; d++)
                        ckminus1[d] = (controlPoints[k].getDoublePosition(d) + scale * scaleM * (Math.cos(2*PIM*k) * NorthV1[d] + Math.sin(2*PIM*k) * NorthV2[d]));

                    // compute c[k,0]
                    double[] ck0=new double[nDim];
                    for(int d=0; d<nDim; d++)
                        ck0[d] = (controlPoints[M * (M - 1)].getDoublePosition(d)
                            - SplineBasis.ESpline3(1,PIM) * (ckminus1[d] + controlPoints[k].getDoublePosition(d)))
                            / SplineBasis.ESpline3(0,PIM);

                    double basisFactor = SplineBasis.ESpline3(sVal,PIM) * SplineBasis.ESpline3(tVal,2.0*PIM);
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
        for (int l = 1; l <= M - 1; l++) {
            double sVal = s-l;
            if (sVal > -halfSupport && sVal < halfSupport) {
                for (int k = 0; k < M; k++) {
                    double tVal=wrapIndex(t, k);
                    if (tVal > -halfSupport && tVal < halfSupport){
                        double[] pointValue=new double[nDim];
                        for(int d=0; d<nDim;d++)
                            pointValue[d] = point.getDoublePosition(d) + controlPoints[k + (l - 1) * M].getDoublePosition(d) * SplineBasis.ESpline3(sVal,PIM) * SplineBasis.ESpline3(tVal,2.0*PIM);
                        point.setPosition(pointValue);
                    }
                }
            }
        }
    }

    private double wrapIndex(double t, int k){
        double tVal=t-k;
        if (k < t-halfSupport) {
            if (k + M >= t - SplineBasis.ESPLINE3SUPPORT / 2.0 && k + M <= t + SplineBasis.ESPLINE3SUPPORT / 2.0)
                tVal = t - (k + M);
        } else if (k > t+halfSupport) {
            if (k - M >= t - SplineBasis.ESPLINE3SUPPORT / 2.0 && k - M <= t + SplineBasis.ESPLINE3SUPPORT / 2.0)
                tVal = t - (k - M);
        }
        return tVal;
    }

    /*
    // TODO: clean and remove code duplicates
    public RealPoint parametersToWorld(double t, double s){
        double x = 0.0;
        double y = 0.0;
        double z = 0.0;

        // Everything but the poles
        for (int l = 1; l <= M - 1; l++) {
            double sVal = s-l;
            if (sVal > -halfSupport && sVal < halfSupport) {
                for (int k = 0; k < M; k++) {
                    double tVal=t-k;
                    if (k < t-halfSupport) {
                        if (k + M >= t - SplineBasis.ESPLINE3SUPPORT / 2.0 && k + M <= t + SplineBasis.ESPLINE3SUPPORT / 2.0)
                            tVal = t - (k + M);
                    } else if (k > t+halfSupport) {
                        if (k - M >= t - SplineBasis.ESPLINE3SUPPORT / 2.0 && k - M <= t + SplineBasis.ESPLINE3SUPPORT / 2.0)
                            tVal = t - (k - M);
                    }
                    if (tVal > -halfSupport && tVal < halfSupport){
                        x += controlPoints[k + (l - 1) * M].getDoublePosition(0) * SplineBasis.ESpline3(sVal,PIM) * SplineBasis.ESpline3(tVal,2.0*PIM);
                        y += controlPoints[k + (l - 1) * M].getDoublePosition(1) * SplineBasis.ESpline3(sVal,PIM) * SplineBasis.ESpline3(tVal,2.0*PIM);
                        z += controlPoints[k + (l - 1) * M].getDoublePosition(2) * SplineBasis.ESpline3(sVal,PIM) * SplineBasis.ESpline3(tVal,2.0*PIM);
                    }
                }
            }
        }


        // Dealing with the poles
        double scale = 1.0 / (M * SplineBasis.ESpline3Prime(1.0,PIM));
        double scaleM = 2.0 * (1.0 - Math.cos(2.0 * PIM)) / (Math.cos(PIM) - Math.cos(3.0 * PIM));

        double NorthV1x = controlPoints[M * (M - 1) + 2].getDoublePosition(0) - controlPoints[M * (M - 1)].getDoublePosition(0);
        double NorthV1y = controlPoints[M * (M - 1) + 2].getDoublePosition(1) - controlPoints[M * (M - 1)].getDoublePosition(1);
        double NorthV1z = controlPoints[M * (M - 1) + 2].getDoublePosition(2) - controlPoints[M * (M - 1)].getDoublePosition(2);

        double NorthV2x = controlPoints[M * (M - 1) + 3].getDoublePosition(0) - controlPoints[M * (M - 1)].getDoublePosition(0);
        double NorthV2y = controlPoints[M * (M - 1) + 3].getDoublePosition(1) - controlPoints[M * (M - 1)].getDoublePosition(1);
        double NorthV2z = controlPoints[M * (M - 1) + 3].getDoublePosition(2) - controlPoints[M * (M - 1)].getDoublePosition(2);

        // l = -1
        double sVal = s+1.0;
        if (sVal > -halfSupport && sVal < halfSupport ) {
            for (int k = 0; k < M; k++) {
                double tVal=t-k;
                if (k < t-halfSupport) {
                    if (k + M >= t - SplineBasis.ESPLINE3SUPPORT / 2.0 && k + M <= t + SplineBasis.ESPLINE3SUPPORT / 2.0)
                        tVal = t - (k + M);
                } else if (k > t+halfSupport) {
                    if (k - M >= t - SplineBasis.ESPLINE3SUPPORT / 2.0 && k - M <= t + SplineBasis.ESPLINE3SUPPORT / 2.0)
                        tVal = t - (k - M);
                }
                if (tVal > -halfSupport && tVal < halfSupport) {
                    // compute c[k,-1]
                    double ckminus1x = (controlPoints[k].getDoublePosition(0) + scale * scaleM * (Math.cos(2*PIM*k) * NorthV1x + Math.sin(2*PIM*k) * NorthV2x));
                    double ckminus1y = (controlPoints[k].getDoublePosition(1) + scale * scaleM * (Math.cos(2*PIM*k) * NorthV1y + Math.sin(2*PIM*k) * NorthV2y));
                    double ckminus1z = (controlPoints[k].getDoublePosition(2) + scale * scaleM * (Math.cos(2*PIM*k) * NorthV1z + Math.sin(2*PIM*k) * NorthV2z));

                    double basisFactor = SplineBasis.ESpline3(sVal,PIM) * SplineBasis.ESpline3(tVal,2.0*PIM);
                    x += ckminus1x * basisFactor;
                    y += ckminus1y * basisFactor;
                    z += ckminus1z * basisFactor;
                }
            }
        }

        // l = 0
        sVal=s;
        if (sVal > -halfSupport && sVal < halfSupport) {
            for (int k = 0; k < M; k++) {
                double tVal=t-k;
                if (k < t-halfSupport) {
                    if (k + M >= t - SplineBasis.ESPLINE3SUPPORT / 2.0 && k + M <= t + SplineBasis.ESPLINE3SUPPORT / 2.0)
                        tVal = t - (k + M);
                } else if (k > t+halfSupport) {
                    if (k - M >= t - SplineBasis.ESPLINE3SUPPORT / 2.0 && k - M <= t + SplineBasis.ESPLINE3SUPPORT / 2.0)
                        tVal = t - (k - M);
                }
                if (tVal > -halfSupport && tVal < halfSupport) {
                    // compute c[k,-1]
                    double ckminus1x = (controlPoints[k].getDoublePosition(0) + scale * scaleM * (Math.cos(2*PIM*k) * NorthV1x + Math.sin(2*PIM*k) * NorthV2x));
                    double ckminus1y = (controlPoints[k].getDoublePosition(1) + scale * scaleM * (Math.cos(2*PIM*k) * NorthV1y + Math.sin(2*PIM*k) * NorthV2y));
                    double ckminus1z = (controlPoints[k].getDoublePosition(2) + scale * scaleM * (Math.cos(2*PIM*k) * NorthV1z + Math.sin(2*PIM*k) * NorthV2z));

                    // compute c[k,0]
                    double ck0x = (controlPoints[M * (M - 1)].getDoublePosition(0)
                            - SplineBasis.ESpline3(1,PIM) * (ckminus1x + controlPoints[k].getDoublePosition(0)))
                            / SplineBasis.ESpline3(0,PIM);
                    double ck0y = (controlPoints[M * (M - 1)].getDoublePosition(1)
                            - SplineBasis.ESpline3(1,PIM) * (ckminus1y + controlPoints[k].getDoublePosition(1)))
                            / SplineBasis.ESpline3(0,PIM);
                    double ck0z = (controlPoints[M * (M - 1)].getDoublePosition(2)
                            - SplineBasis.ESpline3(1,PIM) * (ckminus1z + controlPoints[k].getDoublePosition(2)))
                            / SplineBasis.ESpline3(0,PIM);

                    double basisFactor = SplineBasis.ESpline3(sVal,PIM) * SplineBasis.ESpline3(tVal,2.0*PIM);
                    x += ck0x * basisFactor;
                    y += ck0y * basisFactor;
                    z += ck0z * basisFactor;
                }
            }
        }

        double SouthV1x = controlPoints[M * (M - 1) + 4].getDoublePosition(0) - controlPoints[M * (M - 1) + 1].getDoublePosition(0);
        double SouthV1y = controlPoints[M * (M - 1) + 4].getDoublePosition(1) - controlPoints[M * (M - 1) + 1].getDoublePosition(1);
        double SouthV1z = controlPoints[M * (M - 1) + 4].getDoublePosition(2) - controlPoints[M * (M - 1) + 1].getDoublePosition(2);

        double SouthV2x = controlPoints[M * (M - 1) + 5].getDoublePosition(0) - controlPoints[M * (M - 1) + 1].getDoublePosition(0);
        double SouthV2y = controlPoints[M * (M - 1) + 5].getDoublePosition(1) - controlPoints[M * (M - 1) + 1].getDoublePosition(1);
        double SouthV2z = controlPoints[M * (M - 1) + 5].getDoublePosition(2) - controlPoints[M * (M - 1) + 1].getDoublePosition(2);

        // l = M+1
        sVal=s-M-1;
        if (sVal > -halfSupport && sVal < halfSupport) {
            for (int k = 0; k < M; k++) {
                double tVal=t-k;
                if (k < t-halfSupport) {
                    if (k + M >= t - SplineBasis.ESPLINE3SUPPORT / 2.0 && k + M <= t + SplineBasis.ESPLINE3SUPPORT / 2.0)
                        tVal = t - (k + M);
                } else if (k > t+halfSupport) {
                    if (k - M >= t - SplineBasis.ESPLINE3SUPPORT / 2.0 && k - M <= t + SplineBasis.ESPLINE3SUPPORT / 2.0)
                        tVal = t - (k - M);
                }
                if (tVal > -halfSupport && tVal < halfSupport) {
                    // compute c[k,M+1]
                    double ckMplusx = (controlPoints[k + (M - 2) * M].getDoublePosition(0)
                            + scale * scaleM * (Math.cos(2*PIM*k) * SouthV1x + Math.sin(2*PIM*k) * SouthV2x));
                    double ckMplusy = (controlPoints[k + (M - 2) * M].getDoublePosition(1)
                            + scale * scaleM * (Math.cos(2*PIM*k) * SouthV1y + Math.sin(2*PIM*k) * SouthV2y));
                    double ckMplusz = (controlPoints[k + (M - 2) * M].getDoublePosition(2)
                            + scale * scaleM * (Math.cos(2*PIM*k) * SouthV1z + Math.sin(2*PIM*k) * SouthV2z));

                    double basisFactor = SplineBasis.ESpline3(sVal,PIM) * SplineBasis.ESpline3(tVal,2.0*PIM);
                    x += ckMplusx * basisFactor;
                    y += ckMplusy * basisFactor;
                    z += ckMplusz * basisFactor;
                }
            }
        }

        sVal=s-M;
        if (sVal > -halfSupport && sVal < halfSupport) {
            for (int k = 0; k < M; k++) {
                double tVal=t-k;
                if (k < t-halfSupport) {
                    if (k + M >= t - SplineBasis.ESPLINE3SUPPORT / 2.0 && k + M <= t + SplineBasis.ESPLINE3SUPPORT / 2.0)
                        tVal = t - (k + M);
                } else if (k > t+halfSupport) {
                    if (k - M >= t - SplineBasis.ESPLINE3SUPPORT / 2.0 && k - M <= t + SplineBasis.ESPLINE3SUPPORT / 2.0)
                        tVal = t - (k - M);
                }
                if (tVal > -SplineBasis.ESPLINE3SUPPORT / 2.0 && tVal < SplineBasis.ESPLINE3SUPPORT / 2.0) {
                    // compute c[k,M+1]
                    double ckMplusx = (controlPoints[k + (M - 2) * M].getDoublePosition(0)
                            + scale * scaleM * (Math.cos(2 * PIM * k) * SouthV1x + Math.sin(2 * PIM * k) * SouthV2x));
                    double ckMplusy = (controlPoints[k + (M - 2) * M].getDoublePosition(1)
                            + scale * scaleM * (Math.cos(2 * PIM * k) * SouthV1y + Math.sin(2 * PIM * k) * SouthV2y));
                    double ckMplusz = (controlPoints[k + (M - 2) * M].getDoublePosition(2)
                            + scale * scaleM * (Math.cos(2 * PIM * k) * SouthV1z + Math.sin(2 * PIM * k) * SouthV2z));

                    // compute c[k,M]
                    double ckMx = (controlPoints[M * (M - 1) + 1].getDoublePosition(0)
                            - SplineBasis.ESpline3(1, PIM) * (ckMplusx + controlPoints[k + (M - 2) * M].getDoublePosition(0)))
                            / SplineBasis.ESpline3(0, PIM);
                    double ckMy = (controlPoints[M * (M - 1) + 1].getDoublePosition(1)
                            - SplineBasis.ESpline3(1, PIM) * (ckMplusy + controlPoints[k + (M - 2) * M].getDoublePosition(1)))
                            / SplineBasis.ESpline3(0, PIM);
                    double ckMz = (controlPoints[M * (M - 1) + 1].getDoublePosition(2)
                            - SplineBasis.ESpline3(1, PIM) * (ckMplusz + controlPoints[k + (M - 2) * M].getDoublePosition(2)))
                            / SplineBasis.ESpline3(0, PIM);

                    double basisFactor = SplineBasis.ESpline3(sVal, PIM) * SplineBasis.ESpline3(tVal, 2.0 * PIM);
                    x += ckMx * basisFactor;
                    y += ckMy * basisFactor;
                    z += ckMz * basisFactor;
                }
            }
        }

        return new RealPoint(x,y,z);
    }
     */
}
