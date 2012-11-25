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
package org.opensimkit.models;

import java.io.FileWriter;
import java.io.IOException;
import java.io.Serializable;

import org.opensimkit.SimHeaders;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.org.glassfish.gmbal.ManagedAttribute;

/**
 * Implementation of an abstract model class.
 * 
 * @author J. Eickhoff
 * @author A. Brandt
 * @author T. Pieper
 * @version 1.3
 * @since 2.4.0
 */
public class BaseModel implements Model, Serializable {
	private transient static final Logger LOG = LoggerFactory.getLogger(BaseModel.class);
	/** Name of the Model. */
	protected String name;
	/** Description of the Model. */
	protected String description;
	/** Type of the Model. */
	protected String type;
	protected String numSolverType;
	protected double maxIntegStepSize;
	protected double minIntegStepSize;
	protected int localNAckFlag;

	public BaseModel(final String name, final String type,
			final String numSolverType, final double maxts, final double mints) {

		this.name = name;
		this.type = type;
		this.numSolverType = numSolverType;
		this.maxIntegStepSize = maxts;
		this.minIntegStepSize = mints;
		LOG.info(SimHeaders.DEBUG_SHORT, "Constructor Model {}", name);
	}

	public int timeStep(final double d1, final double d2) {
        LOG.info("% {} TimeStep-Computation", name);
		return 0;
	}

	public int regulStep() {
        LOG.info("% {} RegulStep-Computation", name);
		return 0;
	}

	public int iterationStep() {
        LOG.info("% {} IterationStep-Computation", name);
		return 0;
	}

	public int backIterStep() {
        LOG.info("% {} BackiterStep-Computation", name);
		return 0;
	}

	public int save(final FileWriter f) throws IOException {
        f.write("Model: '" + name + "'" + SimHeaders.NEWLINE);
		return 0;
	}

	public void init() {
		/** Intentionally empty. */
	}


	// -----------------------------------------------------------------------------------
	// Methods added for JMX monitoring and setting initial properties via CDI
	// Extensions

	@ManagedAttribute
	public String getName() {
		return name;
	}

	@ManagedAttribute
	public String getType() {
		return type;
	}

	@ManagedAttribute
	public String getNumSolverType() {
		return numSolverType;
	}

	@ManagedAttribute
	public double getMaxIntegStepSize() {
		return maxIntegStepSize;
	}

	@ManagedAttribute
	public double getMinIntegStepSize() {
		return minIntegStepSize;
	}

	@ManagedAttribute
	public String getDescription() {
		return description;
	}

	@ManagedAttribute
	public int getLocalNAckFlag() {
		return localNAckFlag;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setType(String type) {
		this.type = type;
	}

}
