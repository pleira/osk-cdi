package org.opensimkit.models.astris.parts;

import javax.inject.Inject;
import javax.inject.Named;

import org.apache.deltaspike.core.api.config.annotation.ConfigProperty;
import org.opensimkit.config.NumberConfig;
import org.opensimkit.config.Util;
import org.opensimkit.models.rocketpropulsion.PipeT1;
import org.opensimkit.ports.PureGasPort;


public class Pipe03 extends PipeT1 {
		
	@Inject
	public Pipe03(@Named("01_PureGasDat") PureGasPort inputPort,
			@Named("03_PureGasDat") PureGasPort outputPort) {
		super("Pipe03", inputPort, outputPort);
	}
	
	@Inject
	void initLength(@NumberConfig(name = "pipe3.length", defaultValue = "1.5") Double value) {
	setLength(value);
	}
	
	@Inject
	void initSpecificMass(@NumberConfig(name = "pipe3.specificMass", defaultValue = "0.6") Double value) {
	setSpecificMass(value);
	}
	
	@Inject
	void initInnerDiameter(@NumberConfig(name = "pipe3.innerDiameter", defaultValue = "0.0085") Double value) {
	setInnerDiameter(value);
	}
	
	@Inject
	void initSpecificHeatCapacity(@NumberConfig(name = "pipe3.specificHeatCapacity", defaultValue = "5ß0.0") Double value) {
	setSpecificHeatCapacity(value);
	}
	
	@Inject
	void initSurfaceRoughness(@NumberConfig(name = "pipe3.surfaceRoughness", defaultValue = "0.0085") Double value) {
	setSurfaceRoughness(value);
	}
	
	@Inject
	void initTemperatures(@ConfigProperty(name = "pipe3.temperatures", 
			defaultValue = "300.0 300.0 300.0 300.0 300.0 300.0 300.0 300.0 300.0 300.0") String values) {
		setTemperatures(Util.extractDoubleArray(values));
	}
}
