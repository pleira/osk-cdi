package org.osk.models.astris.parts;
import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Event;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.deltaspike.core.api.config.ConfigProperty;
import org.osk.config.NumberConfig;
import org.osk.config.Util;
import org.osk.events.BackIter;
import org.osk.events.Iter;
import org.osk.events.TimeIter;
import org.osk.events.TimeStep;
import org.osk.interceptors.Log;
import org.osk.models.PressureRegulator;
import org.osk.ports.FluidPort;


@Log
@ApplicationScoped
public class PReg12  {
		
	public final static String NAME = "PReg12";

	@Inject PressureRegulator model;
	@Inject @Named(NAME) @Iter Event<FluidPort> event;
	@Inject @Named(NAME) @TimeIter Event<FluidPort> outputEvent;
	@Inject @Named(Pipe11.NAME) @BackIter Event<FluidPort> backEvent;
	@Inject @TimeStep Double tStepSize;
	
	public void iteration(@Observes @Named(Pipe11.NAME) @Iter FluidPort inputPort) {
		FluidPort output = model.calculateOutletMassFlow(inputPort);
		event.fire(output);
	}

	public void timeIteration(@Observes @Named(Pipe11.NAME) @TimeIter FluidPort inputPort) {
		model.propagate(tStepSize, inputPort);
		FluidPort output = model.createOutputPort(inputPort);
		outputEvent.fire(output);
	}

	public void backIterate(@Observes @Named(NAME) @BackIter FluidPort outputPort) {
		// Regulators just have to regulate the amount asked from the tank, pipes, etc, 
		// no modification is done to the requested value
		backEvent.fire(outputPort);
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
