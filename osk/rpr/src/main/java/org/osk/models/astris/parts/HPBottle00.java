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
import org.osk.models.rocketpropulsion.HPBottleT1;
import org.osk.ports.FluidPort;

@Log
@ApplicationScoped
public class HPBottle00  {

	public final static String NAME = "HPBottle00"; 
	@Inject HPBottleT1 model;
	
	// We produce events marked for this element
	@Inject	@Named(NAME) @Iter     Event<FluidPort> event;
	@Inject	@Named(NAME) @TimeIter Event<FluidPort> timeEvent;
	@Inject	@Named(NAME) @BackIter Event<FluidPort> backIterEvent;
		
    // The Helium Bottles are the start of the event chains
    // concerning the Iter and TimeIter calculations in the simulation. 
	public void iteration(@Observes @Iter Iteration iter) {
		FluidPort output = model.getOutputPortStatus();
		event.fire(output);
	}

	public void timeIteration(@Observes @TimeIter TimeIteration timeIter) {
		model.timeStep();		
		timeEvent.fire(model.createInputPortIter());
	}

	public void backIterate(@Observes @Named(Pipe02.NAME) @BackIter FluidPort outputPort) {
    	// Set the requested helium mass flow coming from the tank through the different 
    	// elements
		model.setMftotal(outputPort.getBoundaryMassflow());
		// iteration(new Iteration()); // nobody should listen for more, or we can start the forward iteration
	}
	
	//---------------------------------------------------------------------------------------
	// Initialisation values
    @PostConstruct
    void initModel() {
    	model.init(NAME);
    }
    
	@Inject
	void initMassFlow(@NumberConfig(name = "hpb0.massFlow", defaultValue = "0.01") Double value) {
	model.setMftotal(value);
	}
	@Inject
	void initMass(@NumberConfig(name = "hpb0.mass", defaultValue = "28.0") Double value) {
	model.setMass(value);
	}
	@Inject
	void initVolume(@NumberConfig(name = "hpb0.volume", defaultValue = "0.135") Double value) {
	model.setVolume(value);
	}
	@Inject
	void initPtotal(@NumberConfig(name = "hpb0.ptotal", defaultValue = "280.0") Double value) {
	model.setPtotal(value);
	}
	@Inject
	void initTtotal(@NumberConfig(name = "hpb0.ttotal", defaultValue = "300.0") Double value) {
	model.setTtotal(value);
	}
	@Inject
	void initSpecificHeatCapacity(@NumberConfig(name = "hpb0.specificHeatCapacity", defaultValue = "800.0") Double value) {
	model.setSpecificHeatCapacity(value);
	}
	@Inject
	void initFluid(@ConfigProperty(name = "hpb0.fluid", defaultValue = "Helium") String value) {
	model.setFluid(value);
	}

}
