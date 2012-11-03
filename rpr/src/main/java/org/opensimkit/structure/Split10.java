package org.opensimkit.structure;

import javax.inject.Inject;
import javax.inject.Named;

import org.opensimkit.models.ports.PureGasPort;
import org.opensimkit.models.rocketpropulsion.SplitT1;


public class Split10 extends SplitT1 {
		
	@Inject
	public Split10(@Named("09_PureGasDat") PureGasPort inputPort,
			@Named("10_PureGasDat") PureGasPort outputPortLeft,
			@Named("14_PureGasDat") PureGasPort outputPortRight) {
		super("Split10", inputPort, outputPortLeft, outputPortRight);
	}
}
