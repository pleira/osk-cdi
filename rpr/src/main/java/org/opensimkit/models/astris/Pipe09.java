package org.opensimkit.models.astris;

import javax.inject.Inject;
import javax.inject.Named;

import org.opensimkit.models.rocketpropulsion.PipeT1;
import org.opensimkit.ports.PureGasPort;


public class Pipe09 extends PipeT1 {
		
	@Inject
	public Pipe09(@Named("08_PureGasDat") PureGasPort inputPort,
			@Named("09_PureGasDat") PureGasPort outputPort) {
		super("Pipe09", inputPort, outputPort);
	}
}
