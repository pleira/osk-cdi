/*
 * FluidFlowValve.java
 *
 * Created on 31. December 2008
 *
 *  Model definition for a fluid flow valve.
t this.name = name;  
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
 *
 *      File under GPL  see OpenSimKit Documentation.
 *
 *      No warranty and liability for correctness by author.
 *
*/
package org.osk.models.rocketpropulsion;

import javax.inject.Inject;

import org.osk.models.BaseModel;
import org.osk.ports.AnalogPort;
import org.osk.ports.FluidPort;
import org.osk.time.TimeHandler;
import org.slf4j.Logger;

import com.sun.org.glassfish.gmbal.ManagedAttribute;

/**
 * Model definition for fluid flow valve.
 *
 * @author J. Eickhoff
 * @author A. Brandt
 * @author P. Pita
 */


public class FluidFlowValve extends BaseModel {

	@Inject Logger LOG;
	@Inject TimeHandler timeHandler;

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
	private double controlValue;
	private double DP;

	private static final String TYPE = "FluidFlowValve";
	private static final String SOLVER = "none";
	
    public FluidFlowValve() {
        super(TYPE, SOLVER);
	}

    public void init(String name) {
    	this.name = name;  
        massflow = 0.0;
 
        /* Specify a reference pressure loss at nominal massflow rate and
           controValue = 1. */
        referencePressureLoss = referencePressureLoss * 1.E5;
    }
	   
    public FluidPort calculateMassFlow(final long simTime, FluidPort inputPort, AnalogPort controlPort) {
//        LOG.info(name);
        controlValue = controlPort.getAnalogValue();
//        LOG.info("Reading controlValue: '{}'", controlValue);
        if (controlValue < 0.0) {
            controlValue = 0.0;
        }
//        LOG.info("Corrected controlValue: '{}'", controlValue);

        fluid = inputPort.getFluid();
        pin   = inputPort.getPressure();
        tin   = inputPort.getTemperature();
        mfin  = inputPort.getMassflow();

        if (simTime == 0) {
            massflow = mfin;
        } else {
            massflow = referenceMassFlow * controlValue;
        }
//        LOG.info("Massflow: '{}'", massflow);

        //Skip iteration step computation if no flow in valve
        if (mfin <= 1.E-6) {
            pout  = pin;
            tout  = tin;
            mfout = mfin;
//            LOG.info("Massflow: '{}'", mfout);
            return createOutputPort();
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
        return createOutputPort();        
    }
   
   	public FluidPort createOutputPort() {
   		FluidPort outputPort = new FluidPort();
   		outputPort.setFluid(fluid);
   		outputPort.setPressure(pout);
   		outputPort.setTemperature(tout);
   		outputPort.setMassflow(mfout);
   		return outputPort;
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
