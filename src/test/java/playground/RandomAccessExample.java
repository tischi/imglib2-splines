package playground;

import net.imglib2.RandomAccess;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.array.ArrayImg;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.img.basictypeaccess.array.ShortArray;
import net.imglib2.type.numeric.integer.UnsignedShortType;

public class RandomAccessExample
{
	public static void main( String[] args )
	{
		final RandomAccessibleInterval< UnsignedShortType > rai = ArrayImgs.unsignedShorts( 100, 100, 100 );

		final RandomAccess< UnsignedShortType > access = rai.randomAccess();

		access.setPosition( new long[]{10,10,10} );
		final UnsignedShortType unsignedShortType = access.get();

		final int integer = unsignedShortType.getInteger();

	}
}
