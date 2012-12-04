package org.osk.ports;

/**
 * Definition of analog line port connection dataset.
 *
 */
public class AnalogPort  {

	private double analogValue;

    public void setAnalogValue(final double analogValue) {
        this.analogValue = analogValue;
    }

    public double getAnalogValue() {
        return analogValue;
    }

    @Override
    public String toString() {
        return "analogValue = " + analogValue;
    }
}
