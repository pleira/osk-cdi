/*
 *      File under GPL  see OpenSimKit Documentation.
 *
 *      No warranty and liability for correctness by authors.
 */

package org.osk.solver;

import java.io.IOException;

import javax.enterprise.event.Event;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

import org.jboss.weld.environment.se.events.ContainerInitialized;
import org.osk.events.BackIter;
import org.osk.events.ECI;
import org.osk.events.Iter;
import org.osk.events.Iteration;
import org.osk.events.RegulIter;
import org.osk.events.TimeIteration;
import org.osk.interceptors.AuditTime;
import org.osk.time.TimeHandler;
import org.slf4j.Logger;

/**
 * Implementation of a class for master objects of sequentially modular
 * system solvers.
 *
 * @author J. Eickhoff
 * @author A. Brandt
 * @author T. Pieper
 * @author P. Pita
 */
public class SeqModSim  {
    @Inject Logger LOG; 
    @Inject TimeHandler    timeHandler;
   
    @Inject @Iter Event<Iteration> iterEvent;
    @Inject @BackIter Event<Iteration> backIterEvent;
    @Inject @RegulIter Event<Iteration> regulIterEvent;
    @Inject Event<TimeIteration> timeEvent;
    @Inject @ECI Event<Iteration> positionEvent;
  
    public void printSimSettings() {
        LOG.info("Simulation: Step size is: {}.",
                timeHandler.getStepSizeAsDouble());
    }


/* -------------------------------------------------------------------------- */
/*                                Computation
/* -------------------------------------------------------------------------- */
    @AuditTime
    public void initSim(@Observes ContainerInitialized init) throws IOException {
//    public void initSim(ContainerInitialized init) throws IOException {
    	          // Here, we have initialised all our components
    	// in debug mode, the initial values of the models should be checked 
    	// before doing the computations
 //       ExecutorService service = Executors.newSingleThreadExecutor();
//		service.submit(this);

        double time  = timeHandler.getSimulatedMissionTimeAsDouble();
        double tinit = timeHandler.getSimulatedMissionTimeAsDouble();

        LOG.info("Starting simulation...\n");
        LOG.info("Time: {}", time);
        
        
        // We set up the boundary conditions, 
        // the event will start a chain of events starting from the spacecraft body, 
        // which delivers initial altitude information to the environment model
        // and then, the engine will set boundary conditions on the amount of 
        // fuel and oxidizer requested, setting initial mass flows of the 
        // components up to the helium tanks. For this order, this overall process 
        // is called backIteration
//        LOG.info("Initial system boundary condition iteration...\n");
        // Maybe it is needed to fire two backiterations, one concerning the sc body
        // to setup altitude and environment model
        // and the second one, concerning the engine to setup fluid conditions across the model
//        backIterEvent.fire(new Iteration()); // the event chain deals with backIter methods
        /*
         * The the forward iteration takes place, observing whether any
         * error value model complains about not fulfilled hydraulic or
         * boundary condition. */
    	LOG.info("Rocket Model Forward Iteration");
    	LOG.info("S/C Position in ECI and gravity\n");
        iterEvent.fire(new Iteration()); // the event chain deals with iteration methods
        
        //positionEvent.fire(new Iteration());
        
        // backIterEngineEvent.fire(new Iteration());
        time = tinit;

//        while (true) {
        try {
        	LOG.info("Time iteration...\n");
            timeEvent.fire(new TimeIteration(time, timeHandler.getStepSizeAsDouble()));
            LOG.info("Regul iteration...\n");
            regulIterEvent.fire(new Iteration());
            LOG.info("Back iteration (set new boundary conditions) \n");
            backIterEvent.fire(new Iteration());
            time = time + timeHandler.getStepSizeAsDouble();

            LOG.info("Time: {}",
            String.format("%1$tFT%1$tH:%1$tM:%1$tS.%1$tL",
            timeHandler.getSimulatedMissionTime()));
        } catch(Exception e) {
        	LOG.error("Got exception: " + e.getMessage());
        	LOG.error("A cause can be that the models for gas flows are not accurate.");
        	LOG.error("Please check for WARN or other ERROR messages.");
        }
//        }
    }

}
