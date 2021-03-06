package org.osk.frames;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Event;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

import org.osk.events.ECEFpv;
import org.osk.events.ECI;
import org.osk.events.PVCoordinates;
import org.osk.time.TimeHandler;

/**
 * The sole purpose of this class is to catch s/c ECI pos/vel and 
 * convert to ECEF, firing them an event related to ECEF.
 * 
 * @author P. Pita
 *
 */

@ApplicationScoped
public class ECI2ECEFHandler {

	@Inject TimeHandler timeHandler;
	@Inject Event<ECEFpv> event;
	
	ECEFBuilder builder = new ECEFBuilder();
	
	/**
	 * This methods gathers ECI position/velocity events and 
	 * fires the same values but in ECEF coordinates
	 * 
	 * @param posVel
	 */
	public void eci2ecefHandler(/*Observes @ECI*/ PVCoordinates posVel) {
        double mjdMissionTime = (timeHandler.getSimulatedMissionTimeAsDouble()-946684800)/86400;
        ECEFpv pv = builder.eci2ecef(mjdMissionTime, 0.0, 15, 
        		posVel.getPosition().toArray(), posVel.getVelocity().toArray());        
        event.fire(pv);
	}
}
