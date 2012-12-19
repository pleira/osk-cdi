package org.osk.models.astris.parts;
import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Event;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.inject.Named;

import org.osk.config.NumberConfig;
import org.osk.events.BackIter;
import org.osk.events.Fuel;
import org.osk.events.Iter;
import org.osk.events.RegulIter;
import org.osk.events.TimeIter;
import org.osk.interceptors.Log;
import org.osk.models.rocketpropulsion.FluidFlowValve;
import org.osk.ports.AnalogPort;
import org.osk.ports.FluidPort;
import org.osk.time.TimeHandler;

@Log
@ApplicationScoped
public class FFV18  {
		
	public final static String NAME = "FFV18";

	@Inject FluidFlowValve model;
	@Inject @Named(NAME) @Iter Event<FluidPort> event;
	@Inject @Named(NAME) @TimeIter Event<FluidPort> outputEvent;
	@Inject @Named(NAME) @RegulIter Event<AnalogPort> controlEvent;
	@Inject @Named(Tank17.NAME) @Fuel @BackIter Event<FluidPort> backEvent;
	@Inject TimeHandler timeHandler;

	FluidPort inputPort;
	AnalogPort controlPort;
	
	public void iterationFuel(@Observes @Named(Tank17.NAME) @Fuel @Iter FluidPort input) {
		inputPort = input;
//		if (controlPort != null) {
			fireIteration();
//		}
	}
	
//	public void regulationControl(@Observes @Named(EngineController21.NAME) @Fuel @Iter AnalogPort input) {
//		controlPort = input;
//		if (inputPort != null) {
//			fireIteration();
//		}
//	}

	public void timeIteration(@Observes @Named(Tank17.NAME) @Fuel @TimeIter FluidPort input) {
		outputEvent.fire(input);
//		inputPort = input;
//		if (controlPort != null) {
//			fireTimeIteration();
//		}
	}

//	public void timeIteration(@Observes @Named(EngineController21.NAME) @Fuel @TimeIter AnalogPort input) {
//		controlPort = input;
//		if (inputPort != null) {
//			fireTimeIteration();
//		}
//	}

	public void backIterate(@Observes @Named(NAME) @BackIter FluidPort outputPort) {
		// at this stage, do not change boundary conditions requested
		backEvent.fire(outputPort);
	}

	private void fireIteration() {
		if (controlPort == null) {
			controlPort = new AnalogPort();
		}
		FluidPort output = model.calculateMassFlow(timeHandler.getSimulatedMissionTime(), inputPort, controlPort);
		event.fire(output);
		inputPort = null;
		controlPort = null;
	}

	private void fireTimeIteration() {		
		if (controlPort == null) {
			controlPort = new AnalogPort();
		}
//		FluidPort output = model.timeStep(inputPort, controlPort);
//		outputEvent.fire(output);
		inputPort = null;
		controlPort = null;
	}

	//---------------------------------------------------------------------------------------
	// Initialisation values

	@PostConstruct
    void initModel() {
    	model.init(NAME);
    }
				
	@Inject
	void initReferencePressureLoss(@NumberConfig(name = "ffv18.referencePressureLoss", defaultValue = "1.0") Double value) {
		model.setReferencePressureLoss(value);
	}
	@Inject
	void initReferenceMassFlow(@NumberConfig(name = "ffv18.referenceMassFlow", defaultValue = "10.0") Double value) {
		model.setReferenceMassFlow(value);
	}
	
}
