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
//		2dimage =  RealViews.transform( 3dimage, transformation );

	}
}
