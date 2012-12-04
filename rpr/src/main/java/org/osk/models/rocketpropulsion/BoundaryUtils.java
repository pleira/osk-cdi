package org.osk.models.rocketpropulsion;

import org.osk.ports.FluidPort;

public class BoundaryUtils {

	public static FluidPort createBoundaryPort(String fluid, double mflow) {
		return createBoundaryPort(fluid,-999999.99,-999999.99, mflow);
	}

	public static FluidPort createBoundaryPort(
			FluidPort outputPort) {
		return createBoundaryPort(outputPort.getBoundaryFluid(),-999999.99,-999999.99, outputPort.getBoundaryMassflow());
	}

	public static FluidPort createBoundaryPort(String fluid, double pressure,
			double temp, double mflow) {
		FluidPort inputPort = new FluidPort();
        inputPort.setBoundaryFluid(fluid);
        inputPort.setBoundaryPressure(pressure);
        inputPort.setBoundaryTemperature(temp);
        inputPort.setBoundaryMassflow(mflow);
		return inputPort;
	}
}
