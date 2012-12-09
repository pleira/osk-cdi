package org.osk.models.environment.atmosphere;

import org.apache.commons.math3.exception.util.DummyLocalizable;
import org.osk.errors.OskException;
import org.osk.models.environment.Atmosphere;

/******************************************************************/
/*                                                                */
/*    Simple Earth atmosphere model for pressure and temperature  */
/*    http://www.grc.nasa.gov/WWW/K-12/airplane/atmosmet.html     */
/*                                                                */
/******************************************************************/
public class NasaSimpleEarthAtmosphere implements Atmosphere {
	
	@Override
	public double getAirPressure(double alt) throws OskException {
		double Ta, rhoa, pa;
		if ( alt <= 11000 ) {   
		    /* Troposphere */
		    Ta = 15.04 - 0.00649*alt;
		    pa = (101.29*(Math.pow((Ta+273.15)/288.08,5.256)))*1000;
		    rhoa = pa/(286.9*(Ta+273.15));

		} else if ( alt > 11000 && alt < 25000 ) {  
		    /* Lower Stratosphere */
		    Ta = -56.46;
		    pa = 22.56*(Math.exp(1.73 - 0.000157*alt))*1000;
		    rhoa = pa/(286.9*(Ta+273.15));
		                
		} else if ( alt >= 25000 ) {  
		    /* Upper Stratosphere */
		    Ta = -131.21 + 0.00299*alt;
		    pa = (2.488*(Math.pow((Ta+273.15)/216.6,-11.388)))*1000;
		    rhoa = pa/(286.9*(Ta+273.15));
		
		} else {
//		    LOG.error("% Engine: Negative altitude");
		    throw new OskException(new DummyLocalizable("% Engine: Negative altitude"));
		}
		return pa;
	}


}
