package org.osk.models.astris.parts;
import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Event;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.osk.config.NumberConfig;
import org.osk.events.BackIter;
import org.osk.events.Fuel;
import org.osk.events.Iter;
import org.osk.events.Oxid;
import org.osk.events.RegulIter;
import org.osk.events.TimeIter;
import org.osk.interceptors.Log;
import org.osk.models.rocketpropulsion.EngineController;
import org.osk.ports.AnalogPort;
import org.osk.ports.FluidPort;

@Log
@ApplicationScoped
public class EngineController21   {
	
	public final static String NAME = "EngineController21";

	@Inject EngineController model;	
	@Inject @Named(NAME) @Fuel @Iter Event<AnalogPort> fuelEvent;
	@Inject @Named(NAME) @Oxid @Iter Event<AnalogPort> oxidEvent;
	@Inject @Named(NAME) @Fuel @TimeIter Event<AnalogPort> fuelTimeEvent;
	@Inject @Named(NAME) @Oxid @TimeIter Event<AnalogPort> oxidTimeEvent;
	@Inject @Named(NAME) @Fuel @RegulIter Event<AnalogPort> fuelRegulEvent;
	@Inject @Named(NAME) @Oxid @RegulIter Event<AnalogPort> oxidRegulEvent;
	@Inject @Named(FFV18.NAME) @Fuel @BackIter Event<AnalogPort> fuelBackEvent;
	@Inject @Named(FFV19.NAME) @Oxid @BackIter Event<AnalogPort> oxidBackEvent;
	
	boolean receivedFuel = false;
	boolean receivedOxid = false;
	AnalogPort fuelPort;
	AnalogPort oxidPort;
	
	public void iterationFuel(@Observes @Named(FFV18.NAME) @Iter FluidPort inputPort) {
		receivedFuel = true;
		if (receivedOxid) {
			fireIteration();
		}
	}
	
	public void iterationOxid(@Observes @Named(FFV19.NAME) @Iter FluidPort inputPort) {
		receivedOxid = true;
		if (receivedFuel) {
			fireIteration();
		}
	}

	public void timeIterationFuel(@Observes @Named(FFV18.NAME) @TimeIter FluidPort inputPort) {
		ImmutablePair<AnalogPort, AnalogPort> output = model.timeStep();
		fuelRegulEvent.fire(output.getLeft());
//		oxidRegulEvent.fire(output.getRight());
	}

	public void timeIterationOxid(@Observes @Named(FFV19.NAME) @TimeIter FluidPort inputPort) {
		 ImmutablePair<AnalogPort, AnalogPort> output = model.timeStep();
//		fuelRegulEvent.fire(output.getLeft());
		oxidRegulEvent.fire(output.getRight());
	}

	public void regulIterateFuel(@Observes @Named(FFV19.NAME) @Fuel @RegulIter AnalogPort outputPort) {
		fuelPort = outputPort;
		if (oxidPort != null) {
			fireRegulIteration();
		}
	}

	public void regulIterateOxid(@Observes @Named(NAME) @Oxid @RegulIter AnalogPort outputPort) {
		oxidPort = outputPort;
		if (fuelPort != null) {
			fireRegulIteration();
		}
	}

	private void fireRegulIteration() {
		ImmutablePair<AnalogPort, AnalogPort> output = model.regulStep(fuelPort, oxidPort);
		fuelRegulEvent.fire(output.getLeft());
		oxidRegulEvent.fire(output.getRight());
		receivedFuel = receivedOxid = false;
	}

	private void fireIteration() {
		ImmutablePair<AnalogPort, AnalogPort> pair = model.createNewControlSignal();
		fuelEvent.fire(pair.getLeft());
		oxidEvent.fire(pair.getRight());
		receivedFuel = receivedOxid = false;
	}

	//---------------------------------------------------------------------------------------
	// Initialisation values

	@PostConstruct
    void initModel() {
    	model.init(NAME);
    }
		
	@Inject
	void controlRangeMax(@NumberConfig(name = "econtroller21.controlRangeMax", defaultValue = "1.0") Double value) {
		model.setControlRangeMax(value);
	}
	@Inject
	void controlRangeMin(@NumberConfig(name = "econtroller21.controlRangeMin", defaultValue = "0.0") Double value) {
		model.setControlRangeMin(value);
	}
	@Inject
	void controlValue1Nom(@NumberConfig(name = "econtroller21.controlValue1Nom", defaultValue = "0.0") Double value) {
		model.setControlValue1Nom(value);
	}
	@Inject
	void controlValue2Nom(@NumberConfig(name = "econtroller21.controlValue2Nom", defaultValue = "0.0") Double value) {
		model.setControlValue2Nom(value);
	}
}
