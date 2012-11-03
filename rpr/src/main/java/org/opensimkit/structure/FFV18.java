package org.opensimkit.structure;

import javax.inject.Inject;
import javax.inject.Named;

import org.opensimkit.config.NumberConfig;
import org.opensimkit.models.ports.AnalogPort;
import org.opensimkit.models.ports.PureLiquidPort;
import org.opensimkit.models.rocketpropulsion.FluidFlowValve;


public class FFV18 extends FluidFlowValve {
		
	@Inject
	public FFV18(@Named("18_PureLiquidDat") PureLiquidPort inputPort,
			@Named("19_PureLiquidDat") PureLiquidPort outputPort, 
			@Named("23_Fuel_Flow_Control_Signal") AnalogPort controlPort) {
		super("18_FluidFlowValve", inputPort, outputPort, controlPort);
	}
	@Inject
	void initReferencePressureLoss(@NumberConfig(name = "ffv18.referencePressureLoss", defaultValue = "1.0") Double value) {
		setReferencePressureLoss(value);
	}
	@Inject
	void initReferenceMassFlow(@NumberConfig(name = "ffv18.referenceMassFlow", defaultValue = "10.0") Double value) {
		setReferenceMassFlow(value);
	}	
}
