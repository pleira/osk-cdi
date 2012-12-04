package org.osk.models.astris.parts;

import javax.enterprise.event.Event;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.osk.events.BackIter;
import org.osk.events.Iter;
import org.osk.events.Left;
import org.osk.events.Right;
import org.osk.events.TimeIter;
import org.osk.models.rocketpropulsion.SplitT1;
import org.osk.ports.FluidPort;



public class Split10 {
		
	public final static String NAME = "Split10"; 
	
	@Inject SplitT1 model;
	@Inject @Named(NAME) @Right @Iter Event<FluidPort> eventRight;
	@Inject @Named(NAME) @Left  @Iter Event<FluidPort> eventLeft;
	@Inject @Named(NAME) @Right @TimeIter Event<FluidPort> outputEventRight;
	@Inject @Named(NAME) @Left  @TimeIter Event<FluidPort> outputEventLeft;
	@Inject @Named(Pipe09.NAME) @BackIter Event<FluidPort> backEvent;
	
	FluidPort left;
	FluidPort right;

	public void iteration(@Observes @Named(Pipe09.NAME) @Iter FluidPort inputPort) {
		ImmutablePair<FluidPort, FluidPort> output = model.iterationStep(inputPort);
		eventRight.fire(output.getRight());
		eventLeft.fire(output.getLeft());
	}

	public void timeIteration(@Observes @Named(Pipe09.NAME) @TimeIter FluidPort inputPort) {
		FluidPort left = model.createGasPort(inputPort.getFluid(), model.getMfboundLeft());
		FluidPort right = model.createGasPort(inputPort.getFluid(), model.getMfboundRight());
		outputEventRight.fire(right);
		outputEventLeft.fire(left);
	}

	public void backIterateLeft(@Observes @Named(NAME) @Left @BackIter FluidPort outputPort) {
		left = outputPort;
		if (right !=  null) {
			fireBackIterate();
		}
	}
	
	public void backIterateRight(@Observes @Named(NAME) @Right @BackIter FluidPort outputPort) {
		right = outputPort;
		if (left !=  null) {
			fireBackIterate();
		}
	}

	private void fireBackIterate() {
		FluidPort input = model.backIterate(left, right);
		backEvent.fire(input);
		left = right = null; // events processed
	}

}
