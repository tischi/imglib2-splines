package de.embl.cba.splines.distance;

import bdv.util.RealRandomAccessibleSource;
import net.imglib2.*;
import net.imglib2.realtransform.AffineTransform3D;
import net.imglib2.type.numeric.real.DoubleType;

import java.util.List;


public class PointsDistanceSource extends RealRandomAccessibleSource< DoubleType >
{
	protected Interval interval;

	protected AffineTransform3D sourceTransform = new AffineTransform3D();

	public PointsDistanceSource( final String name, final Interval interval, final List< RealPoint > points )
	{
		super( new PointsDistanceRealRandomAccessible( points ), new DoubleType(), name );
		this.interval = interval;
	}

	@Override
	public Interval getInterval( final int t, final int level )
	{
		return interval;
	}

	static class PointsDistanceRealRandomAccessible implements RealRandomAccessible< DoubleType >
	{
		private final int n = 3;

		private final List< RealPoint > points;

		public PointsDistanceRealRandomAccessible( final List< RealPoint > points )
		{
			this.points = points;
		}

		@Override
		public int numDimensions()
		{
			return n;
		}

		@Override
		public RealRandomAccess< DoubleType > realRandomAccess()
		{
			return new Access( );
		}

		@Override
		public RealRandomAccess< DoubleType > realRandomAccess( final RealInterval interval )
		{
			return new Access( );
		}

		public class Access extends RealPoint implements RealRandomAccess< DoubleType >
		{
			private final DoubleType type;

			public Access(  )
			{
				super( PointsDistanceRealRandomAccessible.this.n );
				type = new DoubleType();
				type.setZero();
			}

			@Override
			public DoubleType get()
			{
				double distance = getDoublePosition( 0 ); // TODO
//				final RealPoint realPoint = points.get( 0 );

				type.set( distance );

				return type;
			}

			@Override
			public Access copy()
			{
				return this;
			}

			@Override
			public Access copyRealRandomAccess()
			{
				return copy();
			}
		}
	}
}
