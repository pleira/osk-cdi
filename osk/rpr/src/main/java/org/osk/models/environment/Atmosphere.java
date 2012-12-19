package org.osk.models.environment;

import org.osk.errors.OskException;

public interface Atmosphere {

	double getAirPressure(double alt) throws OskException;

}
