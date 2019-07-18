package de.embl.cba.splines.utils;

import net.imglib2.RealPoint;
import net.imglib2.realtransform.AffineTransform3D;
import net.imglib2.realtransform.RealTransform;
import net.imglib2.realtransform.RealTransformSequence;
import bdv.util.BdvOverlay;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class SplineGridOverlay extends BdvOverlay
{
	private final static int MAXSAMPLINGRATE = 500;
	private int samplingRate;

	private final int numControlPointsT;
	private final int numControlPointsS;

	private final int lineDensity;

	private final SurfaceSplineToRealPointTransform spline;

	private final AffineTransform3D transform;

	private List< List< RealPoint > > grid;

	public SplineGridOverlay( int lineDensity, int m, double width, double height, double depth )
	{
		numControlPointsT = m;
		numControlPointsS = m;
		this.lineDensity = lineDensity;
		transform = new AffineTransform3D();
		spline = new SurfaceSplineToRealPointTransform( m, m, width, height, depth );
	}

	private List< List< RealPoint > > createGrid( int lineDensity, int numControlPointsT, int numControlPointsS )
	{
		List< List< RealPoint > > gridPoints = new ArrayList();

		int numLongitudes = lineDensity * (numControlPointsT);

		for( int i = 0; i <= numLongitudes; i++ )
		{
			RealPoint xStartPoint = RealPoint.wrap( new double[] { i / (double) lineDensity, 0 } );
			RealPoint xEndPoint = RealPoint.wrap( new double[] { i / (double) lineDensity, numControlPointsS-1 } );
			gridPoints.add( createGridPoints( xStartPoint, xEndPoint ) );
		}

		int numLatitudes = lineDensity * (numControlPointsS-1);

		for( int i = 0; i <= numLatitudes; i++ )
		{
			RealPoint yStartPoint = RealPoint.wrap( new double[] { 0,  i / (double) lineDensity } );
			RealPoint yEndPoint = RealPoint.wrap( new double[] { numControlPointsT, i / (double) lineDensity} );
			gridPoints.add( createGridPoints( yStartPoint, yEndPoint ) );
		}
		return gridPoints;
	}

	private List< RealPoint > createGridPoints( RealPoint startPoint, RealPoint endPoint )
	{
		List< RealPoint > points = new ArrayList< RealPoint >();
		RealPoint realPoint;
		int dim = startPoint.numDimensions();

		for( int i = 0; i <= samplingRate; i++)
		{
			realPoint = new RealPoint( dim );
			for( int j = 0; j < dim; j++)
			{
				double position = ( i / (double) samplingRate ) * startPoint.getDoublePosition( j )
								  + (1 - (i / (double) samplingRate ) ) * endPoint.getDoublePosition( j );
				realPoint.setPosition( position, j );
			}
			points.add( realPoint );
		}
		return points;
	}

	private double getSamplingRate()
	{
		ArrayList< RealPoint > controlPoints = getControlPoints();
		ArrayList< RealPoint > controlPointsScreen = new ArrayList< RealPoint >();

		for( int i = 0; i < controlPoints.size(); i++ )
		{
			RealPoint point = new RealPoint( 3 );
			transform.apply( controlPoints.get( i ), point );
			controlPointsScreen.add( point );
		}

		double max = -1;
		for( int i = 0; i < controlPointsScreen.size() - 1; i++ )
		{
			double pos = 0;
			for( int j = 0; j < 3; j++ )
			{
				double d2 = controlPointsScreen.get( i+1 ).getDoublePosition( j );
				double d1 = controlPointsScreen.get( i ).getDoublePosition( j );
				pos += Math.pow( d2-d1, 2 );
			}
			pos = Math.sqrt(pos);
			if( max < pos )
				max = pos;
		}
		
		return Math.min( controlPoints.size()*max, MAXSAMPLINGRATE );
	}

	@Override
	protected void draw( final Graphics2D g )
	{
		g.setColor( Color.RED );
		RealTransformSequence transformSequence = new RealTransformSequence();
		transformSequence.add( spline );

		getCurrentTransform3D( transform );
		transformSequence.add( transform );

		samplingRate = (int) getSamplingRate();

		grid = createGrid( lineDensity, numControlPointsT, numControlPointsS );

		drawLines( g, transformSequence, grid );
	}

	private static void drawLines( Graphics2D g, RealTransform t, List< List< RealPoint > > grid )
	{
		for( List< RealPoint > points: grid )
			drawLine( g, t, points );
	}

	private static void drawLine( Graphics2D g, RealTransform t, List< RealPoint > points )
	{
		final RealPoint screenCoordinates = new RealPoint( 3 );
		int[] x = new int[ points.size() ];
		int[] y = new int[ points.size() ];

		for( int i = 0; i < points.size(); i++ )
		{
			t.apply( points.get( i ), screenCoordinates);
			x[ i ] = ( int ) screenCoordinates.getDoublePosition( 0 );
			y[ i ] = ( int ) screenCoordinates.getDoublePosition( 1 );
		}

		g.drawPolyline( x, y, points.size() );
	}

	public ArrayList<RealPoint> getControlPoints()
	{
		return this.spline.getControlPoints();
	}
}		