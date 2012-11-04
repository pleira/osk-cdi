package org.opensimkit.structure;

import java.io.IOException;
import java.util.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

import org.apache.deltaspike.core.api.config.PropertyFileConfig;
import org.jboss.weld.environment.se.events.ContainerInitialized;

@ApplicationScoped
public class Setup  implements PropertyFileConfig   {

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
    
//	Use this method to check the instantiation of the rocket model
//    public void initSim(@Observes ContainerInitialized init) throws IOException {
    public void initSim(ContainerInitialized init) throws IOException {
    	      	Logger log = Logger.getGlobal();
    	log.info(fflow18.getName());
        log.info("Input Port " + fflow18.getInputPort().getName() + " flows to " + fflow18.getInputPort().getToModel().getName());
        log.info("Output Port " + fflow18.getOutputPort().getName() + " flows from " + fflow18.getOutputPort().getFromModel().getName());
        log.info(fflow19.getName());
    	log.info("Input Port " + fflow19.getInputPort().getName() + " flows to " + fflow19.getInputPort().getToModel().getName());
    	log.info("Output Port " + fflow19.getOutputPort().getName() + " flows from " + fflow19.getOutputPort().getFromModel().getName());
		log.info(tank17.getName());
		log.info("tank17 fuel type: " + tank17.getFuel());
		log.info("tank17 oxidizer type: " + tank17.getOxidizer());
		log.info("tank17 vtbr: " + tank17.getVTBR());
		log.info("Pipe02 " + pipe02.getName());
		log.info("Pipe03 " + pipe03.getName());
		log.info("Pipe05 " + pipe05.getName());
		log.info("Pipe07 " + pipe07.getName());
		log.info("Pipe09 " + pipe09.getName());
		log.info("Pipe11 " + pipe11.getName());
		log.info("Pipe13 " + pipe13.getName());
		log.info("Pipe14 " + pipe14.getName());
		log.info("Pipe16 " + pipe16.getName());
		log.info(junction04.getName());
		log.info(split10.getName());
		log.info(preg15.getName());
		log.info(engineController21.getName());
		log.info(engine20.getName());
    }
	
    @Override
    public String getPropertyFileName()
    {
        return "sim.properties";
    }


}
