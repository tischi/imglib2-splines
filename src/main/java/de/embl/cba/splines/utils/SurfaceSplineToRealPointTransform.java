package de.embl.cba.splines.utils;

import net.imglib2.RealLocalizable;
import net.imglib2.RealPoint;
import net.imglib2.RealPositionable;
import net.imglib2.realtransform.RealTransform;

public class SurfaceSplineToRealPointTransform implements RealTransform
{
	/*
	* Number of parameters for parametrized surface representation,
	* not to be confused with the control points.
	*/
	final static int numParameters = 2;
	final static int numDimensions = 3;
	private final SplineSphere splineSphere;
	private final double width;
	private final double height;
	private final double depth;


	public SurfaceSplineToRealPointTransform( int m, double width, double height, double depth )
	{
		splineSphere = new SplineSphere( m );
		this.width = width;
		this.height = height;
		this.depth = depth;
		splineSphere.initializeDefaultShape( this.width, this.height, this.depth );
	}

	public SurfaceSplineToRealPointTransform( SplineSphere splineSphere )
	{
		this.splineSphere = splineSphere;
		this.width = 0;
		this.height = 0;
		this.depth = 0;
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
		final RealPoint realPoint = splineSphere.parametersToWorld( source[ 0 ], source[ 1 ] );

		for ( int d = 0; d < numDimensions; d++ )
			target[ d ] = realPoint.getDoublePosition( d );

	}

	@Override
	public void apply( RealLocalizable source, RealPositionable target )
	{
		final RealPoint realPoint = splineSphere.parametersToWorld(
				source.getDoublePosition( 0 ),
				source.getDoublePosition( 1 ) );

		for ( int d = 0; d < numDimensions; d++ )
			target.setPosition( realPoint.getDoublePosition( d ), d );
	}

	@Override
	public RealTransform copy()
	{
		return new SurfaceSplineToRealPointTransform( splineSphere.getM(), width, height, depth );
	}
}
