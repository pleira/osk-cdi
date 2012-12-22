package org.osk.models.t1;

import org.osk.ports.FluidPort;

public class BoundaryUtils {

	public static FluidPort createBoundaryPort(String fluid, double mflow) {
		return createBoundaryPort(fluid,0.0,0.0, mflow);
	}

	public static FluidPort createBoundaryPort(
			FluidPort outputPort) {
		return createBoundaryPort(outputPort.getFluid(),0.0,0.0, outputPort.getMassflow());
	}

	public static FluidPort createBoundaryPort(String fluid, double pressure,
			double temp, double mflow) {
		return new FluidPort(fluid, pressure, temp, mflow);
	}
}
