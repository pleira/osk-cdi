package org.osk.models.astris.parts;
import org.osk.interceptors.Log;

import javax.annotation.PostConstruct;
import javax.enterprise.event.Event;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.deltaspike.core.api.config.annotation.ConfigProperty;
import org.osk.config.NumberConfig;
import org.osk.config.Util;
import org.osk.events.BackIter;
import org.osk.events.Iter;
import org.osk.events.TimeIter;
import org.osk.models.rocketpropulsion.PRegT1;
import org.osk.ports.FluidPort;


@Log
public class PReg12  {
		
	public final static String NAME = "PReg12";

	@Inject PRegT1 model;
	@Inject @Named(NAME) @Iter Event<FluidPort> event;
	@Inject @Named(NAME) @TimeIter Event<FluidPort> outputEvent;
	@Inject @Named(Pipe11.NAME) @BackIter Event<FluidPort> backEvent;
	
	public void iteration(@Observes @Named(Pipe11.NAME) @Iter FluidPort inputPort) {
		FluidPort output = model.iterationStep(inputPort);
		event.fire(output);
	}

	public void timeIteration(@Observes @Named(Pipe11.NAME) @TimeIter FluidPort inputPort) {
		model.timeStep(inputPort);
		FluidPort output = model.createOutputPort(inputPort.getFluid());
		outputEvent.fire(output);
	}

	public void backIterate(@Observes @Named(NAME) @BackIter FluidPort outputPort) {
		FluidPort input = model.backIterStep(outputPort);
		backEvent.fire(input);
	}

	//---------------------------------------------------------------------------------------
	// Initialisation values

	@PostConstruct
    void initModel() {
    	model.init(NAME);
    }

	@Inject
	void initLength(@NumberConfig(name = "preg12.length", defaultValue = "0.1") Double value) {
	model.setLength(value);
	}
	
	@Inject
	void initMass(@NumberConfig(name = "preg12.mass", defaultValue = "2.6") Double value) {
	model.setMass(value);
	}
	
	@Inject
	void initInnerDiameter(@NumberConfig(name = "preg12.innerDiameter", defaultValue = "0.014") Double value) {
	model.setInnerDiameter(value);
	}
	
	@Inject
	void initSpecificHeatCapacity(@NumberConfig(name = "preg12.specificHeatCapacity", defaultValue = "900.0") Double value) {
	model.setSpecificHeatCapacity(value);
	}

	@Inject
	void initPcoeff(@ConfigProperty(name = "preg12.pcoeff", 
	defaultValue = "17.70 0.0 0.0 0.0") String values) {
	model.setPcoeff(Util.extractDoubleArray(values));
	}
		
	@Inject
	void initTemperature(@NumberConfig(name = "preg12.temperature", defaultValue = "300.0") Double value) {
	model.setTemperature(value);
	}

}
