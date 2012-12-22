package org.osk.models.astris.parts;
import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Event;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.deltaspike.core.api.config.annotation.ConfigProperty;
import org.osk.config.NumberConfig;
import org.osk.events.BackIter;
import org.osk.events.Iter;
import org.osk.events.Iteration;
import org.osk.events.TimeIter;
import org.osk.events.TimeIteration;
import org.osk.interceptors.Log;
import org.osk.models.HPBottle;
import org.osk.ports.FluidPort;
import org.osk.time.TimeHandler;

@Log
@ApplicationScoped
public class HPBottle01 {

	public final static String NAME = "HPBottle01"; 

	@Inject HPBottle model;
	@Inject	@Named(NAME) @Iter     Event<FluidPort> event;
	@Inject	@Named(NAME) @TimeIter Event<FluidPort> timeEvent;
	@Inject	@Named(NAME) @BackIter Event<FluidPort> backIterEvent;
	@Inject TimeHandler timeHandler;
			
    // The Helium Bottles are the start of the event chains
    // concerning the Iter and TimeIter calculations in the simulation. 
	public void iteration(@Observes @Iter Iteration iter) {
		FluidPort output = model.getOutputPortStatus();
		event.fire(output);
	}

	public void timeIteration(@Observes TimeIteration timeIter) {
		model.calculateMassFlow(timeHandler.getSimulatedMissionTimeAsDouble());		
		timeEvent.fire(model.createInputPortIter());
	}

	public void backIterate(@Observes @Named(NAME) @BackIter FluidPort outputPort) {
    	// Set the requested helium mass flow coming from the tank through the different 
    	// elements
		model.setMftotal(outputPort.getMassflow());
		// iteration(new Iteration()); // nobody should listen for more, or we can start the forward iteration
	}
	
	//---------------------------------------------------------------------------------------
	// Initialisation values
	
    @PostConstruct
    void initModel() {
    	model.init(NAME);
    }
	
	@Inject
	void initMassFlow(@NumberConfig(name = "hpb1.massFlow", defaultValue = "0.01") Double value) {
	model.setMftotal(value);
	}
	@Inject
	void initMass(@NumberConfig(name = "hpb1.mass", defaultValue = "28.0") Double value) {
	model.setMass(value);
	}
	@Inject
	void initVolume(@NumberConfig(name = "hpb1.volume", defaultValue = "0.135") Double value) {
	model.setVolume(value);
	}
	@Inject
	void initPtotal(@NumberConfig(name = "hpb1.ptotal", defaultValue = "280.0") Double value) {
	model.setPtotal(value);
	}
	@Inject
	void initTtotal(@NumberConfig(name = "hpb1.ttotal", defaultValue = "300.0") Double value) {
	model.setTtotal(value);
	}
	@Inject
	void initSpecificHeatCapacity(@NumberConfig(name = "hpb1.specificHeatCapacity", defaultValue = "800.0") Double value) {
	model.setSpecificHeatCapacity(value);
	}
	@Inject
	void initFluid(@ConfigProperty(name = "hpb1.fluid", defaultValue = "Helium") String value) {
	model.setFluid(value);
	}
}
