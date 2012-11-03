package org.opensimkit.structure;

import javax.inject.Inject;
import javax.inject.Named;

import org.opensimkit.models.ports.PureGasPort;
import org.opensimkit.models.rocketpropulsion.PipeT1;


public class Pipe03 extends PipeT1 {
		
	@Inject
	public Pipe03(@Named("01_PureGasDat") PureGasPort inputPort,
			@Named("03_PureGasDat") PureGasPort outputPort) {
		super("Pipe03", inputPort, outputPort);
	}
}
