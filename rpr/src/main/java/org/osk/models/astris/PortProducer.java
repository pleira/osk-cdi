package org.osk.models.astris;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Named;

import org.osk.ports.AnalogPort;
import org.osk.ports.FluidPort;
import org.osk.ports.FluidPort;

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

//	@Produces @Named("23_Fuel_Flow_Control_Signal") final AnalogPort prfflow23 = new AnalogPort(id23);
//    @Produces @Named("24_Ox_Flow_Control_Signal") final AnalogPort proxflow24 = new AnalogPort(id24);
//    @Produces @Named("00_PureGasDat") final FluidPort gp00 = new FluidPort("00_PureGasDat");
//    @Produces @Named("01_PureGasDat") final FluidPort gp01 = new FluidPort("01_PureGasDat");
//    @Produces @Named("02_PureGasDat") final FluidPort gp02 = new FluidPort("02_PureGasDat");
//    @Produces @Named("03_PureGasDat") final FluidPort gp03 = new FluidPort("03_PureGasDat");
//    @Produces @Named("04_PureGasDat") final FluidPort gp04 = new FluidPort("04_PureGasDat");
//    @Produces @Named("05_PureGasDat") final FluidPort gp05 = new FluidPort("05_PureGasDat");
//    @Produces @Named("06_PureGasDat") final FluidPort gp06 = new FluidPort("06_PureGasDat");
//    @Produces @Named("07_PureGasDat") final FluidPort gp07 = new FluidPort("07_PureGasDat");
//    @Produces @Named("08_PureGasDat") final FluidPort gp08 = new FluidPort("08_PureGasDat");
//    @Produces @Named("09_PureGasDat") final FluidPort gp09 = new FluidPort("09_PureGasDat");
//    @Produces @Named("10_PureGasDat") final FluidPort gp10 = new FluidPort("10_PureGasDat");
//    @Produces @Named("11_PureGasDat") final FluidPort gp11 = new FluidPort("11_PureGasDat");
//    @Produces @Named("13_PureGasDat") final FluidPort gp13 = new FluidPort("13_PureGasDat");
//    @Produces @Named("12_PureGasDat") final FluidPort gp12 = new FluidPort("12_PureGasDat");
//    @Produces @Named("14_PureGasDat") final FluidPort gp14 = new FluidPort("14_PureGasDat");
//    @Produces @Named("17_PureGasDat") final FluidPort gp17 = new FluidPort("17_PureGasDat");
//    @Produces @Named("15_PureGasDat") final FluidPort gp15 = new FluidPort("15_PureGasDat");
//    @Produces @Named("16_PureGasDat") final FluidPort gp16 = new FluidPort("16_PureGasDat");
//	@Produces @Named("18_PureLiquidDat") final FluidPort() plp18 = new FluidPort()(pldid18); 
//	@Produces @Named("19_PureLiquidDat") final FluidPort() plp19 = new FluidPort()(pldid19); 
//	@Produces @Named("20_PureLiquidDat") final FluidPort() plp20 = new FluidPort()(pldid20); 
//	@Produces @Named("21_PureLiquidDat") final FluidPort() plp21 = new FluidPort()(pldid21); 
	
}
