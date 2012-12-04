package org.opensimkit.frames;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Event;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

import org.opensimkit.TimeHandler;
import org.opensimkit.events.ECEFpv;
import org.opensimkit.events.ECI;
import org.opensimkit.events.ScPV;

/**
 * The sole purpose of this class is to catch s/c ECI pos/vel and 
 * convert to ECEF, firing them an event related to ECEF.
 * 
 * @author P. Pita
 *
 */
@ApplicationScoped
public class ECI2ECEFHandler {

	@Inject 
	Event<ECEFpv> event;
	
	@Inject
	TimeHandler timeHandler;
	
	ECEFBuilder builder = new ECEFBuilder();
	
	/**
	 * This methods gathers ECI position/velocity events and 
	 * fires the same values but in ECEF coordinates
	 * 
	 * @param posVel
	 */
	public void eci2ecefHandler(@Observes @ECI ScPV posVel) {
        double mjdMissionTime = (timeHandler.getSimulatedMissionTimeAsDouble()-946684800)/86400;
        ECEFpv pv = builder.eci2ecef(mjdMissionTime, 0.0, 15, posVel.getScPosition(), posVel.getScVelocity());        
        event.fire(pv);
	}
}
