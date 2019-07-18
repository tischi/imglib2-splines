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
	private final int numControlPointsT;
	private final int numControlPointsS;

	private final SurfaceSplineToRealPointTransform spline;

	private final AffineTransform3D transform = new AffineTransform3D();

	private List< List< RealPoint > > grid;

	public SplineGridOverlay( int pointDensity, int m, double width, double height, double depth )
	{
		//this.spline = spline;
		numControlPointsT = m;
		numControlPointsS = m;
		spline = new SurfaceSplineToRealPointTransform( m, m, width, height, depth );
		grid = createGrid( pointDensity, numControlPointsT, numControlPointsS );
	}

	private static List< List< RealPoint > > createGrid( int pointDensity, int numControlPointsT, int numControlPointsS )
	{
		List< List< RealPoint > > gridPoints = new ArrayList();

		int numLongitudes = pointDensity * (numControlPointsT);

		for( int i = 0; i <= numLongitudes; i++ )
		{
			RealPoint xStartPoint = RealPoint.wrap( new double[] { i / (double) pointDensity, 0 } );
			RealPoint xEndPoint = RealPoint.wrap( new double[] { i / (double) pointDensity, numControlPointsS-1 } );
			gridPoints.add( createGridPoints( xStartPoint, xEndPoint ) );
		}

		int numLatitudes = pointDensity * (numControlPointsS-1);

		for( int i = 0; i <= numLatitudes; i++ )
		{
			RealPoint yStartPoint = RealPoint.wrap( new double[] { 0,  i / (double) pointDensity } );
			RealPoint yEndPoint = RealPoint.wrap( new double[] { numControlPointsT, i / (double) pointDensity} );
			gridPoints.add( createGridPoints( yStartPoint, yEndPoint ) );
		}
		return gridPoints;
	}

	private static List< RealPoint > createGridPoints( RealPoint startPoint, RealPoint endPoint )
	{
		List< RealPoint > points = new ArrayList< RealPoint >();
		RealPoint realPoint;
		int dim = startPoint.numDimensions();

		// TODO: Take care of sampling! --> Adapt to bigdataviewer distance between pixels
		for( int i = 0; i <= 100; i++)
		{
			realPoint = new RealPoint( dim );
			for( int j = 0; j < dim; j++)
			{
				double position = ( i / 100. ) * startPoint.getDoublePosition( j )
								  + (1 - (i / 100. ) ) * endPoint.getDoublePosition( j );
				realPoint.setPosition( position, j );
			}
			points.add( realPoint );
		}
		return points;
	}

	@Override
	protected void draw( final Graphics2D g )
	{
		g.setColor( Color.RED );
		RealTransformSequence transformSequence = new RealTransformSequence();
		transformSequence.add( spline );

		//final AffineTransform3D t = new AffineTransform3D();
		getCurrentTransform3D( transform );
		transformSequence.add( transform );

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

	public AffineTransform3D getAffineTransform3D()
	{
		return this.transform;
	}
}		