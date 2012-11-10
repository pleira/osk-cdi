package org.opensimkit.models.astris;

import java.util.SortedMap;
import java.util.TreeMap;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.deltaspike.core.api.config.PropertyFileConfig;
import org.opensimkit.Model;
import org.opensimkit.models.structure.ScStructure;
/*
 * This class is intantiated by the CDI container. 
 * Itself, it instantiates the different elements of the rocket model.
 * The !wiring! of the rocket model is done therefore by the CDI container.
 * By setting the properties file here, the configuration of the 
 * different fields in the elements is done by the ConfigProperty CDI extension  
 * from Apache DeltaSpike 
 * 
 * @author P. Pita
 * 
 */
@ApplicationScoped
public class AstrisRocket extends ScStructure implements PropertyFileConfig   {

  private static final long serialVersionUID = 13344563563L;

  @Inject FFV18 fflow18;
  @Inject FFV19 fflow19;
  @Inject Pipe02 pipe02;
  @Inject Pipe03 pipe03;
  @Inject Pipe05 pipe05;
  @Inject Pipe07 pipe07;
  @Inject Pipe09 pipe09;
  @Inject Pipe11 pipe11;  
  @Inject Pipe13 pipe13;
  @Inject Pipe14 pipe14;
  @Inject Pipe16 pipe16;
  @Inject Filter06 filter06;
  @Inject HPBottle00 hpbottle00;
  @Inject HPBottle01 hpbottle01;
  @Inject PReg08 preg08;
  @Inject PReg12 preg12;
  @Inject PReg15 preg15;
  @Inject Split10 split10;
  @Inject Junction04 junction04;
  @Inject Tank17 tank17;  
  @Inject Engine20 engine20;
  @Inject EngineController21 engineController21;
  
  @Produces @Named("ALL_ITEMS_MAP")
  final SortedMap<String, Model> items = new TreeMap<String, Model>();

  public AstrisRocket() {
		super("ASTRIS_ROCKET");
	}
 
//	Use the annotated method to check the instantiation of the rocket model
//    public void initSim(@Observes ContainerInitialized init) throws IOException {
//      	Logger log = Logger.getGlobal();
//    	log.info(fflow18.getName());
//        log.info("Input Port " + fflow18.getInputPort().getName() + " flows to " + fflow18.getInputPort().getToModel().getName());
//        log.info("Output Port " + fflow18.getOutputPort().getName() + " flows from " + fflow18.getOutputPort().getFromModel().getName());
//        log.info(fflow19.getName());
//    	log.info("Input Port " + fflow19.getInputPort().getName() + " flows to " + fflow19.getInputPort().getToModel().getName());
//    	log.info("Output Port " + fflow19.getOutputPort().getName() + " flows from " + fflow19.getOutputPort().getFromModel().getName());
//		log.info(tank17.getName());
//		log.info("tank17 fuel type: " + tank17.getFuel());
//		log.info("tank17 oxidizer type: " + tank17.getOxidizer());
//		log.info("tank17 vtbr: " + tank17.getVTBR());
//		log.info("Pipe02 " + pipe02.getName());
//		log.info("Pipe03 " + pipe03.getName());
//		log.info("Pipe05 " + pipe05.getName());
//		log.info("Pipe07 " + pipe07.getName());
//		log.info("Pipe09 " + pipe09.getName());
//		log.info("Pipe11 " + pipe11.getName());
//		log.info("Pipe13 " + pipe13.getName());
//		log.info("Pipe14 " + pipe14.getName());
//		log.info("Pipe16 " + pipe16.getName());
//		log.info(junction04.getName());
//		log.info(split10.getName());
//		log.info(preg15.getName());
//		log.info(engineController21.getName());
//		log.info(engine20.getName());
//    }
    
    @PostConstruct
    void initItems() {
    	  items.put( fflow18.getName(), fflow18);
    	  items.put( fflow19.getName(), fflow19);
    	  items.put( pipe02.getName(), pipe02);
    	  items.put( pipe03.getName(), pipe03);
    	  items.put( pipe05.getName(), pipe05);
    	  items.put( pipe07.getName(), pipe07);
    	  items.put( pipe09.getName(), pipe09);
    	  items.put( pipe11.getName(), pipe11);  
    	  items.put( pipe13.getName(), pipe13);
    	  items.put( pipe14.getName(), pipe14);
    	  items.put( pipe16.getName(), pipe16);
    	  items.put( filter06.getName(), filter06);
    	  items.put( hpbottle00.getName(), hpbottle00);
    	  items.put( hpbottle01.getName(), hpbottle01);
    	  items.put( preg08.getName(), preg08);
    	  items.put( preg12.getName(), preg12);
    	  items.put( preg15.getName(), preg15);
    	  items.put( split10.getName(), split10);
    	  items.put( junction04.getName(), junction04);
    	  items.put( tank17.getName(), tank17);  
    	  items.put( engine20.getName(), engine20);
    	  items.put( engineController21.getName(), engineController21);
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

	public SortedMap<String, Model> getItems() {
		return items;
	}


}
