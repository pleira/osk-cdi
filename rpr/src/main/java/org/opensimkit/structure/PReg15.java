package org.opensimkit.structure;

import javax.inject.Inject;
import javax.inject.Named;

import org.opensimkit.models.rocketpropulsion.PRegT1;
import org.opensimkit.ports.PureGasPort;


public class PReg15 extends PRegT1 {
		
	@Inject
	public PReg15(@Named("15_PureGasDat") PureGasPort inputPort,
			@Named("16_PureGasDat") PureGasPort outputPort) {
		super("PReg15", inputPort, outputPort);
	}
}
