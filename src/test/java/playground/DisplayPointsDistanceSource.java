package playground;

import bdv.util.BdvFunctions;
import de.embl.cba.splines.distance.PointsDistanceSource;
import de.embl.cba.splines.utils.SplineSphere;
import net.imglib2.FinalInterval;
import net.imglib2.Point;
import net.imglib2.RealPoint;

import java.util.ArrayList;

public class DisplayPointsDistanceSource
{
	public static void main( String[] args )
	{
		final FinalInterval interval = new FinalInterval( new long[]{ 0, 0, 0 }, new long[]{ 100, 100, 100 } );

		SplineSphere spline = new SplineSphere( 6 );
		spline.initializeDefaultShape(interval.dimension(0),interval.dimension(1),interval.dimension(2));

		//final ArrayList< RealPoint > points = spline.getControlPoints();
		final ArrayList< RealPoint > points = spline.getSampledSurface();

		final PointsDistanceSource pointsDistanceSource = new PointsDistanceSource( "distance to points", interval, points );

		BdvFunctions.show( pointsDistanceSource );
	}
}
