package org.osk.events;

public class ECEFpv {

    /** Structure SCVelocity in ECEF frame. */
    private double[] scVelocityECEF; // = new double[3];
    /** Structure SCPosition in ECEF frame. */
    private double[] scPositionECEF; // = new double[3];


    public ECEFpv(final double[] scVelocityECEF, final double[] scPositionECEF) {
		this.scVelocityECEF = scVelocityECEF;
		this.scPositionECEF = scPositionECEF;
	}

    public double getAltitude() {
    	return scPositionECEF[2];
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



}
