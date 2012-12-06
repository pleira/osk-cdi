package org.osk.models.astris.parts;
import org.osk.interceptors.Log;

import javax.enterprise.event.Event;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.inject.Named;

import org.osk.config.NumberConfig;
import org.osk.events.BackIter;
import org.osk.events.Fuel;
import org.osk.events.Iter;
import org.osk.events.Oxid;
import org.osk.events.TimeIter;
import org.osk.models.rocketpropulsion.FluidFlowValve;
import org.osk.ports.AnalogPort;
import org.osk.ports.FluidPort;


@Log
public class FFV19 {
		
	public final static String NAME = "FFV19";

	@Inject FluidFlowValve model;
	@Inject @Named(NAME) @Oxid @Iter Event<FluidPort> event;
	@Inject @Named(NAME) @Oxid @TimeIter Event<FluidPort> outputEvent;
	@Inject @Named(NAME) Event<AnalogPort> controlEvent;
	@Inject @Named(Tank17.NAME) @BackIter Event<FluidPort> backEvent;

	FluidPort inputPort;
	AnalogPort controlPort;
	
	public void iterationFuel(@Observes @Named(Tank17.NAME) @Oxid @Iter FluidPort input) {
		inputPort = input;
		if (controlPort != null) {
			fireIteration();
		}
	}
	
	public void regulationControl(@Observes @Named(EngineController21.NAME) @Oxid AnalogPort input) {
		controlPort = input;
		if (inputPort != null) {
			fireIteration();
		}
	}

	public void timeIteration(@Observes @Named(Tank17.NAME) @Oxid @TimeIter FluidPort input) {
		inputPort = input;
		if (controlPort != null) {
			fireTimeIteration();
		}
	}

	public void timeIteration(@Observes @Named(EngineController21.NAME) @Oxid @TimeIter AnalogPort input) {
		controlPort = input;
		if (inputPort != null) {
			fireTimeIteration();
		}
	}

	public void backIterate(@Observes @Named(NAME) @BackIter FluidPort outputPort) {
		FluidPort input = model.backIterStep(outputPort, controlPort);
		backEvent.fire(input);
	}

	private void fireIteration() {
		FluidPort output = model.iterationStep(inputPort, controlPort);
		event.fire(output);
		inputPort = null;
		controlPort = null;
	}

	private void fireTimeIteration() {		
		FluidPort output = model.timeStep(inputPort, controlPort);
		outputEvent.fire(output);
		inputPort = null;
		controlPort = null;
	}

	//---------------------------------------------------------------------------------------
	// Initialisation values
	
	@Inject
	void initReferencePressureLoss(@NumberConfig(name = "ffv19.referencePressureLoss", defaultValue = "1.0") Double value) {
		model.setReferencePressureLoss(value);
	}
	@Inject
	void initReferenceMassFlow(@NumberConfig(name = "ffv19.referenceMassFlow", defaultValue = "10.0") Double value) {
		model.setReferenceMassFlow(value);
	}	
}
