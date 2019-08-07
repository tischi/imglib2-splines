package de.embl.cba.splines.utils;

import net.imglib2.RealLocalizable;
import net.imglib2.RealPoint;
import net.imglib2.RealPositionable;
import net.imglib2.realtransform.RealTransform;

import java.util.ArrayList;

public class SurfaceSplineToRealPointTransform implements RealTransform
{
	/*
	* Number of parameters for parametrized surface representation,
	* not to be confused with the control points.
	*/
	final static int numParameters = 2;
	final static int numDimensions = 3;
	private final AbstractSplineSurface splineSurface;
	private final double width;
	private final double height;
	private final double depth;


	public SurfaceSplineToRealPointTransform( AbstractSplineSurface spline, double width, double height, double depth )
	{
		splineSurface = spline;
		this.width = width;
		this.height = height;
		this.depth = depth;
		splineSurface.initializeDefaultShape( this.width, this.height, this.depth );
	}

	@Override
	public int numSourceDimensions()
	{
		return numParameters;
	}

	@Override
	public int numTargetDimensions()
	{
		return numDimensions;
	}

	@Override
	public void apply( double[] source, double[] target )
	{
		final RealPoint realPoint = splineSurface.parametersToWorld( source[ 0 ], source[ 1 ] );

		for ( int d = 0; d < numDimensions; d++ )
			target[ d ] = realPoint.getDoublePosition( d );

	}

	@Override
	public void apply( RealLocalizable source, RealPositionable target )
	{
		final RealPoint realPoint = splineSurface.parametersToWorld(
				source.getDoublePosition( 0 ),
				source.getDoublePosition( 1 ) );

		for ( int d = 0; d < numDimensions; d++ )
			target.setPosition( realPoint.getDoublePosition( d ), d );
	}

	@Override
	public RealTransform copy()
	{
		return new SurfaceSplineToRealPointTransform( splineSurface, width, height, depth );
	}

	public ArrayList<RealPoint> getControlPoints()
	{
		return splineSurface.getControlPoints();
	}
}
