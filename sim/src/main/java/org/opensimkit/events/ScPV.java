package org.opensimkit.events;

/**
 * This class works with ECI coordinates.
 * @author P. Pita
 *
 */
public class ScPV {
	
	private double[] scPosition = new double[3];
	private double[] scVelocity = new double[3];

	public ScPV(final double[] scPosition, final double[] scVelocity) {
		this.scPosition = scPosition;
		this.scVelocity = scVelocity;
	}

	public double[] getScPosition() {
		return scPosition;
	}

	public void setScPosition(double[] scPosition) {
		this.scPosition = scPosition;
	}

	public double[] getScVelocity() {
		return scVelocity;
	}

	public void setScVelocity(double[] scVelocity) {
		this.scVelocity = scVelocity;
	}

}
