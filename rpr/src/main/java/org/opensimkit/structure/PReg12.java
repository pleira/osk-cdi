package org.opensimkit.structure;

import javax.inject.Inject;
import javax.inject.Named;

import org.opensimkit.models.ports.PureGasPort;
import org.opensimkit.models.rocketpropulsion.PRegT1;


public class PReg12 extends PRegT1 {
		
	@Inject
	public PReg12(@Named("11_PureGasDat") PureGasPort inputPort,
			@Named("12_PureGasDat") PureGasPort outputPort) {
		super("PReg12", inputPort, outputPort);
	}
}
