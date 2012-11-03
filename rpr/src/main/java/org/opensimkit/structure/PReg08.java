package org.opensimkit.structure;

import javax.inject.Inject;
import javax.inject.Named;

import org.opensimkit.models.ports.PureGasPort;
import org.opensimkit.models.rocketpropulsion.PRegT1;


public class PReg08 extends PRegT1 {
		
	@Inject
	public PReg08(@Named("07_PureGasDat") PureGasPort inputPort,
			@Named("08_PureGasDat") PureGasPort outputPort) {
		super("PReg08", inputPort, outputPort);
	}
}
