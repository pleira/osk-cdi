package org.opensimkit.models.astris.parts;

import javax.inject.Inject;
import javax.inject.Named;

import org.apache.deltaspike.core.api.config.annotation.ConfigProperty;
import org.opensimkit.config.NumberConfig;
import org.opensimkit.config.Util;
import org.opensimkit.models.rocketpropulsion.PipeT1;
import org.opensimkit.ports.PureGasPort;


public class Pipe05 extends PipeT1 {
		
	@Inject
	public Pipe05(@Named("04_PureGasDat") PureGasPort inputPort,
			@Named("05_PureGasDat") PureGasPort outputPort) {
		super("Pipe05", inputPort, outputPort);
	}
	
	@Inject
	void initLength(@NumberConfig(name = "pipe5.length", defaultValue = "2.5") Double value) {
	setLength(value);
	}
	
	@Inject
	void initSpecificMass(@NumberConfig(name = "pipe5.specificMass", defaultValue = "0.6") Double value) {
	setSpecificMass(value);
	}
	
	@Inject
	void initInnerDiameter(@NumberConfig(name = "pipe5.innerDiameter", defaultValue = "0.0085") Double value) {
	setInnerDiameter(value);
	}
	
	@Inject
	void initSpecificHeatCapacity(@NumberConfig(name = "pipe5.specificHeatCapacity", defaultValue = "500.0") Double value) {
	setSpecificHeatCapacity(value);
	}
	
	@Inject
	void initSurfaceRoughness(@NumberConfig(name = "pipe5.surfaceRoughness", defaultValue = "1.E-6") Double value) {
	setSurfaceRoughness(value);
	}
	
	@Inject
	void initTemperatures(@ConfigProperty(name = "pipe5.temperatures", 
			defaultValue = "300.0 300.0 300.0 300.0 300.0 300.0 300.0 300.0 300.0 300.0") String values) {
	setTemperatures(Util.extractDoubleArray(values));
	}
}
