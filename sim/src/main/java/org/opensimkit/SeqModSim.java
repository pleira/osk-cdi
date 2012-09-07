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

package org.opensimkit;

import java.io.*;

import java.io.IOException;
import java.util.Date;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.opensimkit.providerSubscriber.ProviderSubscriber;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.opensimkit.steps.CIterationStep;
import org.opensimkit.steps.CRegulStep;
import org.opensimkit.steps.CTimeStep;

/**
 * Implementation of a class for master objects of sequentially modular
 * system solvers.
 *
 * @author J. Eickhoff
 * @author A. Brandt
 * @author T. Pieper
 * @version 2.0
 * @since 2.4.0
 */
@ApplicationScoped
public class SeqModSim {
    private static final int LAST_OP_STEP_INIT = 0;
    private static final int LAST_OP_STEP_IS   = 1;
    private static final int LAST_OP_STEP_TS   = 2;
    private static final int LAST_OP_STEP_RS   = 3;
    private static final int LAST_OP_STEP_EC   = 4;
    private static final Logger LOG = LoggerFactory.getLogger(SeqModSim.class);
    private final String      name;
    //private final Kernel      kernel;
    @Inject TimeHandler timeHandler;
    private boolean        isComputing;
    private double         time;
    private double         tinit;
    private int            lastop;
    private int            localNAckFlag;
    private long           startTime2;
    private CTimeStep      tStep;
    private CRegulStep     rStep;
    private CIterationStep iStep;
    private Date           startTime;
    @Inject PacketCreator  packetCreator;
    private SimulatorState state;
    private TabGenerator   outTab;
    @Inject ProviderSubscriber providerSubscriber;

    public SeqModSim() {
        this.name        = "Simulation";
        //this.timeHandler = kernel.getTimeHandler();
        //this.providerSubscriber = kernel.getProviderSubscriber();

        tStep = new CTimeStep("Time-Step-Object");
        rStep = new CRegulStep("Regul-Step-Object");
        iStep = new CIterationStep("Iteration-Step-Object");

        isComputing = true;
        state = SimulatorState.NOT_RUNNING;

        LOG.debug(SimHeaders.DEBUG_SHORT, "Constructor");
    }

    public void printSimSettings() {
        LOG.info("Simulation: Step size is: {}.",
                timeHandler.getStepSizeAsDouble());
    }

    public int calcStepInit(final ComHandler cHand, final MeshHandler mHand,
        final TabGenerator tabGenerator) {
        LOG.debug(SimHeaders.DEBUG_SHORT, "CalcStepInit");

        if (cHand.calcStepInit(tStep, rStep) == 1) {
            // Error message submitted by tstep and rstep obj.
            return 1;
        }
        if (mHand.calcStepInit(iStep) == 1) {
            // Error message submitted by istep obj.
            return 1;
        }
        outTab = tabGenerator;
        return 0;
    }

/* -------------------------------------------------------------------------- */
/*                                Computation
/* -------------------------------------------------------------------------- */
    public int compute() throws IOException {
        startCompute();
        doCompute();
        stopCompute();

        return 0;
    }

    private void startCompute() throws IOException {
        LOG.debug(SimHeaders.DEBUG_SHORT, "Compute");

        setStateToRunning();

        time  = timeHandler.getSimulatedMissionTimeAsDouble();
        tinit = timeHandler.getSimulatedMissionTimeAsDouble();

        LOG.info("Starting simulation...\n");
        LOG.info("Time: {}", time);
        startTime2 = System.currentTimeMillis();

        // TODO: interceptor or decorator for report writing
        outTab.tabInit();

        LOG.info("Initial system boundary condition iteration...\n");
        if (iStep.calc() == 1) {  // Iteration-Step t=0
            // Error message submitted by istep obj.
            lastop = LAST_OP_STEP_INIT;
            return;
        }

        //outTab.tabWrite(timeHandler.getSimulatedMissionTime(),
        //timeHandler.getStepSizeAsDouble());
        outTab.tabWrite(timeHandler.getSimulatedMissionTime(),
                timeHandler.getStepSizeAsDouble());
    }

    private void doCompute() throws IOException {
        long beforeCalculation = 0;
        long afterCalculation = 0;
        long passedTime = 0;
        long sleepTime = 0;
        long interval = timeHandler.getInterval();
        boolean doCompute = true;

        time = tinit;

        while (doCompute) {
            if (getState() == SimulatorState.RUNNING) {
                beforeCalculation = System.currentTimeMillis();

                LOG.debug(SimHeaders.DEBUG_SHORT, "TimeStep computation ");
                if (tStep.calc(time, timeHandler.getStepSizeAsDouble()) == 1) {
                    // Error message submitted by tstep obj.
                    lastop = LAST_OP_STEP_TS;
                    return;
                }
                if (providerSubscriber != null) {
                  providerSubscriber.calc();
                }
                else  {
                  LOG.warn("No ProviderSubscriber instance existing.");
                }
                
                LOG.debug(SimHeaders.DEBUG_SHORT, "RegulStep computation ");
                if (rStep.calc() == 1) {
                    // Error message submitted by rstep obj.
                    lastop = LAST_OP_STEP_RS;
                    return;
                }
                
                LOG.debug(SimHeaders.DEBUG_SHORT, "IterationStep computation ");
                if (iStep.calc() == 1) {
                    // Error message submitted by istep obj.
                    lastop = LAST_OP_STEP_IS;
                    return;
                }

                /* Update the TimeHandler. This increases the time by stepSize
                 * and signals a finished time step. */
                timeHandler.update();

                LOG.info("Time: {}",
                        String.format("%1$tFT%1$tH:%1$tM:%1$tS.%1$tL",
                        timeHandler.getSimulatedMissionTime()));
                outTab.tabIntervalWrite(timeHandler.getSimulatedMissionTime(),
                        timeHandler.getStepSizeAsDouble());
                
                packetCreator.sendData();
//                kernel.getOutputWriter().write(time + stepSize + "\r\n");
//                kernel.getOutputWriter().flush();

                afterCalculation = System.currentTimeMillis();

                /* Calculate the passed time. If there is fewer time passed as
                 * specified in the interval then wait the remaining time. If
                 * the calculation of the simulation step took longer than the
                 * interval length then issue a warning.
                 */
                passedTime = afterCalculation - beforeCalculation;

                if (passedTime <= interval) {
                    sleepTime = interval - passedTime;
                } else {
                    LOG.warn("Simulation time step took longer than the"
                            + " permitted interval length!");
                    sleepTime = 0;
                }

                time = time + timeHandler.getStepSizeAsDouble();

                try {
                    /* Simulation speed control. */
                    Thread.sleep(sleepTime);
                } catch (InterruptedException ex) {
                    LOG.error("Exception:", ex);
                }

            } else if (getState() == SimulatorState.STOPPING) {
                doCompute = false;
            } else {
                /* Simulation paused. Do nothing. */
            }
        }
    }

    private void stopCompute() throws IOException {
        outTab.tabEndWrite(time, timeHandler.getStepSizeAsDouble());

        LOG.info("Computation finished.");
        long endTime = System.currentTimeMillis();
        LOG.info("Simulation duration: {}d {}h {}m {}s {}ms", new Object[] {
            ((endTime - startTime2) / 86400000), //days
            ((endTime - startTime2) / 3600000), //hours
            ((endTime - startTime2) / 60000),   //minutes
            ((endTime - startTime2) / 1000),    //seconds
             (endTime - startTime2)});          //microseconds

        setStateToNotRunning();
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
