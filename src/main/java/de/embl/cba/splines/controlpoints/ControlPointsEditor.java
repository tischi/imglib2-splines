package de.embl.cba.splines.controlpoints;

import bdv.tools.boundingbox.AbstractTransformedBoxModel;
import bdv.tools.boundingbox.TransformedBoxOverlay;
import bdv.tools.brightness.SetupAssignments;
import bdv.viewer.ViewerPanel;
import org.scijava.listeners.ChangeListener;
import org.scijava.listeners.ListenableVar;
import org.scijava.ui.behaviour.Behaviour;
import org.scijava.ui.behaviour.BehaviourMap;
import org.scijava.ui.behaviour.InputTrigger;
import org.scijava.ui.behaviour.io.InputTriggerConfig;
import org.scijava.ui.behaviour.util.Behaviours;
import org.scijava.ui.behaviour.util.TriggerBehaviourBindings;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static bdv.tools.boundingbox.TransformedBoxOverlay.BoxDisplayMode.FULL;


public class ControlPointsEditor
{
	private static final String BOUNDING_BOX_TOGGLE_EDITOR = "edit bounding-box";

	private static final String[] BOUNDING_BOX_TOGGLE_EDITOR_KEYS = new String[] { "button1" };

	private static final String BOUNDING_BOX_MAP = "bounding-box";

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
			final SetupAssignments setupAssignments,
			final TriggerBehaviourBindings triggerbindings,
			final AbstractTransformedBoxModel model )
	{
		this( keyconf, viewer, setupAssignments, triggerbindings, model, "selection", bdv.tools.boundingbox.TransformedBoxEditor.BoxSourceType.PLACEHOLDER );
	}

	public ControlPointsEditor(
			final InputTriggerConfig keyconf,
			final ViewerPanel viewer,
			final SetupAssignments setupAssignments,
			final TriggerBehaviourBindings triggerbindings,
			final AbstractControlPointsModel model,
			final String boxSourceName,
			final bdv.tools.boundingbox.TransformedBoxEditor.BoxSourceType boxSourceType )
	{
		this.viewer = viewer;
		this.triggerbindings = triggerbindings;

		/*
		 * Create an Overlay to show 3D points
		 */
		pointsOverlay = new ControlPointsOverlay( model );
		pointsOverlay.setPerspective( 0 );
		pointsOverlay.boxDisplayMode().listeners().add( () -> {
			viewer.requestRepaint();
			updateEditability();
		} );

		/*
		 * Create DragPointsBehaviour
		 */

		behaviours = new Behaviours( keyconf, "bdv" );
		behaviours.behaviour( new DragControlPointBehaviour( pointsOverlay, model ), BOUNDING_BOX_TOGGLE_EDITOR, BOUNDING_BOX_TOGGLE_EDITOR_KEYS );

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
		viewer.getDisplay().addHandler( pointsOverlay.getCornerHighlighter() );

		refreshBlockMap();
		updateEditability();

		if ( boxSource != null )
			boxSource.addToViewer();
	}

	public void uninstall()
	{
		viewer.getDisplay().removeOverlayRenderer( pointsOverlay );
		viewer.removeTransformListener( pointsOverlay );
		viewer.getDisplay().removeHandler( pointsOverlay.getCornerHighlighter() );

		triggerbindings.removeInputTriggerMap( BOUNDING_BOX_MAP );
		triggerbindings.removeBehaviourMap( BOUNDING_BOX_MAP );

		unblock();

		if ( boxSource != null )
			boxSource.removeFromViewer();
	}

	/**
	 * Only meaningful if {@code BoxSourceType == NONE}
	 */
	public boolean getFillIntersection()
	{
		return pointsOverlay.getFillIntersection();
	}

	/**
	 * Only meaningful if {@code BoxSourceType == NONE}
	 */
	public void setFillIntersection( final boolean fill )
	{
		pointsOverlay.fillIntersection( fill );
	}

	public ListenableVar< TransformedBoxOverlay.BoxDisplayMode, ChangeListener > boxDisplayMode()
	{
		return pointsOverlay.boxDisplayMode();
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
		pointsOverlay.showCornerHandles( editable );
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
		if ( editable && pointsOverlay.boxDisplayMode().get() == FULL )
		{
			pointsOverlay.setHighlightedCornerListener( this::highlightedCornerChanged );
			behaviours.install( triggerbindings, BOUNDING_BOX_MAP );
			highlightedCornerChanged();
		}
		else
		{
			pointsOverlay.setHighlightedCornerListener( null );
			triggerbindings.removeInputTriggerMap( BOUNDING_BOX_MAP );
			triggerbindings.removeBehaviourMap( BOUNDING_BOX_MAP );
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

	private void highlightedCornerChanged()
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

		final Set< InputTrigger > moveCornerTriggers = new HashSet<>();
		for ( final String s : BOUNDING_BOX_TOGGLE_EDITOR_KEYS )
			moveCornerTriggers.add( InputTrigger.getFromString( s ) );

		final Map< InputTrigger, Set< String > > bindings = triggerbindings.getConcatenatedInputTriggerMap().getAllBindings();
		final Set< String > behavioursToBlock = new HashSet<>();
		for ( final InputTrigger t : moveCornerTriggers )
			behavioursToBlock.addAll( bindings.get( t ) );

		blockMap.clear();
		final Behaviour block = new Behaviour() {};
		for ( final String key : behavioursToBlock )
			blockMap.put( key, block );
	}
}

