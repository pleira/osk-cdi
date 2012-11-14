package org.opensimkit.models.astris;

import javax.inject.Inject;

import org.apache.deltaspike.core.api.config.annotation.ConfigProperty;
import org.opensimkit.config.NumberConfig;
import org.opensimkit.config.Util;
import org.opensimkit.models.structure.ScStructure;

public class ScStructure22 extends ScStructure {

	public ScStructure22() {
		super("22_ASTRIS_Rorket_Structure");
	}

	@Inject
	void initScPositionECI(@ConfigProperty(name = "sc.scPositionECI", defaultValue = "6978137.0 0.0 0.0") String values) {
	setScPositionECI(Util.extractDoubleArray(values));
	}
	
	@Inject
	void initScVelocityECI(@ConfigProperty(name = "sc.scVelocityECI", defaultValue = "0.0 2700.0 7058.0") String values) {
	setScVelocityECI(Util.extractDoubleArray(values));
	}
		
	@Inject
	void initScMass(@NumberConfig(name = "sc.scMass", defaultValue = "1000.0") Double value) {
	setScMass(value);
	}
}
