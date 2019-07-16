package playground;

import bdv.util.Bdv;
import bdv.util.BdvFunctions;
import bdv.util.BdvOptions;
import bdv.util.BdvOverlay;
import de.embl.cba.splines.controlpoints.ControlPointsEditor;
import de.embl.cba.splines.controlpoints.ControlPointsModel;
import de.embl.cba.splines.utils.SplineGridOverlay;
import de.embl.cba.splines.utils.SplineSphere;
import de.embl.cba.splines.utils.SurfaceSplineToRealPointTransform;
import net.imglib2.FinalInterval;
import net.imglib2.RealPoint;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.realtransform.AffineTransform3D;
import net.imglib2.type.numeric.integer.UnsignedByteType;
import org.scijava.ui.behaviour.io.InputTriggerConfig;

import java.util.ArrayList;
import java.util.List;

public class InteractiveControlledMesh
{
	public static void main( String[] args )
	{
		final Img<UnsignedByteType> img = ArrayImgs.unsignedBytes( 100, 100, 50, 10 );

		final AffineTransform3D imageTransform = new AffineTransform3D();
		//imageTransform.set( 2, 2, 2 );

		final Bdv bdv = BdvFunctions.show( img, "image", BdvOptions.options().sourceTransform( imageTransform ) );

		final FinalInterval interval = new FinalInterval( new long[]{ 0, 0, 0 },
    													  new long[]{ 100, 100, 100 } );
    	final SplineGridOverlay splineOverlay = new SplineGridOverlay( 3,
    																   6,
    																   interval.dimension( 0 ),
    																   interval.dimension( 1 ),
    																   interval.dimension( 2 ) );    	
    	BdvFunctions.showOverlay( splineOverlay, "overlay", Bdv.options().addTo( bdv ) );

        final ArrayList<RealPoint> controlPoints = splineOverlay.getControlPoints();

		ControlPointsModel model=new ControlPointsModel(controlPoints, imageTransform);
		ControlPointsEditor pointsEditor = new ControlPointsEditor(new InputTriggerConfig(),
																   bdv.getBdvHandle().getViewerPanel(),
																   bdv.getBdvHandle().getTriggerbindings(),
																   model);
		//pointsEditor.setPerspective(1.0D, 1000.0D);
		pointsEditor.setEditable(true);
		pointsEditor.install();
		model.pointsChangedListeners().add( () -> {
			bdv.getBdvHandle().getViewerPanel().getDisplay().repaint();
		} );

	}
}