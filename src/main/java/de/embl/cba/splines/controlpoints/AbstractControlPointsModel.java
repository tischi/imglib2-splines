/*
 * #%L
 * BigDataViewer core classes with minimal dependencies
 * %%
 * Copyright (C) 2012 - 2016 Tobias Pietzsch, Stephan Saalfeld, Stephan Preibisch,
 * Jean-Yves Tinevez, HongKee Moon, Johannes Schindelin, Curtis Rueden, John Bogovic
 * %%
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * #L%
 */
package de.embl.cba.splines.controlpoints;

import bdv.tools.boundingbox.TransformedBox;
import net.imglib2.Point;
import net.imglib2.RealInterval;
import net.imglib2.RealPoint;
import net.imglib2.realtransform.AffineTransform3D;
import org.scijava.listeners.Listeners;

import java.util.List;

/**
 * Controls points that can be modified and notify listeners about changes.
 * Represented as a list of points (defined in subclasses) that is placed into
 * global coordinate system by an {@code AffineTransform3D}.
 */
public abstract class AbstractControlPointsModel implements ControlPoints
{
	public interface PointsChangedListener
	{
		void pointsChanged();
	}

	private final AffineTransform3D transform; // TODO: do we really need this? Could be the transform of the image to which the points are attached

	private final Listeners.List< PointsChangedListener > listeners;

	public AbstractControlPointsModel( final AffineTransform3D transform )
	{
		this.transform = transform;
		listeners = new Listeners.SynchronizedList<>();
	}

	@Override
	public void getTransform( final AffineTransform3D t )
	{
		t.set( transform );
	}

	public Listeners< PointsChangedListener > pointsChangedListeners()
	{
		return listeners;
	}

	public abstract void setPoints( List< RealPoint > points );

	protected void notifyPointsChanged()
	{
		listeners.list.forEach( PointsChangedListener::pointsChanged );
	}
}
