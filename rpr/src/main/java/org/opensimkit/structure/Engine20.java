package org.opensimkit.structure;

import javax.inject.Inject;
import javax.inject.Named;

import org.opensimkit.config.NumberConfig;
import org.opensimkit.models.ports.PureLiquidPort;
import org.opensimkit.models.rocketpropulsion.Engine;

public class Engine20 extends Engine {

	public Engine20(@Named("21_PureLiquidDat") PureLiquidPort inputPortOxidizer,
			@Named("19_PureLiquidDat") PureLiquidPort inputPortFuel) {
		super("20_Engine", inputPortOxidizer, inputPortFuel);
	}
	
	@Inject
	void initIngnitionFuelFlow(@NumberConfig(name = "engine20.ingnitionFuelFlow", defaultValue = "0.0") Double value) {
		setIngnitionFuelFlow(value);
	}
	@Inject
	void initIngnitionOxidizerFlow(@NumberConfig(name = "engine20.ingnitionOxidizerFlow", defaultValue = "0.0") Double value) {
		setIngnitionOxidizerFlow(value);
	}
	@Inject
	void initAlt(@NumberConfig(name = "engine20.atl", defaultValue = "600000") Double value) {
		setAlt(value);
	}

}
