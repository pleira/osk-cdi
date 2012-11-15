package org.opensimkit.frames;

import org.opensimkit.events.ECEFpv;

public class ECEFBuilder {
	

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
    public ECEFpv eci2ecef(double JD2000,double timeDiffFrac,int timeDiffInt,double[] R_ECI,double[] V_ECI) {
               
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
        double[] scPositionECEF = matrvectmult(U, R_ECI);
        //System.out.println("scPositionECEF[0,1,2]:");
        //System.out.println(scPositionECEF[0]);
        //System.out.println(scPositionECEF[1]);
        //System.out.println(scPositionECEF[2]);
        double[] aux01 = matrvectmult(U, V_ECI);
        double[] aux02 = matrvectmult(U_dot, R_ECI);

        double[] scVelocityECEF = new double[3];
        // This is the calculation of the derivative (=the velocity), applying the product rule
        for (int i=0; i<3; i++) {
            scVelocityECEF[i] = aux01[i] + aux02[i];
        }
        return new ECEFpv(scPositionECEF, scVelocityECEF);
    }
    

    /**
     * Calculation of Latitude, Longitude, Altitude based on a spheric Earth. 
     * 
     * @author I. Kossev
     * @return S/C position as Latitude, Longitude, Altitude.
     */
    public double[] getPositionSphereLatLonAlt(final double[] scPositionECEF) {
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
    public double[] getPositionEllipsoidLatLonAlt(final double[] scPositionECEF) {
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
