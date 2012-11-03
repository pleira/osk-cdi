/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.opensimkit.ports;

import org.opensimkit.Kernel;
import org.opensimkit.manipulation.Manipulatable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author A. Brandt
 */
public class FluidPort extends BasePort {
    private static final Logger LOG = LoggerFactory.getLogger(FluidPort.class);
    @Manipulatable private String fluid;
    @Manipulatable private double pressure;
    @Manipulatable private double temperature;
    @Manipulatable private double massflow;
    @Manipulatable private String boundaryFluid;
    @Manipulatable private double boundaryPressure;
    @Manipulatable private double boundaryTemperature;
    @Manipulatable private double boundaryMassflow;

    /**
     * This constructor is needed for the FluidPort initialisation during
     * the XML file parsing.
     */
    public FluidPort(final String name) {
        super(name);
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

    public String getFluid() {
        return fluid;
    }

    public double getPressure() {
        return pressure;
    }

    public double getTemperature() {
        return temperature;
    }

    public double getMassflow() {
        return massflow;
    }

    public String getBoundaryFluid() {
        return boundaryFluid;
    }

    public double getBoundaryPressure() {
        return boundaryPressure;
    }

    public double getBoundaryTemperature() {
        return boundaryTemperature;
    }

    public double getBoundaryMassflow() {
        return boundaryMassflow;
    }

    @Override
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

        result.append(this.getClass().getName());
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
