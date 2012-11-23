package org.opensimkit.models.astris.parts;

import javax.inject.Inject;
import javax.inject.Named;

import org.opensimkit.models.rocketpropulsion.PRegT1;
import org.opensimkit.ports.PureGasPort;


public class PReg12 extends PRegT1 {
		
	@Inject
	public PReg12(@Named("11_PureGasDat") PureGasPort inputPort,
			@Named("12_PureGasDat") PureGasPort outputPort) {
		super("PReg12", inputPort, outputPort);
	}
}
