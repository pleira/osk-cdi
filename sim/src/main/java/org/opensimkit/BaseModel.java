/*
 * Model.java
 *
 * Created on 3. Juli 2007, 22:24
 *
 * Implementation of an abstract model class.
 *
 * -----------------------------------------------------------------------------
 *
 * Modification History:
 *
 *  2004-12-05
 *  File created - J. Eickhoff:
 *
 *      Class architecture is a derivative from ObjectSim 2.0.3.,
 *      a simulation program published in:
 *
 *        Eickhoff, J.:
 *        Modulare Programmarchitektur fuer ein wissensbasiertes
 *        Simulationssystem mit erweiterter Anwendbarkeit in der
 *        Entwicklung und Betriebsueberwachung verfahrenstechnischer
 *        Anlagen.
 *        PhD thesis in Department Process Engineering of
 *        TU Hamburg-Harburg, 1996.
 *
 *      See also file history cited there and see historic relation of
 *      this OpenSimKit class to a.m. ObjectSim explained in
 *      OpenSimKit Documentation.
 *
 *      File under GPL - see OpenSimKit Documentation.
 *
 *      No warranty and liability for correctness by author.
 *
 *  2008-05
 *      OpenSimKit V 2.4
 *      Ported from C++ to Java
 *      A. Brandt  alexander.brandt@gmail.com
 *
 *  2009-04
 *      Added the description variable.
 *      A. Brandt
 *
 *  2009-06
 *     Integrated logging support.
 *     T. Pieper
 *
 *  2009-07
 *     Enhanced scope of logged data in debug setting.
 *     J. Eickhoff
 *
 */
package org.opensimkit;

import java.io.FileWriter;
import java.io.IOException;
import org.opensimkit.manipulation.Callable;
import org.opensimkit.manipulation.Manipulatable;
import org.opensimkit.manipulation.Readable;
import org.opensimkit.steps.CRegulStep;
import org.opensimkit.steps.CTimeStep;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation of an abstract model class.
 *
 * @author J. Eickhoff
 * @author A. Brandt
 * @author T. Pieper
 * @version 1.3
 * @since 2.4.0
 */
public class BaseModel implements Model {
    private static final Logger LOG = LoggerFactory.getLogger(BaseModel.class);
    /** Name of the Model. */
    @Readable      protected String   name;
    /** Description of the Model. */
    @Manipulatable protected String   description;
    /** Type of the Model. */
    @Manipulatable protected String   type;
    @Readable      protected String   numSolverType;
    @Readable      protected double   maxIntegStepSize;
    @Readable      protected double   minIntegStepSize;
                   protected int      localNAckFlag;
    /** If the Model has to compute in... */
    @Readable      private   int      rStep;
    /** If the Model has to compute in... */
    @Readable      private   int      tStep;

    /**
     * Creates a new instance of BaseModel.
     *
     * @param name   the name of the Model.
     * @param type   the type of the Model.
     * @param numSolverType
     * @param maxts
     * @param mints
     * @param ts
     * @param rs
     */
    public BaseModel(final String name, final String type,
        final String numSolverType, final double maxts, final double mints,
        final int ts, final int rs) {

        this.name = name;
        this.type = type;
        this.numSolverType = numSolverType;
        this.maxIntegStepSize = maxts;
        this.minIntegStepSize = mints;
        this.rStep = rs;
        this.tStep = ts;
        LOG.debug(SimHeaders.DEBUG_SHORT, "Constructor Model {}", name);
    }

    /**
     *
     * @param d1
     * @param d2
     * @return  error code
     */
    public int timeStep(final double d1, final double d2) {
        return 0;
    }

    /**
     *
     * @return error code
     */
    public int regulStep() {
        return 0;
    }

    /**
     *
     * @return error code
     */
    public int iterationStep() {
        return 0;
    }

    /**
     *
     * @param f
     * @return error code
     * @throws IOException
     */
    public int save(final FileWriter f) throws IOException {
        return 0;
    }

    /**
     * The initialization of the Model takes place in this method. It is
     * called after the creation of the instance and the loading of its default
     * values so that derived variables can be calculated after loading or
     * re-calculated after the change of a manipulatable variable (but in this
     * case the init method must be called manually!).
     */
    @Callable
    public void init() {
        /** Intentionally empty. */
    }

    /**
     *
     * @param ctime
     * @param cregul
     * @return error code
     */
    public int initCalcSteps(final CTimeStep ctime, final CRegulStep cregul) {
        LOG.debug(SimHeaders.DEBUG_SHORT, "InitCalcSteps {}", name);
        if (tStep > 0) {
            ctime.addItem(this);
            LOG.debug(SimHeaders.DEBUG_SHORT,
                    "BaseModel {} added to timestep.", name);
        }
        if (rStep > 0) {
            cregul.addItem(this);
            LOG.debug(SimHeaders.DEBUG_SHORT,
                    "BaseModel {} added to regulstep.", name);
        }
        return 0;
    }

    // Data that are to be accessed by special reply functions e.g. for Apple
    // Event access.
    //-------------------------------------------------------------------------

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public String getNumSolverType() {
        return numSolverType;
    }

    public double getMaxIntegStepSize() {
        return maxIntegStepSize;
    }

    public double getMinIntegStepSize() {
        return minIntegStepSize;
    }

    /**
     *
     * @return error code
     */
    public int backIterStep() {
        return 0;
    }

}
