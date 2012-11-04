package org.opensimkit.structure;

import javax.inject.Inject;
import javax.inject.Named;

import org.opensimkit.models.rocketpropulsion.PipeT1;
import org.opensimkit.ports.PureGasPort;


public class Pipe11 extends PipeT1 {
		
	@Inject
	public Pipe11(@Named("10_PureGasDat") PureGasPort inputPort,
			@Named("11_PureGasDat") PureGasPort outputPort) {
		super("Pipe11", inputPort, outputPort);
	}
}
