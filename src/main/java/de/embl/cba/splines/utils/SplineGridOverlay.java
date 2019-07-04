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
	private final RealTransform spline;

	private List< List< RealPoint > > grid = createGrid( 15 );

	public SplineGridOverlay( RealTransform spline )
	{
		this.spline = spline;
	}

	private static List< List< RealPoint > > createGrid( int numLines )
	{
		List< List< RealPoint > > lines = new ArrayList();

		for( int i = 0; i <= 6*numLines; i++ )
		{
			RealPoint xStartPoint = RealPoint.wrap( new double[] { i / (double) numLines, 0 } );
			RealPoint xEndPoint = RealPoint.wrap( new double[] { i / (double) numLines, 6 } );
			lines.add( createGridLine( xStartPoint, xEndPoint ) );
		}

		for( int i = 0; i <= 6*numLines; i++ )
		{
			RealPoint yStartPoint = RealPoint.wrap( new double[] { 0,  i / (double) numLines } );
			RealPoint yEndPoint = RealPoint.wrap( new double[] { 6, i / (double) numLines } );
			lines.add( createGridLine( yStartPoint, yEndPoint ) );
		}
		return lines;
	}

	private static List< RealPoint > createGridLine( RealPoint startPoint, RealPoint endPoint )
	{
		List< RealPoint > points = new ArrayList<>( 100 );
		RealPoint realPoint;
		int dim = startPoint.numDimensions();

		for( int i = 0; i <= 100; i++)
		{
			realPoint = new RealPoint( dim );
			for( int j = 0; j < dim; j++)
			{
				double position = ( i / 100. ) * startPoint.getDoublePosition( j )
								  + (1 - (i / 100. ) ) * endPoint.getDoublePosition( j );
				realPoint.setPosition( position, j );
			}
			points.add(realPoint);
		}
		return points;
	}

	@Override
	protected void draw( final Graphics2D g )
	{
		g.setColor( Color.RED );
		RealTransformSequence transformSequence = new RealTransformSequence();
		transformSequence.add( spline );

		final AffineTransform3D t = new AffineTransform3D();
		getCurrentTransform3D( t );
		transformSequence.add( t );

		drawLines( g, transformSequence, grid );
	}

	private static void drawLines( Graphics2D g, RealTransform t, List< List< RealPoint > > grid)
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
}