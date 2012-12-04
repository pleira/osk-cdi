package org.opensimkit.events;

public class D4Value {
	
	private double[] value = new double[4];

	public D4Value(final double[] value) {
		this.value = value;
	}

	public double[] getValue() {
		return value;
	}

	public void setValue(double[] value) {
		this.value = value;
	}

}
