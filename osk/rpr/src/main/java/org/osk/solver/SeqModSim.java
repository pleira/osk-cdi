/*
 * SeqModSim.java
 *
 * Created on 3. Juli 2007, 22:24
 *
 * Implementation of a class for master objects of sequentially modular
 * system solvers.
 *
 *-----------------------------------------------------------------------------
 * Modification History:
 *
 *  2004-12-05
 *      File created  J. Eickhoff:
 *
 *      Class architecture is a derivative from ObjectSim 2.0.3.,
 *      a simulation program published in:
 *
 *        Eickhoff, J.:
 *        Modulare Programmarchitektur fuer ein wissensbasiertes
 *        Simulationssystem mit erweiterter Anwendbarkeit in der
 *        Entwicklung und Betriebsberwachung verfahrenstechnischer
 *        Anlagen.
 *        PhD thesis in Department Process Engineering of
 *        TU Hamburg-Harburg, 1996.
 *
 *      See also file history cited there and see historic relation of
 *      this OpenSimKit class to a.m. ObjectSim explained in
 *      OpenSimKit Documentation.
 *
 *      File under GPL  see OpenSimKit Documentation.
 *
 *      No warranty and liability for correctness by author.
 *
 *
 *  2005-09
 *      OpenSimKit V 2.2
 *      Modifications enterd for XML input file parsing by
 *      Peter Heinrich  peterhe@student.ethz.ch
 *
 *  2006-03
 *      OpenSimKit V 2.3
 *      Modifications entered for I/O file handling.
 *      calcStepInit () method argument list changed.
 *      J. Eickhoff
 *
 *  2008-05
 *      OpenSimKit V 2.4
 *      Ported from C++ to Java
 *      A. Brandt  alexander.brandt@gmail.com
 *
 *  2009-06
 *     Integrated logging support.
 *     T. Pieper
 *
 *  2011-01
 *      Provider-Subscriber Prototype added to kernel.java and reference here.
 *      A. Brandt
 *      This mechanism is used for data interchange between models which have no
 *      physical line connection (data line, power line, fluid pipe etc.)
 *
 *
 *-----------------------------------------------------------------------------
 *
 *      File under GPL  see OpenSimKit Documentation.
 *
 *      No warranty and liability for correctness by authors.
 *
 *
 *-----------------------------------------------------------------------------
 */

package org.osk.solver;

import java.io.IOException;

import javax.enterprise.event.Event;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

import org.jboss.weld.environment.se.events.ContainerInitialized;
import org.osk.events.BackIter;
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
        LOG.info("Initial system boundary condition iteration...\n");
        // Maybe it is needed to fire two backiterations, one concerning the sc body
        // to setup altitude and environment model
        // and the second one, concerning the engine to setup fluid conditions across the model
        backIterEvent.fire(new Iteration());
        // backIterEngineEvent.fire(new Iteration());
        time = tinit;

//        while (true) {
        	LOG.info("Time iteration...\n");
            timeEvent.fire(new TimeIteration(time, timeHandler.getStepSizeAsDouble()));
            LOG.info("Regul iteration...\n");
            regulIterEvent.fire(new Iteration());
            LOG.info("Back iteration...\n");
            backIterEvent.fire(new Iteration());
            time = time + timeHandler.getStepSizeAsDouble();

            LOG.info("Time: {}",
               String.format("%1$tFT%1$tH:%1$tM:%1$tS.%1$tL",
               timeHandler.getSimulatedMissionTime()));
//        }
    }

}
