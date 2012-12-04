package org.osk.models.astris.parts;

import javax.enterprise.event.Event;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.deltaspike.core.api.config.annotation.ConfigProperty;
import org.osk.config.NumberConfig;
import org.osk.config.Util;
import org.osk.events.BackIter;
import org.osk.events.D4Value;
import org.osk.events.Iter;
import org.osk.events.Iteration;
import org.osk.events.Oxid;
import org.osk.events.ScPV;
import org.osk.events.TimeIter;
import org.osk.models.structure.ScStructure;

public class ScStructure22 {

	public final static String NAME = "ScStructure22";
	
	@Inject ScStructure model;
	@Inject @Named(NAME) @Iter Event<Iteration> event;
	@Inject @Named(NAME) @TimeIter Event<ScPV> timerEvent;
	@Inject @Named(Engine20.NAME) @Oxid @BackIter Event<Iteration> backEvent;

	public void iteration(
			@Observes @Named(Engine20.NAME) @Iter Iteration iter) {
		event.fire(new Iteration());
	}

	public void timeIteration(
			@Observes @Named(Engine20.NAME) @TimeIter D4Value thrust) {
		ScPV scPosVel = model.timeStep(thrust);
		timerEvent.fire(scPosVel);
	}

	public void backIterate(
			@Observes @Named(NAME) @BackIter Iteration backIter) {
		backEvent.fire(backIter);
	}
	
//	PUBLIC VOID ALTITUDEHANDLER(@OBSERVES ECEFPV PV) {
//		MODEL.SETALT(PV.GETALTITUDE());
//	}

	//---------------------------------------------------------------------------------------
	// Initialisation values

	@Inject
	void initScPositionECI(@ConfigProperty(name = "sc.scPositionECI", defaultValue = "6978137.0 0.0 0.0") String values) {
	model.setScPositionECI(Util.extractDoubleArray(values));
	}
	
	@Inject
	void initScVelocityECI(@ConfigProperty(name = "sc.scVelocityECI", defaultValue = "0.0 2700.0 7058.0") String values) {
	model.setScVelocityECI(Util.extractDoubleArray(values));
	}
		
	@Inject
	void initScMass(@NumberConfig(name = "sc.scMass", defaultValue = "1000.0") Double value) {
	model.setScMass(value);
	}
}
