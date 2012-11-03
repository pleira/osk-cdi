package org.opensimkit.structure;

import javax.inject.Inject;
import javax.inject.Named;

import org.opensimkit.models.ports.PureGasPort;
import org.opensimkit.models.rocketpropulsion.PipeT1;


public class Pipe13 extends PipeT1 {
		
	@Inject
	public Pipe13(@Named("12_PureGasDat") PureGasPort inputPort,
			@Named("13_PureGasDat") PureGasPort outputPort) {
		super("Pipe13", inputPort, outputPort);
	}
}
