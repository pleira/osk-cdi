package org.opensimkit.models.astris;

import javax.inject.Inject;
import javax.inject.Named;

import org.opensimkit.models.rocketpropulsion.FilterT1;
import org.opensimkit.ports.PureGasPort;

public class Filter06 extends FilterT1 {

	@Inject
	public Filter06(@Named("05_PureGasDat") PureGasPort inputPort, 
			@Named("06_PureGasDat") PureGasPort outputPort) {
		super("Filter06", inputPort, outputPort);
	}
}
