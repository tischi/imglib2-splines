package playground;

import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.Random;

import bdv.tools.boundingbox.TransformedBoxEditor;
import bdv.tools.boundingbox.TransformedBoxModel;
import net.imglib2.FinalInterval;
import net.imglib2.Interval;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.realtransform.AffineTransform3D;
import net.imglib2.type.numeric.integer.UnsignedByteType;
import net.imglib2.util.Intervals;

import bdv.util.BdvFunctions;
import bdv.util.Bdv;
import bdv.util.BdvOptions;

import org.scijava.ui.behaviour.io.InputTriggerConfig;

public class InteractiveControlPoints
{
	public static void main( final String[] args )
	{
		System.setProperty( "apple.laf.useScreenMenuBar", "true" );

		final Random random = new Random();

		final Img< UnsignedByteType > img = ArrayImgs.unsignedBytes( 100, 100, 50, 10 );
		img.forEach( t -> t.set( random.nextInt( 128 ) ) );

		final AffineTransform3D imageTransform = new AffineTransform3D();
		imageTransform.set( 2, 2, 2 );
		final Bdv bdv = BdvFunctions.show( img, "image", BdvOptions.options().sourceTransform( imageTransform ) );

		final Interval initialInterval = Intervals.createMinMax( 30, 30, 15, 80, 80, 40 );

		TransformedBoxModel model=new TransformedBoxModel(initialInterval, imageTransform);
		TransformedBoxEditor boxEditor = new TransformedBoxEditor(new InputTriggerConfig(), bdv.getBdvHandle().getViewerPanel(), bdv.getBdvHandle().getSetupAssignments(), bdv.getBdvHandle().getTriggerbindings(), model);
		boxEditor.setPerspective(1.0D, 1000.0D);
		boxEditor.setEditable(true);
		boxEditor.install();
		model.intervalChangedListeners().add( () -> {
			bdv.getBdvHandle().getViewerPanel().getDisplay().repaint();
		} );
	}
}