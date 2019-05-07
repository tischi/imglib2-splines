package de.embl.cba.splines.utils;

public class SplineBasis {

    /** Length of the support of the linear B-spline. */
    public static int LINEARBSPLINESUPPORT = 2;

    /** Length of the support of the quadratic B-spline. */
    public static int QUADRATICBSPLINESUPPORT = 3;

    /** Length of the support of the cubic B-spline. */
    public static int CUBICBSPLINESUPPORT = 4;

    /**
     * Length of the support of the exponential spline basis function with three
     * roots.
     */
    public static int ESPLINE3SUPPORT = 3;

    /**
     * Length of the support of Hermite splines.
     */
    public static int HSPLINESUPPORT = 2;

    /** Linear B-spline. */
    public static double LinearBSpline(double t) {
        double SplineValue = 0.0;
        if ((t >= -1.0) && (t <= -0.0)) {
            SplineValue = t + 1;
        } else if ((t > -0.0) & (t <= 1.0)) {
            SplineValue = -t + 1;
        }
        return SplineValue;
    }

    /** First derivative of the linear B-spline. */
    public static double LinearBSplinePrime(double t) {
        double SplinePrimeValue = 0.0;
        if ((t > -1) & (t <= 0)) {
            SplinePrimeValue = 1;
        } else if ((t > 0) & (t < 1)) {
            SplinePrimeValue = -1;
        }
        return SplinePrimeValue;
    }

    /** Quadratic B-spline. */
    public static double QuadraticBSpline(double t) {
        double SplineValue = 0.0;
        if ((t >= -1.5) && (t <= -0.5)) {
            SplineValue = 0.5 * t * t + 1.5 * t + 1.125;
        } else if ((t > -0.5) & (t <= 0.5)) {
            SplineValue = -t * t + 0.75;
        } else if ((t > 0.5) & (t <= 1.5)) {
            SplineValue = 0.5 * t * t - 1.5 * t + 1.125;
        }
        return SplineValue;
    }

    /*// TODO
    *//** First derivative of the quadratic B-spline. *//*
    public static double QuadraticBSplinePrime(double t) {
        double SplinePrimeValue = 0.0;
        if ((t >= -1.5) && (t <= -0.5)) {
            SplinePrimeValue = ;
        } else if ((t > -0.5) & (t <= 0.5)) {
            SplinePrimeValue = ;
        } else if ((t > 0.5) & (t <= 1.5)) {
            SplinePrimeValue = ;
        }
        return SplinePrimeValue;
    }*/

    /** Cubic B-spline. */
    public static double CubicBSpline(double t) {
        double SplineValue = 0.0;
        if ((t >= -2.0) && (t <= -1.0)) {
            SplineValue = 1.0 / 6.0 * t * t * t + t * t + 2.0 * t + 4.0 / 3.0;
        } else if ((t > -1.0) & (t <= 0.0)) {
            SplineValue = -0.5 * t * t * t - t * t + 2.0 / 3.0;
        } else if ((t > 0.0) & (t <= 1.0)) {
            SplineValue = 0.5 * t * t * t - t * t + 2.0 / 3.0;
        } else if ((t > 1.0) & (t <= 2.0)) {
            SplineValue = -1.0 / 6.0 * t * t * t + t * t - 2.0 * t + 4.0 / 3.0;
        }
        return SplineValue;
    }

    /*// TODO
     *//** First derivative of the cubic B-spline. *//*
     public static double CubicBSplinePrime(double t) {
        double SplinePrimeValue = 0.0;
        if ((t >= -2.0) && (t <= -1.0)) {
            SplinePrimeValue = ;
        } else if ((t > -1.0) & (t <= 0.0)) {
            SplinePrimeValue = ;
        } else if ((t > 0.0) & (t <= 1.0)) {
            SplinePrimeValue = ;
        } else if ((t > 1.0) & (t <= 2.0)) {
            SplinePrimeValue = ;
        }
        return SplinePrimeValue;
    }*/


    /** Exponential spline with parameters (0,-j alpha, j alpha). */
    public static double ESpline3(double t, double alpha) {
        // This function is best defined as a causal
        t+=(ESPLINE3SUPPORT/2.0);

        double alphaHalf = alpha / 2.0;
        double ESplineValue = 0.0;
        double eta = 2 * (1 - Math.cos(alpha));
        if ((t >= 0) & (t <= 1)) {
            ESplineValue = (2 * (1 - Math.cos(alphaHalf * t) * Math.cos(alphaHalf * t)));
        } else if ((t > 1) & (t <= 2)) {
            ESplineValue = (Math.cos(alpha * (t - 2)) + Math.cos(alpha * (t - 1)) - 2 * Math.cos(alpha));
        } else if ((t > 2) & (t <= 3)) {
            ESplineValue = (1 - Math.cos(alpha * (t - 3)));
        }
        ESplineValue = ESplineValue / eta;
        return ESplineValue;
    }

    /**
     * First derivative of the exponential spline with parameters (0,-j alpha, j
     * alpha).
     */
    public static double ESpline3Prime(double t, double alpha) {
        // This function is best defined as a causal
        t+=(ESPLINE3SUPPORT/2.0);

        double ESplinePrimeValue = 0.0;
        double eta = 2 * (1 - Math.cos(alpha)) / alpha;
        if ((t >= 0) & (t <= 1)) {
            ESplinePrimeValue = Math.sin(alpha * t);
        } else if ((t > 1) & (t <= 2)) {
            ESplinePrimeValue = -(Math.sin(alpha * (t - 2)) + Math.sin(alpha * (t - 1)));
        } else if ((t > 2) & (t <= 3)) {
            ESplinePrimeValue = Math.sin(alpha * (t - 3));
        }
        ESplinePrimeValue = ESplinePrimeValue / eta;
        return ESplinePrimeValue;
    }

    /** Polynomial first order Hermite splines. */
    public static double H1Spline1(double x) {
        double HSplineValue = 0.0;
        if (x >= 0 && x <= 1) {
            HSplineValue = (1.0 + (2.0 * x)) * (x - 1) * (x - 1);
        } else if (x < 0 && x >= -1) {
            HSplineValue = (1.0 - (2.0 * x)) * (x + 1) * (x + 1);
        }
        return HSplineValue;
    }

    public static double H1Spline2(double x) {
        double HSplineValue = 0.0;
        if (x >= 0 && x <= 1) {
            HSplineValue = x * (x - 1) * (x - 1);
        } else if (x < 0 && x >= -1) {
            HSplineValue = x * (x + 1) * (x + 1);
        }
        return HSplineValue;
    }

    /** First derivatives of the polynomial first order Hermite splines. */
    public static double H1Spline1Prime(double x) {
        double HSplinePrimeValue = 0.0;
        if (x >= 0 && x <= 1) {
            HSplinePrimeValue = (6.0 * x) * (x - 1);
        } else if (x < 0 && x >= -1) {
            HSplinePrimeValue = (-6.0 * x) * (x + 1);
        }
        return HSplinePrimeValue;
    }

    public static double H1Spline2Prime(double x) {
        double HSplinePrimeValue = 0.0;
        if (x >= 0 && x <= 1) {
            HSplinePrimeValue = (x - 1) * ((3.0 * x) - 1);
        } else if (x < 0 && x >= -1) {
            HSplinePrimeValue = (x + 1) * ((3.0 * x) + 1);
        }
        return HSplinePrimeValue;
    }

    /** Exponential first order Hermite splines. */
    private static double G1(double x, double M) {
        double G1Val = 0.0;
        if (x >= 0 && x <= 1) {
            double denom = ((Math.PI / M) * Math.cos((Math.PI / M))) - Math.sin((Math.PI / M));
            double num = (0.5 * (((2.0 * Math.PI / M) * Math.cos((Math.PI / M))) - Math.sin((Math.PI / M))))
                    - ((Math.PI / M) * Math.cos((Math.PI / M)) * x) - (0.5 * Math.sin((Math.PI / M) - ((2.0 * Math.PI / M) * x)));
            G1Val = num / denom;
        }
        return G1Val;
    }

    private static double G2(double x, double M) {
        double G2Val = 0.0;
        if (x >= 0 && x <= 1) {
            double denom = (((Math.PI / M) * Math.cos((Math.PI / M))) - Math.sin((Math.PI / M))) * (8.0 * (Math.PI / M))
                    * Math.sin((Math.PI / M));
            double num = -(((2.0 * Math.PI / M) * Math.cos((2.0 * Math.PI / M))) - Math.sin((2.0 * Math.PI / M)))
                    - (4.0 * (Math.PI / M) * Math.sin((Math.PI / M)) * Math.sin((Math.PI / M)) * x)
                    - (2.0 * Math.sin((Math.PI / M)) * Math.cos((2.0 * Math.PI / M) * (x - 0.5)))
                    + ((2.0 * Math.PI / M) * Math.cos((2.0 * Math.PI / M) * (x - 1)));
            G2Val = num / denom;
        }
        return G2Val;
    }

    public static double EH1Spline1(double x, double alpha) {
        double EHSplineValue = 0.0;
        if (x >= 0) {
            EHSplineValue = G1(x, alpha);
        } else {
            EHSplineValue = G1(-x, alpha);
        }
        return EHSplineValue;
    }

    public static double EH1Spline2(double x, double alpha) {
        double EHSplineValue = 0.0;
        if (x >= 0) {
            EHSplineValue = G2(x, alpha);
        } else {
            EHSplineValue = -1.0 * G2(-x, alpha);
        }
        return EHSplineValue;
    }

    /** First derivatives of the exponential first order Hermite. */
    private static double G1Prime(double x, double M) {
        double G1PrimeVal = 0.0;
        if (x >= 0 && x <= 1) {
            double denom = ((Math.PI / M) * Math.cos((Math.PI / M))) - Math.sin((Math.PI / M));
            double num = -((Math.PI / M) * Math.cos((Math.PI / M))) + ((Math.PI / M) * Math.cos((Math.PI / M) - ((2.0 * Math.PI / M) * x)));
            G1PrimeVal = num / denom;
        }
        return G1PrimeVal;
    }

    private static double G2Prime(double x, double M) {
        double G2PrimeVal = 0.0;
        if (x >= 0 && x <= 1) {
            double denom = (((Math.PI / M) * Math.cos((Math.PI / M))) - Math.sin((Math.PI / M))) * (8.0 * (Math.PI / M))
                    * Math.sin((Math.PI / M));
            double num = -(4.0 * (Math.PI / M) * Math.sin((Math.PI / M)) * Math.sin((Math.PI / M)))
                    + (4.0 * (Math.PI / M) * Math.sin((Math.PI / M)) * Math.sin((2.0 * Math.PI / M) * (x - 0.5)))
                    - ((2.0 * Math.PI / M) * (2.0 * Math.PI / M) * Math.sin((2.0 * Math.PI / M) * (x - 1)));
            G2PrimeVal = num / denom;
        }
        return G2PrimeVal;
    }

    public static double EH1Spline1Prime(double x, double alpha) {
        double EHSplinePrimeValue = 0.0;
        if (x >= 0) {
            EHSplinePrimeValue = G1Prime(x, alpha);
        } else {
            EHSplinePrimeValue = -1.0 * G1Prime(-x, alpha);
        }
        return EHSplinePrimeValue;
    }

    public static double EH1Spline2Prime(double x, double alpha) {
        double EHSplinePrimeValue = 0.0;
        if (x >= 0) {
            EHSplinePrimeValue = G2Prime(x, alpha);
        } else {
            EHSplinePrimeValue = G2Prime(-x, alpha);
        }
        return EHSplinePrimeValue;
    }


}
