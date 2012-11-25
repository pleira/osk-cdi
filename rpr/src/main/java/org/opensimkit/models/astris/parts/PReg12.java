package org.opensimkit.models.astris.parts;

import javax.inject.Inject;
import javax.inject.Named;

import org.apache.deltaspike.core.api.config.annotation.ConfigProperty;
import org.opensimkit.config.NumberConfig;
import org.opensimkit.config.Util;
import org.opensimkit.models.rocketpropulsion.PRegT1;
import org.opensimkit.ports.PureGasPort;


public class PReg12 extends PRegT1 {
		
	@Inject
	public PReg12(@Named("11_PureGasDat") PureGasPort inputPort,
			@Named("12_PureGasDat") PureGasPort outputPort) {
		super("PReg12", inputPort, outputPort);
	}

	@Inject
	void initLength(@NumberConfig(name = "preg12.length", defaultValue = "0.1") Double value) {
	setLength(value);
	}
	
	@Inject
	void initMass(@NumberConfig(name = "preg12.mass", defaultValue = "2.6") Double value) {
	setMass(value);
	}
	
	@Inject
	void initInnerDiameter(@NumberConfig(name = "preg12.innerDiameter", defaultValue = "0.014") Double value) {
	setInnerDiameter(value);
	}
	
	@Inject
	void initSpecificHeatCapacity(@NumberConfig(name = "preg12.specificHeatCapacity", defaultValue = "900.0") Double value) {
	setSpecificHeatCapacity(value);
	}

	@Inject
	void initPcoeff(@ConfigProperty(name = "preg12.pcoeff", 
	defaultValue = "17.70 0.0 0.0 0.0") String values) {
	setPcoeff(Util.extractDoubleArray(values));
	}
		
	@Inject
	void initTemperature(@NumberConfig(name = "preg12.temperature", defaultValue = "300.0") Double value) {
	setTemperature(value);
	}

}
