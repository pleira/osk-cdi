package org.osk.models.astris.parts;

import javax.enterprise.event.Event;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.deltaspike.core.api.config.annotation.ConfigProperty;
import org.osk.config.NumberConfig;
import org.osk.events.BackIter;
import org.osk.events.Iter;
import org.osk.events.Iteration;
import org.osk.events.TimeIter;
import org.osk.events.TimeIteration;
import org.osk.models.rocketpropulsion.HPBottleT1;
import org.osk.ports.FluidPort;

public class HPBottle00  {

	public final static String NAME = "HPBottle00"; 
	@Inject HPBottleT1 model;
	
	// We produce events marked for this element
	@Inject	@Named(NAME) @Iter     Event<FluidPort> event;
	@Inject	@Named(NAME) @TimeIter Event<ImmutablePair<Double, FluidPort> > timeEvent;
	@Inject	@Named(NAME) @BackIter Event<FluidPort> backIterEvent;
		
	
    // The Helium Bottles are the start of the event chains
    // concerning the simulation. Therefore, this observer method
    // does not depend on any structural element and it is located
    // here
	public void iteration(@Observes @Iter Iteration iter) {
		FluidPort output = model.iterationStep();
		event.fire(output);
	}

	public void timeIteration(@Observes TimeIteration timeIter) {
		model.timeStep(timeIter.time, timeIter.timeStep);		
		timeEvent.fire(model.createInputPortIter(timeIter.timeStep));
	}


	public void backIterate(@Observes @Named(Pipe02.NAME) FluidPort outputPort) {
		model.backIterStep(outputPort);
		iteration(new Iteration());
	}
	
	//---------------------------------------------------------------------------------------
	// Initialisation values
	
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