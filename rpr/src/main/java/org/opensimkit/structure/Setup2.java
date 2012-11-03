package org.opensimkit.structure;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.logging.Logger;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.inject.Inject;

import org.jboss.weld.environment.se.events.ContainerInitialized;
import org.opensimkit.models.ports.AnalogPort;
import org.opensimkit.models.ports.PureLiquidPort;
import org.opensimkit.models.ports.Valve;
import org.opensimkit.models.ports.ValveID;
import org.opensimkit.models.rocketpropulsion.ID;

//@ApplicationScoped
public class Setup2 {

//	@Produces @Valve(id = ValveID.PLP18)  PureLiquidPort plp18;
//	@Inject @Valve(id = ValveID.PLP19) PureLiquidPort plp19;
//    @Inject @ID("23_Fuel_Flow_Control_Signal") AnalogPort fflow23;
//    @Inject @ID("24_Ox_Flow_Control_Signal") AnalogPort oxflow24;
//
//    @Inject FFV18 fflow18;
    
//    @Inject @ID(id = "18_FluidFlowValve") FluidFlowValve fflow18;
//    @Inject @ID(id = "19_FluidFlowValve") FluidFlowValve oxflow19;
//    @Inject Engine engine;
//    @Inject Instance<EngineController> engineControllerInstance;
    
    //@Inject @ID(id = "21_EngineController") EngineController engineController;
//    EngineController engineController;
    
	// @Inject @Valve(ValveEnum.PLP18) ValveProducer vb18;
	
	//	@Inject @Produces @Valve("19_PureLiquidDat") PureLiquidPort plp19;
//	@Inject @Produces @Valve("20_PureLiquidDat") PureLiquidPort plp20;
//	@Inject @Produces @Valve("21_PureLiquidDat") PureLiquidPort plp21;
	
    @PostConstruct
    public void initConnections() {
    	// Set connections
//    	engineController = engineControllerInstance.get();
//    	fflow23.setToModel(engineController);
//    	fflow23.setFromModel(fflow18);
//    	oxflow24.setToModel(engineController);
//    	OXFLOW24.SETFROMMODEL(OXFLOW19);
    	
    }
	
//    public void initSim(@Observes ContainerInitialized init) throws IOException {
//    	Logger log = Logger.getGlobal();
//	//plp18 = vb18.
//    log.info(plp18.getName());
//    log.info(plp19.getName());
////    log.info(engine.getName());
////    log.info(fflow18.getName());
////    log.info(oxflow19.getName());
////    log.info(fflow23.getName());
////    log.info(oxflow24.getName());
//
//    // --- Ports 
////    log.info(engineController.getName());
//    // TODO the EngineControllerProducer should also benefit from DI when generating EngineController
//    // but how to do it? With the bean manager?
////    log.info("\t" + engineController.controlPort1.getName());
////    log.info("\t" + engineController.controlPort2.getName());
//    }


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
	

}
