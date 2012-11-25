package org.opensimkit.models.astris.parts;

import javax.inject.Inject;
import javax.inject.Named;

import org.apache.deltaspike.core.api.config.annotation.ConfigProperty;
import org.opensimkit.config.NumberConfig;
import org.opensimkit.config.Util;
import org.opensimkit.models.rocketpropulsion.PRegT1;
import org.opensimkit.ports.PureGasPort;


public class PReg08 extends PRegT1 {
		
	@Inject
	public PReg08(@Named("07_PureGasDat") PureGasPort inputPort,
			@Named("08_PureGasDat") PureGasPort outputPort) {
		super("PReg08", inputPort, outputPort);
	}

	@Inject
	void initLength(@NumberConfig(name = "preg08.length", defaultValue = "0.1") Double value) {
	setLength(value);
	}
	
	@Inject
	void initMass(@NumberConfig(name = "preg08.mass", defaultValue = "2.6") Double value) {
	setMass(value);
	}
	
	@Inject
	void initInnerDiameter(@NumberConfig(name = "preg08.innerDiameter", defaultValue = "0.014") Double value) {
	setInnerDiameter(value);
	}
	
	@Inject
	void initSpecificHeatCapacity(@NumberConfig(name = "preg08.specificHeatCapacity", defaultValue = "900.0") Double value) {
	setSpecificHeatCapacity(value);
	}

	@Inject
	void initPcoeff(@ConfigProperty(name = "preg08.pcoeff", 
	defaultValue = "24.10245 .4462006 -1.84912E-3 2.580329E-6") String values) {
	setPcoeff(Util.extractDoubleArray(values));
	}
		
	@Inject
	void initTemperature(@NumberConfig(name = "preg08.temperature", defaultValue = "300.0") Double value) {
	setTemperature(value);
	}

}
