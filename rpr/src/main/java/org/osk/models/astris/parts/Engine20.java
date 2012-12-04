package org.opensimkit.models.astris.parts;

import javax.inject.Inject;
import javax.inject.Named;

import org.opensimkit.config.NumberConfig;
import org.opensimkit.models.rocketpropulsion.Engine;
import org.opensimkit.ports.PureLiquidPort;

public class Engine20 extends Engine {

	@Inject
	public Engine20(@Named("21_PureLiquidDat") PureLiquidPort inputPortOxidizer,
			@Named("19_PureLiquidDat") PureLiquidPort inputPortFuel) {
		super("20_Engine", inputPortOxidizer, inputPortFuel);
	}
	
	@Inject
	void initIngnitionFuelFlow(@NumberConfig(name = "engine20.ignitionFuelFlow", defaultValue = "0.0") Double value) {
		setIgnitionFuelFlow(value);
	}
	@Inject
	void initIngnitionOxidizerFlow(@NumberConfig(name = "engine20.ignitionOxidizerFlow", defaultValue = "0.0") Double value) {
		setIgnitionOxidizerFlow(value);
	}
	@Inject
	void initAlt(@NumberConfig(name = "engine20.alt", defaultValue = "600000") Double value) {
		setAlt(value);
	}
}
