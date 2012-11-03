package org.opensimkit.structure;

import javax.inject.Inject;
import javax.inject.Named;

import org.opensimkit.models.ports.PureGasPort;
import org.opensimkit.models.rocketpropulsion.PipeT1;


public class Pipe16 extends PipeT1 {
		
	@Inject
	public Pipe16(@Named("16_PureGasDat") PureGasPort inputPort,
			@Named("17_PureGasDat") PureGasPort outputPort) {
		super("Pipe16", inputPort, outputPort);
	}
}
