package org.opensimkit.models.astris;

import javax.inject.Inject;
import javax.inject.Named;

import org.opensimkit.models.rocketpropulsion.JunctionT1;
import org.opensimkit.ports.PureGasPort;


public class Junction04 extends JunctionT1 {
		
	@Inject
	public Junction04(@Named("02_PureGasDat") PureGasPort inputPortLeft,
			@Named("03_PureGasDat") PureGasPort inputPortRight,
			@Named("04_PureGasDat") PureGasPort outputPort) {
		super("Junction04", inputPortLeft, inputPortRight, outputPort);
	}
}
