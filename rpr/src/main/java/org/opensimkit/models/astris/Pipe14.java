package org.opensimkit.models.astris;

import javax.inject.Inject;
import javax.inject.Named;

import org.opensimkit.models.rocketpropulsion.PipeT1;
import org.opensimkit.ports.PureGasPort;


public class Pipe14 extends PipeT1 {
		
	@Inject
	public Pipe14(@Named("14_PureGasDat") PureGasPort inputPort,
			@Named("15_PureGasDat") PureGasPort outputPort) {
		super("Pipe14", inputPort, outputPort);
	}
}
