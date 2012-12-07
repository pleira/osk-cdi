package org.osk.models.astris.parts;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Event;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.inject.Named;

import org.osk.events.D4Value;
import org.osk.events.ECI;
import org.osk.events.Gravity;
import org.osk.events.Iter;
import org.osk.events.ScPV;
import org.osk.interceptors.Log;
import org.osk.models.environment.OSKGravityModel;

@Log
@ApplicationScoped
public class Gravity23 {
	
	public final static String NAME = "Gravity23";

	@Inject OSKGravityModel model;
	@Inject @Named(NAME) @Gravity Event<D4Value> outputEvent;

    
	public void iteration(@Observes @Named(ScStructure22.NAME) @ECI @Iter ScPV posVel) {
		model.setScPositionECI(posVel.getScPosition());
		D4Value gravity = model.timeStep();
		outputEvent.fire(gravity);
	}

	//---------------------------------------------------------------------------------------
	// Initialisation values

	@PostConstruct
    void initModel() {
    	model.init();
    }


}
