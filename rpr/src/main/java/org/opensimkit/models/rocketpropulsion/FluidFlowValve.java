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

import java.io.FileWriter;
import java.io.IOException;

import javax.annotation.PostConstruct;

import org.opensimkit.BaseModel;
import org.opensimkit.Kernel;
import org.opensimkit.SimHeaders;
import org.opensimkit.manipulation.Manipulatable;
import org.opensimkit.manipulation.Readable;
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
    /** Logger instance for the FluidFlowValve. */
    private static final Logger LOG
            = LoggerFactory.getLogger(FluidFlowValve.class);
    /** Commandeable mass flow. */
    @Manipulatable private double massflow;
    @Manipulatable private double referencePressureLoss;
    @Manipulatable private double referenceMassFlow;
    @Readable      private String fluid;
    @Readable      private double pin;
    @Readable      private double tin;
    @Readable      private double mfin;
    @Readable      private double pout;
    @Readable      private double tout;
    @Readable      private double mfout;
    @Readable      private double localtime;
    @Readable      private double controlValue;
    @Readable      private double DP;
    //E id;

    private static final String TYPE      = "FluidFlowValve";
    private static final String SOLVER    = "none";
    private static final double MAXTSTEP  = 10.0;
    private static final double MINTSTEP  = 0.001;
    private static final int    TIMESTEP  = 1;
    private static final int    REGULSTEP = 0;

    @Manipulatable protected PureLiquidPort inputPort;

//	@Inject @Named("19_PureLiquidDat")
	@Manipulatable protected PureLiquidPort outputPort;
	
//	@Inject @Named("23_Fuel_Flow_Control_Signal")
	@Manipulatable protected AnalogPort     controlPort;


   public FluidFlowValve(final String name) {
        super(name, TYPE, SOLVER, MAXTSTEP, MINTSTEP, TIMESTEP, REGULSTEP);
    }

    /**
     * Creates a new instance of the Fluid Flow Valve.
     *
     * @param name Name of the instance.
     * @param kernel Reference to the kernel.
     */
    public FluidFlowValve(final String name, final Kernel kernel) {
        super(name, TYPE, SOLVER, MAXTSTEP, MINTSTEP, TIMESTEP, REGULSTEP);
    }

    public FluidFlowValve(String name, PureLiquidPort inputPort,
			PureLiquidPort outputPort, AnalogPort controlPort) {
        super(name, TYPE, SOLVER, MAXTSTEP, MINTSTEP, TIMESTEP, REGULSTEP);
		this.inputPort = inputPort;
		this.outputPort = outputPort;
		this.controlPort = controlPort;		
	}

	public double getReferencePressureLoss() {
		return referencePressureLoss;
	}

	public void setReferencePressureLoss(double referencePressureLoss) {
		this.referencePressureLoss = referencePressureLoss;
	}

	public double getReferenceMassFlow() {
		return referenceMassFlow;
	}

	public void setReferenceMassFlow(double referenceMassFlow) {
		this.referenceMassFlow = referenceMassFlow;
	}

	@PostConstruct
    public void completeConnections() {
    	LOG.info("completeConnections for " + name);
        inputPort.setToModel(this);
        outputPort.setFromModel(this);
        controlPort.setFromModel(this);
    }
    
	public PureLiquidPort getInputPort() {
		return inputPort;
	}

	public PureLiquidPort getOutputPort() {
		return outputPort;
	}

	public AnalogPort getControlPort() {
		return controlPort;
	}

	/**
    * The initialization of the Component takes place in this method. It is
    * called after the creation of the instance and the loading of its default
    * values so that derived variables can be calculated after loading or
    * re-calculated after the change of a manipulatable variable (but in this
    * case the init method must be called manually!).
    */
    @Override    
    public void init() {
    	completeConnections();
        /* Computation of derived initialization parameters. */
        /* Initializing mass flow. */
        massflow = 0.0;
        localtime = 0.0;
        //controlValue = 0.0;

        /* Specify a reference pressure loss at nominal massflow rate and
           controValue = 1. */
        referencePressureLoss = referencePressureLoss * 1.E5;
    }


    @Override
    public int timeStep(final double time, final double tStepSize) {
        LOG.debug("% {} TimeStep-Computation", name);

        controlValue = controlPort.getAnalogValue();
        LOG.debug("Reading controlValue: '{}'", controlValue);
        if (controlValue < 0.0) {
            controlValue = 0.0;
        }
        LOG.debug("Corrected controlValue: '{}'", controlValue);

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
        LOG.debug("Massflow: '{}'", massflow);

        outputPort.setFluid(fluid);
        outputPort.setPressure(pout);
        outputPort.setTemperature(tout);
        outputPort.setMassflow(mfout);
        localtime = localtime + 0.5;
        return 0;
    }
    
	@Override
    public int iterationStep() {
        LOG.debug("% {} IterationStep-Computation", name);

        controlValue = controlPort.getAnalogValue();
        LOG.debug("Reading controlValue: '{}'", controlValue);
        if (controlValue < 0.0) {
            controlValue = 0.0;
        }
        LOG.debug("Corrected controlValue: '{}'", controlValue);

        fluid = inputPort.getFluid();
        pin   = inputPort.getPressure();
        tin   = inputPort.getTemperature();
        mfin  = inputPort.getMassflow();

        if (localtime == 0.0) {
            massflow = mfin;
            LOG.debug("Massflow: '{}'", massflow);
        } else {
            massflow = referenceMassFlow * controlValue;
            LOG.debug("Massflow: '{}'", massflow);
        }

        //Skip iteration step computation if no flow in valve
        if (mfin <= 1.E-6) {
            pout  = pin;
            tout  = tin;
            mfout = mfin;
            LOG.debug("Massflow: '{}'", mfout);

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
        LOG.debug("% {} BackIteration-Computation", name);

        controlValue = controlPort.getAnalogValue();
        LOG.debug("Reading controlValue: '{}'", controlValue);
        if (controlValue < 0.0) {
            controlValue = 0.0;
        }
        LOG.debug("Corrected controlValue: '{}'", controlValue);

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
        LOG.debug("Massflow: '{}'", massflow);

        inputPort.setBoundaryFluid(outputPort.getBoundaryFluid());
        inputPort.setBoundaryPressure(-999999.99);
        inputPort.setBoundaryTemperature(-999999.99);
        inputPort.setBoundaryMassflow(massflow);

        return result;
    }	

	@Override
    public int regulStep() {
        LOG.debug("% {} RegulStep-Computation", name);
        return 0;
    }


    @Override
    public int save(final FileWriter outFile) throws IOException {
        outFile.write("FluidFlowValve: '" + name + "'" + SimHeaders.NEWLINE);
        return 0;
    }
}
