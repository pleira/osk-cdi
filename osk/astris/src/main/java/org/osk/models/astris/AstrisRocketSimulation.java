package org.osk.models.astris;

import org.apache.deltaspike.core.api.config.PropertyFileConfig;

/**
 * 
 * The properties file used for the simulation is set here.
 * The values are used for the configuration of the 
 * different fields in the model structural elements.
 * The values are passed using CDI extensions from Apache DeltaSpike 
 * 
 * @author Pablo Pita
 *
 */
public class AstrisRocketSimulation implements PropertyFileConfig  {

	private static final long serialVersionUID = 13344563563L;

   /**
     * This method is used by Apache Delta-Spike to initialize
     * the model values
     */
    @Override
    public String getPropertyFileName()
    {
        return "sim.properties";
    }
    	
}
