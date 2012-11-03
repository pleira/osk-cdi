package org.opensimkit.structure;

import javax.inject.Inject;
import javax.inject.Named;

import org.opensimkit.models.ports.PureGasPort;
import org.opensimkit.models.rocketpropulsion.HPBottleT1;

public class HPBottle01 extends HPBottleT1 {

	@Inject
	public HPBottle01(@Named("01_PureGasDat") PureGasPort outputPort) {
		super("01_HPBottle", outputPort);
	}

//	@Inject
//	setSpecificCapacity(@ConfigProperty @Named("") String 

}
