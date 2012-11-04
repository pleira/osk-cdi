package org.opensimkit.ports;


/**
 *
 * @author P. Pita
 */
public class PureLiquidPort extends FluidPort {

    public PureLiquidPort() {
        super("");
    }
	    
    public PureLiquidPort(String id) {
		super(id);
	}

	public void setName(String name) {
    	this.name = name;
    }
}
