package org.osk.ports;

import net.gescobar.jmx.annotation.ManagedAttribute;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author A. Brandt
 */
public class FluidPort  {
    private static final Logger LOG = LoggerFactory.getLogger(FluidPort.class);
     private String fluid;
     private double pressure;
     private double temperature;
     private double massflow;
     private String boundaryFluid;
     private double boundaryPressure;
     private double boundaryTemperature;
     private double boundaryMassflow;

     public FluidPort() {}
     
    public FluidPort(final double pressure, 
    		final double temperature, final double massflow) {
        this.massflow = massflow;
        this.temperature = temperature;
        this.pressure = pressure;
    }
    
    public FluidPort(final String name, String fluid, final double pressure, 
    		final double temperature, final double massflow) {
    	this.fluid = fluid;
    	this.massflow = massflow;
        this.temperature = temperature;
        this.pressure = pressure;
    }
    
	public FluidPort clone() {
		FluidPort outputPort = new FluidPort();
		outputPort.setFluid(fluid);
		outputPort.setPressure(pressure);
		outputPort.setTemperature(temperature);
		outputPort.setMassflow(massflow);
		return outputPort;
	}
	
    public void setFluid(final String fluid) {
        this.fluid = fluid;
    }

    public void setPressure(final double pressure) {
        this.pressure = pressure;
    }

    public void setTemperature(final double temperature) {
        this.temperature = temperature;
    }

    public void setMassflow(final double massflow) {
        this.massflow = massflow;
    }

    public void setBoundaryFluid(final String boundaryFluid) {
        this.boundaryFluid = boundaryFluid;
    }

    public void setBoundaryPressure(final double boundaryPressure) {
        this.boundaryPressure = boundaryPressure;
    }

    public void setBoundaryTemperature(final double boundaryTemperature) {
        this.boundaryTemperature = boundaryTemperature;
    }

    public void setBoundaryMassflow(final double boundaryMassflow) {
        this.boundaryMassflow = boundaryMassflow;
    }

    @ManagedAttribute
    public String getFluid() {
        return fluid;
    }

    @ManagedAttribute
    public double getPressure() {
        return pressure;
    }

    @ManagedAttribute
    public double getTemperature() {
        return temperature;
    }

    @ManagedAttribute
    public double getMassflow() {
        return massflow;
    }

    @ManagedAttribute
    public String getBoundaryFluid() {
        return boundaryFluid;
    }

    @ManagedAttribute
    public double getBoundaryPressure() {
        return boundaryPressure;
    }

    @ManagedAttribute
    public double getBoundaryTemperature() {
        return boundaryTemperature;
    }

    @ManagedAttribute
    public double getBoundaryMassflow() {
        return boundaryMassflow;
    }

     public void printValues(final String txt) {
        LOG.info("Fluid: {}", fluid);
        LOG.info("Pressure: {}", pressure);
        LOG.info("Temperature: {}", temperature);
        LOG.info("Mass Flow: {}", massflow);
        LOG.info("Boundary Fluid: {}", boundaryFluid);
        LOG.info("Boundary Pressure: {}", boundaryPressure);
        LOG.info("Boundary Temperature: {}", boundaryTemperature);
        LOG.info("Boundary Mass Flow: {}", boundaryMassflow);
        LOG.info(txt);
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();

        result.append(": fluid == ");
        result.append(fluid);
        result.append("; pressure == ");
        result.append(pressure);
        result.append("; temperature == ");
        result.append(temperature);
        result.append("; massflow == ");
        result.append(massflow);
        result.append("; boundaryFluid == ");
        result.append(boundaryFluid);
        result.append("; boundaryPressure == ");
        result.append(boundaryPressure);
        result.append("; boundaryTemperature");
        result.append(boundaryTemperature);
        result.append("; boundaryMassflow == ");
        result.append(boundaryMassflow);
        result.append(".");

        return result.toString();
    }
}
