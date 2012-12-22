package org.osk.models.t2;

import net.gescobar.jmx.annotation.ManagedAttribute;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.osk.models.BaseModel;
import org.osk.models.Tank;
import org.osk.ports.FluidPort;


/**
 * Model definition for a rocket stage fuel/oxidizer tank with
 * medium energetic fuels.
 *
 * @author J. Eickhoff
 * @author P. Heinrich
 * @author A. Brandt
 * @author P. Pita
 */

public class TankT2 extends BaseModel implements Tank  {
	
	/** Fuel type. */
	private String fuel;
	/** Oxidizer type. */
	private String oxidizer;

	/** Pressure gas for fuel compartment. */
	private String fuPressGas;
	/** Pressure gas for oxidizer compartment. */
	private String oxPressGas;
	/** Massflow boundary condition at fuel outlet. */
	private double mfBoundFuelPress;
	/** Massflow boundary condition at oxidizer outlet. */
	private double mfBoundOxPress;
	/** Pressure boundary condition at fuel outlet. */
	private double pBoundFuelPress;
	/** Pressure boundary condition at oxidizer outlet. */
	private double pBoundOxPress;
	/** Fuel type boundary. */
	private double mfBoundFuel;
	/** Oxidizer type boundary. */
	private double mfBoundOx;

	/**
	 * Fluid states for pressurization gas inflows and fuel/oxidizer liquid
	 * outflows.
	 */
	private double pinFPG;
	private double tinFPG;
	private double mfinFPG;
	private double pinOPG;
	private double tinOPG;
	private double mfinOPG;
	private double poutFuel;
	private double toutFuel;
	private double mfoutFuel;
	private double poutOxidizer;
	private double toutOxidizer;
	private double mfoutOxidizer;

	/** Volume of fuel tank. */
	private double VTBR;

	/** Volume of oxidizer tank. */
	private double VTOX;

	/** Outer wall surface of oxidizer compartment. */
	/** Outer wall surface of oxidizer tank. */
	private double FAWO;
	/** Separation wall surface of oxidizer tank. */
	private double FTWO;
	/** Outer wall surface of fuel tank. */
	private double FAWB;
	/** Sep. wall surface of fuel tank. */
	private double FTWB;
	/**
	 * Boundary fill level of oxidizer. Empty to boundary fill level other
	 * polynomials for Surface interpolations are used than for bound. level to
	 * full tank - see tank sketch at top of this file.
	 */
	private double HGOX;
	/**
	 * Boundary fill level of fuel. Empty to boundary fill level other
	 * polynomials for surface interpolations are used than for bound. level to
	 * full tank - see tank sketch at top of this file.
	 */
	private double HGBR;
	/** Characteristic measure for Ox. tank - diameter. */
	private double CHARMO;
	/** Characteristic measure for fuel tank - diameter. */
	private double CHARMB;
	/** Area specific mass of outer wall of ox compartment. */
	private double FMAWO;
	/** Area specific mass of separation wall. */
	private double FMTW;
	/** Area specific mass of outer wall of fuel compartment. */
	private double FMAWB;
	/** Specific heat capacity of oxidizer. */
	private double SPWKO;
	/** Specific heat capacity of fuel. */
	private double SPWKB;

	/** Nominal pressure in ox. compartment. */
	private double PTO;
	/** Pressure of ox at blowdown end. */
	private double PENDOX;
	/** Nominal pressure in fuel compartment. */
	private double PTB;
	/** Pressure of fuel at blowdown end. */
	private double PENDBR;
	/** Ox volume at start. */
	private double VANFOX;
	/** Ox. temp at start. */
	private double TANFOX;
	/** Fuel volume at start. */
	private double VANFBR;
	/** Temp. of fuel at start. */
	private double TANFBR;

	private double PVO;
	private double AO, BO;

	/** Temp. of pressure gas entering tank oxid. compartment. */
	private double THEINO;
	/** Temp. of pressure gas entering tank fuel compartment. */
	private double THEINB;
	/** Mass of ox. compartm. outer wall in contact with gaseous phase. */
	private double MAWGO;
	/** Mass of ox. compartm. sep. wall in contact with gaseous phase. */
	private double MTWGO;
	/** Mass of fuel compartm. outer wall in contact with gaseous phase. */
	private double MAWGB;
	/** Mass of fuel compartm. sep. wall in contact with gaseous phase. */
	private double MTWGB;
	/** Mass flow of pressure gas into tank oxid. compartment. */
	private double MPHEOX;
	/** Mass flow of pressure gas into tank fuel compartment. */
	private double MPHEBR;

	/** Value of MAWGO of previous timestep. */
	private double MAWGOA;
	/** Value of MTWGO of previous timestep. */
	private double MTWGOA;
	/** Value of MAWGB of previous timestep. */
	private double MAWGBA;
	/** Value of MTWGB of previous timestep. */
	private double MTWGBA;
	/** Time of previous timestep. */
	private double ZEITA;

	/** Mass of liquid oxidizer in ox. compartment. */
	private double MLOX;
	/** Mass of liquid fuel in fuel compartment. */
	private double MLBR;
	/** Mass flow of liquid oxidizer to thruster. */
	private double MPKTLO;
	/** Mass flow of liquid fuel to thruster. */
	private double MPKTLB;
	/** Mass of gaseous oxidizer in ox. compartment. */
	private double MDO;

	/** Mass of pressure gas in ox. compartment - previous timestep. */
	private double MHEOXA;
	/** Mass of pressure gas in ox. compartment - previous timestep. */
	private double MHEBRA;

	/**
	 * Polynomial approximations for fuel compartment:
	 * ====================================================
	 * 
	 * Fuel filling level [m] as function of volume [m^3].
	 */
	private double fuLevel[] = new double[8];
	/**
	 * Fuel covered outer wall surface [m^2] as function of filling level [m].
	 */
	private double fuCOutWSfc[] = new double[8];
	/**
	 * Fuel covered separ. wall surface [m^2] as function of filling level [m].
	 */
	private double fuCSepWSfc[] = new double[8];
	/** Free surface of liquid fuel [m^2] as function of filling level [m]. */
	private double fuSfc[] = new double[8];
	/**
	 * For levels below boundary level: -------------------------------- Fuel
	 * covered outer wall surface [m^2] as function of filling level [m].
	 */
	private double fuCOutWSfc2[] = new double[8];
	/**
	 * Fuel covered separ. wall surface [m^2] as function of filling level [m].
	 */
	private double fuCSepWSfc2[] = new double[8];
	/** Free surface of liquid fuel [m^2] as function of filling level [m]. */
	private double fuSfc2[] = new double[8];

	/**
	 * Polynomial approximations for oxidizer compartment:
	 * ====================================================
	 * 
	 * Oxidizer filling level [m] as function of volume [m^3].
	 */
	private double oxLevel[] = new double[8];
	/**
	 * Oxidizer covered outer wall surface [m^2] as function of filling level
	 * [m].
	 */
	private double oxCOutWSfc[] = new double[8];
	/**
	 * Oxidizer covered separ. wall surface [m^2] as function of filling level
	 * [m].
	 */
	private double oxCSepWSfc[] = new double[8];
	/**
	 * Free surface of liquid oxidizer [m^2] as function of filling level [m].
	 */
	private double oxSfc[] = new double[8];

	/**
	 * For levels below boundary level: --------------------------------
	 * 
	 * Oxidizer covered outer wall surface [m^2] as function of filling level
	 * [m].
	 */
	private double oxCOutWSfc2[] = new double[8];
	/**
	 * Oxidizer covered separ. wall surface [m^2] as function of filling level
	 * [m].
	 */
	private double oxCSepWSfc2[] = new double[8];
	/**
	 * Free surface of liquid oxidizer [m^2] as function of filling level [m].
	 */
	private double oxSfc2[] = new double[8];

	/** State variables vector of tank DEQ system. */
	private double YK[] = new double[20];
	/** Max. num. of calls of the Diff() method within one integ. step. */
	private int IFMAX;
	/** Number of necessary calls. */
	private int IFANZ;
	/** Error parameter of the integration routine DEqSys. */
	private int IFEHL;

	/** Blowdown flag. */
	private int BDFLAG;

	/** For printout of TabGenerator. */
	private double poxt;
	private double tGOxT;
	private double tLOxT;
	private double PFuT;
	private double tGFuT;
	private double tLFuT;

	private static final String TYPE = "TankT1";
	private static final String SOLVER = "RKF-4/5";
	

    public TankT2() {
         super(TYPE, SOLVER);
 	}

    @Override
	public void init(String name) {
    	this.name = name;  
    }

    @Override
	public ImmutablePair<FluidPort,  FluidPort> calculateOutletsMassFlow(FluidPort inputPortFuelPressureGas, FluidPort inputPortOxidizerPressureGas) {
        toutFuel = 5000; poutFuel=100000; mfoutFuel=1;
        FluidPort outputPortFuel = new FluidPort(fuel, poutFuel, toutFuel, mfoutFuel);
        toutOxidizer = 5000; poutOxidizer=200000; mfoutOxidizer=2;
        FluidPort outputPortOxidizer = new FluidPort(oxidizer, poutOxidizer, toutOxidizer, mfoutOxidizer);

        return new ImmutablePair<FluidPort,  FluidPort>(outputPortFuel, outputPortOxidizer);
    }


    @Override
	public ImmutablePair<FluidPort,  FluidPort> propagate(final double time, final double tStepSize, 
    		 final FluidPort inputPortOxidizerPressureGas, final FluidPort inputPortFuelPressureGas) {

        FluidPort outputPortFuel = new FluidPort(fuel, poutFuel, toutFuel, mfoutFuel);
        FluidPort outputPortOxidizer = new FluidPort(oxidizer, poutOxidizer, toutOxidizer, mfoutOxidizer);
        return new ImmutablePair<FluidPort,  FluidPort>(outputPortFuel, outputPortOxidizer);
    }

    
    //-----------------------------------------------------------------------------------
    // Methods added for JMX monitoring	and setting initial properties via CDI Extensions

	@Override
	@ManagedAttribute    
    public String getFuel() {
		return fuel;
	}
	@Override
	public void setFuel(String fuel) {
		this.fuel = fuel;
	}
    
	@Override
	@ManagedAttribute    
    public String getOxidizer() {
		return oxidizer;
	}
	@Override
	public void setOxidizer(String oxidizer) {
		this.oxidizer = oxidizer;
	}
	@Override
	@ManagedAttribute    
	public String getFuPressGas() {
		return fuPressGas;
	}
	@Override
	public void setFuPressGas(String fuPressGas) {
		this.fuPressGas = fuPressGas;
	}
	@Override
	@ManagedAttribute    
	public String getOxPressGas() {
		return oxPressGas;
	}
	@Override
	public void setOxPressGas(String oxPressGas) {
		this.oxPressGas = oxPressGas;
	}
	@Override
	@ManagedAttribute    
	public double getMfBoundFuelPress() {
		return mfBoundFuelPress;
	}
	@Override
	public void setMfBoundFuelPress(double mfBoundFuelPress) {
		this.mfBoundFuelPress = mfBoundFuelPress;
	}
	@Override
	@ManagedAttribute    
	public double getMfBoundOxPress() {
		return mfBoundOxPress;
	}
	@Override
	public void setMfBoundOxPress(double mfBoundOxPress) {
		this.mfBoundOxPress = mfBoundOxPress;
	}
	@Override
	@ManagedAttribute    
	public double getpBoundFuelPress() {
		return pBoundFuelPress;
	}
	@Override
	public void setpBoundFuelPress(double pBoundFuelPress) {
		this.pBoundFuelPress = pBoundFuelPress;
	}
	@Override
	@ManagedAttribute    
	public double getpBoundOxPress() {
		return pBoundOxPress;
	}
	@Override
	public void setpBoundOxPress(double pBoundOxPress) {
		this.pBoundOxPress = pBoundOxPress;
	}
	@Override
	@ManagedAttribute    
	public double getMfBoundFuel() {
		return mfBoundFuel;
	}
	@Override
	public void setMfBoundFuel(double mfBoundFuel) {
		this.mfBoundFuel = mfBoundFuel;
	}
	@Override
	@ManagedAttribute    
	public double getMfBoundOx() {
		return mfBoundOx;
	}
	@Override
	public void setMfBoundOx(double mfBoundOx) {
		this.mfBoundOx = mfBoundOx;
	}
	@Override
	@ManagedAttribute    
	public double getPinFPG() {
		return pinFPG;
	}
	@Override
	public void setPinFPG(double pinFPG) {
		this.pinFPG = pinFPG;
	}
	@Override
	@ManagedAttribute    
	public double getTinFPG() {
		return tinFPG;
	}
	@Override
	public void setTinFPG(double tinFPG) {
		this.tinFPG = tinFPG;
	}
	@Override
	@ManagedAttribute    
	public double getMfinFPG() {
		return mfinFPG;
	}
	@Override
	public void setMfinFPG(double mfinFPG) {
		this.mfinFPG = mfinFPG;
	}
	@Override
	@ManagedAttribute    
	public double getPinOPG() {
		return pinOPG;
	}
	@Override
	public void setPinOPG(double pinOPG) {
		this.pinOPG = pinOPG;
	}
	@Override
	@ManagedAttribute    
	public double getTinOPG() {
		return tinOPG;
	}
	@Override
	public void setTinOPG(double tinOPG) {
		this.tinOPG = tinOPG;
	}
	@Override
	@ManagedAttribute    
	public double getMfinOPG() {
		return mfinOPG;
	}
	@Override
	public void setMfinOPG(double mfinOPG) {
		this.mfinOPG = mfinOPG;
	}
	@Override
	@ManagedAttribute    
	public double getPoutFuel() {
		return poutFuel;
	}
	@Override
	public void setPoutFuel(double poutFuel) {
		this.poutFuel = poutFuel;
	}
	@Override
	@ManagedAttribute    
	public double getToutFuel() {
		return toutFuel;
	}
	@Override
	public void setToutFuel(double toutFuel) {
		this.toutFuel = toutFuel;
	}
	@Override
	@ManagedAttribute    
	public double getMfoutFuel() {
		return mfoutFuel;
	}
	@Override
	public void setMfoutFuel(double mfoutFuel) {
		this.mfoutFuel = mfoutFuel;
	}
	@Override
	@ManagedAttribute    
	public double getPoutOxidizer() {
		return poutOxidizer;
	}
	@Override
	public void setPoutOxidizer(double poutOxidizer) {
		this.poutOxidizer = poutOxidizer;
	}
	@Override
	@ManagedAttribute    
	public double getToutOxidizer() {
		return toutOxidizer;
	}
	@Override
	public void setToutOxidizer(double toutOxidizer) {
		this.toutOxidizer = toutOxidizer;
	}
	@Override
	@ManagedAttribute    
	public double getMfoutOxidizer() {
		return mfoutOxidizer;
	}
	@Override
	public void setMfoutOxidizer(double mfoutOxidizer) {
		this.mfoutOxidizer = mfoutOxidizer;
	}
	@Override
	@ManagedAttribute    
	public double getVTBR() {
		return VTBR;
	}
	@Override
	public void setVTBR(double vTBR) {
		VTBR = vTBR;
	}
	@Override
	@ManagedAttribute    
	public double getVTOX() {
		return VTOX;
	}
	@Override
	public void setVTOX(double vTOX) {
		VTOX = vTOX;
	}
	@Override
	@ManagedAttribute    
	public double getFAWO() {
		return FAWO;
	}
	@Override
	public void setFAWO(double fAWO) {
		FAWO = fAWO;
	}
	@Override
	@ManagedAttribute    
	public double getFTWO() {
		return FTWO;
	}
	@Override
	public void setFTWO(double fTWO) {
		FTWO = fTWO;
	}
	@Override
	@ManagedAttribute    
	public double getFAWB() {
		return FAWB;
	}
	@Override
	public void setFAWB(double fAWB) {
		FAWB = fAWB;
	}
	@Override
	@ManagedAttribute    
	public double getFTWB() {
		return FTWB;
	}
	@Override
	public void setFTWB(double fTWB) {
		FTWB = fTWB;
	}
	@Override
	@ManagedAttribute    
	public double getHGOX() {
		return HGOX;
	}
	@Override
	public void setHGOX(double hGOX) {
		HGOX = hGOX;
	}
	@Override
	@ManagedAttribute    
	public double getHGBR() {
		return HGBR;
	}
	@Override
	public void setHGBR(double hGBR) {
		HGBR = hGBR;
	}
	@Override
	@ManagedAttribute    
	public double getCHARMO() {
		return CHARMO;
	}
	@Override
	public void setCHARMO(double cHARMO) {
		CHARMO = cHARMO;
	}
	@Override
	@ManagedAttribute    
	public double getCHARMB() {
		return CHARMB;
	}
	@Override
	public void setCHARMB(double cHARMB) {
		CHARMB = cHARMB;
	}
	@Override
	@ManagedAttribute    
	public double getFMAWO() {
		return FMAWO;
	}
	@Override
	public void setFMAWO(double fMAWO) {
		FMAWO = fMAWO;
	}
	@Override
	@ManagedAttribute    
	public double getFMTW() {
		return FMTW;
	}
	@Override
	public void setFMTW(double fMTW) {
		FMTW = fMTW;
	}
	@Override
	@ManagedAttribute    
	public double getFMAWB() {
		return FMAWB;
	}
	@Override
	public void setFMAWB(double fMAWB) {
		FMAWB = fMAWB;
	}
	@Override
	@ManagedAttribute    
	public double getSPWKO() {
		return SPWKO;
	}
	@Override
	public void setSPWKO(double sPWKO) {
		SPWKO = sPWKO;
	}
	@Override
	@ManagedAttribute    
	public double getSPWKB() {
		return SPWKB;
	}
	@Override
	public void setSPWKB(double sPWKB) {
		SPWKB = sPWKB;
	}
	@Override
	@ManagedAttribute    
	public double getPTO() {
		return PTO;
	}
	@Override
	public void setPTO(double pTO) {
		PTO = pTO;
	}
	@Override
	@ManagedAttribute    
	public double getPENDOX() {
		return PENDOX;
	}
	@Override
	public void setPENDOX(double pENDOX) {
		PENDOX = pENDOX;
	}
	@Override
	@ManagedAttribute    
	public double getPTB() {
		return PTB;
	}
	@Override
	public void setPTB(double pTB) {
		PTB = pTB;
	}
	@Override
	@ManagedAttribute    
	public double getPENDBR() {
		return PENDBR;
	}
	@Override
	public void setPENDBR(double pENDBR) {
		PENDBR = pENDBR;
	}
	@Override
	@ManagedAttribute    
	public double getVANFOX() {
		return VANFOX;
	}
	@Override
	public void setVANFOX(double vANFOX) {
		VANFOX = vANFOX;
	}
	@Override
	@ManagedAttribute    
	public double getTANFOX() {
		return TANFOX;
	}
	@Override
	public void setTANFOX(double tANFOX) {
		TANFOX = tANFOX;
	}
	@Override
	@ManagedAttribute    
	public double getVANFBR() {
		return VANFBR;
	}
	@Override
	public void setVANFBR(double vANFBR) {
		VANFBR = vANFBR;
	}
	@Override
	@ManagedAttribute    
	public double getTANFBR() {
		return TANFBR;
	}
	@Override
	public void setTANFBR(double tANFBR) {
		TANFBR = tANFBR;
	}
	@Override
	@ManagedAttribute    
	public double getPVO() {
		return PVO;
	}
	@Override
	public void setPVO(double pVO) {
		PVO = pVO;
	}
	@Override
	@ManagedAttribute    
	public double getTHEINO() {
		return THEINO;
	}
	@Override
	public void setTHEINO(double tHEINO) {
		THEINO = tHEINO;
	}
	@Override
	@ManagedAttribute    
	public double getTHEINB() {
		return THEINB;
	}
	@Override
	public void setTHEINB(double tHEINB) {
		THEINB = tHEINB;
	}
	@Override
	@ManagedAttribute    
	public double getMAWGO() {
		return MAWGO;
	}
	@Override
	public void setMAWGO(double mAWGO) {
		MAWGO = mAWGO;
	}
	@Override
	@ManagedAttribute    
	public double getMTWGO() {
		return MTWGO;
	}
	@Override
	public void setMTWGO(double mTWGO) {
		MTWGO = mTWGO;
	}
	@Override
	@ManagedAttribute    
	public double getMAWGB() {
		return MAWGB;
	}
	@Override
	public void setMAWGB(double mAWGB) {
		MAWGB = mAWGB;
	}
	@Override
	@ManagedAttribute    
	public double getMTWGB() {
		return MTWGB;
	}
	@Override
	public void setMTWGB(double mTWGB) {
		MTWGB = mTWGB;
	}
	@Override
	@ManagedAttribute    
	public double getMPHEOX() {
		return MPHEOX;
	}
	@Override
	public void setMPHEOX(double mPHEOX) {
		MPHEOX = mPHEOX;
	}
	@Override
	@ManagedAttribute    
	public double getMPHEBR() {
		return MPHEBR;
	}
	@Override
	public void setMPHEBR(double mPHEBR) {
		MPHEBR = mPHEBR;
	}
	@Override
	@ManagedAttribute    
	public double getMAWGOA() {
		return MAWGOA;
	}
	@Override
	public void setMAWGOA(double mAWGOA) {
		MAWGOA = mAWGOA;
	}
	@Override
	@ManagedAttribute    
	public double getMTWGOA() {
		return MTWGOA;
	}
	@Override
	public void setMTWGOA(double mTWGOA) {
		MTWGOA = mTWGOA;
	}
	@Override
	@ManagedAttribute    
	public double getMAWGBA() {
		return MAWGBA;
	}
	@Override
	public void setMAWGBA(double mAWGBA) {
		MAWGBA = mAWGBA;
	}
	@Override
	@ManagedAttribute    
	public double getMTWGBA() {
		return MTWGBA;
	}
	@Override
	public void setMTWGBA(double mTWGBA) {
		MTWGBA = mTWGBA;
	}
	@Override
	@ManagedAttribute    
	public double getMLOX() {
		return MLOX;
	}
	@Override
	public void setMLOX(double mLOX) {
		MLOX = mLOX;
	}
	@Override
	@ManagedAttribute    
	public double getMLBR() {
		return MLBR;
	}
	@Override
	public void setMLBR(double mLBR) {
		MLBR = mLBR;
	}
	@Override
	@ManagedAttribute    
	public double getMPKTLO() {
		return MPKTLO;
	}
	@Override
	public void setMPKTLO(double mPKTLO) {
		MPKTLO = mPKTLO;
	}
	@Override
	@ManagedAttribute    
	public double getMPKTLB() {
		return MPKTLB;
	}
	@Override
	public void setMPKTLB(double mPKTLB) {
		MPKTLB = mPKTLB;
	}
	@Override
	@ManagedAttribute    
	public double getMDO() {
		return MDO;
	}
	@Override
	public void setMDO(double mDO) {
		MDO = mDO;
	}
	@Override
	@ManagedAttribute    
	public double getMHEOXA() {
		return MHEOXA;
	}
	@Override
	public void setMHEOXA(double mHEOXA) {
		MHEOXA = mHEOXA;
	}
	@Override
	@ManagedAttribute    
	public double getMHEBRA() {
		return MHEBRA;
	}
	@Override
	public void setMHEBRA(double mHEBRA) {
		MHEBRA = mHEBRA;
	}
	@Override
	@ManagedAttribute    
	public double[] getFuLevel() {
		return fuLevel;
	}
	@Override
	public void setFuLevel(double[] fuLevel) {
		this.fuLevel = fuLevel;
	}
	@Override
	@ManagedAttribute    
	public double[] getFuCOutWSfc() {
		return fuCOutWSfc;
	}
	@Override
	public void setFuCOutWSfc(double[] fuCOutWSfc) {
		this.fuCOutWSfc = fuCOutWSfc;
	}
	@Override
	@ManagedAttribute    
	public double[] getFuCSepWSfc() {
		return fuCSepWSfc;
	}
	@Override
	public void setFuCSepWSfc(double[] fuCSepWSfc) {
		this.fuCSepWSfc = fuCSepWSfc;
	}
	@Override
	@ManagedAttribute    
	public double[] getFuSfc() {
		return fuSfc;
	}
	@Override
	public void setFuSfc(double[] fuSfc) {
		this.fuSfc = fuSfc;
	}
	@Override
	@ManagedAttribute    
	public double[] getFuCOutWSfc2() {
		return fuCOutWSfc2;
	}
	@Override
	public void setFuCOutWSfc2(double[] fuCOutWSfc2) {
		this.fuCOutWSfc2 = fuCOutWSfc2;
	}
	@Override
	@ManagedAttribute    
	public double[] getFuCSepWSfc2() {
		return fuCSepWSfc2;
	}
	@Override
	public void setFuCSepWSfc2(double[] fuCSepWSfc2) {
		this.fuCSepWSfc2 = fuCSepWSfc2;
	}
	@Override
	@ManagedAttribute    
	public double[] getFuSfc2() {
		return fuSfc2;
	}
	@Override
	public void setFuSfc2(double[] fuSfc2) {
		this.fuSfc2 = fuSfc2;
	}
	@Override
	@ManagedAttribute    
	public double[] getOxLevel() {
		return oxLevel;
	}
	@Override
	public void setOxLevel(double[] oxLevel) {
		this.oxLevel = oxLevel;
	}
	@Override
	@ManagedAttribute    
	public double[] getOxCOutWSfc() {
		return oxCOutWSfc;
	}
	@Override
	public void setOxCOutWSfc(double[] oxCOutWSfc) {
		this.oxCOutWSfc = oxCOutWSfc;
	}
	@Override
	@ManagedAttribute    
	public double[] getOxCSepWSfc() {
		return oxCSepWSfc;
	}
	@Override
	public void setOxCSepWSfc(double[] oxCSepWSfc) {
		this.oxCSepWSfc = oxCSepWSfc;
	}
	@Override
	@ManagedAttribute    
	public double[] getOxSfc() {
		return oxSfc;
	}
	@Override
	public void setOxSfc(double[] oxSfc) {
		this.oxSfc = oxSfc;
	}
	@Override
	@ManagedAttribute    
	public double[] getOxCOutWSfc2() {
		return oxCOutWSfc2;
	}
	@Override
	public void setOxCOutWSfc2(double[] oxCOutWSfc2) {
		this.oxCOutWSfc2 = oxCOutWSfc2;
	}
	@Override
	@ManagedAttribute    
	public double[] getOxCSepWSfc2() {
		return oxCSepWSfc2;
	}
	@Override
	public void setOxCSepWSfc2(double[] oxCSepWSfc2) {
		this.oxCSepWSfc2 = oxCSepWSfc2;
	}
	@Override
	@ManagedAttribute    
	public double[] getOxSfc2() {
		return oxSfc2;
	}
	@Override
	public void setOxSfc2(double[] oxSfc2) {
		this.oxSfc2 = oxSfc2;
	}
	@Override
	@ManagedAttribute    
	public int getIFMAX() {
		return IFMAX;
	}
	@Override
	public void setIFMAX(int iFMAX) {
		IFMAX = iFMAX;
	}
	@Override
	@ManagedAttribute    
	public int getIFANZ() {
		return IFANZ;
	}
	@Override
	public void setIFANZ(int iFANZ) {
		IFANZ = iFANZ;
	}
	@Override
	@ManagedAttribute    
	public int getIFEHL() {
		return IFEHL;
	}
	@Override
	public void setIFEHL(int iFEHL) {
		IFEHL = iFEHL;
	}
	@Override
	@ManagedAttribute    
	public int getBDFLAG() {
		return BDFLAG;
	}
	@Override
	public void setBDFLAG(int bDFLAG) {
		BDFLAG = bDFLAG;
	}
	@Override
	@ManagedAttribute    
	public double getPoxt() {
		return poxt;
	}
	@Override
	public void setPoxt(double poxt) {
		this.poxt = poxt;
	}
	@Override
	@ManagedAttribute    
	public double gettGOxT() {
		return tGOxT;
	}
	@Override
	public void settGOxT(double tGOxT) {
		this.tGOxT = tGOxT;
	}
	@Override
	@ManagedAttribute    
	public double gettLOxT() {
		return tLOxT;
	}
	@Override
	public void settLOxT(double tLOxT) {
		this.tLOxT = tLOxT;
	}
	@Override
	@ManagedAttribute    
	public double getPFuT() {
		return PFuT;
	}
	@Override
	public void setPFuT(double pFuT) {
		PFuT = pFuT;
	}
	@Override
	@ManagedAttribute    
	public double gettGFuT() {
		return tGFuT;
	}
	@Override
	public void settGFuT(double tGFuT) {
		this.tGFuT = tGFuT;
	}
	@Override
	@ManagedAttribute    
	public double gettLFuT() {
		return tLFuT;
	}
	@Override
	public void settLFuT(double tLFuT) {
		this.tLFuT = tLFuT;
	}
}
