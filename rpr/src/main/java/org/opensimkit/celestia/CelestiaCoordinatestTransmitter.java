package org.opensimkit.celestia;

import jat.matvec.data.Matrix;
import jat.matvec.data.Quaternion;
import jat.matvec.data.VectorN;

import javax.decorator.Decorator;
import javax.decorator.Delegate;
import javax.inject.Inject;
import javax.interceptor.AroundInvoke;
import javax.interceptor.InvocationContext;

import org.opensimkit.Model;
import org.opensimkit.SimVisThread;
import org.opensimkit.TimeHandler;
import org.opensimkit.manipulation.Manipulatable;
import org.opensimkit.models.structure.ScStructure;
import org.opensimkit.steps.CRegulStep;
import org.opensimkit.steps.CTimeStep;

/**
 * the real-time simulator realizes its connection to the network and
 * the transmission of the required data to Celestia. 
 * It is created in a way to allow the activation of the
 * visualization at any time during a simulation by setting
 * a specific parameter. After doing so, the created model
 * collects time, orbit and attitude data from the
 * environment model and puts them into an array which is
 * written periodically to a network socket
 * 
 * It is computed as the
 * time data + orbit data + attitude data 
 * in Celestiabarycentric-time-format.
 * 
 * This can slow the simulation.
 * 
 * @author P. Pita
 *
 */
@Decorator
public class CelestiaCoordinatestTransmitter implements Model {

	private @Inject @Delegate ScStructure scStructure;

    //  Entries for Celestia interface variables.
    @Manipulatable private String celestiaTime;
    @Manipulatable private double xPosition;
    @Manipulatable private double yPosition;
    @Manipulatable private double zPosition;
    @Manipulatable private double wQuat;
    @Manipulatable private double xQuat;
    @Manipulatable private double yQuat;
    @Manipulatable private double zQuat;
    /** Direct cosine matrix ECI to Body as JAT matrix. */
    private Matrix DCM_ECI2BODY = new Matrix(3, 3);
    /** Diverse JAT vector variables. */
    private VectorN    xBodyFrameAxisInECI = new VectorN(3), 
                       yBodyFrameAxisInECI = new VectorN(3),
                       zBodyFrameAxisInECI = new VectorN(3), 
                       dummyVectorN1 = new VectorN(3), 
                       dummyVectorN2 = new VectorN(3);
    /** Direction vector (unitized) of engine thrust in ECI frame. */
    private double[] thrustVecECI = new double[3];

    
    /** Time handler object. */
    private final TimeHandler timeHandler;
    /** Attitude in ECI as JAT quaternion. */
    private Quaternion quaternionRelECI = new Quaternion();
    /** Attitude quaternion ECI as normal Java vector. */
    private double[]   quaternionInComponent = {0.0, 0.0, 0.0, 0.0};

    //  Entries for simVisThread.
    @Manipulatable private int visSocketNumber = 1520;
    private static SimVisThread visThread;
  
	@Override
    public int timeStep(final double time, final double tStepSize) {

		//Computes the UTC J2000 time in Celestia compatible string format
	    celestiaTime = timeHandler.getCelestiaUTCJulian2000();
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

        // Prints the direction cosine matrix on the console.
        //DCM_ECI2BODY.print();
        
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

        DCM_ECI2BODY.setColumn(0, xBodyFrameAxisInECI);
	    DCM_ECI2BODY.setColumn(1, yBodyFrameAxisInECI);
	    DCM_ECI2BODY.setColumn(2, zBodyFrameAxisInECI);

	    // Transforms the direction cosine matrix into the corresponding
        // quaternion ....not yet Celestia quaternion format.
        quaternionRelECI = new Quaternion(DCM_ECI2BODY);
        // Decomposition of the quaternion for further use  (ECI here).
        for (int i = 0; i < 4; i++) {
                quaternionInComponent[i] = quaternionRelECI.get(i);   
        }
    
    // Generating the quaternion output for Celestia 
    // =====================================================================
    // Celestia needs the position vector in [km], not in [m]. 
    xPosition = scPositionX / 1000.0;
    yPosition = scPositionY / 1000.0;
    zPosition = scPositionZ / 1000.0;
    
    wQuat = quaternionInComponent[3];
    xQuat = quaternionInComponent[0];
    yQuat = quaternionInComponent[1];
    zQuat = quaternionInComponent[2];

    // fixme
    return 0;
}

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getType() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void init() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public int iterationStep() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int backIterStep() {
		// TODO Auto-generated method stub
		return 0;
	}
    
}
