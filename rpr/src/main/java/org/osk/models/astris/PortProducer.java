package org.opensimkit.models.astris;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Named;

import org.opensimkit.ports.AnalogPort;
import org.opensimkit.ports.PureGasPort;
import org.opensimkit.ports.PureLiquidPort;

/**
 * This class is a utility class to instantiate the connections used in 
 * the model. Nobody calls directly this class, only the CDI container
 * to fectch the ports.
 * 
 * The use of @ApplicationScoped is needed (even if recommended), because otherwise
 * Weld would create several instances of this class and therefore there would be 
 * several producers for the structural elements. 
 *  
 * @author P. Pita
 */
@ApplicationScoped
public class PortProducer {
	
	@Produces @Named("18_PureLiquidDatId")  final String pldid18 =  "18_PureLiquidDat";
	@Produces @Named("19_PureLiquidDatId")  final String pldid19 =  "19_PureLiquidDat";
	@Produces @Named("20_PureLiquidDatId")  final String pldid20 =  "20_PureLiquidDat";
	@Produces @Named("21_PureLiquidDatId")  final String pldid21 =  "21_PureLiquidDat";
	@Produces @Named("23_Fuel_Flow_Control_SignalId") final String id23 = "23_Fuel_Flow_Control_Signal";	
	@Produces @Named("24_Ox_Flow_Control_SignalId")   final String id24 = "24_Ox_Flow_Control_Signal";
	
    // Here I use a direct instantiation of the ports. But it is a bit repetitive
    // It would be interesting to code a CDI extension and generate such values in 
    // the CDI container

	@Produces @Named("23_Fuel_Flow_Control_Signal") final AnalogPort prfflow23 = new AnalogPort(id23);
    @Produces @Named("24_Ox_Flow_Control_Signal") final AnalogPort proxflow24 = new AnalogPort(id24);
    @Produces @Named("00_PureGasDat") final PureGasPort gp00 = new PureGasPort("00_PureGasDat");
    @Produces @Named("01_PureGasDat") final PureGasPort gp01 = new PureGasPort("01_PureGasDat");
    @Produces @Named("02_PureGasDat") final PureGasPort gp02 = new PureGasPort("02_PureGasDat");
    @Produces @Named("03_PureGasDat") final PureGasPort gp03 = new PureGasPort("03_PureGasDat");
    @Produces @Named("04_PureGasDat") final PureGasPort gp04 = new PureGasPort("04_PureGasDat");
    @Produces @Named("05_PureGasDat") final PureGasPort gp05 = new PureGasPort("05_PureGasDat");
    @Produces @Named("06_PureGasDat") final PureGasPort gp06 = new PureGasPort("06_PureGasDat");
    @Produces @Named("07_PureGasDat") final PureGasPort gp07 = new PureGasPort("07_PureGasDat");
    @Produces @Named("08_PureGasDat") final PureGasPort gp08 = new PureGasPort("08_PureGasDat");
    @Produces @Named("09_PureGasDat") final PureGasPort gp09 = new PureGasPort("09_PureGasDat");
    @Produces @Named("10_PureGasDat") final PureGasPort gp10 = new PureGasPort("10_PureGasDat");
    @Produces @Named("11_PureGasDat") final PureGasPort gp11 = new PureGasPort("11_PureGasDat");
    @Produces @Named("13_PureGasDat") final PureGasPort gp13 = new PureGasPort("13_PureGasDat");
    @Produces @Named("12_PureGasDat") final PureGasPort gp12 = new PureGasPort("12_PureGasDat");
    @Produces @Named("14_PureGasDat") final PureGasPort gp14 = new PureGasPort("14_PureGasDat");
    @Produces @Named("17_PureGasDat") final PureGasPort gp17 = new PureGasPort("17_PureGasDat");
    @Produces @Named("15_PureGasDat") final PureGasPort gp15 = new PureGasPort("15_PureGasDat");
    @Produces @Named("16_PureGasDat") final PureGasPort gp16 = new PureGasPort("16_PureGasDat");
	@Produces @Named("18_PureLiquidDat") final PureLiquidPort plp18 = new PureLiquidPort(pldid18); 
	@Produces @Named("19_PureLiquidDat") final PureLiquidPort plp19 = new PureLiquidPort(pldid19); 
	@Produces @Named("20_PureLiquidDat") final PureLiquidPort plp20 = new PureLiquidPort(pldid20); 
	@Produces @Named("21_PureLiquidDat") final PureLiquidPort plp21 = new PureLiquidPort(pldid21); 
	
}
