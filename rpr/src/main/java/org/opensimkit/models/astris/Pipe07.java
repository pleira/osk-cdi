package org.opensimkit.models.astris;

import javax.inject.Inject;
import javax.inject.Named;

import org.opensimkit.models.rocketpropulsion.PipeT1;
import org.opensimkit.ports.PureGasPort;


public class Pipe07 extends PipeT1 {
		
	@Inject
	public Pipe07(@Named("06_PureGasDat") PureGasPort inputPort,
			@Named("07_PureGasDat") PureGasPort outputPort) {
		super("Pipe07", inputPort, outputPort);
	}
}
