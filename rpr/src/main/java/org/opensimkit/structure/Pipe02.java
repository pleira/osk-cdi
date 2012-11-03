package org.opensimkit.structure;

import javax.inject.Inject;
import javax.inject.Named;

import org.opensimkit.models.ports.PureGasPort;
import org.opensimkit.models.rocketpropulsion.PipeT1;


public class Pipe02 extends PipeT1 {
		
	@Inject
	public Pipe02(@Named("00_PureGasDat") PureGasPort inputPort,
			@Named("02_PureGasDat") PureGasPort outputPort) {
		super("Pipe02", inputPort, outputPort);
	}
	
	
}
