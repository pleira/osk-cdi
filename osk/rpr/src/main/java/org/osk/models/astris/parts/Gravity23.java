package org.osk.models.astris.parts;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Event;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.osk.errors.OskException;
import org.osk.events.ECI;
import org.osk.events.Gravity;
import org.osk.events.Iter;
import org.osk.events.PVCoordinates;
import org.osk.interceptors.Log;
import org.osk.models.environment.OSKGravityModel;

@Log
@ApplicationScoped
public class Gravity23 {
	
	public final static String NAME = "Gravity23";

	@Inject OSKGravityModel model;
	@Inject @Named(NAME) @Gravity Event<Vector3D> outputEvent;

    
	public void iteration(@Observes @Named(ScStructure22.NAME) @ECI @Iter PVCoordinates posVel) {
		model.setScPositionECI(posVel.getPosition());
		Vector3D gravity = model.computeEarthGravity();
		outputEvent.fire(gravity);
	}

	//---------------------------------------------------------------------------------------
	// Initialisation values

	@PostConstruct
    void initModel() throws OskException {
    	model.init();
    }


}
