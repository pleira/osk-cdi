package org.osk.models.astris.parts;
import org.osk.interceptors.Log;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
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
import org.osk.models.rocketpropulsion.PipeT1;
import org.osk.ports.FluidPort;

@Log
@ApplicationScoped
public class Pipe02 {
		
	public final static String NAME = "Pipe02"; 
	
	@Inject PipeT1 model;
	@Inject @Named(NAME) @Iter Event<FluidPort> event;
	@Inject @Named(NAME) @TimeIter Event<FluidPort> outputEvent;
	@Inject @Named(HPBottle00.NAME) @BackIter Event<FluidPort> backEvent;
	
	public void iteration(@Observes @Named(HPBottle00.NAME) @Iter FluidPort inputPort) {
		FluidPort output = model.iterationStep(inputPort);
		event.fire(output);
	}

	public void timeIteration(@Observes @Named(HPBottle00.NAME) @TimeIter FluidPort  inputPort) {
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
	void initLength(@NumberConfig(name = "pipe2.length", defaultValue = "1.5") Double value) {
	model.setLength(value);
	}
	
	@Inject
	void initSpecificMass(@NumberConfig(name = "pipe2.specificMass", defaultValue = "0.6") Double value) {
		model.setSpecificMass(value);
	}
	
	@Inject
	void initInnerDiameter(@NumberConfig(name = "pipe2.innerDiameter", defaultValue = "0.0085") Double value) {
		model.setInnerDiameter(value);
	}
	
	@Inject
	void initSpecificHeatCapacity(@NumberConfig(name = "pipe2.specificHeatCapacity", defaultValue = "5ß0.0") Double value) {
		model.setSpecificHeatCapacity(value);
	}
	
	@Inject
	void initSurfaceRoughness(@NumberConfig(name = "pipe2.surfaceRoughness", defaultValue = "1.E-6") Double value) {
		model.setSurfaceRoughness(value);
	}
	
	@Inject
	void initTemperatures(@ConfigProperty(name = "pipe2.temperatures", 
			defaultValue = "300.0 300.0 300.0 300.0 300.0 300.0 300.0 300.0 300.0 300.0") String values) {
		model.setTemperatures(Util.extractDoubleArray(values));
	}
	
}
