package de.embl.cba.splines.controlpoints;

import net.imglib2.RealInterval;
import net.imglib2.RealPoint;
import net.imglib2.realtransform.AffineTransform3D;

import java.util.ArrayList;
import java.util.List;

public interface ControlPoints
{
	List< RealPoint > getPoints();

	void getTransform( final AffineTransform3D transform );
}
