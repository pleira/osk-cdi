package org.osk.models.astris.parts;
import javax.enterprise.context.ApplicationScoped;
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
import org.osk.interceptors.Log;
import org.osk.models.rocketpropulsion.SplitT1;
import org.osk.ports.FluidPort;

@Log
@ApplicationScoped
public class Split10 {
		
	final static String NAME = "Split10"; 
	
	@Inject SplitT1 model;
	@Inject @Named(NAME) @Right @Iter Event<FluidPort> eventRight;
	@Inject @Named(NAME) @Left  @Iter Event<FluidPort> eventLeft;
	@Inject @Named(NAME) @Right @TimeIter Event<FluidPort> outputEventRight;
	@Inject @Named(NAME) @Left  @TimeIter Event<FluidPort> outputEventLeft;
	@Inject @Named(Pipe09.NAME) @BackIter Event<FluidPort> backEvent;
	
	FluidPort left;
	FluidPort right;
	ImmutablePair<FluidPort, FluidPort> output;
	
	public void iteration(@Observes @Named(Pipe09.NAME) @Iter FluidPort inputPort) {
		output = model.iterationStep(inputPort);
		eventRight.fire(output.getRight());
		eventLeft.fire(output.getLeft());
	}

	public void timeIteration(@Observes @Named(Pipe09.NAME) @TimeIter FluidPort inputPort) {
		if (output == null) output = model.iterationStep(inputPort);		
		outputEventRight.fire(output.getRight());
		outputEventLeft.fire(output.getLeft());
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
		left = right = null; // events processed
		backEvent.fire(input);
	}

}
