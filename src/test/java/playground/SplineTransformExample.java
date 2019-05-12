package playground;

import net.imglib2.Cursor;
import net.imglib2.RandomAccess;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.array.ArrayImg;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.img.basictypeaccess.array.DoubleArray;
import net.imglib2.realtransform.RealViews;
import net.imglib2.type.numeric.real.DoubleType;
import net.imglib2.view.Views;

public class SplineTransformExample
{
	public static void main( String[] args )
	{
		int m = 3;

		final RandomAccessibleInterval< DoubleType > ts = ArrayImgs.doubles( m * 10, ( m + 1 ) * 10 );

		final RandomAccess< DoubleType > access = ts.randomAccess();

		for ( int t = 0; t < ts.dimension( 0 ); t++ )
		{
			access.setPosition( t, 0 );
			access.setPosition( t, 0 );
		}

		final Cursor< DoubleType > cursor = Views.iterable( ts ).cursor();

		while (cursor.hasNext())
		{
			cursor.next();
		}


		// TODO: not sure what we need...
		// What would we put in access? It seems we only need the indices of the image (i.e., the (t,s) doublets), not the image itself.
//		2dimage =  RealViews.transform( 3dimage, transformation );

		// It seems that transform needs a source, a target and a transform.
		// In our case, the "source" would simply be the sampled (t,s) parameter space?
		// Also, in our case the "transform" depends on the target image itself (as the surface is fitted to ta 3D image content). Seems to be a bit of a circular logic.
		// What we need is to be able to retrieve the image value at (x,y,z) corresponding to a given (t,s) value, for a given sample rate in the parameter space.
		// Is there a way to do that by avoiding a for loop?
		// In other words, we wonder if we can avoid the getSampledSurface function from SplineSphere by iterating over the apply function of SurfaceSplineToRealPointTransform.
	}
}
