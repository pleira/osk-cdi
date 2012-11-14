package org.opensimkit.models.astris;

import java.util.SortedMap;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.deltaspike.core.api.config.PropertyFileConfig;
import org.opensimkit.Model;
import org.opensimkit.models.environment.OSKGravityModel;

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
@ApplicationScoped
public class AstrisRocketSimulation implements PropertyFileConfig  {

	private static final long serialVersionUID = 13344563563L;

	/*
	 * The complete simulation items are the structure items 
	 * (given at injection time) plus the environment (added after 
	 * object construction) 
	 */
	@Inject @Named("STRUCTURE_ITEMS_MAP")
	SortedMap<String, Model> items;
	
	@PostConstruct
	public void addGravityModel() {
		OSKGravityModel grav = new OSKGravityModel();
		items.put(grav.getName(), grav);
	}
    /**
     * This method is used by Apache Delta-Spike to initialize
     * the model values
     */
    @Override
    public String getPropertyFileName()
    {
        return "sim.properties";
    }
    
    @Produces @Named("ALL_ITEMS_MAP")
	public SortedMap<String, Model> getItems() {
		return items;
	}
	
}
