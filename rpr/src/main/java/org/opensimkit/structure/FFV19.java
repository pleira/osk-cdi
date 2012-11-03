package org.opensimkit.structure;

import javax.inject.Inject;
import javax.inject.Named;

import org.opensimkit.config.NumberConfig;
import org.opensimkit.models.ports.AnalogPort;
import org.opensimkit.models.ports.PureLiquidPort;
import org.opensimkit.models.rocketpropulsion.FluidFlowValve;


public class FFV19 extends FluidFlowValve {
		
	@Inject
	public FFV19(@Named("20_PureLiquidDat") PureLiquidPort inputPort,
			@Named("21_PureLiquidDat") PureLiquidPort outputPort, 
			@Named("24_Ox_Flow_Control_Signal") AnalogPort controlPort) {
		super("19_FluidFlowValve", inputPort, outputPort, controlPort);
	}
	@Inject
	void initReferencePressureLoss(@NumberConfig(name = "ffv19.referencePressureLoss", defaultValue = "1.0") Double value) {
		setReferencePressureLoss(value);
	}
	@Inject
	void initReferenceMassFlow(@NumberConfig(name = "ffv19.referenceMassFlow", defaultValue = "10.0") Double value) {
		setReferenceMassFlow(value);
	}	
}
