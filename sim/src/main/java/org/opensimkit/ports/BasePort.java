/*
 * Port.java
 *
 * Created on 3. Juli 2007, 23:01
 *
 * Simulation system port model.
 * Class for port objects that implement the data interconnections between
 * the objects simulation the real systems models like pipes, tanks etc.
 *
 *-----------------------------------------------------------------------------
 *  Modification History
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
 *  2008-05
 *      OpenSimKit V 2.4
 *      Ported from C++ to Java
 *      A. Brandt  alexander.brandt@gmail.com
 *
 *  2009-06
 *     Integrated logging support.
 *     T. Pieper
 *
 *  2009-11-29
 *      Boundary condition handling cleaned up.
 *      A. Brandt
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
package org.opensimkit.ports;

import java.io.Serializable;

import org.opensimkit.Model;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class for port objects that implement the data interconnections between
 * the objects simulation the real systems models like pipes, tanks etc.
 *
 * @author J. Eickhoff
 * @author P. Heinrich
 * @author A. Brandt
 * @author T. Pieper
 * @version 1.2
 * @since 2.4.0
 */
public class BasePort implements Port, Serializable {
    private transient static final Logger LOG = LoggerFactory.getLogger(BasePort.class);
    String name;
    private String id;
    private String description;
    private Model  fromModel;
    
    public Model getFromModel() {
		return fromModel;
	}

	public void setFromModel(Model fromModel) {
		this.fromModel = fromModel;
	}

	public Model getToModel() {
		return toModel;
	}

	public void setToModel(Model toModel) {
		this.toModel = toModel;
	}

	private Model  toModel;
    //private int       localNAckFlag;
    //Only one port-class but several classes for the port-data. This
    //requires a check for a correct initialization from models to the
    //ports. The type field enables this.

    public BasePort(final String name) {
        this.name = name;
    }

    /**
     * Connects two models with this port.
     *
     * @param fromModel  the first model
     * @param toModel    the second model
     */
    public void connectWith(final Model fromModel, final Model toModel) {
        this.fromModel = fromModel;
        this.toModel   = toModel;
    }

    /**
     *
     * @return    name of the model
     */
    public String getDownstreamComp() {
        String workstring;
        if (toModel != null) {
            workstring = toModel.getName();
        } else {
            workstring = "NIL";
        }
        return workstring;
    }

    /**
     *
     * @return    name of the model
     */
    public String getUpstreamComp() {
        String workstring;
        if (fromModel != null) {
            workstring = fromModel.getName();
        } else {
            workstring = "NIL";
        }
        return workstring;
    }

    /**
     * Returns the name of the Port object.
     *
     * @return    name of the Port object
     */
    public String getName() {
        return name;
    }

    /**
     *
     * @param   text
     */
    public void printValues(final String text) {
        LOG.info("% " + name + " " + id);
    }

    protected double[] setVector(final double[] input, final int size) {
    double[] output = new double[size];
    
        if (input.length > size) {
            LOG.error("The length of the new locationVector is {},"
                        + " and thus greater than {}!", input.length, size);
        } else if (input.length == size) {
            output = input;
        } else  if (input.length == 0) {
            LOG.info("The length of the new locationVector is 0!");
        } else {
            for (int i = 0; i < input.length; i++) {
                output[0] = input[i];
            }
        }
    return output;
    }

}
