package org.opensimkit.models.astris.parts;

import javax.inject.Inject;
import javax.inject.Named;

import org.apache.deltaspike.core.api.config.annotation.ConfigProperty;
import org.opensimkit.config.NumberConfig;
import org.opensimkit.models.rocketpropulsion.HPBottleT1;
import org.opensimkit.ports.PureGasPort;

public class HPBottle01 extends HPBottleT1 {

	@Inject
	public HPBottle01(@Named("01_PureGasDat") PureGasPort outputPort) {
		super("01_HPBottle", outputPort);
	}

	@Inject
	void initMass(@NumberConfig(name = "hpb1.mass", defaultValue = "28.0") Double value) {
	setMass(value);
	}
	@Inject
	void initVolume(@NumberConfig(name = "hpb1.volume", defaultValue = "0.135") Double value) {
	setVolume(value);
	}
	@Inject
	void initPtotal(@NumberConfig(name = "hpb1.ptotal", defaultValue = "280.0") Double value) {
	setPtotal(value);
	}
	@Inject
	void initTtotal(@NumberConfig(name = "hpb1.ttotal", defaultValue = "300.0") Double value) {
	setTtotal(value);
	}
	@Inject
	void initSpecificHeatCapacity(@NumberConfig(name = "hpb1.specificHeatCapacity", defaultValue = "800.0") Double value) {
	setSpecificHeatCapacity(value);
	}
	@Inject
	void initFluid(@ConfigProperty(name = "hpb1.fluid", defaultValue = "Helium") String value) {
	setFluid(value);
	}
}
