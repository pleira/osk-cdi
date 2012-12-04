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

package org.opensimkit.solver;

import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

import org.jboss.weld.environment.se.events.ContainerInitialized;
import org.opensimkit.SimHeaders;
import org.opensimkit.SimulatorState;
import org.opensimkit.TimeHandler;
import org.opensimkit.interceptors.AuditTime;
import org.opensimkit.models.BaseModel;
import org.opensimkit.models.Model;
import org.opensimkit.models.astris.IterItems;
import org.opensimkit.models.astris.RegulationItems;
import org.opensimkit.models.astris.TimeStepItems;
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
@ApplicationScoped
public class SeqModSim  {
    @Inject Logger LOG; // = LoggerFactory.getLogger(SeqModSim.class);
    private final String      name;
    @Inject TimeHandler timeHandler;
    private boolean        isComputing;
    private double         time;
    private double         tinit;
    private SimulatorState state;
   
    // This class iterates over the models 
    @Inject @TimeStepItems Collection<Model> timeStepModels;
    @Inject @RegulationItems Collection<Model> regulationModels;
    @Inject @IterItems Collection<Model> iterModels;
    @Inject SimHeaders simHeaders;
    
    @Inject
    public SeqModSim() {
        this.name        = "Simulation";
    }
    
    public void printSimSettings() {
        LOG.info("Simulation: Step size is: {}.",
                timeHandler.getStepSizeAsDouble());
    }


/* -------------------------------------------------------------------------- */
/*                                Computation
/* -------------------------------------------------------------------------- */
    @AuditTime
    public void initSim(@Observes ContainerInitialized init) throws IOException {
        // Here, we have initialised all our components
    	// in debug mode, the initial values of the models should be checked 
    	// before doing the computations
 //       ExecutorService service = Executors.newSingleThreadExecutor();
//		service.submit(this);

        LOG.info(simHeaders.DEBUG_SHORT, "Compute");

        time  = timeHandler.getSimulatedMissionTimeAsDouble();
        tinit = timeHandler.getSimulatedMissionTimeAsDouble();

        LOG.info("Starting simulation...\n");
        LOG.info("Time: {}", time);
        
        LOG.info("Initial system boundary condition iteration...\n");
        iterationCalc();  // Iteration-Step t=0

        time = tinit;

        while (true) {
                computeState();

                LOG.info("Time: {}",
                        String.format("%1$tFT%1$tH:%1$tM:%1$tS.%1$tL",
                        timeHandler.getSimulatedMissionTime()));
        }
    }

    @AuditTime
	public void computeState() {

    	LOG.info(SimHeaders.DEBUG_SHORT, "TimeStep computation ");
		calc(time, timeHandler.getStepSizeAsDouble());
		LOG.info(SimHeaders.DEBUG_SHORT, "RegulStep computation ");
		regulationCalc();
		LOG.info(SimHeaders.DEBUG_SHORT, "IterationStep computation ");
		iterationCalc();
		/* Update the TimeHandler. This increases the time by stepSize
		 * and signals a finished time step. */
		timeHandler.update();
	}

    private void iterationCalc() {

            Iterator it = iterModels.iterator();
            while (it.hasNext()) {
                BaseModel model = (BaseModel) it.next();
                if (model.iterationStep() != 0) {
                    // The only registered object is the main mesh.
                    LOG.info("Model {} - iteration step error!", model.getName());
        			System.exit(1);
                }
            }
	}

	private void regulationCalc() {

            Iterator it = regulationModels.iterator();
            while (it.hasNext()) {
                BaseModel model = (BaseModel) it.next();
                if (model.regulStep() != 0) {   
                    LOG.info("Model {} - regulation step error!", model.getName());
        			System.exit(1);
                }
            }
	}

	private void calc(final double time, final double tStepSize) {

            Iterator it = timeStepModels.iterator();
            while (it.hasNext()) {
                BaseModel model = (BaseModel) it.next();
                if (model.timeStep(time, tStepSize) != 0) {
                	LOG.info("Model {} - timeStep step error!", model.getName());
        			System.exit(1);
                }
            }
        }

    public String getName() {
        return name;
    }

    public boolean getIsComputing() {
        return isComputing;
    }

    public synchronized void setIsComputing(final boolean isComputing) {
        this.isComputing = isComputing;
    }

    public synchronized SimulatorState getState() {
        return state;
    }

    public synchronized void setStateToRunning() {
        state = SimulatorState.RUNNING;
    }

    public synchronized void setStateToNotRunning() {
        state = SimulatorState.NOT_RUNNING;
    }

    public synchronized void setStateToPaused() {
        state = SimulatorState.PAUSED;
    }

    public synchronized void setStateToStopping() {
        state = SimulatorState.STOPPING;
    }

}
