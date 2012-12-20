package org.osk.models.astris.parts;
import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Event;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.inject.Named;

import org.osk.config.NumberConfig;
import org.osk.events.BackIter;
import org.osk.events.Iter;
import org.osk.events.TimeIter;
import org.osk.events.TimeStep;
import org.osk.interceptors.Log;
import org.osk.models.Filter;
import org.osk.ports.FluidPort;

@Log
@ApplicationScoped
public class Filter06  {

	public final static String NAME = "Filter06";

	@Inject Filter model;
	@Inject @Named(NAME) @Iter Event<FluidPort> event;
	@Inject @Named(NAME) @TimeIter Event<FluidPort> outputEvent;
	@Inject @Named(Pipe05.NAME) @BackIter Event<FluidPort> backEvent;
	@Inject @TimeStep Double tStepSize;
	
	public void iteration(@Observes @Named(Pipe05.NAME) @Iter FluidPort inputPort) {
		FluidPort output = model.calculateOutletMassFlow(inputPort);
		event.fire(output);
	}

	public void timeIteration(@Observes @Named(Pipe05.NAME) @TimeIter FluidPort input) {
		model.propagate(tStepSize, input);
		FluidPort output = model.createOutputPort(input);
		outputEvent.fire(output);
	}

	public void backIterate(@Observes @Named(NAME) @BackIter FluidPort outputPort) {
		backEvent.fire(outputPort);
	}

	//---------------------------------------------------------------------------------------
	// Initialisation values

	@PostConstruct
    void initModel() {
    	model.init(NAME);
    }

	@Inject
	void initLength(
			@NumberConfig(name = "filter6.length", defaultValue = "0.1") Double value) {
		model.setLength(value);
	}

	@Inject
	void initSpecificMass(
			@NumberConfig(name = "filter6.specificMass", defaultValue = "3.0") Double value) {
		model.setSpecificMass(value);
	}

	@Inject
	void initInnerDiameter(
			@NumberConfig(name = "filter6.innerDiameter", defaultValue = "0.04") Double value) {
		model.setInnerDiameter(value);
	}

	@Inject
	void initSpecificHeatCapacity(
			@NumberConfig(name = "filter6.specificHeatCapacity", defaultValue = "500.0") Double value) {
		model.setSpecificHeatCapacity(value);
	}
	
	@Inject
	void initReferencePressureLoss(@NumberConfig(name = "filter6.referencePressureLoss", defaultValue = "0.4") Double value) {
		model.setReferencePressureLoss(value);
	}
	@Inject
	void initReferenceMassFlow(@NumberConfig(name = "filter6.referenceMassFlow", defaultValue = "0.1") Double value) {
		model.setReferenceMassFlow(value);
	}	

	@Inject
	void initTemperature(@NumberConfig(name = "filter6.temperature", defaultValue = "300.0") Double value) {
		model.setTemperature(value);
	}

}
