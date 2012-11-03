package org.opensimkit.structure;

import java.io.IOException;
import java.util.logging.Logger;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

import org.apache.deltaspike.core.api.config.PropertyFileConfig;
import org.jboss.weld.environment.se.events.ContainerInitialized;
import org.opensimkit.models.rocketpropulsion.EngineController;

@ApplicationScoped
public class Setup  implements PropertyFileConfig   {

//    @Inject @ID("23_Fuel_Flow_Control_Signal") AnalogPort fflow23; //  = new AnalogPort(id23);
//    @Inject @ID("24_Ox_Flow_Control_Signal") AnalogPort oxflow24;

  @Inject FFV18 fflow18;
  @Inject FFV19 fflow19;
  @Inject Tank17 tank17;
  @Inject Pipe02 pipe02;
  @Inject Pipe03 pipe03;
  @Inject Pipe05 pipe05;
  @Inject Pipe07 pipe07;
  @Inject Pipe09 pipe09;
  @Inject Pipe11 pipe11;  
  @Inject Pipe13 pipe13;
  @Inject Pipe14 pipe14;
  @Inject Pipe16 pipe16;
  @Inject HPBottle00 hpbottle00;
  @Inject HPBottle01 hpbottle01;
  
//    @Inject Engine engine;
//    @Inject Instance<EngineController> engineControllerInstance;
    
    //@Inject @ID(id = "21_EngineController") EngineController engineController;
    EngineController engineController;
    
	// @Inject @Valve(ValveEnum.PLP18) ValveProducer vb18;
	
	
    @PostConstruct
    public void initConnections() {
    	// Set connections
//    	fflow23.setToModel(engineController);
//    	fflow23.setFromModel(fflow18);
//    	oxflow24.setToModel(engineController);
//    	oxflow24.setFromModel(oxflow19);
    	
    }
	
    public void initSim(@Observes ContainerInitialized init) throws IOException {
    	Logger log = Logger.getGlobal();
	//plp18 = vb18.
//    log.info(plp18.getName());
//    log.info(plp19.getName());
//    log.info(engine.getName());
    log.info(fflow18.getName());
//    log.info("Input Port From: " + fflow18.getInputPort().getFromModel().getName());
    log.info("Input Port " + fflow18.getInputPort().getName() + " flows to " + fflow18.getInputPort().getToModel().getName());
    log.info("Output Port " + fflow18.getOutputPort().getName() + " flows from " + fflow18.getOutputPort().getFromModel().getName());
    log.info(fflow19.getName());
//  log.info("Input Port From: " + fflow19.getInputPort().getFromModel().getName());
  log.info("Input Port " + fflow19.getInputPort().getName() + " flows to " + fflow19.getInputPort().getToModel().getName());
  log.info("Output Port " + fflow19.getOutputPort().getName() + " flows from " + fflow19.getOutputPort().getFromModel().getName());
//    log.info("Output Port To: " + fflow18.getOutputPort().getToModel().getName());
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

//  log.info("tank17 vtbr: " + tank17.getVTBR());
//    log.info(fflow18.getName());
    
//    log.info(oxflow19.getName());
//    log.info(fflow23.getName());
//    log.info(oxflow24.getName());

    // --- Ports 
//    log.info(engineController.getName());
    // TODO the EngineControllerProducer should also benefit from DI when generating EngineController
    // but how to do it? With the bean manager?
//    log.info("\t" + engineController.controlPort1.getName());
//    log.info("\t" + engineController.controlPort2.getName());
    }


//	@Produces @ID 
//	public String createId(InjectionPoint ip) {
//		Type clazz = ip.getType();
//		for( Annotation qualifier : ip.getQualifiers() ) {
//			if( qualifier instanceof ID ) {
//				System.out.println("id is " + ((ID) qualifier).value());
//				return ((ID) qualifier).value();
//			}
//		}		
//		return "";
//	}
	
    @Override
    public String getPropertyFileName()
    {
        return "sim.properties";
    }


}
