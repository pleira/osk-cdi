package org.osk.models.astris.parts;

import javax.enterprise.event.Event;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.deltaspike.core.api.config.annotation.ConfigProperty;
import org.osk.config.NumberConfig;
import org.osk.config.Util;
import org.osk.events.BackIter;
import org.osk.events.Fuel;
import org.osk.events.Iter;
import org.osk.events.Oxid;
import org.osk.events.TimeIter;
import org.osk.models.rocketpropulsion.TankT1;
import org.osk.ports.FluidPort;

public class Tank17 {

	public final static String NAME = "Tank17";

	@Inject TankT1 model;
	@Inject @Named(NAME) @Oxid @Iter Event<FluidPort> eventOxid;
	@Inject @Named(NAME) @Fuel @Iter Event<FluidPort> eventFuel;
	@Inject @Named(NAME) @Oxid @TimeIter Event<FluidPort> outputEventOxid;
	@Inject @Named(NAME) @Fuel @TimeIter Event<FluidPort> outputEventFuel;
	@Inject @Named(Pipe13.NAME) @BackIter Event<FluidPort> backEvent13;
	@Inject @Named(Pipe16.NAME) @BackIter Event<FluidPort> backEvent16;

	FluidPort inputFuel;
	FluidPort inputOx;
	FluidPort outputFuel;
	FluidPort outputOx;

	public void iterationFuel(
			@Observes @Named(Pipe13.NAME) @Iter FluidPort inputPort) {
		inputFuel = inputPort;
		if (inputOx != null) {
			fireIterationStep();
		}
	}

	public void iterationOxid(
			@Observes @Named(Pipe16.NAME) @Iter FluidPort inputPort) {
		inputOx = inputPort;
		if (inputFuel != null) {
			fireIterationStep();
		}
	}

	public void timeIterationFuel(
			@Observes @Named(Pipe13.NAME) @TimeIter FluidPort inputPort) {
		inputFuel = inputPort;
		if (inputOx != null) {
			fireTimeIteration();
		}
	}

	public void timeIterationOxid(
			@Observes @Named(Pipe16.NAME) @TimeIter FluidPort inputPort) {
		inputOx = inputPort;
		if (inputFuel != null) {
			fireTimeIteration();
		}
	}

	public void backIterateFuel(
			@Observes @Named(NAME) @BackIter @Fuel FluidPort outputPort) {
		outputFuel = outputPort;
		if (outputOx != null) {
			fireBackIteration();
		}
	}

	public void backIterateOxid(
			@Observes @Named(NAME) @BackIter @Oxid FluidPort outputPort) {
		outputOx = outputPort;
		if (outputFuel != null) {
			fireBackIteration();
		}
	}

	private void fireIterationStep() {
		ImmutablePair<FluidPort, FluidPort> output = model.iterationStep(
				inputFuel, inputOx);
		eventOxid.fire(output.getRight());
		eventFuel.fire(output.getLeft());
		inputFuel = inputOx = null; // events processed
	}

	private void fireTimeIteration() {
		ImmutablePair<FluidPort, FluidPort> output = model.timeStep(inputFuel,
				inputOx);
		outputEventOxid.fire(output.getRight());
		outputEventFuel.fire(output.getLeft());
		inputFuel = inputOx = null; // events processed
	}

	private void fireBackIteration() {
		ImmutablePair<FluidPort, FluidPort> input = model.backIterStep(
				outputFuel, outputOx);
		backEvent13.fire(input.getLeft());
		backEvent16.fire(input.getRight());
		outputFuel = outputOx = null; // events processed
	}

	// ---------------------------------------------------------------------------------------
	// Initialisation values

	@Inject
	void initVtbr(
			@NumberConfig(name = "tank17.vtbr", defaultValue = "1.45") Double value) {
		model.setVTBR(value);
	}

	@Inject
	void initspwkb(
			@NumberConfig(name = "tank17.spwkb", defaultValue = "900.0") Double value) {
		model.setSPWKB(value);
	}

	@Inject
	void initfawb(
			@NumberConfig(name = "tank17.fawb", defaultValue = "3.1705") Double value) {
		model.setFAWB(value);
	}

	@Inject
	void initftwb(
			@NumberConfig(name = "tank17.ftwb", defaultValue = "3.1705") Double value) {
		model.setFTWB(value);
	}

	@Inject
	void initcharmb(
			@NumberConfig(name = "tank17.charmb", defaultValue = "0.87") Double value) {
		model.setCHARMB(value);
	}

	@Inject
	void initfmawb(
			@NumberConfig(name = "tank17.fmawb", defaultValue = "10.0") Double value) {
		model.setFMAWB(value);
	}

	@Inject
	void inithgbr(
			@NumberConfig(name = "tank17.hgbr", defaultValue = "0.58") Double value) {
		model.setHGBR(value);
	}

	@Inject
	void initfuLevel(
			@ConfigProperty(name = "tank17.fuLevel", defaultValue = "4.443198E-2 1.89351 -4.598476 7.462374 -5.766856 1.688202 0.0 0.0") String values) {
		model.setFuLevel(Util.extractDoubleArray(values));
	}

	@Inject
	void initfuCOutWSfc(
			@ConfigProperty(name = "tank17.fuCOutWSfc", defaultValue = "-3.170696 5.466857 -2.74650E-4 0.0 0.0 0.0 0.0 0.0") String values) {
		model.setFuCOutWSfc(Util.extractDoubleArray(values));
	}

	@Inject
	void initfuCSepWSfc(
			@ConfigProperty(name = "tank17.fuCSepWSfc", defaultValue = "3.17 0.0 0.0 0.0 0.0 0.0 0.0 0.0") String values) {
		model.setFuCOutWSfc(Util.extractDoubleArray(values));
	}

	@Inject
	void initfuSfc(
			@ConfigProperty(name = "tank17.fuSfc", defaultValue = "2.670068E-2 4.108399 5.992774 -16.44046 7.087217 0.0 0.0 0.0") String values) {
		model.setFuSfc(Util.extractDoubleArray(values));
	}

	@Inject
	void initfuCOutWSfc2(
			@ConfigProperty(name = "tank17.fuCOutWSfc2", defaultValue = "0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0") String values) {
		model.setFuCOutWSfc2(Util.extractDoubleArray(values));
	}

	@Inject
	void initfuCSepWSfc2(
			@ConfigProperty(name = "tank17.fuCSepWSfc2", defaultValue = "0.0 5.466383 -2.34043E-5 0.0 0.0 0.0 0.0 0.0") String values) {
		model.setFuCSepWSfc2(Util.extractDoubleArray(values));
	}

	@Inject
	void initfuSfc2(
			@ConfigProperty(name = "tank17.fuSfc2", defaultValue = "2.670068E-2 4.108399 5.992774 -16.44046 7.087217 0.0 0.0 0.0") String values) {
		model.setFuSfc2(Util.extractDoubleArray(values));
	}

	@Inject
	void initvtox(
			@NumberConfig(name = "tank17.vtox", defaultValue = "1.30") Double value) {
		model.setVTOX(value);
	}

	@Inject
	void initspwko(
			@NumberConfig(name = "tank17.spwko", defaultValue = "900.0") Double value) {
		model.setSPWKO(value);
	}

	@Inject
	void initfawo(
			@NumberConfig(name = "tank17.fawo", defaultValue = "6.3410") Double value) {
		model.setFAWO(value);
	}

	@Inject
	void initftwo(
			@NumberConfig(name = "tank17.ftwo", defaultValue = "3.2798") Double value) {
		model.setFTWO(value);
	}

	@Inject
	void initcharmo(
			@NumberConfig(name = "tank17.charmo", defaultValue = ".87") Double value) {
		model.setCHARMO(value);
	}

	@Inject
	void initfmawo(
			@NumberConfig(name = "tank17.fmawo", defaultValue = "10.0") Double value) {
		model.setFMAWO(value);
	}

	@Inject
	void inithgox(
			@NumberConfig(name = "tank17.hgox", defaultValue = "0.56") Double value) {
		model.setHGOX(value);
	}

	@Inject
	void initoxLevel(
			@ConfigProperty(name = "tank17.oxLevel", defaultValue = "2.698326E-2 1.661516 -2.258168 1.639969 -.3203411 0.0 0.0 0.0") String values) {
		model.setOxLevel(Util.extractDoubleArray(values));
	}

	@Inject
	void initoxCOutWSfc(
			@ConfigProperty(name = "tank17.oxCOutWSfc", defaultValue = "0.0 5.466371 0.0 0.0 0.0 0.0 0.0 0.0") String values) {
		model.setOxCOutWSfc(Util.extractDoubleArray(values));
	}

	@Inject
	void initoxCSepWSfc(
			@ConfigProperty(name = "tank17.oxCSepWSfc", defaultValue = "-3.061418 5.467036 -3.88878E-4 0.0 0.0 0.0 0.0 0.0") String values) {
		model.setOxCSepWSfc(Util.extractDoubleArray(values));
	}

	@Inject
	void initoxSfc(
			@ConfigProperty(name = "tank17.oxSfc", defaultValue = "1.446676E-2 5.462355 -13.01409 67.72203 -152.602 132.8362 -39.91667 0.0") String values) {
		model.setOxSfc(Util.extractDoubleArray(values));
	}

	@Inject
	void initoxCOutWSfc2(
			@ConfigProperty(name = "tank17.oxCOutWSfc2", defaultValue = "0.0 5.466371 0.0 0.0 0.0 0.0 0.0 0.0") String values) {
		model.setOxCOutWSfc2(Util.extractDoubleArray(values));
	}

	@Inject
	void initoxCSepWSfc2(
			@ConfigProperty(name = "tank17.oxCSepWSfc2", defaultValue = "0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0") String values) {
		model.setOxCSepWSfc2(Util.extractDoubleArray(values));
	}

	@Inject
	void initoxSfc2(
			@ConfigProperty(name = "tank17.oxSfc2", defaultValue = "1.446676E-2 5.462355 -13.01409 67.72203 -152.602 132.8362 -39.91667 0.0") String values) {
		model.setOxSfc2(Util.extractDoubleArray(values));
	}

	@Inject
	void initfmtw(
			@NumberConfig(name = "tank17.fmtw", defaultValue = "7.0") Double value) {
		model.setFMTW(value);
	}

	// @Inject
	// void initfuel(@ConfigProperty(name="tank17.fuel", defaultValue="MON3")
	// String value) {
	// model.setFuel(value);
	// }
	@Inject
	void initfuPressGas(
			@ConfigProperty(name = "tank17.fuPressGas", defaultValue = "Helium") String value) {
		model.setFuPressGas(value);
	}

	@Inject
	void initptb(
			@NumberConfig(name = "tank17.ptb", defaultValue = "18.8") Double value) {
		model.setPTB(value);
	}

	@Inject
	void initpendbr(
			@NumberConfig(name = "tank17.pendbr", defaultValue = "10.0") Double value) {
		model.setPENDBR(value);
	}

	@Inject
	void initvanfbr(
			@NumberConfig(name = "tank17.vanfbr", defaultValue = "1.348") Double value) {
		model.setVANFBR(value);
	}

	@Inject
	void inittanfbr(
			@NumberConfig(name = "tank17.tanfbr", defaultValue = "289.75") Double value) {
		model.setTANFBR(value);
	}

	@Inject
	void initoxidizer(
			@ConfigProperty(name = "tank17.oxidizer", defaultValue = "N2O4") String value) {
		model.setOxidizer(value);
	}

	@Inject
	void initoxPressGas(
			@ConfigProperty(name = "tank17.oxPressGas", defaultValue = "Helium") String value) {
		model.setOxPressGas(value);
	}

	@Inject
	void initpto(
			@NumberConfig(name = "tank17.pto", defaultValue = "17.7") Double value) {
		model.setPTO(value);
	}

	@Inject
	void initpendox(
			@NumberConfig(name = "tank17.pendox", defaultValue = "9.0") Double value) {
		model.setPENDOX(value);
	}

	@Inject
	void initvanfox(
			@NumberConfig(name = "tank17.vanfox", defaultValue = "1.238") Double value) {
		model.setVANFOX(value);
	}

	@Inject
	void inittanfox(
			@NumberConfig(name = "tank17.tanfox", defaultValue = "287.15") Double value) {
		model.setTANFOX(value);
	}

}
