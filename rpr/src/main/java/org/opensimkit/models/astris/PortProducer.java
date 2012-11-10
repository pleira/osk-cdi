package org.opensimkit.models.astris;

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
 * @author P. Pita
 */
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

	@Produces @Named("23_Fuel_Flow_Control_Signal") AnalogPort prfflow23 = new AnalogPort(id23);
    @Produces @Named("24_Ox_Flow_Control_Signal") AnalogPort proxflow24 = new AnalogPort(id24);
    @Produces @Named("00_PureGasDat") PureGasPort gp00 = new PureGasPort("00_PureGasDat");
    @Produces @Named("01_PureGasDat") PureGasPort gp01 = new PureGasPort("01_PureGasDat");
    @Produces @Named("02_PureGasDat") PureGasPort gp02 = new PureGasPort("02_PureGasDat");
    @Produces @Named("03_PureGasDat") PureGasPort gp03 = new PureGasPort("03_PureGasDat");
    @Produces @Named("04_PureGasDat") PureGasPort gp04 = new PureGasPort("04_PureGasDat");
    @Produces @Named("05_PureGasDat") PureGasPort gp05 = new PureGasPort("05_PureGasDat");
    @Produces @Named("06_PureGasDat") PureGasPort gp06 = new PureGasPort("06_PureGasDat");
    @Produces @Named("07_PureGasDat") PureGasPort gp07 = new PureGasPort("07_PureGasDat");
    @Produces @Named("08_PureGasDat") PureGasPort gp08 = new PureGasPort("08_PureGasDat");
    @Produces @Named("09_PureGasDat") PureGasPort gp09 = new PureGasPort("09_PureGasDat");
    @Produces @Named("10_PureGasDat") PureGasPort gp10 = new PureGasPort("10_PureGasDat");
    @Produces @Named("11_PureGasDat") PureGasPort gp11 = new PureGasPort("11_PureGasDat");
    @Produces @Named("13_PureGasDat") PureGasPort gp13 = new PureGasPort("13_PureGasDat");
    @Produces @Named("12_PureGasDat") PureGasPort gp12 = new PureGasPort("12_PureGasDat");
    @Produces @Named("14_PureGasDat") PureGasPort gp14 = new PureGasPort("14_PureGasDat");
    @Produces @Named("17_PureGasDat") PureGasPort gp17 = new PureGasPort("17_PureGasDat");
    @Produces @Named("15_PureGasDat") PureGasPort gp15 = new PureGasPort("15_PureGasDat");
    @Produces @Named("16_PureGasDat") PureGasPort gp16 = new PureGasPort("16_PureGasDat");
	@Produces @Named("18_PureLiquidDat") PureLiquidPort plp18 = new PureLiquidPort(pldid18); 
	@Produces @Named("19_PureLiquidDat") PureLiquidPort plp19 = new PureLiquidPort(pldid19); 
	@Produces @Named("20_PureLiquidDat") PureLiquidPort plp20 = new PureLiquidPort(pldid20); 
	@Produces @Named("21_PureLiquidDat") PureLiquidPort plp21 = new PureLiquidPort(pldid21); 
	
}
