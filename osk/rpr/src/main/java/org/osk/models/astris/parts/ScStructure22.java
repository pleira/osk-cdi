package org.osk.models.astris.parts;
import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Event;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.apache.deltaspike.core.api.config.annotation.ConfigProperty;
import org.osk.config.NumberConfig;
import org.osk.config.Util;
import org.osk.errors.OskException;
import org.osk.events.BackIter;
import org.osk.events.ECI;
import org.osk.events.Gravity;
import org.osk.events.Iter;
import org.osk.events.Iteration;
import org.osk.events.Oxid;
import org.osk.events.PVCoordinates;
import org.osk.events.TimeIter;
import org.osk.interceptors.Log;
import org.osk.models.structure.ScStructure;

@Log
@ApplicationScoped
public class ScStructure22 {

	public final static String NAME = "ScStructure22";
	
	@Inject ScStructure model;
	@Inject @Named(NAME) @ECI @Iter Event<PVCoordinates> event;
	@Inject @Named(NAME) @ECI @TimeIter Event<PVCoordinates> timerEvent;
	@Inject @Named(Engine20.NAME) @Oxid @BackIter Event<PVCoordinates> backEvent;

//	public void iteration(@Observes @Iter Iteration iter) {
//    	event.fire(new PVCoordinates(model.getScPositionECI(), model.getScVelocityECI()));
//	}

	public void timeIteration(
			@Observes @Named(Engine20.NAME) @TimeIter Vector3D thrust) throws OskException {
		PVCoordinates scPosVel = model.calculateECICoordinates(thrust);
		timerEvent.fire(scPosVel);
	}

	public void backIterate(
			@Observes @Named(NAME) @BackIter Iteration backIter) {
    	// pass the initial position/velocity to interested parties
		backEvent.fire(new PVCoordinates(model.getScPositionECI(), model.getScVelocityECI()));
	}
	
	public void handleGravity(@Observes @Named(Gravity23.NAME) @Gravity Vector3D gravity) {
		model.setGravity(gravity);
	}

	//---------------------------------------------------------------------------------------
	// Initialisation values

	@PostConstruct
    void initModel() {
    	model.init(NAME);
    }

	@Inject
	void initScPositionECI(@ConfigProperty(name = "sc.scPositionECI", defaultValue = "6978137.0 0.0 0.0") String values) {
	model.setScPositionECI(new Vector3D(Util.extractDoubleArray(values)));
	}
	
	@Inject
	void initScVelocityECI(@ConfigProperty(name = "sc.scVelocityECI", defaultValue = "0.0 2700.0 7058.0") String values) {
	model.setScVelocityECI(new Vector3D(Util.extractDoubleArray(values)));
	}
		
	@Inject
	void initScMass(@NumberConfig(name = "sc.scMass", defaultValue = "1000.0") Double value) {
	model.setScMass(value);
	}
}
