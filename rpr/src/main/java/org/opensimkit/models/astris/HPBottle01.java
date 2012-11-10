package org.opensimkit.models.astris;

import javax.inject.Inject;
import javax.inject.Named;

import org.opensimkit.models.rocketpropulsion.HPBottleT1;
import org.opensimkit.ports.PureGasPort;

public class HPBottle01 extends HPBottleT1 {

	@Inject
	public HPBottle01(@Named("01_PureGasDat") PureGasPort outputPort) {
		super("01_HPBottle", outputPort);
	}
}
