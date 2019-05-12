package de.embl.cba.splines.controlpoints;

import bdv.viewer.ViewerPanel;
import org.scijava.ui.behaviour.Behaviour;
import org.scijava.ui.behaviour.BehaviourMap;
import org.scijava.ui.behaviour.InputTrigger;
import org.scijava.ui.behaviour.io.InputTriggerConfig;
import org.scijava.ui.behaviour.util.Behaviours;
import org.scijava.ui.behaviour.util.TriggerBehaviourBindings;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ControlPointsEditor
{
	private static final String POINTS_TOGGLE_EDITOR = "edit points";

	private static final String[] POINTS_TOGGLE_EDITOR_KEYS = new String[] { "button1" };

	private static final String POINTS_MAP = "bounding-box";

	private static final String BLOCKING_MAP = "bounding-box-blocking";

	private final ControlPointsOverlay pointsOverlay;

	private final ViewerPanel viewer;

	private final TriggerBehaviourBindings triggerbindings;

	private final Behaviours behaviours;

	private final BehaviourMap blockMap;

	private boolean editable = true;

	public ControlPointsEditor(
			final InputTriggerConfig keyconf,
			final ViewerPanel viewer,
			final TriggerBehaviourBindings triggerbindings,
			final AbstractControlPointsModel model)
	{
		this.viewer = viewer;
		this.triggerbindings = triggerbindings;

		/*
		 * Create an Overlay to show 3D points
		 */
		pointsOverlay = new ControlPointsOverlay( model );
		pointsOverlay.setPerspective( 0 );
		viewer.requestRepaint();

		/*
		 * Create DragPointsBehaviour
		 */

		behaviours = new Behaviours( keyconf, "bdv" );
		behaviours.behaviour( new DragControlPointBehaviour( pointsOverlay, model ), POINTS_TOGGLE_EDITOR, POINTS_TOGGLE_EDITOR_KEYS );

		/*
		 * Create BehaviourMap to block behaviours interfering with
		 * DragBoxCornerBehaviour. The block map is only active while a corner
		 * is highlighted.
		 */
		blockMap = new BehaviourMap();
	}

	public void install()
	{
		viewer.getDisplay().addOverlayRenderer( pointsOverlay );
		viewer.addRenderTransformListener( pointsOverlay );
		viewer.getDisplay().addHandler( pointsOverlay.getPointHighlighter() );

		refreshBlockMap();
		updateEditability();
	}

	public void uninstall()
	{
		viewer.getDisplay().removeOverlayRenderer( pointsOverlay );
		viewer.removeTransformListener( pointsOverlay );
		viewer.getDisplay().removeHandler( pointsOverlay.getPointHighlighter() );

		triggerbindings.removeInputTriggerMap( POINTS_MAP );
		triggerbindings.removeBehaviourMap( POINTS_MAP );

		unblock();
	}

	public boolean isEditable()
	{
		return editable;
	}

	public void setEditable( final boolean editable )
	{
		if ( this.editable == editable )
			return;
		this.editable = editable;
		updateEditability();
	}

	/**
	 * Sets up perspective projection for the overlay. Basically, the projection
	 * center is placed at distance {@code perspective * sourceSize} from the
	 * projection plane (screen). Specify {@code perspective = 0} to set
	 * parallel projection.
	 *
	 * @param perspective
	 *            the perspective value.
	 * @param sourceSize
	 *            the the size of the source.
	 */
	public void setPerspective( final double perspective, final double sourceSize )
	{
		pointsOverlay.setPerspective( perspective );
		pointsOverlay.setSourceSize( sourceSize );
	}


	private void updateEditability()
	{
		if ( editable  )
		{
			pointsOverlay.setHighlightedPointListener( this::highlightedPointChanged );
			behaviours.install( triggerbindings, POINTS_MAP );
			highlightedPointChanged();
		}
		else
		{
			pointsOverlay.setHighlightedPointListener( null );
			triggerbindings.removeInputTriggerMap( POINTS_MAP );
			triggerbindings.removeBehaviourMap( POINTS_MAP );
			unblock();
		}
	}

	private void block()
	{
		triggerbindings.addBehaviourMap( BLOCKING_MAP, blockMap );
	}

	private void unblock()
	{
		triggerbindings.removeBehaviourMap( BLOCKING_MAP );
	}

	private void highlightedPointChanged()
	{
		final int index = pointsOverlay.getHighlightedPointIndex();
		if ( index < 0 )
			unblock();
		else
			block();
	}

	private void refreshBlockMap()
	{
		triggerbindings.removeBehaviourMap( BLOCKING_MAP );

		final Set< InputTrigger > movePointsTriggers = new HashSet<>();
		for ( final String s : POINTS_TOGGLE_EDITOR_KEYS )
			movePointsTriggers.add( InputTrigger.getFromString( s ) );

		final Map< InputTrigger, Set< String > > bindings = triggerbindings.getConcatenatedInputTriggerMap().getAllBindings();
		final Set< String > behavioursToBlock = new HashSet<>();
		for ( final InputTrigger t : movePointsTriggers )
			behavioursToBlock.addAll( bindings.get( t ) );

		blockMap.clear();
		final Behaviour block = new Behaviour() {};
		for ( final String key : behavioursToBlock )
			blockMap.put( key, block );
	}
}

