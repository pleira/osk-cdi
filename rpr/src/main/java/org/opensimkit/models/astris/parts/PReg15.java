package org.opensimkit.models.astris.parts;

import javax.inject.Inject;
import javax.inject.Named;

import org.apache.deltaspike.core.api.config.annotation.ConfigProperty;
import org.opensimkit.config.NumberConfig;
import org.opensimkit.config.Util;
import org.opensimkit.models.rocketpropulsion.PRegT1;
import org.opensimkit.ports.PureGasPort;


public class PReg15 extends PRegT1 {
		
	@Inject
	public PReg15(@Named("15_PureGasDat") PureGasPort inputPort,
			@Named("16_PureGasDat") PureGasPort outputPort) {
		super("PReg15", inputPort, outputPort);
	}

	@Inject
	void initLength(@NumberConfig(name = "preg15.length", defaultValue = "0.1") Double value) {
	setLength(value);
	}
	
	@Inject
	void initMass(@NumberConfig(name = "preg15.mass", defaultValue = "2.6") Double value) {
	setMass(value);
	}
	
	@Inject
	void initInnerDiameter(@NumberConfig(name = "preg15.innerDiameter", defaultValue = "0.014") Double value) {
	setInnerDiameter(value);
	}
	
	@Inject
	void initSpecificHeatCapacity(@NumberConfig(name = "preg15.specificHeatCapacity", defaultValue = "900.0") Double value) {
	setSpecificHeatCapacity(value);
	}

	@Inject
	void initPcoeff(@ConfigProperty(name = "preg15.pcoeff", 
	defaultValue = "17.70 0.0 0.0 0.0") String values) {
	setPcoeff(Util.extractDoubleArray(values));
	}
		
	@Inject
	void initTemperature(@NumberConfig(name = "preg15.temperature", defaultValue = "300.0") Double value) {
	setTemperature(value);
	}

}
