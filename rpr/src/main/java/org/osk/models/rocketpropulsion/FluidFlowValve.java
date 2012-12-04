/*
 * FluidFlowValve.java
 *
 * Created on 31. December 2008
 *
 *  Model definition for a fluid flow valve.
 *
 *                  inputPort  |
 *                  +----------+--------------+
 *    controlPort --+                         |
 *                  +----------+--------------+
 *                             | getOuputPort()
 *
 *  Valve computes the following phenomena:
 *    - Mass flow control according to settings
 *    - Pressure drop according to settings (very simplified only, preparing for
 *      detailed formula)
 *  Valve is considered to be thermally isolated versus the environment.
 *
 *
 *-----------------------------------------------------------------------------
 * Modification History:
 *
 *  2008-12-31
 *      File created  J. Eickhoff:
 *
 *      File under GPL  see OpenSimKit Documentation.
 *
 *      No warranty and liability for correctness by author.
 *
 *
 *
 *  2009-04
 *      Replaced the port array by named ports.
 *      A. Brandt
 *
 *  2009-06-10
 *      Corrections in boundary conditions reading to comply with connection
 *      to rocket engine model.
 *      J. Eickhoff
 *
 *
 *  2009-06-20
 *      Added flow control port (type AnalogPort).
 *      J. Eickhoff
 *
 *  2009-07
 *      OpenSimKit V 2.8
 *      Upgraded for handling of new port classes.
 *      A. Brandt
 *
*/
package org.opensimkit.models.rocketpropulsion;

import javax.annotation.PostConstruct;

import net.gescobar.jmx.annotation.ManagedAttribute;

import org.opensimkit.models.BaseModel;
import org.opensimkit.ports.AnalogPort;
import org.opensimkit.ports.PureLiquidPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Model definition for fluid flow valve.
 *
 * @author J. Eickhoff
 * @author A. Brandt
 */

public abstract class FluidFlowValve extends BaseModel {

	private static final Logger LOG
            = LoggerFactory.getLogger(FluidFlowValve.class);

	private double massflow;
	private double referencePressureLoss;
	private double referenceMassFlow;
	private String fluid;
	private double pin;
	private double tin;
	private double mfin;
	private double pout;
	private double tout;
	private double mfout;
	private double localtime;
	private double controlValue;
	private double DP;

	private static final String TYPE = "FluidFlowValve";
	private static final String SOLVER = "none";
	private static final double MAXTSTEP = 10.0;
	private static final double MINTSTEP = 0.001;
	
	protected final PureLiquidPort inputPort;
	protected final PureLiquidPort outputPort;
	protected final AnalogPort controlPort;
	
    public FluidFlowValve(String name, PureLiquidPort inputPort,
			PureLiquidPort outputPort, AnalogPort controlPort) {
        super(name, TYPE, SOLVER, MAXTSTEP, MINTSTEP);
		this.inputPort = inputPort;
		this.outputPort = outputPort;
		this.controlPort = controlPort;		
	}

    @Override    
	@PostConstruct
    public void init() {
    	completeConnections();
        massflow = 0.0;
        localtime = 0.0;
        //controlValue = 0.0;

        /* Specify a reference pressure loss at nominal massflow rate and
           controValue = 1. */
        referencePressureLoss = referencePressureLoss * 1.E5;
    }

    void completeConnections() {
        inputPort.setToModel(this);
        outputPort.setFromModel(this);
        controlPort.setFromModel(this);
    	LOG.info("completeConnections for " + name + ", (" + inputPort.getName()  + "," + outputPort.getName()  + "," + controlPort.getName() + ")" );
    }

    @Override
    public int timeStep(final double time, final double tStepSize) {
        LOG.info("% {} TimeStep-Computation", name);

        controlValue = controlPort.getAnalogValue();
        LOG.info("Reading controlValue: '{}'", controlValue);
        if (controlValue < 0.0) {
            controlValue = 0.0;
        }
        LOG.info("Corrected controlValue: '{}'", controlValue);

        fluid = inputPort.getFluid();
        pin   = inputPort.getPressure();
        tin   = inputPort.getTemperature();
        mfin  = inputPort.getMassflow();

        /* Skip time step computation if no flow in Valve. */
        if (mfin <= 1.E-6) {
            localtime = localtime + 0.5;
            return 0;
        }
        /* Currently no complex timestep physics forseen here yet. */
        if (localtime == 0.0) {
            massflow = mfin;
        } else {
            massflow = referenceMassFlow * controlValue;
        }
        DP = referencePressureLoss * massflow / 5.0;
        pout = pin - DP;
        tout = tin;
        mfout = massflow;
        LOG.info("Massflow: '{}'", massflow);

        outputPort.setFluid(fluid);
        outputPort.setPressure(pout);
        outputPort.setTemperature(tout);
        outputPort.setMassflow(mfout);
        localtime = localtime + 0.5;
        return 0;
    }
    
	@Override
    public int iterationStep() {
        LOG.info("% {} IterationStep-Computation", name);

        controlValue = controlPort.getAnalogValue();
        LOG.info("Reading controlValue: '{}'", controlValue);
        if (controlValue < 0.0) {
            controlValue = 0.0;
        }
        LOG.info("Corrected controlValue: '{}'", controlValue);

        fluid = inputPort.getFluid();
        pin   = inputPort.getPressure();
        tin   = inputPort.getTemperature();
        mfin  = inputPort.getMassflow();

        if (localtime == 0.0) {
            massflow = mfin;
            LOG.info("Massflow: '{}'", massflow);
        } else {
            massflow = referenceMassFlow * controlValue;
            LOG.info("Massflow: '{}'", massflow);
        }

        //Skip iteration step computation if no flow in valve
        if (mfin <= 1.E-6) {
            pout  = pin;
            tout  = tin;
            mfout = mfin;
            LOG.info("Massflow: '{}'", mfout);

            outputPort.setFluid(fluid);
            outputPort.setPressure(pout);
            outputPort.setTemperature(tout);
            outputPort.setMassflow(mfout);
            return 0;
        }

        /**********************************************************************/
        /*                                                                    */
        /*      Pressure loss in valve as linear dependency of                */
        /*      fluid flow.                                                   */
        /*      Reference pressure loss & reference mass flow                 */
        /*      are design variables read from inputfile.                     */
        /*      This section must be improved by a realistic valve char.      */
        /*                                                                    */
        /**********************************************************************/

        DP = referencePressureLoss * mfin / 5.0;
        pout = pin - DP;
        tout = tin;
        mfout = massflow;

        outputPort.setFluid(fluid);
        outputPort.setPressure(pout);
        outputPort.setTemperature(tout);
        outputPort.setMassflow(mfout);

        return 0;
    }


    @Override
    public int backIterStep() {
        int result;

        result = 0;
        LOG.info("% {} BackIteration-Computation", name);

        controlValue = controlPort.getAnalogValue();
        LOG.info("Reading controlValue: '{}'", controlValue);
        if (controlValue < 0.0) {
            controlValue = 0.0;
        }
        LOG.info("Corrected controlValue: '{}'", controlValue);

        if (outputPort.getBoundaryPressure() >= 0.0) {
            LOG.info("Error! Comp. '{}': Pressure request on port 1 cannot"
                    + " be handled!", name);
            //    nonResumeFlag = 1;
            result = 1;
        }
        if (outputPort.getBoundaryTemperature() >= 0.0) {
            LOG.info("Error! Comp. '{}': Temp. request on port 1 cannot "
                    + "be handled!", name);
            //    nonResumeFlag = 1;
            result = 1;
        }

        /* Init massflow:
         * At initialization reading boundary massflow from downstream...... */
        if (localtime == 0.0) {
            massflow = outputPort.getBoundaryMassflow();
        }
        /* In normal backiterations reflecting upstream the massflow
         * computed from the controller signal which was elaborated in timestep.
         */
        LOG.info("Massflow: '{}'", massflow);

        inputPort.setBoundaryFluid(outputPort.getBoundaryFluid());
        inputPort.setBoundaryPressure(-999999.99);
        inputPort.setBoundaryTemperature(-999999.99);
        inputPort.setBoundaryMassflow(massflow);

        return result;
    }	


    //-----------------------------------------------------------------------------------
    // Methods added for JMX monitoring	and setting initial properties via CDI Extensions

	@ManagedAttribute
	public double getMassflow() {
		return massflow;
	}

	public void setMassflow(double massflow) {
		this.massflow = massflow;
	}

	@ManagedAttribute
	public String getFluid() {
		return fluid;
	}

	public void setFluid(String fluid) {
		this.fluid = fluid;
	}

	@ManagedAttribute
	public double getPin() {
		return pin;
	}

	public void setPin(double pin) {
		this.pin = pin;
	}

	@ManagedAttribute
	public double getTin() {
		return tin;
	}

	public void setTin(double tin) {
		this.tin = tin;
	}

	@ManagedAttribute
	public double getMfin() {
		return mfin;
	}

	public void setMfin(double mfin) {
		this.mfin = mfin;
	}

	@ManagedAttribute
	public double getPout() {
		return pout;
	}

	public void setPout(double pout) {
		this.pout = pout;
	}

	@ManagedAttribute
	public double getTout() {
		return tout;
	}

	public void setTout(double tout) {
		this.tout = tout;
	}

	@ManagedAttribute
	public double getMfout() {
		return mfout;
	}

	public void setMfout(double mfout) {
		this.mfout = mfout;
	}

	@ManagedAttribute
	public double getLocaltime() {
		return localtime;
	}

	public void setLocaltime(double localtime) {
		this.localtime = localtime;
	}

	@ManagedAttribute
	public double getControlValue() {
		return controlValue;
	}

	public void setControlValue(double controlValue) {
		this.controlValue = controlValue;
	}

	@ManagedAttribute
	public double getDP() {
		return DP;
	}

	public void setDP(double dP) {
		DP = dP;
	}
    
	@ManagedAttribute
	public double getReferencePressureLoss() {
		return referencePressureLoss;
	}

	public void setReferencePressureLoss(double referencePressureLoss) {
		this.referencePressureLoss = referencePressureLoss;
	}
	@ManagedAttribute
	public double getReferenceMassFlow() {
		return referenceMassFlow;
	}

	public void setReferenceMassFlow(double referenceMassFlow) {
		this.referenceMassFlow = referenceMassFlow;
	}
       
}
