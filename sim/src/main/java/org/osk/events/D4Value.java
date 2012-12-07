package org.osk.events;

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

	@Override
	public String toString() {
		StringBuilder b = new StringBuilder("D4Value ");
		for (int i=0; i<4; i++) {
			b.append("v").append(i).append(" == ").append(value[i]);
		}
		return b.toString();		
	}
}
