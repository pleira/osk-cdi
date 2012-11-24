/*
 * OSKGravityModel.java
 *
 * Created on 20. February 2009
 *
 *
 *-----------------------------------------------------------------------------
 * Modification History:
 *
 *  2009-02-20
 *      File created  M. Kobald, A. Bohr:
 *
 *  2009-07
 *      OpenSimKit V 2.8
 *      Upgraded for handling of new port classes.
 *      A. Brandt
 *
 *  2011-01
 *      Implemented:
 *      Position handover to OSKGravityModel via provider/subscriber mechanism 
 *      instead of dedicated port class.
 *      Gravity acceleration provision to ScStructure etc. via same mechanism.
 *      Diverse cleanups.
 *      Bugfix for proper handling of 1st timestep with still empty S/C pos. info.
 *      J. Eickhoff
 *
 *
 *-----------------------------------------------------------------------------
 *
 *      File under GPL  see OpenSimKit Documentation.
 *
 *      No warranty and liability for correctness by author.
 *
 *-----------------------------------------------------------------------------
 */
package org.opensimkit.models.environment;

import jat.forces.GravityModel;
import jat.matvec.data.Matrix;
import jat.matvec.data.VectorN;
import jat.spacetime.EarthRef;
import jat.spacetime.Time;

import java.io.File;
import java.io.IOException;

import javax.annotation.PostConstruct;
import javax.enterprise.event.Event;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

import net.gescobar.jmx.annotation.ManagedAttribute;

import org.opensimkit.BaseModel;
import org.opensimkit.TimeHandler;
import org.opensimkit.events.D4Value;
import org.opensimkit.events.ECI;
import org.opensimkit.events.Gravity;
import org.opensimkit.events.ScPV;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author M. Kobald
 * @author A. Bohr
 * @author A. Brandt
 * @author J. Eickhoff
 * @version 2.0
 * @since 2.8.0
 */


public class OSKGravityModel extends BaseModel {
    /** Logger instance for the OSKGravityModel. */
    private static final Logger LOG = LoggerFactory.getLogger(OSKGravityModel.class);
    /** Order of spherical harmonic. */
     private int order = 8; // hardcoded for now 
    /** Degree of spherical harmonic. */
     private int degree = 8; // hardcoded for now 
    /**Earth gravity acceleration: Value and direction vector in ECI. 
       Vector components in [m/s^ 2] */
     private double[] gravAcceleration = new double[4];
    /** Structure SCPosition in ECI frame. */
     private double[] scPositionECI = new double[3];
    /** JAT Class of S/C position vector in ECI. */
    private VectorN  scPositionVector;
    /** JAT Earth reference frame. */
    private EarthRef earthReference;
    /** JAT Earth gravity model. */
    private GravityModel gravityModel;
    /** ECI to ECEF conversion matrix of JAT. */
    private Matrix eci2ECEFMatrix;
    /** Gravity vector in ECI as JAT class. */
    private VectorN gravityVector;
    /** Time handler object. */
    @Inject
    private TimeHandler timeHandler;
    /** OSK SRT time in converted format. */
    Time convertedMissionTime;
    
    private static final String TYPE = "OSKGravity1";
    private static final String SOLVER = "none";
    private static final double MAXTSTEP = 10.0;
    private static final double MINTSTEP = 0.001;
    private static final int TIMESTEP = 1;
    private static final int REGULSTEP = 0;

    @Inject @Gravity Event<D4Value> event;
    
    /*----------------------------------------------------------------------
    Note! The variable(s)
        scPositionECI[]
    is(are) used in subsequent code lines.
    Please assure that the according variable(s) is(are) provided to this 
    model via the provider/subscriber mechanism by specifying an according 
    subscription entry in the simulation input file.
    ------------------------------------------------------------------------
    Note! The variable(s)
        gravAcceleration[]
    is(are) computed for use by other models in subsequent code lines.
    Please assure that the according variable(s) is(are) handed over to
    subscribers by specifying an according provision entry in the simulation 
    input file.
    ----------------------------------------------------------------------*/

    
    /**
     * Creates a new instance of the gravity model.
     *
     * @param name Name of the instance.
     * @param kernel Reference to the kernel.
     */
    public OSKGravityModel() {
        super("23_OSKGravityModel", TYPE, SOLVER, MAXTSTEP, MINTSTEP, TIMESTEP, REGULSTEP);
        name = "23_OSKGravityModel";
    }

    @Override
    @PostConstruct
    public void init() {
        LOG.info("% {} Init-Computation", name);

        scPositionVector = new VectorN(3);
        gravityVector = new VectorN(3);
        convertedMissionTime = new Time();
        earthReference = new EarthRef(convertedMissionTime);
        // Maybe one day this file is read as resource from classpath 
        File path = new File("src/main/resources/earthGravity/JGM3.grv");

        String pathString;
        try {
            pathString = path.getCanonicalPath();
            gravityModel = new GravityModel(order, degree, pathString);
        } catch (IOException ex) {
            LOG.error("Exception: ", ex);
        }
        gravAcceleration[0] = 0;
        gravAcceleration[1] = 0;
        gravAcceleration[2] = 0;
        gravAcceleration[3] = 0;
    }


    @Override
    public int timeStep(final double time, final double tStepSize) {
        double[] scPosition = new double[3];
        double[] scAcceleration = new double[3];
        double scAccelMag;

        LOG.info("% {} TimeStep-Computation", name);

        LOG.info("scPositionECI[0]:  '{}' ", scPositionECI[0]);
        LOG.info("scPositionECI[1]:  '{}' ", scPositionECI[1]);
        LOG.info("scPositionECI[2]:  '{}' ", scPositionECI[2]);

        //Skip potential start condition where S/C Position is not yet valid:
        if (scPositionECI[0]==0 & scPositionECI[1]==0 & scPositionECI[2]==0) {
          return 0;
        }
        
        scPositionVector.set(0, scPositionECI[0]);
        scPositionVector.set(1, scPositionECI[1]);
        scPositionVector.set(2, scPositionECI[2]);

        convertedMissionTime.update(timeHandler.getSimulatedMissionTimeAsDouble());
        eci2ECEFMatrix = earthReference.eci2ecef(convertedMissionTime);

        //    	System.out.println("ECI2ECEF1:" + eci2ECEFMatrix.get(0, 0) + " " +
//				  eci2ECEFMatrix.get(0, 1) + " " +
//				  eci2ECEFMatrix.get(0, 2));
//    	System.out.println("          " + eci2ECEFMatrix.get(1, 0) + " " +
//				  eci2ECEFMatrix.get(1, 1) + " " +
//				  eci2ECEFMatrix.get(1, 2));
//    	System.out.println("          " + eci2ECEFMatrix.get(2, 0) + " " +
//				  eci2ECEFMatrix.get(2, 1) + " " +
//				  eci2ECEFMatrix.get(2, 2));
	
        gravityVector = gravityModel.gravity(scPositionVector, eci2ECEFMatrix);

        scAcceleration = gravityVector.getArray();
        scAccelMag = gravityVector.mag();

        scAcceleration[0] /= scAccelMag;
        scAcceleration[1] /= scAccelMag;
        scAcceleration[2] /= scAccelMag;
        
        gravAcceleration[0] = scAccelMag;
        gravAcceleration[1] = scAcceleration[0];
        gravAcceleration[2] = scAcceleration[1];
        gravAcceleration[3] = scAcceleration[2];

        event.fire(new D4Value(gravAcceleration));
        
        return 0;
    }
    
	public void pvHandler(@Observes @ECI ScPV posVel) {
		double[] p = posVel.getScPosition();
		assert p.length == 3;
		for (int i=0; i<3; i++) {
			scPositionECI[i] = p[i];
		}		
	}

	//----------------------------------------
    // Methods added for JMX monitoring	

	@ManagedAttribute
	public double[] getScPositionECI() {
		return scPositionECI;
	}


	public void setScPositionECI(double[] scPositionECI) {
		this.scPositionECI = scPositionECI;
	}

	@ManagedAttribute
	public int getOrder() {
		return order;
	}


	public void setOrder(int order) {
		this.order = order;
	}

	@ManagedAttribute
	public int getDegree() {
		return degree;
	}


	public void setDegree(int degree) {
		this.degree = degree;
	}

	@ManagedAttribute
	public double[] getGravAcceleration() {
		return gravAcceleration;
	}


	public void setGravAcceleration(double[] gravAcceleration) {
		this.gravAcceleration = gravAcceleration;
	}
	
	
}
