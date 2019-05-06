package playground;

import bdv.util.BdvFunctions;
import de.embl.cba.splines.distance.PointsDistanceSource;
import net.imglib2.FinalInterval;
import net.imglib2.Point;
import net.imglib2.RealPoint;

import java.util.ArrayList;

public class DisplayPointsDistanceSource
{
	public static void main( String[] args )
	{
		final FinalInterval interval = new FinalInterval( new long[]{ 0, 0, 0 }, new long[]{ 100, 100, 100 } );
		final ArrayList< RealPoint > points = new ArrayList<>();
		points.add( new RealPoint( 10,10,10 ) );
		final PointsDistanceSource pointsDistanceSource = new PointsDistanceSource( "distance to points", interval, points );
		BdvFunctions.show( pointsDistanceSource );
	}
}
