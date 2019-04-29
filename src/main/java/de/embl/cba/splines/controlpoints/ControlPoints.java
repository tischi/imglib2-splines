package de.embl.cba.splines.controlpoints;

import net.imglib2.RealInterval;
import net.imglib2.realtransform.AffineTransform3D;

import java.util.ArrayList;

public interface ControlPoints
{
	ArrayList< double[] > getPoints();

	void getTransform( final AffineTransform3D transform );
}
