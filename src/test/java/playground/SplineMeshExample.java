package playground;

import bdv.util.Bdv;
import bdv.util.BdvFunctions;
import bdv.util.BdvOverlay;
import de.embl.cba.splines.utils.SplineGridOverlay;
import net.imglib2.FinalInterval;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.type.numeric.ARGBType;

import de.embl.cba.splines.utils.SplineSphere;
import de.embl.cba.splines.utils.SurfaceSplineToRealPointTransform;

import java.util.Random;

public class SplineMeshExample
{
    public static void main( String[] args )
    {
    	final FinalInterval interval = new FinalInterval( new long[]{ 0, 0, 0 },
    													  new long[]{ 100, 100, 100 } );

		SplineSphere splineSphere = new SplineSphere( 6 );
		splineSphere.initializeDefaultShape( interval.dimension(0),
                							 interval.dimension(1),
											 interval.dimension(2) );

    	SurfaceSplineToRealPointTransform splineTransform = new 
    									SurfaceSplineToRealPointTransform( splineSphere );
    	final BdvOverlay overlay = new SplineGridOverlay( splineTransform );
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
