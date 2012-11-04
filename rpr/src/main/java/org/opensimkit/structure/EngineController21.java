package org.opensimkit.structure;

import javax.inject.Inject;
import javax.inject.Named;

import org.opensimkit.config.NumberConfig;
import org.opensimkit.models.rocketpropulsion.EngineController;
import org.opensimkit.ports.AnalogPort;

public class EngineController21 extends EngineController {
	public EngineController21(@Named("21_PureLiquidDat") AnalogPort controlPort1,
			@Named("19_PureLiquidDat") AnalogPort controlPort2) {
		super("21_EngineController", controlPort1, controlPort2);
	}
	
	@Inject
	void controlRangeMax(@NumberConfig(name = "econtroller21.controlRangeMax", defaultValue = "1.0") Double value) {
		setControlRangeMax(value);
	}
	@Inject
	void controlRangeMin(@NumberConfig(name = "econtroller21.controlRangeMin", defaultValue = "0.0") Double value) {
		setControlRangeMin(value);
	}
	@Inject
	void controlValue1Nom(@NumberConfig(name = "econtroller21.controlValue1Nom", defaultValue = "0.0") Double value) {
		setControlValue1Nom(value);
	}
	@Inject
	void controlValue2Nom(@NumberConfig(name = "econtroller21.controlValue2Nom", defaultValue = "0.0") Double value) {
		setControlValue2Nom(value);
	}
}
