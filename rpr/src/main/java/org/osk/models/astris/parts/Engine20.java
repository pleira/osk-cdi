package org.osk.models.astris.parts;
import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Event;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.osk.config.NumberConfig;
import org.osk.errors.OskException;
import org.osk.events.BackIter;
import org.osk.events.ECEFpv;
import org.osk.events.Fuel;
import org.osk.events.Iter;
import org.osk.events.Iteration;
import org.osk.events.Oxid;
import org.osk.events.TimeIter;
import org.osk.interceptors.Log;
import org.osk.models.rocketpropulsion.Engine;
import org.osk.ports.FluidPort;

@Log
@ApplicationScoped
public class Engine20  {

	public final static String NAME = "Engine20";
	
	@Inject Engine model;
	@Inject @Named(NAME) @Iter Event<Iteration> event;
	@Inject @Named(NAME) @TimeIter Event<Vector3D> timerEvent;
	@Inject @Named(FFV19.NAME) @Oxid @BackIter Event<FluidPort> backOxidEvent;
	@Inject @Named(FFV18.NAME) @Fuel @BackIter Event<FluidPort> backFuelEvent;

	FluidPort inputOxid;
	FluidPort inputFuel;

	public void iterationFuel(
			@Observes @Named(FFV18.NAME) @Iter FluidPort inputPort) throws OskException {
		inputFuel = inputPort;
		if (inputOxid != null) {
			fireIterationStep();
		}
	}

	public void iterationOxid(
			@Observes @Named(FFV19.NAME) @Iter FluidPort inputPort) throws OskException {
		inputOxid = inputPort;
		if (inputFuel != null) {
			fireIterationStep();
		}
	}

	public void timeIterationFuel(
			@Observes @Named(FFV18.NAME) @TimeIter FluidPort inputPort) throws OskException {
		inputFuel = inputPort;
		if (inputOxid != null) {
			fireTimeIteration();
		}
	}

	public void timeIterationOxid(
			@Observes @Named(FFV19.NAME) @TimeIter FluidPort inputPort) throws OskException {
		inputOxid = inputPort;
		if (inputFuel != null) {
			fireTimeIteration();
		}
	}

	public void backIterateFuel(
			@Observes @Named(NAME) @BackIter Iteration back) {
		ImmutablePair<FluidPort, FluidPort> input = model.backIterStep();
		backFuelEvent.fire(input.getLeft());
		backOxidEvent.fire(input.getRight());
	}

	private void fireIterationStep() throws OskException {
		model.iterationStep(inputFuel, inputOxid);
		event.fire(new Iteration());
		inputFuel = inputOxid = null; // events processed
	}

	private void fireTimeIteration() throws OskException {
		Vector3D thrust = model.computeThrust(inputFuel, inputOxid);
		timerEvent.fire(thrust);
		inputFuel = inputOxid = null; // events processed
	}

	
	public void altitudeHandler(@Observes ECEFpv pv) {
		model.setAlt(pv.getAltitude());
	}

	//---------------------------------------------------------------------------------------
	// Initialisation values

	@PostConstruct
    void initModel() {
    	model.init(NAME);
    }
	
	@Inject
	void initIngnitionFuelFlow(@NumberConfig(name = "engine20.ignitionFuelFlow", defaultValue = "0.0") Double value) {
		model.setIgnitionFuelFlow(value);
	}
	@Inject
	void initIngnitionOxidizerFlow(@NumberConfig(name = "engine20.ignitionOxidizerFlow", defaultValue = "0.0") Double value) {
		model.setIgnitionOxidizerFlow(value);
	}
	@Inject
	void initAlt(@NumberConfig(name = "engine20.alt", defaultValue = "600000") Double value) {
		model.setAlt(value);
	}

}
