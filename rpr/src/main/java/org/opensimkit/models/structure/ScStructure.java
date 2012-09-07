/*
 * ScStructure.java
 *
 *  Model definition for a very simple spacecraft structure model.
 *
 *  Created on 20. February 2009
 *
 *
 *-----------------------------------------------------------------------------
 * Modification History:
 *
 *  2009-02-20
 *      File created  C. Ziemke:
 *
 *  2009-07
 *      OpenSimKit V 2.8
 *      Upgraded for handling of new port classes.
 *      A. Brandt
 *
 *  2009-08
 *      Minor cleanups for integration with engine model and others.
 *      J. Eickhoff
 *
 *  2009-11
 *      Added coordinate transformations to ECEF and conversion to
 *      longitude / latitude / altitude coordinates
 *      based on Java Astrodynamics Toolkit.
 *      I. Kossev
 *
 *  2009-12
 *      Update modifying engine thrust vector always being interpreted to
 *      be aligned as tangent to the orbit vector. This allows
 *      simulation of e.g. Hohman transfers already w/o a complete
 *      spacecraft attitude dynamics model.
 *      I. Kossev
 *
 *  2009-12
 *      Eliminated bug in getVelocityECEF.
 *      J. Eickhoff
 *
 *  2010-01
 *      Upgraded to comprise functionality for simplified attitude quaternion computation.
 *      This feature is used in conjunction to coupling OSK to Celestia for 3D 
 *      visualization of spacecraft in-orbit motion.
 *      J. Eickhoff
 *
 *  2010-02
 *      Upgraded to provide S/C position data for 3D visualization in Celestia.
 *      J. Eickhoff
 *
 *  2010-02
 *      Upgraded to provide S/C attitude data for 3D visualization in Celestia.
 *      I. Kossev
 *
 *  2010-03
 *      Experimental debugging of coordinate system differences btw. Celestia
 *      and JAT. Quaternion conversion computations added accordingly.
 *      J. Eickhoff
 *
 *  2010-04
 *      Integration of SimVisThread for each ScStructure instance added.
 *      J. Eickhoff
 *
 *  2010-11
 *      Integration of coordinate transformation bugfix and reintegration 
 *      w. Celestia.
 *      J. Eickhoff, I. Kossev
 *
 *  2010-12
 *      Fix of ECI->ECEF transformation problem with JAT (bug or misuse
 *      due to unclear documentation).
 *      M. Fritz, O. Zeile, J. Eickhoff
 *
 *  2011-01
 *      Implemented:
 *      Altitude handover to engine via provider/subscriber mechanism 
 *      instead of dedicated port class.
 *      Thrust vector handover to ScStructure via same mechanism.
 *      Diverse minor cleanups.
 *      J. Eickhoff
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
 
package org.opensimkit.models.structure;

import jat.matvec.data.Matrix;
import jat.matvec.data.Quaternion;
import jat.matvec.data.VectorN;
import jat.spacetime.EarthRef;
import jat.spacetime.Time;

import javax.enterprise.event.Event;
import javax.enterprise.inject.Any;
import javax.inject.Inject;

import org.opensimkit.BaseModel;
import org.opensimkit.Kernel;
import org.opensimkit.ScData;
import org.opensimkit.TimeHandler;
import org.opensimkit.manipulation.Manipulatable;
import org.opensimkit.manipulation.Readable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.deltaspike.core.api.config.annotation.ConfigProperty;

/**
 * Model definition for a point mass.
 *
 * @author C. Ziemke
 * @author A. Brandt
 * @author J. Eickhoff
 * @author I. Kossev
 * @author M. Fritz
 * @version 4.0
 * @since 2.8.0
 */


public class ScStructure extends BaseModel {
    /** Logger instance for the ScStructure. */
    private static final Logger LOG = LoggerFactory.getLogger(ScStructure.class);
    /** Gravity acceleration imposed onto structure. */
    @Manipulatable private double[] gravityAccel = new double[4];
    /** Structure mass. */
    @Manipulatable private double scMass;
    
    @Inject
    @ConfigProperty(name = "scMass")
    private double scMass0;
    
    /** Structure SCVelocity in ECI frame. */
    @Manipulatable private double[] scVelocityECI = new double[3];
    /** Structure SCPosition in ECI frame. */
    @Manipulatable private double[] scPositionECI = new double[3];
    /**Thrust: Value and direction vector in SC body frame. Vector components in [N] */
    @Manipulatable private double[] tVec = new double[4];

    /** Structure SCVelocity in ECEF frame. */
    private double[] scVelocityECEF = new double[3];
    /** Structure SCPosition in ECEF frame. */
    private double[] scPositionECEF = new double[3];

    /** Structure SCPosition in LatLonAlt frame. */
    private double[] scPositionLatLonAlt = new double[3];

    /** scPosition of previous timestep. */
    private double[] scPositionECI_prev= new double[3];
    /** scVelocity of previous timestep. */
    private double[] scVelocityECI_prev = new double[3];

    @Readable private double scVelocityX, scVelocityY, scVelocityZ;
    @Readable private double scPositionX, scPositionY, scPositionZ;
    @Readable private double scVelX_ECEF, scVelY_ECEF, scVelZ_ECEF;
    @Readable private double scPosX_ECEF, scPosY_ECEF, scPosZ_ECEF;
    @Readable private double scPosLat, scPosLon, scPosAlt;

    /** Time handler object. */
    private final TimeHandler timeHandler;
    /** OSK SRT time in converted format. */
    private Time convertedMissionTime;
    /** Modified Julian date 2000. */
    private double mjdMissionTime;
    /** JAT Earth reference fram. */
    private EarthRef earthReference;
    
    /** Direct cosine matrix ECI to ECEF as Java matrix. */
    private Matrix eci2ECEFMatrix;
    /** Direct cosine matrix ECI to Body as JAT matrix. */
    private Matrix DCM_ECI2BODY = new Matrix(3, 3);
    /** Direct cosine matrix ECEF to Body as JAT matrix. */
    private Matrix DCM_ECEF2BODY = new Matrix(3, 3);

    /** Attitude in ECI as JAT quaternion. */
    private Quaternion quaternionRelECI = new Quaternion();
    /** Attitude in ECEF as JAT quaternion. */
    private Quaternion quaternionRelECEF = new Quaternion();
    /** Attitude quaternion ECI as normal Java vector. */
    private double[]   quaternionInComponent = {0.0, 0.0, 0.0, 0.0};
    /** Diverse JAT vector variables. */
    private VectorN    xBodyFrameAxisInECI = new VectorN(3), 
                       yBodyFrameAxisInECI = new VectorN(3),
                       zBodyFrameAxisInECI = new VectorN(3), 
                       dummyVectorN1 = new VectorN(3), 
                       dummyVectorN2 = new VectorN(3);

    /** Magnitude of engine thrust. */
    private double thrustMag;
    /** Direction vector (unitized) of engine thrust in ECI frame. */
    private double[] thrustVecECI = new double[3];


    @Inject @Any
	Event<ScData> dataEvent;
    

    //  Entries for simVisThread.
    // private static SimVisThread visThread;
    
    private static final String TYPE = "ScStructure";
    private static final String SOLVER = "none";
    private static final double MAXTSTEP = 10.0;
    private static final double MINTSTEP = 0.001;
    private static final int TIMESTEP = 1;
    private static final int REGULSTEP = 0;


    /*----------------------------------------------------------------------
    Note! The variable(s)
        tVec[]
    is(are) used in subsequent code lines.
    Please assure that the according variable(s) is(are) provided to this 
    model via the provider/subscriber mechanism by specifying an according 
    subscription entry in the simulation input file.
    ------------------------------------------------------------------------
    Note! The variable(s)
        scPosAlt
        scPositionECI[]
    is(are) computed for use by other models in subsequent code lines.
    Please assure that the according variable(s) is(are) handed over to
    subscribers by specifying an according provision entry in the simulation 
    input file.
    ----------------------------------------------------------------------*/

    /**
     * Creates a new instance of the S/C structure.
     *
     * @param name Name of the instance.
     * @param kernel Reference to the kernel.
     */
    public ScStructure(final String name, final Kernel kernel) {
        super(name, TYPE, SOLVER, MAXTSTEP, MINTSTEP, TIMESTEP, REGULSTEP);
        timeHandler = kernel.getTimeHandler();
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
        /* Computation of derived initialization parameters. */
        convertedMissionTime = new Time(timeHandler.getSimulatedMissionTimeAsDouble());
        mjdMissionTime = (timeHandler.getSimulatedMissionTimeAsDouble()-946684800)/86400;
        earthReference = new EarthRef(convertedMissionTime);
        thrustMag = 0.0;
        tVec[0] = 0;
        tVec[1] = 0;
        tVec[2] = 0;
        tVec[3] = 0;
        thrustVecECI[0] = 0;
        thrustVecECI[1] = 0;
        thrustVecECI[2] = 0;

        for(int i = 0; i < 3 ; i++){
            scPositionECI_prev[i] = scPositionECI[i];
            scVelocityECI_prev[i] = scVelocityECI[i];
        }
    }


    @Override
    public int timeStep(final double time, final double tStepSize) {
        LOG.debug("% {} TimeStep-Computation", name);

        //Computes the UTC J2000 time in Celestia compatible string format
        String celestiaTime = timeHandler.getCelestiaUTCJulian2000();
        mjdMissionTime = (timeHandler.getSimulatedMissionTimeAsDouble()-946684800)/86400;
        
        LOG.debug("gravityAccel[0]:  '{}' ", gravityAccel[0]);
        LOG.debug("gravityAccel[1]:  '{}' ", gravityAccel[1]);
        LOG.debug("gravityAccel[2]:  '{}' ", gravityAccel[2]);
        LOG.debug("gravityAccel[3]:  '{}' ", gravityAccel[3]);
        
        //System.out.println("celestiaTime =        " + celestiaTime);
        //System.out.println("convertedMissionTime =" + convertedMissionTime);
        //System.out.println("mjdMissionTime =" + mjdMissionTime);
        
        thrustVecECI = this.getNormVector(this.getVecDiff(scPositionECI, scPositionECI_prev));
        thrustMag = tVec[0];
        LOG.debug("thrustMag:  '{}' ", thrustMag);

        double scMass_inverted = 0.0;
        scMass = scMass0;
        if (scMass >= 0.0) {
            scMass_inverted = 1.0 / scMass;
        } else {
            LOG.error("S/C mass is negative({})!", scMass);
        }

        double[] totalAcc = new double[3];
        for (int i = 0; i < 3; i++) {
            if (thrustMag == 0.0) {
                totalAcc[i] = gravityAccel[i+1] * gravityAccel[0];
            } else {
                totalAcc[i] = gravityAccel[i+1] * gravityAccel[0] + thrustVecECI[i]
                        * thrustMag / scMass_inverted;
            }
            LOG.debug("totalAcc[i]:  '{}' ", totalAcc[i]);
        }
        double[] result = new double[3];

        for (int i = 0; i < 3; i++) {
            scVelocityECI_prev[i] = scVelocityECI[i];
        }
        
        /* Calculation of the new SC velocity. */
        result = integratorTimeStep(time, tStepSize, scVelocityECI, totalAcc);
        for (int i = 0; i < 3; i++) {
            scVelocityECI[i] = result[i];
        }

        /* Storage of old SC position. */
        for (int i = 0; i < 3; i++) {
            scPositionECI_prev[i] = scPositionECI[i];
        }

        /* Calculation of the new SC position. */
        result = integratorTimeStep(time, tStepSize, scPositionECI, scVelocityECI);
        for (int i = 0; i < 3; i++) {
            scPositionECI[i] = result[i];
        }
        
        this.eci2ecef(mjdMissionTime, 0.0, 15, scPositionECI, scVelocityECI);
        
        scVelocityX = scVelocityECI[0];
        scVelocityY = scVelocityECI[1];
        scVelocityZ = scVelocityECI[2];
        scPositionX = scPositionECI[0];
        scPositionY = scPositionECI[1];
        scPositionZ = scPositionECI[2];
        scPosX_ECEF = scPositionECEF[0];
        scPosY_ECEF = scPositionECEF[1];
        scPosZ_ECEF = scPositionECEF[2];
        scVelX_ECEF = scVelocityECEF[0];
        scVelY_ECEF = scVelocityECEF[1];
        scVelZ_ECEF = scVelocityECEF[2];
        scPositionLatLonAlt = getPositionEllipsoidLatLonAlt();
        scPosLat = scPositionLatLonAlt[0];
        scPosLon = scPositionLatLonAlt[1];
        scPosAlt = scPositionLatLonAlt[2];

        LOG.debug("scVelocityX:  '{}' ", scVelocityX);
        LOG.debug("scVelocityY:  '{}' ", scVelocityY);
        LOG.debug("scVelocityZ:  '{}' ", scVelocityZ);
        LOG.debug("scPositionX:  '{}' ", scPositionX);
        LOG.debug("scPositionY:  '{}' ", scPositionY);
        LOG.debug("scPositionZ:  '{}' ", scPositionZ);
        LOG.debug("scPosX_ECEF:  '{}' ", scPosX_ECEF);
        LOG.debug("scPosY_ECEF:  '{}' ", scPosY_ECEF);
        LOG.debug("scPosZ_ECEF:  '{}' ", scPosZ_ECEF);
        LOG.debug("scVelX_ECEF:  '{}' ", scVelX_ECEF);
        LOG.debug("scVelY_ECEF:  '{}' ", scVelY_ECEF);
        LOG.debug("scVelZ_ECEF:  '{}' ", scVelZ_ECEF);
        LOG.debug("scPosLat:  '{}' ", scPosLat);
        LOG.debug("scPosLon:  '{}' ", scPosLon);
        LOG.debug("scPosAlt:  '{}' ", scPosAlt);


        // Computing SC attitude: SC aligned tangentially to trajectory
        // =====================================================================
        // Store the position and velocity vectors in JAT-VectorN vector type
        // for easier calculations.
        for (int i = 0; i < 3; i++){
            dummyVectorN1.set(i, scPositionECI[i]);
            dummyVectorN2.set(i, scVelocityECI[i]);
       }
        // Normalizate the new vectors (not absolutely necessary for attitude
        // calculations only).
        dummyVectorN1.unitize();
        dummyVectorN2.unitize();

        // Ivans corrected coordinate system:
        // Set X body axis along the unit vector in the velocity direction.
        // -> flight direction.
        xBodyFrameAxisInECI = dummyVectorN2;

        // Set Y body axis along the cross product of the X body axis unit
        // vector and the unit vector along the position vector. This will only
        // work when both vectors are NOT collinear. Otherwise the old y-Axis
        // can be used (not implemented).
        yBodyFrameAxisInECI = 
                 (xBodyFrameAxisInECI.crossProduct(dummyVectorN1)).unitVector();
        
        // Set Z body axis along the cross product of the X body axis and the
        // Y body axis. -> For S/C orbiting a planet Z is nadir pointing.
        zBodyFrameAxisInECI =
           (xBodyFrameAxisInECI.crossProduct(yBodyFrameAxisInECI)).unitVector();

        
        // Build the transformation matrix from the ECI to body frame according
        // to the following consideration:
        //        a  - vector in a primary frame (e.g. ECI) with coordinate
        //             axes: e1, e2, e3 (vectors)
        //        a' - same vector in a secondary (') frame (e.g. ECEF) with
        //             coordinate axes: e1', e2', e3' (vectors)
        // The coordinate axes of the secondary (') frame have the following
        // representation in the primary frame:
        //        e1' = x1'*e1+x2'*e2+x3'*e3  (1)
        //        e2' = y1'*e1+y2'*e2+y3'*e3  (2)
        //        e3' = z1'*e1+z2'*e2+z3'*e3  (3)
        // The vector a has the following representation in the Body frame:
        //        a'  = a1'*e1'+a2'*e2'+a3'*e3'
        // Substituting back (1),(2) and (3) in this expression yields
        //        a'  = a1'*(x1'*e1+x2'*e2+x3'*e3)+
        //              a2'*(y1'*e1+y2'*e2+y3'*e3)+
        //              a3'*(z1'*e1+z2'*e2+z3'*e3)
        //            = (a1'*x1'+a2'*y1'+a3'*z1')*e1+
        //              (a1'*x2'+a2'*y2'+a3'*z2')*e2+
        //              (a1'*x3'+a2'*y3'+a3'*z3')*e3
        //            = a = a1*e1+a2*e2+a3*e3
        //
        // Comparing the vectors on both sides of the equation componentwise:
        //                       e1' e2' e3'
        //              |a1|   | x1' y1' z1' |   |a1'|
        //        a =   |a2| = | x2' y2' z2' | * |a2'| = |e1' e2' e3'| * a'
        //              |a3|   | x3' y3' z3' |   |a3'|
        //      (Body)
        //        a' = inv(|e1' e2' e3'|) * a = transpose(|e1' e2' e3'|) * a =
        //      
        //                              |transpose(e1')| (ECI)
        //           =  |transpose(e2')| * a
        //              |transpose(e3')|
        //        
        // Build the direction cosine matrix from the ECI to body frame:
        //
        //        DCM_ECI2BODY.setColumn(0, xBodyFrameAxisInECI);
        //        DCM_ECI2BODY.setColumn(1, yBodyFrameAxisInECI);
        //        DCM_ECI2BODY.setColumn(2, zBodyFrameAxisInECI);

        DCM_ECI2BODY.setColumn(0, xBodyFrameAxisInECI);
        DCM_ECI2BODY.setColumn(1, yBodyFrameAxisInECI);
        DCM_ECI2BODY.setColumn(2, zBodyFrameAxisInECI);

        // Prints the direction cosine matrix on the console.
        //DCM_ECI2BODY.print();
        
        // Transforms the direction cosine matrix into the corresponding
        // quaternion ....not yet Celestia quaternion format.
        quaternionRelECI = new Quaternion(DCM_ECI2BODY);
        // Prints the quaternion on the console.
        //quaternionRelECI.print();
        
        
        
        /*
        This section is still buggy as it is not recoded to use the new eci2ecef method.
        It still is based on the old JAT conversion which leads to wrong results.
        
        
        //Get the ECI-to-ECEF transformation matrix.
        convertedMissionTime.update(
                timeHandler.getSimulatedMissionTimeAsDouble());
        eci2ECEFMatrix = earthReference.eci2ecef(convertedMissionTime);

        
        DCM_ECEF2BODY.setColumn(0,eci2ECEFMatrix.times(xBodyFrameAxisInECI));
        DCM_ECEF2BODY.setColumn(1,eci2ECEFMatrix.times(yBodyFrameAxisInECI));
        DCM_ECEF2BODY.setColumn(2,eci2ECEFMatrix.times(zBodyFrameAxisInECI));

        
        // Transforms the direction cosine matrix ECEF2BODY into the 
        // corresponding quaternion.
        quaternionRelECEF = new Quaternion(DCM_ECEF2BODY);
        // Prints the quaternion on the console.
        //quaternionRelECEF.print();
        //quaternion check (magnutude must be = 1.0)
        // System.out.println("quat. rel. to ECEF: " + quaternionRelECEF.mag());
         
        */

        
       // Decomposition of the quaternion for further use  (ECI here).
        for (int i = 0; i < 4; i++) {
                quaternionInComponent[i] = quaternionRelECI.get(i);   
        }
        
        /*
        This section is still buggy as it is not recoded to use the new eci2ecef method.
        It still is based on the old JAT conversion which leads to wrong results.
        
        //In the following lines alternatively ECI quaternions are
        //provided for tests or ECEF.
        //Just comment out what you don't need.
        
        // Decomposition of the quaternion for further use.
        //for (int i = 0; i < 4; i++) {
        //        quaternionInComponent[i] = quaternionRelECEF.get(i);   
        //}
        
        */
        
        
        // Generating the quaternion output for Celestia 
        // =====================================================================
        ScData scData = (new ScData.Builder())
        		.withTime(celestiaTime)
        		.withPosition(scPositionECI)
        		.withQuaternion(quaternionInComponent)
        		.build();
        
        dataEvent.fire(scData);

        return 0;
    }



    /**
     * Calculates and returns the left hand side of the model's ODE.
     */
    public double[] getValue(final double time, final double[] x,
            final double[] inp) {
        double[] result = new double[3];

        result[0] = inp[0];
        result[1] = inp[1];
        result[2] = inp[2];

        return result;
    }



    /**
    * Runge Kutta Integrator for the model's ODE.
    */
    public double[] integratorTimeStep(final double t, final double dt,
            final double[] state, final double[] input) {
        int i;
        int resultSize = state.length;
        double[] result = new double[resultSize];
        // temporary
        double[] k1 = new double[resultSize];
        double[] k2 = new double[resultSize];
        double[] k3 = new double[resultSize];
        double[] k4 = new double[resultSize];
        ////double a1= 1./6., a2 = 1./3., a3 = 1./3., a4= 1./6.;

        k1 = getValue(t, state, input);
        for (i = 0; i < resultSize; i++) {
            result[i] = state[i] + k1[i] * dt / 2.;
        }

        k2 = getValue(t + dt / 2., result, input);
        for (i = 0; i < resultSize; i++) {
            result[i] = state[i] + k2[i] * dt / 2.;
        }

        k3 = getValue(t + dt / 2., result, input);
        for (i = 0; i < resultSize; i++) {
            result[i] = state[i] + k3[i] * dt;
        }

        k4 = getValue(t + dt, result, input);
        for (i = 0; i < resultSize; i++) {
            result[i] = state[i] + 1. / 6. * dt
                    * (k1[i] + 2. * k2[i] + 2. * k3[i] + k4[i]);
        }

        return result;
    }



    /**
     * Returns the norm vector of a vector.
     * @param vector
     * @return
     */
    public double[] getNormVector(final double[] vector) {
        double norm = Math.sqrt(getScalarProduct(vector, vector));
        double[] result = new double[3];

        if (norm <= 0.0000001){
            for(int i=0; i<3; i++){
                result[i] = 0.0;
            }
        } else {
            for (int i = 0; i < 3; i++) {
                result[i] = vector[i] / norm;
            }
        }
        return result;
    }



    /**
     * Returns the scalar product of two vectors.
     * @param vector_a
     * @param vector_b
     * @return
     */
    public double getScalarProduct(final double[] vector_a,
        final double[] vector_b) {
        double tmp = 0.0;
        for (int i = 0; i < 3; i++) {
            tmp = tmp + vector_a[i] * vector_b[i];
        }
        return tmp;
    }



    /**
     * Calculation of Latitude, Longitude, Altitude based on a spheric Earth. 
     * 
     * @author I. Kossev
     * @return S/C position as Latitude, Longitude, Altitude.
     */
    public double[] getPositionSphereLatLonAlt() {
        double[] result = new double[3];
        final double RAD2GRAD = 57.295779513082320876798154814105; //=180/PI

        /* Latitude. phi = atan(z/(x^2+y^2)^0.5). atan() returns an angle between 
         * -pi/2 and pi/2. */
        result[0] = Math.atan(scPositionECEF[2]/
                    Math.sqrt(Math.pow(scPositionECEF[0],2.0) + 
                    Math.pow(scPositionECEF[1],2.0))) * RAD2GRAD;

        /* Longitude. lambda = atan2(y,x). atan2 returns an angle between -pi 
         * and pi. This agrees with the definition of the Longitude: 
         * east -> positive, west -> negative.
         */
        result[1] = Math.atan2(scPositionECEF[1], scPositionECEF[0]) * RAD2GRAD;

        /* Altitude.  h = (x^2+y^2+z^2)^0.5 */
        result[2] = Math.sqrt(Math.pow(scPositionECEF[0],2.0) 
                    + Math.pow(scPositionECEF[1],2.0)
                    + Math.pow(scPositionECEF[2],2.0))
                    - jat.cm.Constants.R_Earth;

        return result;
    }



    /**
     * Calculation of Latitude, Longitude, Altitude based on ellipsoidal Earth. 
     * This algorithm requires no iterations and provides an approximation of
     * the Latitude, Longitude and Altitude above a ellipsoidal Earth. This
     * algorithm is given in (in German):
     *
     * W. Fichter, W. Grimm: Flugmechanik, 2009, p. 14-15.
     * 
     * @author I. Kossev
     * @return S/C position as Latitude, Longitude, Altitude.
     */
    public double[] getPositionEllipsoidLatLonAlt() {
        double[] result = new double[3];
        final double A = 6378137.0; /* semi-major axis (WGS84 GPS) */
        final double e = 0.08181919; /* eccentricity */

        double b, p, theta, e_prim_quadr, Re;

        // Calculation of auxiliary quantities: 
        b = A*Math.sqrt(1.0-Math.pow(e, 2.0));
        e_prim_quadr = (Math.pow(A,2.0)-Math.pow(b, 2.0))/Math.pow(b, 2.0);
        p = Math.sqrt(Math.pow(scPositionECEF[0],2.0)
                              + Math.pow(scPositionECEF[1],2.0));
        theta = Math.atan(scPositionECEF[2]*A/(p*b));
        // Calculation of the latitude:
        result[0] = Math.atan((scPositionECEF[2]+e_prim_quadr*b
                    *Math.pow(Math.sin(theta),3.0))/
                    (p-Math.pow(e, 2.0)*A
                    *Math.pow(Math.cos(theta),3.0)));
        // auxiliary quantity
        Re=A/Math.sqrt(1.0-Math.pow(e, 2.0)*Math.pow(Math.sin(result[0]),2.0));
        // Calculation of the longitude:
        result[1] = Math.atan2(scPositionECEF[1], scPositionECEF[0]);
        // Calculation of the altitude:
        result[2] = p/Math.cos(result[0]) - Re;

        return result;
    }



    /**
     * Calculation of Latitude, Longitude, Altitude based on ellipsoidal Earth.
     * This is an iteration algorithm given at (in German):
     *
     * http://ivvgeo.uni-muenster.de/Vorlesung/GPS_Script/messung_transformation.html 
     *
     *
     * @author Jonas?, Helmut Koch
     * @return S/C position as Latitude, Longitude, Altitude.
     *
     */
    public double[] getPositionEllipsoidLatLonAltIterative() {
        /*Iterative calculation of geographic position data*/
        double[] result = new double[3];
        double[] tempResult = new double[3];
        final double A = 6378137.0;  //semi-major axis (WGS84 GPS)
        final double b = 6356752.31424; //semi-minor axis (WGS84 GPS)
        double e1; //1. numeric eccentricity
        double N; //transverse warping radius (Querkruemmungsradius)
        double x = scPositionECEF[0];
        double y = scPositionECEF[1];
        double z = scPositionECEF[2]; 
        double lat;
        final double eps_precision = 0.00001;
        final double RAD2GRAD = 57.295779513082320876798154814105; //=180/PI

        e1 = Math.sqrt((Math.pow(A, 2.0)-Math.pow(b, 2.0))/Math.pow(A, 2.0));

        tempResult[0] = Math.atan(z/(Math.sqrt(Math.pow(x, 2.0) + 
                        Math.pow(y, 2.0))));
        N = A/Math.sqrt(1 - Math.pow(e1, 2.0) * 
            Math.pow(Math.sin(tempResult[0]), 2.0));

        tempResult[1] = Math.atan2(y, x); //Longitude

        //Iteration for latitude value
        lat = getPositionSphereLatLonAlt()[0];
        while((lat-tempResult[0]) > eps_precision) {
            lat = tempResult[0];
            tempResult[0] = Math.atan2(z+Math.pow(e1, 2.0)*N*Math.sin(lat),
                            Math.sqrt(Math.pow(x, 2.0) +
                            Math.pow(y, 2.0)));
            N = A/Math.sqrt(1-Math.pow(e1, 2.0)
                * Math.pow(Math.sin(tempResult[0]), 2.0));
        }
        //Altitude
        tempResult[2] = (x/(Math.cos(tempResult[0]) * Math.cos(tempResult[1])));

        result[0] = tempResult[0] * RAD2GRAD;
        result[1] = tempResult[1] * RAD2GRAD;
        double eccentr_quadr = 1.0 - Math.pow(b/A, 2.0);
        result[2] = tempResult[2] - A/Math.sqrt(1.0-eccentr_quadr*
                    Math.pow(Math.sin(result[0]),2.0));

        return result;
    }



    public double[] getVecDiff(double[] vector2, double[] vector1) throws ArithmeticException {
        if (vector2.length != vector1.length) {
            throw new ArithmeticException("Dimensions do not fit.");
        }
        if ((vector1.length == 0) || (vector2.length == 0)) {
            throw new  ArithmeticException("Dimension is zero.");
        }
        double[] result = new double[vector2.length];
        for (int i = 0; i < 3; i++) {
            result[i] = vector2[i]-vector1[i];
        }
        return result;
    }



    /**
     * Conversion from ECI to ECEF.
     * Algorithm developed by O.Zeile in Simulink in the frame of the 
     * Small Satellite Programme of University of Stuttgart, Germany.
     * Algorithm use granted by courtesy of Institute of Space Systems.
     * Reimplementation in Java by M.Fritz.
     *
     * @author Michael Fritz
     * @return S/C ECEF position and velocity
     *
     */    
   
    // This method converts position and velocity from ECI to ECEF coordinate system
    public double eci2ecef(double JD2000,double timeDiffFrac,int timeDiffInt,double[] R_ECI,double[] V_ECI) {
               
        // This line converts the Julian Date from UTC to UT1
        // As the required Julian Date is not started at midnight but at noon, half a day from the committed one is substracted
        double JD2000_UT1 = JD2000 + timeDiffFrac/(60*60*24) - 0.5;
        System.out.println("JD2000_UT1: " + JD2000_UT1);
        
        // This line converts the Julian Date from UTC to TT
        // As the required Julian Date is not started at midnight but at noon, half a day from the committed one is substracted
        double JD2000_TT = JD2000 + (timeDiffInt + 32.194 + 19)/(60*60*24) - 0.5;
        
        // GMST 2000 is the rotation angle (in radian) between vernal equinox and x-axis of J2000
        double GMST2000=1.74476716333061;
        
        // This is the Earth rotational rate in radian per second
        double om_e=7.2921158553*Math.pow(10,(-5));
        
        
        // This is the rotation angle between vernal equinox and ECEF x-axis calculated via the Earth rotational rate
        double GMST=GMST2000+om_e*86400*(JD2000_UT1+0.5);
        System.out.println("GMST: " + GMST);
        
        // This is the transformation matrix for ECI to ECEF conversion without nutation and precession effects
        double THETA[][] = {{Math.cos(GMST), Math.sin(GMST), 0},{-Math.sin(GMST), Math.cos(GMST), 0}, {0, 0, 1}};
        
        /*
        System.out.println("THETA");
        for (int i=0; i<3; i++) {
            for (int j=0; j<3; j++) {
                System.out.println(THETA[i][j]);
            }
        }
        */
        
        // date conversion from days to centuries
        double T=JD2000_TT/36525; 
        
        // These three values are necessary to calculate the transformation matrix comprising precession effects
        // zeta
        double c=2306.2181*T+0.30188*Math.pow(T,2)+0.017998*Math.pow(T,3);
        // theta
        double t=2004.3109*T-0.42665*Math.pow(T,2)-0.041833*Math.pow(T,3);
        // z
        double z=c+0.79280*Math.pow(T,2)+0.000205*Math.pow(T,3);
        
        // conversion from deg/sec to rad/h
        c=c/3600*3.14159265/180;
        t=t/3600*3.14159265/180;
        z=z/3600*3.14159265/180;
        
        double p11=-Math.sin(z)*Math.sin(c)+Math.cos(z)*Math.cos(t)*Math.cos(c);
        double p21=Math.cos(z)*Math.sin(c)+Math.sin(z)*Math.cos(t)*Math.cos(c);
        double p31=Math.sin(t)*Math.cos(c);
        double p12=-Math.sin(z)*Math.cos(c)-Math.cos(z)*Math.cos(t)*Math.sin(c);
        double p22=Math.cos(z)*Math.cos(c)-Math.sin(z)*Math.cos(t)*Math.sin(c);
        double p32=-Math.sin(t)*Math.sin(c);
        double p13=-Math.cos(z)*Math.sin(t);
        double p23=-Math.sin(z)*Math.sin(t);
        double p33=Math.cos(t);
        
        // This is the transformation matrix comprising precession effects between J-2000 and simulated time 
        double[][] P = {{p11,p12,p13},{p21,p22,p23},{p31,p32,p33}};
        
        /*
        System.out.println("P");
        for (int i=0; i<3; i++) {
            for (int j=0; j<3; j++) {
                System.out.println(P[i][j]);
            }
        }
        */
        
        // This is longitude of the ascending node of the moon. It has an enormous impact on the Earth's nutation
        double Om=125*3600+2*60+40.28-(1934*3600+8*60+10.539)*T+7.455*Math.pow(T,2)+0.008*Math.pow(T,3);

        // conversion from deg/sec to rad/h
        Om=Om/3600*3.14159265/180;
        
        // periodical change of the position of the Earth's vernal equinox
        double dp=-17.2/3600*3.14159265/180*Math.sin(Om);
        
        // periodical change of the obliquity of the ecliptic
        double de=9.203/3600*3.14159265/180*Math.cos(Om);
        
        // obliquity of the ecliptic at J-2000
        double e=23.43929111*3.14159265/180;
        
        double n11=Math.cos(dp);
        double n21=Math.cos(e+de)*Math.sin(dp);
        double n31=Math.sin(e+de)*Math.sin(dp);
        double n12=-Math.cos(e)*Math.sin(dp);
        double n22=Math.cos(e)*Math.cos(e+de)*Math.cos(dp)+Math.sin(e)*Math.sin(e+de);
        double n32=Math.cos(e)*Math.sin(e+de)*Math.cos(dp)-Math.sin(e)*Math.cos(e+de);
        double n13=-Math.sin(e)*Math.sin(dp);
        double n23=Math.sin(e)*Math.cos(e+de)*Math.cos(dp)-Math.cos(e)*Math.sin(e+de);
        double n33=Math.sin(e)*Math.sin(e+de)*Math.cos(dp)+Math.cos(e)*Math.cos(e+de);
        
        //  This is the transformation matrix comprising nutation effects between J-2000 and simulated time 
        double N[][] = {{n11,n12,n13},{n21,n22,n23},{n31,n32,n33}};
        
        /*
        System.out.println("N");
        for (int i=0; i<3; i++) {
            for (int j=0; j<3; j++) {
                System.out.println(N[i][j]);
            }
        }
        */
        
        // This is the transformation comprising all above calculated effects
        double[][] U;
        U = matrixmult(THETA,N);
        U = matrixmult(U,P);
        
        /*
        System.out.println("U");
        for (int i=0; i<3; i++) {
            for (int j=0; j<3; j++) {
                System.out.println(U[i][j]);
            }
        }
        */

        // This is an auxiliary matrix in order to simplify the derivative of THETA
        double[][] MAT = {{0, 1, 0}, {-1, 0, 0}, {0, 0, 0}};
  
        // This again is the Earth's rotational rate which has to be multiplicated with THETA
        double help=7.2921158553*Math.pow(10,(-5));

        // THETA_dot is the derivative of THETA
        double[][] THETA_dot = matrixmult(MAT,THETA);
        for(int i=0; i<THETA_dot.length; i++) {
            for(int j=0; j<THETA_dot.length; j++) {
                THETA_dot[i][j] = THETA_dot[i][j]*help;
            }
        }

        // U_dot is the derivative of U
        double[][] U_dot = matrixmult(THETA_dot,N);
        U_dot = matrixmult(U_dot,P);

        // This is the conversion of ECI coordinates into the ECEF system
        scPositionECEF = matrvectmult(U, R_ECI);
        //System.out.println("scPositionECEF[0,1,2]:");
        //System.out.println(scPositionECEF[0]);
        //System.out.println(scPositionECEF[1]);
        //System.out.println(scPositionECEF[2]);
        double[] aux01 = matrvectmult(U, V_ECI);
        double[] aux02 = matrvectmult(U_dot, R_ECI);

        // This is the calculation of the derivative (=the velocity), applying the product rule
        for (int i=0; i<3; i++) {
            scVelocityECEF[i] = aux01[i] + aux02[i];
        }
        return 0;
    }
    
    // The following method allows to multiply two 3x3 matrices
    public double[][] matrixmult(double[][] matrix01, double[][] matrix02) {
        double[][] resmatrix = {{0,0,0},{0,0,0},{0,0,0}};
        for(int i=0; i<matrix01.length; i++) {
            for(int j=0; j<matrix01.length; j++) {
                resmatrix[i][j] = matrix01[i][0]*matrix02[0][j] + matrix01[i][1]*matrix02[1][j] + matrix01[i][2]*matrix02[2][j];
            }
        }
        return resmatrix;
    }
    
    public double[] matrvectmult(double[][] matrix, double[] vector) {
        double[] resvector = {0,0,0};
        for(int i=0; i<matrix.length; i++) {
            resvector[i] = matrix[i][0] * vector[0] + matrix[i][1] * vector[1] + matrix[i][2] * vector[2];
        }
        return resvector;
    }
}