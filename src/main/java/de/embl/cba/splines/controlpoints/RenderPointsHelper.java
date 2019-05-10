package de.embl.cba.splines.controlpoints;

import net.imglib2.RealPoint;
import net.imglib2.realtransform.AffineTransform3D;

import java.util.List;

public final class RenderPointsHelper
{
	/**
	 * distance from the eye to the projection plane z=0.
	 */
	private double depth = 10.0;

	/**
	 * scale the 2D projection of the points by this factor.
	 */
	private double scale = 0.1;

	private boolean perspective = false;

	private final double[] origin = new double[ 3 ];

	final int numPoints;

	final double[][] transformedPoints;

	final double[][] projectedPoints;

	public RenderPointsHelper(int numPoints){
		this.numPoints=numPoints;
		transformedPoints = new double[ numPoints ][ 3 ];
		projectedPoints = new double[ numPoints ][ 2 ];
	}

	public void setPerspectiveProjection( final boolean b )
	{
		perspective = b;
	}

	public void setDepth( final double depth )
	{
		this.depth = depth;
		origin[ 2 ] = -depth;
	}

	public void setOrigin( final double x, final double y )
	{
		origin[ 0 ] = x;
		origin[ 1 ] = y;
	}

	/**
	 * Project a point.
	 *
	 * @param point
	 *            point to project
	 * @param projection
	 *            projected point is stored here
	 */
	public void project( final double[] point, final double[] projection )
	{
		final double f = perspective
				? scale * depth / ( point[ 2 ] - origin[ 2 ] )
				: scale;
		projection[ 0 ] = ( point[ 0 ] - origin[ 0 ] ) * f + origin[ 0 ];
		projection[ 1 ] = ( point[ 1 ] - origin[ 1 ] ) * f + origin[ 1 ];
	}

	/**
	 * Project a point.
	 *
	 * @param point
	 *            point to project
	 * @return projected point
	 */
	public double[] project( final double[] point )
	{
		final double[] projection = new double[ 2 ];
		project( point, projection );
		return projection;
	}

	/**
	 * Reproject a point
	 *
	 * @param x
	 *            projected x
	 * @param y
	 *            projected y
	 * @param z
	 *            z plane to which to reproject
	 * @return reprojected point
	 */
	public double[] reproject( final double x, final double y, final double z )
	{
		final double[] point = new double[ 3 ];
		final double f = perspective
				? ( z - origin[ 2 ] ) / ( scale * depth )
				: 1. / scale;
		point[ 0 ] = ( x - origin[ 0 ] ) * f + origin[ 0 ];
		point[ 1 ] = ( y - origin[ 1 ] ) * f + origin[ 1 ];
		point[ 2 ] = z;
		return point;
	}

	public void renderPoints(
			final List< RealPoint > points,
			final AffineTransform3D transform)
	{
		for ( int i = 0; i <numPoints ; ++i )
		{
			final double[] position = new double[ 3 ];
			for ( int d = 0; d < 3; d++ )
				position[ d ] = points.get( i ).getDoublePosition( d );

			transform.apply( position, transformedPoints[ i ] );
			project( transformedPoints[ i ], projectedPoints[ i ] );
		}
	}
}
