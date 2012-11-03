package org.opensimkit.structure;

import javax.inject.Inject;
import javax.inject.Named;

import org.opensimkit.models.ports.PureGasPort;
import org.opensimkit.models.rocketpropulsion.PipeT1;


public class Pipe05 extends PipeT1 {
		
	@Inject
	public Pipe05(@Named("01_PureGasDat") PureGasPort inputPort,
			@Named("03_PureGasDat") PureGasPort outputPort) {
		super("Pipe05", inputPort, outputPort);
	}
}
