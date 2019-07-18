package playground;

import bdv.util.Bdv;
import bdv.util.BdvFunctions;
import bdv.util.BdvOverlay;
import de.embl.cba.splines.utils.SplineGridOverlay;
import net.imglib2.FinalInterval;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.type.numeric.ARGBType;

import java.util.Random;

public class SplineMeshExample
{
    public static void main( String[] args )
    {
    	final FinalInterval interval = new FinalInterval( new long[]{ 0, 0, 0 },
    													  new long[]{ 100, 100, 100 } );

    	final BdvOverlay overlay = new SplineGridOverlay( 5, 6, interval.dimension(0), interval.dimension(1), interval.dimension(2) );
    	final Bdv bdv3D = BdvFunctions.show( greenExampleImage(), "greens" );
    	BdvFunctions.showOverlay( overlay, "overlay", Bdv.options().addTo( bdv3D ) );
    }

    private static Img<ARGBType> greenExampleImage()
	{
		final Random random = new Random();
		final Img< ARGBType > img = ArrayImgs.argbs( 100, 100, 100 );
		// img.forEach( t -> t.set( random.nextInt() & 0xFF003F00 ) );
		return img;
	}
}
