package org.opensimkit.models.astris;

import javax.inject.Inject;
import javax.inject.Named;

import org.opensimkit.models.rocketpropulsion.HPBottleT1;
import org.opensimkit.ports.PureGasPort;

public class HPBottle00 extends HPBottleT1 {

	@Inject
	public HPBottle00(@Named("00_PureGasDat") PureGasPort outputPort) {
		super("00_HPBottle", outputPort);
	}
}
