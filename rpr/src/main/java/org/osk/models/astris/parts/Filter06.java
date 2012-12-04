package org.osk.models.astris.parts;

import javax.enterprise.event.Event;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.inject.Named;

import org.osk.events.BackIter;
import org.osk.events.Iter;
import org.osk.events.TimeIter;
import org.osk.models.rocketpropulsion.FilterT1;
import org.osk.ports.FluidPort;

public class Filter06  {

	public final static String NAME = "Filter06";

	@Inject FilterT1 model;
	@Inject @Named(NAME) @Iter Event<FluidPort> event;
	@Inject @Named(NAME) @TimeIter Event<FluidPort> outputEvent;
	@Inject @Named(Pipe05.NAME) @BackIter Event<FluidPort> backEvent;
	
	public void iteration(@Observes @Named(Pipe05.NAME) @Iter FluidPort inputPort) {
		FluidPort output = model.iterationStep(inputPort);
		event.fire(output);
	}

	public void timeIteration(@Observes @Named(Pipe05.NAME) @TimeIter FluidPort input) {
		model.timeStep(input);
		FluidPort output = model.createOutputPort(input.getFluid());
		outputEvent.fire(output);
	}

	public void backIterate(@Observes @Named(NAME) @BackIter FluidPort outputPort) {
		FluidPort input = model.backIterStep(outputPort);
		backEvent.fire(input);
	}

}
