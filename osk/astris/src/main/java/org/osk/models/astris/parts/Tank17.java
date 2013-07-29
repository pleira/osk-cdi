package org.osk.models.astris.parts;
import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Event;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.deltaspike.core.api.config.ConfigProperty;
import org.osk.config.NumberConfig;
import org.osk.config.Util;
import org.osk.events.BackIter;
import org.osk.events.Fuel;
import org.osk.events.Iter;
import org.osk.events.Oxid;
import org.osk.events.TimeIter;
import org.osk.interceptors.Log;
import org.osk.models.Tank;
import org.osk.models.t1.BoundaryUtils;
import org.osk.ports.FluidPort;
import org.osk.time.TimeHandler;

@Log
@ApplicationScoped
public class Tank17 {

	public final static String NAME = "Tank17";

	@Inject Tank model;
	@Inject @Named(NAME) @Oxid @Iter Event<FluidPort> eventOxid;
	@Inject @Named(NAME) @Fuel @Iter Event<FluidPort> eventFuel;
	@Inject @Named(NAME) @Oxid @TimeIter Event<FluidPort> outputEventOxid;
	@Inject @Named(NAME) @Fuel @TimeIter Event<FluidPort> outputEventFuel;
	@Inject @Named(Pipe13.NAME) @BackIter Event<FluidPort> backEvent13;
	@Inject @Named(Pipe16.NAME) @BackIter Event<FluidPort> backEvent16;
	@Inject TimeHandler timeHandler;

	FluidPort inputFuel;
	FluidPort inputOx;
	FluidPort outputFuel;
	FluidPort outputOx;

	public void iterationFuel(
			@Observes @Named(Pipe13.NAME) @Iter FluidPort inputPort) {
		inputFuel = inputPort;
		if (inputOx != null) {
			fireIterationStep();
		}
	}

	public void iterationOxid(
			@Observes @Named(Pipe16.NAME) @Iter FluidPort inputPort) {
		inputOx = inputPort;
		if (inputFuel != null) {
			fireIterationStep();
		}
	}

	public void timeIterationFuel(
			@Observes @Named(Pipe13.NAME) @TimeIter FluidPort inputPort) {
		inputFuel = inputPort;
		if (inputOx != null) {
			fireTimeIteration();
		}
	}

	public void timeIterationOxid(
			@Observes @Named(Pipe16.NAME) @TimeIter FluidPort inputPort) {
		inputOx = inputPort;
		if (inputFuel != null) {
			fireTimeIteration();
		}
	}

	public void backIterateFuel(
			@Observes @Named(NAME) @BackIter @Fuel FluidPort outputPort) {
		outputFuel = outputPort;
		if (outputOx != null) {
			fireBackIteration();
		}
	}

	public void backIterateOxid(
			@Observes @Named(NAME) @BackIter @Oxid FluidPort outputPort) {
		outputOx = outputPort;
		if (outputFuel != null) {
			fireBackIteration();
		}
	}

	private void fireIterationStep() {
		ImmutablePair<FluidPort, FluidPort> output = model.calculateOutletsMassFlow(
				inputFuel, inputOx);
		inputFuel = inputOx = null; // events processed
		eventOxid.fire(output.getRight());
		eventFuel.fire(output.getLeft());
	}

	private void fireTimeIteration() {
		ImmutablePair<FluidPort, FluidPort> output = model.propagate(
				timeHandler.getSimulatedMissionTimeAsDouble(),
				timeHandler.getStepSizeAsDouble(),
				inputFuel,
				inputOx);
		inputFuel = inputOx = null; // events processed
		outputEventOxid.fire(output.getRight());
		outputEventFuel.fire(output.getLeft());
	}

	private void fireBackIteration() {
		// the tank just request a mass flow 
		// FIXME: it seems there is no connection with oxid/fuel mass flow values from valves
        FluidPort inputPortFuel = BoundaryUtils.createBoundaryPort("Helium", model.getMfBoundFuelPress());
        FluidPort inputPortOxidizer = BoundaryUtils.createBoundaryPort("Helium", model.getMfBoundOxPress());
		backEvent13.fire(inputPortFuel);
		backEvent16.fire(inputPortOxidizer);
		outputFuel = outputOx = null; // events processed
	}

	// ---------------------------------------------------------------------------------------
	// Initialisation values

	@PostConstruct
    void initModel() {
    	model.init(NAME);
    }

}
