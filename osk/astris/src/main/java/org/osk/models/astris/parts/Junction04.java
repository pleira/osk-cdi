package org.osk.models.astris.parts;
import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Event;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.osk.events.BackIter;
import org.osk.events.Iter;
import org.osk.events.TimeIter;
import org.osk.interceptors.Log;
import org.osk.models.t1.JunctionT1;
import org.osk.ports.FluidPort;

@Log
@ApplicationScoped
public class Junction04  {
		
	public final static String NAME = "Junction04"; 
	
	@Inject JunctionT1 model;
	@Inject @Named(NAME) @Iter Event<FluidPort> event;
	@Inject @Named(NAME) @TimeIter Event<FluidPort> outputEvent;
	@Inject @Named(Pipe02.NAME) @BackIter Event<FluidPort> backEvent02;
	@Inject @Named(Pipe03.NAME) @BackIter Event<FluidPort> backEvent03;

	FluidPort left;
	FluidPort right;
	
	public void iterationLeft(@Observes @Named(Pipe02.NAME) @Iter FluidPort inputPort) {
		left = inputPort;
		if (right !=  null) {
			fireIterationStep();
		}
	}

	public void iterationRight(@Observes @Named(Pipe03.NAME) @Iter FluidPort inputPort) {
		right = inputPort;
		if (left !=  null) {
			fireIterationStep();
		}
	}

	public void timeIterationLeft(@Observes @Named(Pipe02.NAME) @TimeIter FluidPort inputPort) {
		left = inputPort;
		if (right !=  null) {
			fireTimeIteration();
		}
	}
	public void timeIterationRight(@Observes @Named(Pipe03.NAME) @TimeIter FluidPort inputPort) {
		right = inputPort;
		if (left !=  null) {
			fireTimeIteration();
		}
	}

	public void backIterate(@Observes @Named(NAME) @BackIter FluidPort outputPort) {
		ImmutablePair<FluidPort, FluidPort> pair = model.backIterStep(outputPort);
		backEvent02.fire(pair.left);
		backEvent03.fire(pair.right);
	}

	private void fireIterationStep() {
		FluidPort output = model.calculateOutletMassFlow(left, right);
		left = right = null; // events processed
		event.fire(output);
	}

	private void fireTimeIteration() {
		FluidPort output = model.getOutputPortStatus(left.getFluid());
		left = right = null; // events processed
		outputEvent.fire(output);
	}
	//---------------------------------------------------------------------------------------
	// Initialisation values

	@PostConstruct
    void initModel() {
    	model.init(NAME);
    }
	
	
}
