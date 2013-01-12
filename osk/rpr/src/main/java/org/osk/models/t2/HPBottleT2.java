package org.osk.models.t2;

import javax.enterprise.inject.Alternative;

import net.gescobar.jmx.annotation.ManagedAttribute;

import org.osk.models.BaseModel;
import org.osk.models.HPBottle;
import org.osk.models.materials.HeliumPropertiesBuilder;
import org.osk.models.materials.MaterialProperties;
import org.osk.ports.FluidPort;

@Alternative
public class HPBottleT2 extends BaseModel implements HPBottle {
	/** Mass of pressure vessel. */
	private double mass;
	/** Volume of vessel. */
	private double volume;
	/** Specific. heat capacity of vessel. */
	private double specificHeatCapacity;
	/** Diameter of vessel. */
	private double diam;
	/** Surface of vessel (spherical bottle assumed). */
	private double surface;
	/** Pressure of gas in vessel. */
	private double ptotal;
	/** Temperature of gas in vessel. */
	private double ttotal;
	/** Vessel wall temperature. */
	private double twall;
	/** Mass of gas in vessel. */
	private double mtotal;
	/** Mass flow of gas into pipe. */
	private double mftotal;
	/** Gas in vessel. */
	private String fluid;
	/** Heat flow from wall to fluid for pressure regul. elements. */
	private double qHFlow;
	/** Initial pressure of gas in vessel. */
	private double pinit;

	private static final String TYPE = "HPBottleT1";
	private static final String SOLVER = "Euler";
	
    public HPBottleT2() {
        super(TYPE, SOLVER);
    }

    @Override
	public void init(String name) {
    	this.name = name;  
        final double ptotal_Pa = ptotal * 1.E5;
        final double radius = Math.pow((volume * 3 / (4 * Math.PI)), .33333);
        diam = radius * 2;
        surface = 4 * Math.PI * Math.pow(radius, 2);
        qHFlow = 0.0;
        pinit = ptotal_Pa; 
        twall = ttotal;
        MaterialProperties helium = HeliumPropertiesBuilder.build(ptotal, ttotal);
        mtotal = helium.DENSITY * volume;
    }
    
    @Override
	public void calculateMassFlow(double timeStep) {
    	ptotal /= 1.01;
    	ttotal /= 1.01;
    	mftotal /= 1.01;
    }

	@Override
	public FluidPort createInputPortIter() {
		return new FluidPort(name, fluid, ptotal, ttotal, mftotal);
	}

	@Override
	public FluidPort getOutputPortStatus() {
		return new FluidPort(name, fluid, ptotal, ttotal, mftotal);
	}

    //-----------------------------------------------------------------------------------
    // Methods added for JMX monitoring	and setting initial properties via CDI Extensions

	@Override
	@ManagedAttribute
	public double getMass() {
		return mass;
	}
	@Override
	public void setMass(double mass) {
		this.mass = mass;
	}
	@Override
	@ManagedAttribute	
	public double getVolume() {
		return volume;
	}
	@Override
	public void setVolume(double volume) {
		this.volume = volume;
	}
	@Override
	@ManagedAttribute
	public double getSpecificHeatCapacity() {
		return specificHeatCapacity;
	}
	@Override
	public void setSpecificHeatCapacity(double specificHeatCapacity) {
		this.specificHeatCapacity = specificHeatCapacity;
	}
	@Override
	@ManagedAttribute
	public double getPtotal() {
		return ptotal;
	}
	@Override
	public void setPtotal(double ptotal) {
		this.ptotal = ptotal;
	}
	@Override
	@ManagedAttribute
	public double getTtotal() {
		return ttotal;
	}
	@Override
	public void setTtotal(double ttotal) {
		this.ttotal = ttotal;
	}
	@Override
	@ManagedAttribute
	public String getFluid() {
		return fluid;
	}
	@Override
	public void setFluid(String fluid) {
		this.fluid = fluid;
	}
	@Override
	@ManagedAttribute
	public double getDiam() {
		return diam;
	}
	@Override
	public void setDiam(double diam) {
		this.diam = diam;
	}
	@Override
	@ManagedAttribute
	public double getSurface() {
		return surface;
	}
	@Override
	public void setSurface(double surface) {
		this.surface = surface;
	}
	@Override
	@ManagedAttribute
	public double getTwall() {
		return twall;
	}
	@Override
	public void setTwall(double twall) {
		this.twall = twall;
	}
	@Override
	@ManagedAttribute
	public double getMtotal() {
		return mtotal;
	}
	@Override
	public void setMtotal(double mtotal) {
		this.mtotal = mtotal;
	}
	@Override
	@ManagedAttribute
	public double getMftotal() {
		return mftotal;
	}
	@Override
	public void setMftotal(double mftotal) {
		this.mftotal = mftotal;
	}
	@Override
	@ManagedAttribute
	public double getqHFlow() {
		return qHFlow;
	}
	@Override
	public void setqHFlow(double qHFlow) {
		this.qHFlow = qHFlow;
	}
	@Override
	@ManagedAttribute
	public double getPinit() {
		return pinit;
	}
	@Override
	public void setPinit(double pinit) {
		this.pinit = pinit;
	}

}
