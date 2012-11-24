package org.opensimkit.models.astris;

import java.util.Collection;
import java.util.SortedMap;
import java.util.TreeMap;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.inject.Named;
import javax.management.InstanceAlreadyExistsException;

import net.gescobar.jmx.Management;
import net.gescobar.jmx.ManagementException;

import org.opensimkit.Mesh;
import org.opensimkit.Model;
import org.opensimkit.models.astris.parts.Engine20;
import org.opensimkit.models.astris.parts.EngineController21;
import org.opensimkit.models.astris.parts.FFV18;
import org.opensimkit.models.astris.parts.FFV19;
import org.opensimkit.models.astris.parts.Filter06;
import org.opensimkit.models.astris.parts.HPBottle00;
import org.opensimkit.models.astris.parts.HPBottle01;
import org.opensimkit.models.astris.parts.Junction04;
import org.opensimkit.models.astris.parts.PReg08;
import org.opensimkit.models.astris.parts.PReg12;
import org.opensimkit.models.astris.parts.PReg15;
import org.opensimkit.models.astris.parts.Pipe02;
import org.opensimkit.models.astris.parts.Pipe03;
import org.opensimkit.models.astris.parts.Pipe05;
import org.opensimkit.models.astris.parts.Pipe07;
import org.opensimkit.models.astris.parts.Pipe09;
import org.opensimkit.models.astris.parts.Pipe11;
import org.opensimkit.models.astris.parts.Pipe13;
import org.opensimkit.models.astris.parts.Pipe14;
import org.opensimkit.models.astris.parts.Pipe16;
import org.opensimkit.models.astris.parts.ScStructure22;
import org.opensimkit.models.astris.parts.Split10;
import org.opensimkit.models.astris.parts.Tank17;
import org.opensimkit.models.environment.OSKGravityModel;
/**
 * This class is instantiated by the CDI container. 
 * Itself, it instantiates the different elements of the rocket model.
 * The !wiring! of the rocket model is done therefore by the CDI container.
 * 
 * @author P. Pita
 * 
 */
@ApplicationScoped
public class AstrisRocket  {

	// Structural items in the numerical simulation
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
	@Inject ScStructure22 scStructure22;
	  
	@Inject OSKGravityModel gravityModel23;

	// Boundary conditions
	Mesh mesh0 = new Mesh("mesh_0", "top");
	Mesh mesh1 = new Mesh("mesh_1", "sub");
	Mesh mesh2 = new Mesh("mesh_2", "sub");

	@Produces
	@Named("STRUCTURE_ITEMS_MAP")
	final SortedMap<String, Model> timeStepItems = new TreeMap<String, Model>();

	final SortedMap<String, Model> regulationItems = new TreeMap<String, Model>();

	final SortedMap<String, Model> iterItems = new TreeMap<String, Model>();

	// This collection is used to iterate the models
	@Produces @TimeStepItems	
	public Collection<Model> iterItems() {
		return timeStepItems.values();
	}
    
	@Produces @RegulationItems
	public Collection<Model> regulationItems() {
		return regulationItems.values();
	}
    
	@Produces @IterItems
	public Collection<Model> timeStepItems() {
		return iterItems.values();
	}
    
	@PostConstruct
	void initNumericalSimulation() {
		initItems();
		initBoundaryConditions();
		// TODO: fire a CDI event to say that the model is ready for starting the
		// simulation?
		registerMBeans();
	}

	void initItems() {
		timeStepItems.put(fflow18.getName(), fflow18);
		timeStepItems.put(fflow19.getName(), fflow19);
		timeStepItems.put(pipe02.getName(), pipe02);
		timeStepItems.put(pipe03.getName(), pipe03);
		timeStepItems.put(pipe05.getName(), pipe05);
		timeStepItems.put(pipe07.getName(), pipe07);
		timeStepItems.put(pipe09.getName(), pipe09);
		timeStepItems.put(pipe11.getName(), pipe11);
		timeStepItems.put(pipe13.getName(), pipe13);
		timeStepItems.put(pipe14.getName(), pipe14);
		timeStepItems.put(pipe16.getName(), pipe16);
		timeStepItems.put(filter06.getName(), filter06);
		timeStepItems.put(hpbottle00.getName(), hpbottle00);
		timeStepItems.put(hpbottle01.getName(), hpbottle01);
		timeStepItems.put(preg08.getName(), preg08);
		timeStepItems.put(preg12.getName(), preg12);
		timeStepItems.put(preg15.getName(), preg15);
		timeStepItems.put(split10.getName(), split10);
		timeStepItems.put(junction04.getName(), junction04);
		timeStepItems.put(tank17.getName(), tank17);
		timeStepItems.put(engine20.getName(), engine20);
		timeStepItems.put(engineController21.getName(), engineController21);
		timeStepItems.put(scStructure22.getName(), scStructure22);
		timeStepItems.put(gravityModel23.getName(), gravityModel23);
		
		regulationItems.put(engineController21.getName(), engineController21);
		iterItems.put(mesh0.getName(), mesh0);

		
//		items.put(mesh1.getName(), mesh1);
//		items.put(mesh2.getName(), mesh2);
	}

	void initBoundaryConditions() {
		// initialize the meshes with the elements contained in each.
		// The solver will impose later the boundary conditions.
		// Order matters
		mesh0.add(engineController21);
		mesh0.add(mesh1);
		mesh0.add(scStructure22);
		mesh0.add(gravityModel23);

		mesh1.add(mesh2);
		mesh1.add(pipe05);
		mesh1.add(filter06);
		mesh1.add(pipe07);
		mesh1.add(preg08);
		mesh1.add(pipe09);
		mesh1.add(split10);
		mesh1.add(pipe11);
		mesh1.add(preg12);
		mesh1.add(pipe13);
		mesh1.add(pipe14);
		mesh1.add(preg15);
		mesh1.add(pipe16);
		mesh1.add(tank17);
		mesh1.add(fflow18);
		mesh1.add(fflow19);
		mesh1.add(engine20);

		mesh2.add(hpbottle00);
		mesh2.add(hpbottle01);
		mesh2.add(pipe02);
		mesh2.add(pipe03);
		mesh2.add(junction04);
	}
    
    // move this method out of this class (catch an event and then, register? 
	private void registerMBeans() {
		for (Model model : timeStepItems.values()) {
			try {
				Management.register(model,
						"org.opensimkit:type=" + model.getType() + ",name="
								+ model.getName());
			} catch (InstanceAlreadyExistsException e) {
				e.printStackTrace();
			} catch (ManagementException e) {
				e.printStackTrace();
			}
		}
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
    
}
