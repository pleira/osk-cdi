package org.opensimkit.structure;

import javax.inject.Inject;
import javax.inject.Named;

import org.apache.deltaspike.core.api.config.annotation.ConfigProperty;
import org.opensimkit.config.NumberConfig;
import org.opensimkit.config.Util;
import org.opensimkit.models.ports.PureGasPort;
import org.opensimkit.models.ports.PureLiquidPort;
import org.opensimkit.models.rocketpropulsion.TankT1;

public class Tank17 extends TankT1 {
	
	@Inject
	public Tank17(@Named("18_PureLiquidDat") PureLiquidPort outputPortFuel, 
			@Named("20_PureLiquidDat") PureLiquidPort outputPortOxidizer, 
			@Named("13_PureGasDat") PureGasPort inputPortFuelPressureGas, 
			@Named("17_PureGasDat") PureGasPort inputPortOxidizerPressureGas, 
			@ConfigProperty(name = "tank17.fuel") String fuel, 
			@ConfigProperty(name = "tank17.oxidizer") String ox) {
		super("17_Tank", outputPortFuel, outputPortOxidizer, inputPortFuelPressureGas, inputPortOxidizerPressureGas);
		setFuel(fuel);
		setOxidizer(ox);
	}

	@Inject
	void initVtbr(@NumberConfig(name = "tank17.vtbr", defaultValue = "1.45") Double value) {
		setVTBR(value);
	}
	@Inject
	void initspwkb(@NumberConfig(name="tank17.spwkb", defaultValue="900.0") Double value) {
	 setSPWKB(value);
	}
	@Inject
	void initfawb(@NumberConfig(name="tank17.fawb", defaultValue="3.1705") Double value) {
	 setFAWB(value);
	}
	@Inject
	void initftwb(@NumberConfig(name="tank17.ftwb", defaultValue="3.1705") Double value) {
	 setFTWB(value);
	}
	@Inject
	void initcharmb(@NumberConfig(name="tank17.charmb", defaultValue="0.87") Double value) {
	 setCHARMB(value);
	}
	@Inject
	void initfmawb(@NumberConfig(name="tank17.fmawb", defaultValue="10.0") Double value) {
	 setFMAWB(value);
	}
	@Inject
	void inithgbr(@NumberConfig(name="tank17.hgbr", defaultValue="0.58") Double value) {
	 setHGBR(value);
	}
	@Inject
	void initfuLevel(@ConfigProperty(name="tank17.fuLevel", defaultValue="4.443198E-2 1.89351 -4.598476 7.462374 -5.766856 1.688202 0.0 0.0") String values) {
		setFuLevel(Util.extractDoubleArray(values));
	}	
	@Inject
	void initfuCOutWSfc(@ConfigProperty(name="tank17.fuCOutWSfc", defaultValue="-3.170696 5.466857 -2.74650E-4 0.0 0.0 0.0 0.0 0.0") String values) {
	 setFuCOutWSfc(Util.extractDoubleArray(values));
	}
	@Inject
	void initfuCSepWSfc(@ConfigProperty(name="tank17.fuCSepWSfc", defaultValue="3.17 0.0 0.0 0.0 0.0 0.0 0.0 0.0") String values) {
	 setFuCOutWSfc(Util.extractDoubleArray(values));
	}
	@Inject
	void initfuSfc(@ConfigProperty(name="tank17.fuSfc", defaultValue="2.670068E-2 4.108399 5.992774 -16.44046 7.087217 0.0 0.0 0.0") String values) {
	 setFuSfc(Util.extractDoubleArray(values));
	}
	@Inject
	void initfuCOutWSfc2(@ConfigProperty(name="tank17.fuCOutWSfc2", defaultValue="0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0") String values) {
	 setFuCOutWSfc2(Util.extractDoubleArray(values));
	}
	@Inject
	void initfuCSepWSfc2(@ConfigProperty(name="tank17.fuCSepWSfc2", defaultValue="0.0 5.466383 -2.34043E-5 0.0 0.0 0.0 0.0 0.0") String values) {
	 setFuCSepWSfc2(Util.extractDoubleArray(values));
	}
	@Inject
	void initfuSfc2(@ConfigProperty(name="tank17.fuSfc2", defaultValue="2.670068E-2 4.108399 5.992774 -16.44046 7.087217 0.0 0.0 0.0") String values) {
	 setFuSfc2(Util.extractDoubleArray(values));
	}
	@Inject
	void initvtox(@NumberConfig(name="tank17.vtox", defaultValue="1.30") Double value) {
	 setVTOX(value);
	}
	@Inject
	void initspwko(@NumberConfig(name="tank17.spwko", defaultValue="900.0") Double value) {
	 setSPWKO(value);
	}
	@Inject
	void initfawo(@NumberConfig(name="tank17.fawo", defaultValue="6.3410") Double value) {
	 setFAWO(value);
	}
	@Inject
	void initftwo(@NumberConfig(name="tank17.ftwo", defaultValue="3.2798") Double value) {
	 setFTWO(value);
	}
	@Inject
	void initcharmo(@NumberConfig(name="tank17.charmo", defaultValue=".87") Double value) {
	 setCHARMO(value);
	}
	@Inject
	void initfmawo(@NumberConfig(name="tank17.fmawo", defaultValue="10.0") Double value) {
	 setFMAWO(value);
	}
	@Inject
	void inithgox(@NumberConfig(name="tank17.hgox", defaultValue="0.56") Double value) {
	 setHGOX(value);
	}
	@Inject
	void initoxLevel(@ConfigProperty(name="tank17.oxLevel", defaultValue="2.698326E-2 1.661516 -2.258168 1.639969 -.3203411 0.0 0.0 0.0") String values) {
	 setOxLevel(Util.extractDoubleArray(values));
	}
	@Inject
	void initoxCOutWSfc(@ConfigProperty(name="tank17.oxCOutWSfc", defaultValue="0.0 5.466371 0.0 0.0 0.0 0.0 0.0 0.0") String values) {
	 setOxCOutWSfc(Util.extractDoubleArray(values));
	}
	@Inject
	void initoxCSepWSfc(@ConfigProperty(name="tank17.oxCSepWSfc", defaultValue="-3.061418 5.467036 -3.88878E-4 0.0 0.0 0.0 0.0 0.0") String values) {
	 setOxCSepWSfc(Util.extractDoubleArray(values));
	}
	@Inject
	void initoxSfc(@ConfigProperty(name="tank17.oxSfc", defaultValue="1.446676E-2 5.462355 -13.01409 67.72203 -152.602 132.8362 -39.91667 0.0") String values) {
	 setOxSfc(Util.extractDoubleArray(values));
	}
	@Inject
	void initoxCOutWSfc2(@ConfigProperty(name="tank17.oxCOutWSfc2", defaultValue="0.0 5.466371 0.0 0.0 0.0 0.0 0.0 0.0") String values) {
	 setOxCOutWSfc2(Util.extractDoubleArray(values));
	}
	@Inject
	void initoxCSepWSfc2(@ConfigProperty(name="tank17.oxCSepWSfc2", defaultValue="0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0") String values) {
	 setOxCSepWSfc2(Util.extractDoubleArray(values));
	}
	@Inject
	void initoxSfc2(@ConfigProperty(name="tank17.oxSfc2", defaultValue="1.446676E-2 5.462355 -13.01409 67.72203 -152.602 132.8362 -39.91667 0.0") String values) {
	 setOxSfc2(Util.extractDoubleArray(values));
	}
	@Inject
	void initfmtw(@NumberConfig(name="tank17.fmtw", defaultValue="7.0") Double value) {
	 setFMTW(value);
	}
//	@Inject
//	void initfuel(@ConfigProperty(name="tank17.fuel", defaultValue="MON3") String value) {
//	 setFuel(value);
//	}
	@Inject
	void initfuPressGas(@ConfigProperty(name="tank17.fuPressGas", defaultValue="Helium") String value) {
	 setFuPressGas(value);
	}
	@Inject
	void initptb(@NumberConfig(name="tank17.ptb", defaultValue="18.8") Double value) {
	 setPTB(value);
	}
	@Inject
	void initpendbr(@NumberConfig(name="tank17.pendbr", defaultValue="10.0") Double value) {
	 setPENDBR(value);
	}
	@Inject
	void initvanfbr(@NumberConfig(name="tank17.vanfbr", defaultValue="1.348") Double value) {
	 setVANFBR(value);
	}
	@Inject
	void inittanfbr(@NumberConfig(name="tank17.tanfbr", defaultValue="289.75") Double value) {
	 setTANFBR(value);
	}
	@Inject
	void initoxidizer(@ConfigProperty(name="tank17.oxidizer", defaultValue="N2O4") String value) {
	 setOxidizer(value);
	}
	@Inject
	void initoxPressGas(@ConfigProperty(name="tank17.oxPressGas", defaultValue="Helium") String value) {
	 setOxPressGas(value);
	}
	@Inject
	void initpto(@NumberConfig(name="tank17.pto", defaultValue="17.7") Double value) {
	 setPTO(value);
	}
	@Inject
	void initpendox(@NumberConfig(name="tank17.pendox", defaultValue="9.0") Double value) {
	 setPENDOX(value);
	}
	@Inject
	void initvanfox(@NumberConfig(name="tank17.vanfox", defaultValue="1.238") Double value) {
	 setVANFOX(value);
	}
	@Inject
	void inittanfox(@NumberConfig(name="tank17.tanfox", defaultValue="287.15") Double value) {
	 setTANFOX(value);
	}
	
}
