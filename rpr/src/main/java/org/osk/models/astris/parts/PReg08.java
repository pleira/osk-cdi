package org.osk.models.astris.parts;

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


public class PReg08  {
		
	public final static String NAME = "PReg08";

	@Inject PRegT1 model;
	@Inject @Named(NAME) @Iter Event<FluidPort> event;
	@Inject @Named(NAME) @TimeIter Event<FluidPort> outputEvent;
	@Inject @Named(Pipe07.NAME) @BackIter Event<FluidPort> backEvent;
	
	public void iteration(@Observes @Named(Pipe07.NAME) @Iter FluidPort inputPort) {
		FluidPort output = model.iterationStep(inputPort);
		event.fire(output);
	}

	public void timeIteration(@Observes @Named(Pipe07.NAME) @TimeIter FluidPort inputPort) {
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

	@Inject
	void initLength(@NumberConfig(name = "preg08.length", defaultValue = "0.1") Double value) {
	model.setLength(value);
	}
	
	@Inject
	void initMass(@NumberConfig(name = "preg08.mass", defaultValue = "2.6") Double value) {
	model.setMass(value);
	}
	
	@Inject
	void initInnerDiameter(@NumberConfig(name = "preg08.innerDiameter", defaultValue = "0.014") Double value) {
	model.setInnerDiameter(value);
	}
	
	@Inject
	void initSpecificHeatCapacity(@NumberConfig(name = "preg08.specificHeatCapacity", defaultValue = "900.0") Double value) {
	model.setSpecificHeatCapacity(value);
	}

	@Inject
	void initPcoeff(@ConfigProperty(name = "preg08.pcoeff", 
	defaultValue = "24.10245 .4462006 -1.84912E-3 2.580329E-6") String values) {
	model.setPcoeff(Util.extractDoubleArray(values));
	}
		
	@Inject
	void initTemperature(@NumberConfig(name = "preg08.temperature", defaultValue = "300.0") Double value) {
	model.setTemperature(value);
	}

}
