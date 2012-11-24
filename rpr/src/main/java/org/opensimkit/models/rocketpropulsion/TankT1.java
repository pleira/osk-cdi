/*
 * TankT1.java
 *
 * Created on 8. Juli 2007, 21:39
 *
 *  Model definition for a rocket stage fuel/oxidizer tank with
 *  medium energetic fuels.
 *
 *                                V  inputPortFuelPressureGas
 *                                |  (Pressure Gas Inlet)
 *                                |
 *                              ++++++
 *                          ++++      ++++
 *                      ++++              ++++
 *                   +++----------------------+++
 *                 ++            Fuel            ++
 *                +                                +
 *               + ++                            ++ +
 *              +    +                          +    +
 *             +      +                        +      +
 *             +       ++                    ++       +
 *            +          +++              +++          +
 *            +-------------+++        +++-------------+---<--  inputPort
 *            +                ++++++++                +    OxidizerPressureGas
 *             +                    |                  +    (Pressure Gas Inlet)
 *             +                    |                 +
 *              +                   |                +
 *               +    Oxidizer      |               +
 *                +                 |              +
 *                 ++               |            ++
 *                   +++            |         +++
 *                      ++++        |     ++++
 *                          ++++    | ++++
 *                              ++++|+
 *                               |  |
 *                               |  |
 *                               V  V
 *                       Oxidizer    Fuel
 *                         outlet    outlet
 *             outputPortOxidizer    outputPortFuel
 *
 *  The pressure vessel object computes the following phenomena for each
 *  compartment:
 *    - Pressurization of tank.
 *    - Heat transfer from tank walls to gas phase.
 *    - Heat transfer from tank walls to liquid phase.
 *    - Heat transfer between liquid and gas phase.
 *    - Compartments are thermally coupled via separation wall.
 *    - Condensation of oxidizer due to temperature drop in tank during stage
 *      operation.
 *  Assumed is that the tank can be recognized adiabatic to
 *  the environment. Furthermore it is assumed that the vapour pressure of fuel
 *  (e.g. MMH) can be recognized as neglectable at stage operation start
 *  temperature (ca. 300K). Thus fuel condensation effects are neglected. The
 *  complex
 *  geometrics that influence how much of the tank walls is covered by liquid
 *  phase or gas phase is dynamically computed. Polynomial descriptions read
 *  from the input deck specify geometric conditions such as free surface of
 *  fluid as function of filling level in tank.
 *
 *  The tank requires fuel and oxidizer outlet mass flow rates to be specified
 *  and therefrom computes the required pressure gas amounts for fuel and
 *  oxidizer compartment.
 *
 *  Component physics for rocket tank pressurization systems are taken from:
 *
 *    [1]
 *    Eickhoff, J.:
 *    Erstellung und Programmierung eines Rechenverfahrens zur
 *    thermodynamischen Erfassung des Druckgas-Foerdersystems der
 *    ARIANE L5-Stufe und Berechnung des noetigen Heliumbedarfs zur
 *    Treibstoffoerderung.
 *    Studienarbeit am Institut fuer Thermodynamik der Luft- und Raumfahrt
 *    Universitaet Stuttgart, Pfaffenwaldring 31, 7000 Stuttgart 80, 1988
 *
 *-----------------------------------------------------------------------------
 * Modification History:
 *
 *  2004-12-05
 *      File created  J. Eickhoff:
 *
 *      Class architecture is a derivative from ObjectSim 2.0.3.,
 *      a simulation program published in:
 *
 *        Eickhoff, J.:
 *        Modulare Programmarchitektur fuer ein wissensbasiertes
 *        Simulationssystem mit erweiterter Anwendbarkeit in der
 *        Entwicklung und Betriebsberwachung verfahrenstechnischer
 *        Anlagen.
 *        PhD thesis in Department Process Engineering of
 *        TU Hamburg-Harburg, 1996.
 *
 *      See also file history cited there and see historic relation of
 *      this OpenSimKit class to a.m. ObjectSim explained in
 *      OpenSimKit Documentation.
 *
 *      File under GPL  see OpenSimKit Documentation.
 *
 *      No warranty and liability for correctness by author.
 *
 *
 *
 *  2005-09
 *      OpenSimKit V 2.2
 *      Modifications enterd for XML input file parsing by
 *        Peter Heinrich  peterhe@student.ethz.ch
 *
 *  2008-05
 *      OpenSimKit V 2.4
 *      Ported from C++ to Java
 *      A. Brandt  alexander.brandt@gmail.com
 *
 *
 *  2009-01
 *      Diverse minor cleanups and entire textual translation to english.
 *      J. Eickhoff
 *
 *  2009-04
 *      Replaced the port array by named ports.
 *      A. Brandt
 *
 *  2009-07
 *      OpenSimKit V 2.8
 *      Upgraded for handling of new port classes.
 *      A. Brandt
 */
package org.opensimkit.models.rocketpropulsion;

import java.io.FileWriter;
import java.io.IOException;

import javax.annotation.PostConstruct;

import net.gescobar.jmx.annotation.ManagedAttribute;

import org.opensimkit.BaseModel;
import org.opensimkit.DEQClient;
import org.opensimkit.DEqSys;
import org.opensimkit.GLoad;
import org.opensimkit.MaterialProperties;
import org.opensimkit.SimHeaders;
import org.opensimkit.manipulation.Manipulatable;
import org.opensimkit.manipulation.Readable;
import org.opensimkit.ports.PureGasPort;
import org.opensimkit.ports.PureLiquidPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Model definition for a rocket stage fuel/oxidizer tank with
 * medium energetic fuels.
 *
 * @author J. Eickhoff
 * @author P. Heinrich
 * @author A. Brandt
 * @version 1.3
 * @since 2.4.0
 */
public abstract class TankT1 extends BaseModel implements DEQClient {
	/** Logger instance for the TankT1. */
	private static final Logger LOG = LoggerFactory.getLogger(TankT1.class);
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
	private static final double MAXTSTEP = 5.0;
	private static final double MINTSTEP = 0.001;
	private static final int TIMESTEP = 1;
	private static final int REGULSTEP = 0;

	private PureGasPort inputPortFuelPressureGas;
	private PureGasPort inputPortOxidizerPressureGas;
	private PureLiquidPort outputPortFuel;
	private PureLiquidPort outputPortOxidizer;

    public TankT1(String name, 
    		PureLiquidPort outputPortFuel, PureLiquidPort outputPortOxidizer, 
    		PureGasPort inputPortFuelPressureGas, PureGasPort inputPortOxidizerPressureGas) {
         super(name, TYPE, SOLVER, MAXTSTEP, MINTSTEP, TIMESTEP, REGULSTEP);
 		this.outputPortFuel = outputPortFuel;
 		this.outputPortOxidizer = outputPortOxidizer;
 		this.inputPortFuelPressureGas = inputPortFuelPressureGas;
 		this.inputPortOxidizerPressureGas = inputPortOxidizerPressureGas;
 	}

    @Override
 	@PostConstruct
    public void init() {
    	completeConnections();
    	
        double RSPOXD = 90.372;
        double RSPHE  = 2077;

        // Computation of derived design parameters
        //---------------------------------------------------------------------
        //
        PTB=PTB*1.E5;
        PENDBR=PENDBR*1.E5;
        PTO=PTO*1.E5;
        PENDOX=PENDOX*1.E5;

        // Computation of derived initialization parameters
        //---------------------------------------------------------------------
        //
        // Initializing pressure in ox. and fuel compartment
        YK[3]= PTO;
        YK[9]= PTB;
        //
        // Initializing boundary conditions pressure definition for
        // inflow of pressure gas
        pBoundFuelPress = PTB;
        pBoundOxPress = PTO;
        //
        // Initializing blowdown flag
        BDFLAG=0;
        //
        // Initializing ox. and fuel masses in compartment
        MLOX=VANFOX*(2066.0-1.979*TANFOX-4.826E-4*Math.pow(TANFOX,2));
        MLBR=VANFBR*(1150.34-.93949*TANFBR);
        YK[18]=MLOX;
        YK[19]=MLBR;
        //
        // Initializing volume of gaseous phase in ox. compartment
        YK[2]=VTOX-VANFOX;
        //
        // Initializing temperature. of gaseous and liquid phase in oxidizer
        // compartment
        YK[1]=TANFOX;
        YK[4]=TANFOX;
        //
        // Initializing wall temperature. ox. compartment
        for(int j=5;j<9;j++) {
            YK[j]=TANFOX;
        }
        //
        // Initializing oxidizer vapour pressure
        if(TANFOX < 233) {
            AO=-2881.007;
            BO=13.4;
        }
        if(TANFOX >= 233)
            if(TANFOX < 263) {
            AO=-2373.331;
            BO=11.214;
            } else {
            AO=-1741.348;
            BO=8.814;
            }

        PVO=Math.pow(10,(AO/TANFOX+BO))*133.332;
        //
        // Initializing mass of oxidizer vapour in gaseous phase in ox.
        // compartment.
        MDO=PVO*YK[2]/(TANFOX*RSPOXD);
        //
        // Initializing mass of pressure gas in gaseous phase in ox. compartment
        YK[0]=(YK[3]-PVO)*YK[2]/(TANFOX*RSPHE);
        MHEOXA=YK[0];
        //
        // Initializing volume of gaseous phase in fuel compartment
        YK[13]=VTBR-VANFBR;
        //
        // Initializing mass of pressure gas in fuel compartment
        YK[10]=YK[9]*YK[13]/(RSPHE*TANFBR);
        MHEBRA=YK[10];
        //
        // Initializing temperature. of gaseous and liquid phase in fuel
        // compartment
        YK[11]=TANFBR;
        YK[12]=TANFBR;
        //
        // Initializing wall temperature. of fuel compartment
        for(int j=14;j<18;j++) {
            YK[j]=TANFBR;
        }
        //
        // Initializing mass flow rates of pressure gas into tank compartments
        MPHEOX=0.;
        MPHEBR=0.;
        mfBoundFuelPress = 0.;
        mfBoundOxPress = 0.;
        //
        // Initializing numeric settings for DEQ solver
        // epsabs and epsrel now are global variables for all components
        IFMAX=4;
        ZEITA=-.5;
    }

    void completeConnections() {
     	inputPortFuelPressureGas.setToModel(this);
     	inputPortOxidizerPressureGas.setToModel(this);
     	outputPortFuel.setFromModel(this);
     	outputPortOxidizer.setFromModel(this);
    	LOG.info("completeConnections for " + name + ", (" + inputPortFuelPressureGas.getName()  + "," + inputPortOxidizerPressureGas.getName() + "," + outputPortFuel.getName() + "," + outputPortOxidizer.getName() + ")" );
     }

    public int DEQDeriv(final double X, final double Y[], final int N,
            final double F[]) {
        MaterialProperties Helium_ox = new MaterialProperties();
        MaterialProperties Helium_brenn = new MaterialProperties();
        double CPHE,CVHE,RALLG,RSPHE,RSPOXD,MMOLHE,MMOLO,C,D,E,CB,DB;
        double RSPMO,DICHGO,DICHLO,DICHLB,NHEO,NOG,NGES;

        double POXD,VLOX,VLBR,PEGLO,PEGLB,LAMOL,LAMOG,LAMGO,LAMBL;
        double ETALB,GRAWGO,GRAWLO,GRAWGB,GRAWLB,GRTWGO,GRTWLO;
        double GRLGO,GRTWGB,GRTWLB,GRLGB;
        double CPLO,CPLB,CPOG,CPGO,CVOG,CVGO,PRGO,PRLO,PRGB,PRLB;
        double NULGO,NULGB,ALAWGO,ALAWLO,ALAWGB,ALAWLB,ALTWGO,ALTWLO,ALTWGB;
        double ALTWLB,ALLGO,ALLGB,QPAWGO,QPTWGO,QPTWLO;
        double QPLGO,QPAWGB,QPAWLB,QPTWGB,QPTWLB,QPLGB;
        double FAWLO,FTWLO,FFO,FAWLB,FTWLB,FFB;
        double FAWGO,FTWGO,FAWGB,FTWGB,YHEO,YDO,ETAGO,ETAOL,ETAOG;
        double QPAWLO,SUQPLO,SUQPGO,SUQPLB,SUQPGB,HHEINO,HHEINB,VOLOUT,VBLOUT;
        double NUAWGO,NUAWLO,NUTWGO,NUTWLO,NUAWGB,NUAWLB,NUTWGB,NUTWLB;
        double KO = 0,LO = 0,MPAWGO,MPTWGO,MPAWGB,MPTWGB;// JH MAWGBA;
        double MAWLO,MTWLO,MAWLB,MTWLB,VPTGBR = 0,FB = 0,PHEO;
        int I;

        LOG.info("Tank: Computing derivations of state variables of DEQ"
                + " system");

        CPHE=5223.2;
        CVHE=3146.5;
        RALLG=8314.3;
        RSPHE=2077;
        RSPOXD=90.372;
        MMOLHE=4.003E-3;
        MMOLO=92.011E-3;
        C=2066.0;
        D=-1.979;
        E=-4.826E-4;
        CB=1150.34;
        DB=-.93949;

        /*********************************************************************/
        /*                                                                   */
        /*    The programme computes the derivatives vector F(I) of          */
        /*    the state variables Y(I) over the entire tank (ox. & fuel      */
        /*    compartment and tank walls (for heat flow from walls to fluids)*/
        /*                                                                   */
        /*********************************************************************/

        //  if (X == 0.){
        //    Y[18]=MLOX;
        //    Y[19]=MLBR;
        //  }

        F[18]=-1.*mfBoundOx;
        F[19]=-1.*mfBoundFuel;

        /*******************************************************************/
        /*                                                                 */
        /*    Computing the specific gas constant of the ox. vapour /      */
        /*    pressure gas mixture in ox. compartment.                     */
        /*    Computing density of mixture.                                */
        /*                                                                 */
        /*******************************************************************/
        RSPMO=(Y[0]*RSPHE+MDO*RSPOXD)/(Y[0]+MDO);
        DICHGO=Y[3]/(RSPMO*Y[1]);

        /*******************************************************************/
        /*                                                                 */
        /*    The algorithm for solving the DEQ system in tank needs       */
        /*    the to be integrated parameter and state variables as e.g.   */
        /*       .           .          .                                  */
        /*       M           T          T       etc                        */
        /*        GAS         GAS        L                                 */
        /*                                                                 */
        /*    in vector form - e.g. like:                                  */
        /*              .                                                  */
        /*     F(1)=    T    = F1(t,  m    ,m   ,p  ,T ,  P....)           */
        /*               GAS           GAS    D                            */
        /*      .       .      .                .                          */
        /*      .       .      .                .                          */
        /*      .       .      .                .                          */
        /*      .       .      .                .                          */
        /*                                                                 */
        /*******************************************************************/
        /*                                                                 */
        /*    Here the vectors are defined                                 */
        /*    F(1)  to  F(N) , as well as X and Y(1) to Y(N) .             */
        /*                                                                 */
        /*    X=t                 Time                                     */
        /*                                                                 */
        /*    F(0)=MSHEOX         Mass flow of press. gas to ox. tank      */
        /*                                                                 */
        /*    F(1)=TPKTGO         Temp. gradient of gas phase in ox tank   */
        /*                                                                 */
        /*    F(2)=VPKTGO         Volume gradient of gas phase in ox tank  */
        /*                                                                 */
        /*    F(3)=PGTOX          Pressure gradient in ox tank             */
        /*                          (only for blowdown mode)               */
        /*                                                                 */
        /*    F(4)=TPKTLO         Temp. gradient of liquid oxidizer        */
        /*                                                                 */
        /*    F(5)=TGAWGO         Temp gradient of ox tank outer wall      */
        /*                          with gas contact                       */
        /*                                                                 */
        /*    F(6)=TGAWLO         Temp gradient of ox tank outer wall      */
        /*                          with liquid contact                    */
        /*                                                                 */
        /*    F(7)=TGTWGO         Temp gradient of ox tank sep. wall       */
        /*                          with gas contact                       */
        /*                                                                 */
        /*    F(8)=TGTWLO         Temp gradient of ox tank sep. wall       */
        /*                          with liquid contact                    */
        /*                                                                 */
        /*    F(9)=PGTBR          Pressure gradient in fuel tank           */
        /*                          (only for blowdown mode)               */
        /*                                                                 */
        /*    F(10)=MSHEBR        Massflow of press gas into fuel tank     */
        /*                                                                 */
        /*    F(11)=TPKTGB        Temp gradient of gas phase in fuel tank  */
        /*                                                                 */
        /*    F(12)=TPKTLB        Temp grad of liquid phase in fuel tank   */
        /*                                                                 */
        /*    F(13)=VPKTGB        Volume gradient of gas ph in fuel tank   */
        /*                                                                 */
        /*    F(14)=TGAWGB        Temp gradient of fuel tank outer wall    */
        /*                          with gas contact                       */
        /*                                                                 */
        /*    F(15)=TGAWLB        Temp gradient of fuel tank outer wall    */
        /*                          with liquid contact                    */
        /*                                                                 */
        /*    F(16)=TGTWGB        Temp gradient of fuel tank sep. wall     */
        /*                          with gas contact                       */
        /*                                                                 */
        /*    F(17)=TGTWLB        Temp gradient of fuel tank sep. wall     */
        /*                          with liquid contact                    */
        /*                                                                 */
        /*                 *******************************                 */
        /*                                                                 */
        /*    F(18)=mfBoundOx     Massflow of liquid oxidizer to engine    */
        /*                                                                 */
        /*                                                                 */
        /*    F(19)=mfBoundFuel   Massflow of liquid fuel to engine        */
        /*                                                                 */
        /*                                                                 */
        /*                 ********************************                */
        /*                                                                 */
        /*                                                                 */
        /*                                                                 */
        /*    Y(0)=MHEOX          Mass of press gas in ox tank             */
        /*                                                                 */
        /*    Y(1)=TGOX           Temp of gas phase in ox tank             */
        /*                                                                 */
        /*    Y(2)=VGOX           Volume of gas phase in ox tank           */
        /*                                                                 */
        /*    Y(3)=PTOX           Pressure in ox tank                      */
        /*                                                                 */
        /*    Y(4)=TLOX           Temp of liquid oxidizer                  */
        /*                                                                 */
        /*    Y(5)=TAWGO          Temp of ox tank outer wall with          */
        /*                          gas contact                            */
        /*                                                                 */
        /*    Y(6)=TAWLO          Temp of ox tank outer wall with          */
        /*                          liquid contact                         */
        /*                                                                 */
        /*    Y(7)=TTWGO          Temp of ox tank sep wall with            */
        /*                          gas contact                            */
        /*                                                                 */
        /*    Y(8)=TTWLO          TTemp of ox tank sep wall with           */
        /*                          liquid contact                         */
        /*                                                                 */
        /*                                                                 */
        /*    Y(9)=PTBR           Pess in fuel tank                        */
        /*                                                                 */
        /*    Y(10)=MHEBR         Mass of press gas in fuel tank           */
        /*                                                                 */
        /*    Y(11)=TGBR          Temp of gas phase in fuel tank           */
        /*                                                                 */
        /*    Y(12)=TLBR          Temp of liq. ph. in fuel tank            */
        /*                                                                 */
        /*    Y(13)=VGBR          Volume of gas phase in fuel tank         */
        /*                                                                 */
        /*    Y(14)=TAWGB         Temp of fuel tank outer wall with        */
        /*                          gas contact                            */
        /*                                                                 */
        /*    Y(15)=TAWLB         Temp of fuel tank outer wall with        */
        /*                          liquid contact                         */
        /*                                                                 */
        /*    Y(16)=TTWGB         Temp of fuel tank sep wall with          */
        /*                          gas contact                            */
        /*                                                                 */
        /*    Y(17)=TTWLB         Temp of fuel tank sep wall with          */
        /*                          liquid contact                         */
        /*                                                                 */
        /*                       ****************                          */
        /*                                                                 */
        /*    Y(18)=MLOX          Mass of liquid ox in ox tank             */
        /*                                                                 */
        /*    Y(19)=MLBR          Mass of liquid fuel in fuel tank         */
        /*                                                                 */
        /*                                                                 */
        /*******************************************************************/
        /*                                                                 */
        /*    Computing partial pressure of ox. vapour and                 */
        /*    pressure gas partial pressure in oxidizer compartment.       */
        /*                                                                 */
        /*******************************************************************/
        POXD=MDO*RSPOXD*Y[1]/Y[2];
        PHEO=Y[3]-POXD;

        /*******************************************************************/
        /*                                                                 */
        /*    Density of fluid oxidizer can be computed according          */
        /*                                                                 */
        /*                                 2                               */
        /*    DICHLO = C + D*T  + E*( T  )                                 */
        /*                    l       l                                    */
        /*                                                                 */
        /*******************************************************************/
        DICHLO=C+D*Y[4]+E*Math.pow(Y[4],2);

        /*******************************************************************/
        /*                                                                 */
        /*    Density of fluid fuel can be computed according              */
        /*                                                                 */
        /*    DICHLB = CB + DB*T                                           */
        /*                                                                 */
        /*******************************************************************/
        DICHLB=CB+DB*Y[12];

        /*******************************************************************/
        /*                                                                 */
        /*    Computation of actual oxidizer & fuel levels                 */
        /*    in the compartments as polynomial interpolations.            */
        /*                                                                 */
        /*******************************************************************/
        VLOX=Y[18]/DICHLO;
        VLBR=Y[19]/DICHLB;

        PEGLO=0;
        PEGLB=0;

        for (I=0;I<8;I++) {
            PEGLO=PEGLO+oxLevel[I]*Math.pow(VLOX,I);
            PEGLB=PEGLB+fuLevel[I]*Math.pow(VLBR,I);
        }

        /*******************************************************************/
        /*                                                                 */
        /*    Computation of oxidizer resp. fuel covered                   */
        /*    tank outer wall surface resp. separ. wall surface            */
        /*    as function of fill height of fluid in compartment.          */
        /*    Computation of oxidizer & fuel covered outer- resp.          */
        /*    separation wall surface as a function of the fluid fill      */
        /*    level.                                                       */
        /*    FAW##    Aera of outer wall                                  */
        /*    FTW##    Area of separation wall                             */
        /*       GO    Area in contact to gas phase in ox compartment      */
        /*       LO       "     "    "      "  fluid phase "               */
        /*       GB       "     "    "      "  gas phase in fuel compartm. */
        /*       LB       "     "    "      "  fluid phase  "              */
        /*    FFO      Free surface of oxidizer                            */
        /*    FFB         "     "    "  fuel                               */
        /*                                                                 */
        /*    HGOX= boundary fill level in ox compartment                  */
        /*    HGBR= boundary fill level in fuel compartment                */
        /*                                                                 */
        /*******************************************************************/
        FAWLO=0;
        FTWLO=0;
        FFO=0;

        FAWLB=0;
        FTWLB=0;
        FFB=0;

        for (I=0;I<8;I++) {
            if (PEGLO>HGOX) {
                FAWLO=FAWLO+oxCOutWSfc[I]*Math.pow(PEGLO,I);
                FTWLO=FTWLO+oxCSepWSfc[I]*Math.pow(PEGLO,I);
                FFO=FFO+oxSfc[I]*Math.pow(PEGLO,I);
            } else {
                FAWLO=FAWLO+oxCOutWSfc2[I]*Math.pow(PEGLO,I);
                FTWLO=FTWLO+oxCSepWSfc2[I]*Math.pow(PEGLO,I);
                FFO=FFO+oxSfc2[I]*Math.pow(PEGLO,I);
            }
            if (PEGLB>HGBR) {
                FAWLB=FAWLB+fuCOutWSfc[I]*Math.pow(PEGLB,I);
                FTWLB=FTWLB+fuCSepWSfc[I]*Math.pow(PEGLB,I);
                FFB=FFB+fuSfc[I]*Math.pow(PEGLB,I);
            } else {
                FAWLB=FAWLB+fuCOutWSfc2[I]*Math.pow(PEGLB,I);
                FTWLB=FTWLB+fuCSepWSfc2[I]*Math.pow(PEGLB,I);
                FFB=FFB+fuSfc2[I]*Math.pow(PEGLB,I);
            }
        }

        FAWGO=FAWO-FAWLO;
        FTWGO=FTWO-FTWLO;

        FAWGB=FAWB-FAWLB;
        FTWGB=FTWB-FTWLB;

        /*******************************************************************/
        /*                                                                 */
        /*    Computation of viscosities and heat conductivities of        */
        /*    gas and fluid phase in fuel and oxidizer compartment         */
        /*                                                                 */
        /*******************************************************************/

        /*******    In Ox Tank    ******************************************/

        /*******************************************************************/
        /*                                                                 */
        /*    NHEO   No. of moles of pressure gas in ox tank               */
        /*    NOG      "   "   "   oxidizer gas in ox tank                 */
        /*    NGES     "   "   "   overall                                 */
        /*                                                                 */
        /*      in analogy the molar fractions Y###                        */
        /*                                                                 */
        /*******************************************************************/
        NHEO=Y[0]/MMOLHE;
        NOG=MDO/MMOLO;
        NGES=NHEO+NOG;


        YHEO=NHEO/NGES;
        YDO=NOG/NGES;

        /*******************************************************************/
        /*                                                                 */
        /*    Viscosities & heat conductivities of fluids                  */
        /*                                                                 */
        /*    ###OG  Oxidizer Gas,                                         */
        /*    ###OL     "     Liquid                                       */
        /*    ###GO  Gas phase in OxTank (mix of ox. vapour & pressure gas)*/
        /*    ###BL  Fuel liquid                                           */
        /*    ###LB  Liquid phase in fuel tank                             */
        /*                                                                 */
        /*******************************************************************/
        ETAOG=(-1.241265+8.57137E-3*Y[1])*1E-5;

        ETAOL=7.533E-3-6.167E-5*Y[4]+2.055E-7*Math.pow(Y[4],2);
        ETAOL=ETAOL-3.234E-10*Math.pow(Y[4],3)+1.966E-13*Math.pow(Y[4],4);

        double PK_OX = org.opensimkit.Helium.HELIUM(PHEO, Y[1], Helium_ox);

        ETAGO=Helium_ox.ETA*YHEO*Math.pow(MMOLHE,.5);
        ETAGO=ETAGO+ETAOG*YDO*Math.pow(MMOLO,.5);
        ETAGO=ETAGO/(YHEO*Math.pow(MMOLHE,.5)+YDO*Math.pow(MMOLO,.5));

        LAMOL=-.13791+2.3304E-3*Y[4]-4.7897E-6*Math.pow(Y[4],2);
        LAMOG=.08223-2.026E-4*Y[1];
        LAMGO=LAMOG*YDO+Helium_ox.LAMBDA*YHEO;

        /*******     In Fuel Tank    ***************************************/

        double PK_brenn = org.opensimkit.Helium.HELIUM(Y[9], Y[11], Helium_brenn);

        LAMBL=.14246+9.211E-4*Y[12]-1.9029E-6*Math.pow(Y[12],2);

        ETALB=36.77076-.385516*Y[12]+1.263832E-3*Math.pow(Y[12],2);
        ETALB=ETALB-1.40667E-6*Math.pow(Y[12],3);
        ETALB=Math.pow(10,ETALB);


        /*******************************************************************/
        /*                                                                 */
        /*    Heat transfer coefficients Alfa                              */
        /*                                                                 */
        /*    ALAWGO = Alfa btw outer wall & gas phase in ox compartment   */
        /*                                                                 */
        /*    ALAWLO = Alfa btw outer wall & liquid phase in ox compartment*/
        /*                                                                 */
        /*    ALTWGO = Alfa btw sep. wall & gas phase in ox compartment    */
        /*                                                                 */
        /*    ALTWLO = Alfa btw sep. wall & liquid phase in ox compartment */
        /*                                                                 */
        /*    ALLGO  = Alfa btw fluid- & gas phase in ox compartment       */
        /*                                                                 */
        /*                         *******                                 */
        /*                                                                 */
        /*    ALAWGB = Alfa btw outer wall & gas phase in fuel compartment */
        /*                                                                 */
        /*    ALAWLB = Alfa btw outer wall & liquid phase in fuel comp.    */
        /*                                                                 */
        /*    ALTWGB = Alfa btw sep. wall & gas phase in fuel compartment  */
        /*                                                                 */
        /*    ALTWLB = Alfa btw sep. wall & liquid phase in fuel comp.     */
        /*                                                                 */
        /*    ALLGB  = Alfa btw fluid- & gas phase in fuel compartment     */
        /*                                                                 */
        /*                                                                 */
        /*    Same naming convention for Nusselt-,                         */
        /*    Prandtl und Grashof-numbers, as example                      */
        /*                                                                 */
        /*    NUAWGO, NUTWLO, GRAWGB, PRGO, PRLO, PRGB etc.                */
        /*                                                                 */
        /*                                                                 */
        /*         **********************************************          */
        /*                                                                 */
        /*                                        .345                     */
        /*    NUAWGO = .098 * (GRAWGO * PRGO )                             */
        /*                                                                 */
        /*                                        .345                     */
        /*    NUAWLO = .098 * (GRAWLO * PRLO )                             */
        /*                                                                 */
        /*                                        .345                     */
        /*    NUTWGO = .098 * (GRTWGO * PRGO )                             */
        /*                                                                 */
        /*                                        .345                     */
        /*    NUTWLO = .098 * (GRTWLO * PRLO )                             */
        /*                                                                 */
        /*                                        .333                     */
        /*    NULGO  = .14 * (GRAWGO * PRGO )                              */
        /*                                                                 */
        /*                                                                 */
        /*                                                                 */
        /*                                        .345                     */
        /*    NUAWGB = .098 * (GRAWGB * PRGB )                             */
        /*                                                                 */
        /*                                        .345                     */
        /*    NUAWLB = .098 * (GRAWLB * PRLB )                             */
        /*                                                                 */
        /*                                        .345                     */
        /*    NUTWGB = .098 * (GRTWGB * PRGB )                             */
        /*                                                                 */
        /*                                        .345                     */
        /*    NUTWLB = .098 * (GRTWLB * PRLB )                             */
        /*                                                                 */
        /*                                        .333                     */
        /*    NULGB  = .14 * (GRAWGB * PRGB )                              */
        /*                                                                 */
        /*                                                                 */
        /*******************************************************************/
        GRAWGO=GLoad.load()*Math.pow(CHARMO,3)*(Y[5]-Y[1]);
        GRAWGO=GRAWGO/(Math.pow((ETAGO/DICHGO),2)*(Y[5]+Y[1])/2);
        GRAWGO=Math.abs(GRAWGO);

        GRAWLO=GLoad.load()*Math.pow(CHARMO,3)*(Y[6]-Y[4]);
        GRAWLO=GRAWLO/(Math.pow((ETAOL/DICHLO),2)*(Y[6]+Y[4])/2);
        GRAWLO=Math.abs(GRAWLO);

        GRTWGO=GLoad.load()*Math.pow(CHARMO,3)*(Y[7]-Y[1]);
        GRTWGO=GRTWGO/(Math.pow((ETAGO/DICHGO),2)*(Y[7]+Y[1])/2);
        GRTWGO=Math.abs(GRTWGO);

        GRTWLO=GLoad.load()*Math.pow(CHARMO,3)*(Y[8]-Y[4]);
        GRTWLO=GRTWLO/(Math.pow((ETAOL/DICHLO),2)*(Y[8]+Y[4])/2);
        GRTWLO=Math.abs(GRTWLO);

        GRLGO=GLoad.load()*Math.pow(CHARMO,3)*(Y[4]-Y[1]);
        GRLGO=GRLGO/(Math.pow((ETAGO/DICHGO),2)*(Y[4]+Y[1])/2);
        GRLGO=Math.abs(GRLGO);

        GRAWGB=GLoad.load()*Math.pow(CHARMB,3)*(Y[14]-Y[11]);
        GRAWGB=GRAWGB/(Math.pow(Helium_brenn.NUE,2)*(Y[14]+Y[11])/2);
        GRAWGB=Math.abs(GRAWGB);

        GRAWLB=GLoad.load()*Math.pow(CHARMB,3)*(Y[15]-Y[12]);
        GRAWLB=GRAWLB/(Math.pow((ETALB/DICHLB),2)*(Y[15]+Y[12])/2);
        GRAWLB=Math.abs(GRAWLB);

        GRTWGB=GLoad.load()*Math.pow(CHARMB,3)*(Y[16]-Y[11]);
        GRTWGB=GRTWGB/(Math.pow(Helium_brenn.NUE,2)*(Y[16]+Y[11])/2);
        GRTWGB=Math.abs(GRTWGB);

        GRTWLB=GLoad.load()*Math.pow(CHARMB,3)*(Y[17]-Y[12]);
        GRTWLB=GRTWLB/(Math.pow((ETALB/DICHLB),2)*(Y[17]+Y[12])/2);
        GRTWLB=Math.abs(GRTWLB);

        GRLGB=GLoad.load()*Math.pow(CHARMB,3)*(Y[12]-Y[11]);
        GRLGB=GRLGB/(Math.pow(Helium_brenn.NUE,2)*(Y[12]+Y[11])/2);
        GRLGB=Math.abs(GRLGB);

        /*******************************************************************/
        /*                                                                 */
        /*    Computing the cp , cv according naming conventions           */
        /*    ###OG,###GO etc. Further computing Nusselt no's & heat       */
        /*    transfer coeffs Alpha for transfers from outer & sep walls   */
        /*    to each compartment                                          */
        /*                                                                 */
        /*******************************************************************/
        CPLO=-1.78232E4+1.9312E2*Y[4]-.65038*Math.pow(Y[4],2);
        CPLO=CPLO+7.4106E-4*Math.pow(Y[4],3);
        CPLB=2.7331E3-7.2365E-2*Y[12]+2.4762E-3*Math.pow(Y[12],2);
        CPOG=361.7617+2.035667*Y[1]-1.248147E-3*Math.pow(Y[1],2);
        CVOG=CPOG-RSPOXD;
        CPGO=(CPHE*Y[0]+CPOG*MDO)/(Y[0]+MDO);
        CVGO=CPGO-RSPMO;

        PRGO=CPGO*ETAGO/LAMGO;
        PRLO=CPLO*ETAOL/LAMOL;
        PRGB=CPHE*Helium_brenn.ETA/Helium_brenn.LAMBDA;
        PRLB=CPLB*ETALB/LAMBL;

        NUAWGO=.098*Math.pow((GRAWGO*PRGO),.345);
        NUAWLO=.098*Math.pow((GRAWLO*PRLO),.345);
        NUTWGO=.098*Math.pow((GRTWGO*PRGO),.345);
        NUTWLO=.098*Math.pow((GRTWLO*PRLO),.345);
        NULGO=.14*Math.pow((GRLGO*PRGO),.333);
        NUAWGB=.098*Math.pow((GRAWGB*PRGB),.345);
        NUAWLB=.098*Math.pow((GRAWLB*PRLB),.345);
        NUTWGB=.098*Math.pow((GRTWGB*PRGB),.345);
        NUTWLB=.098*Math.pow((GRTWLB*PRLB),.345);
        NULGB=.14*Math.pow((GRLGB*PRGB),.333);

        ALAWGO=NUAWGO*LAMGO/CHARMO;
        ALAWLO=NUAWLO*LAMOL/CHARMO;
        ALTWGO=NUTWGO*LAMGO/CHARMO;
        ALTWLO=NUTWLO*LAMOL/CHARMO;
        ALLGO=NULGO*LAMGO/CHARMO;
        ALAWGB=NUAWGB*Helium_brenn.LAMBDA/CHARMB;
        ALAWLB=NUAWLB*LAMBL/CHARMB;
        ALTWGB=NUTWGB*Helium_brenn.LAMBDA/CHARMB;
        ALTWLB=NUTWLB*LAMBL/CHARMB;
        ALLGB=NULGB*Helium_brenn.LAMBDA/CHARMB;

        /*******************************************************************/
        /*                                                                 */
        /*    Computing the heat fluxes QP####                             */
        /*                                                                 */
        /*******************************************************************/
        QPAWGO=FAWGO*ALAWGO*(Y[5]-Y[1]);
        QPAWLO=FAWLO*ALAWLO*(Y[6]-Y[4]);
        QPTWGO=FTWGO*ALTWGO*(Y[7]-Y[1]);
        QPTWLO=FTWLO*ALTWLO*(Y[8]-Y[4]);
        QPLGO=FFO*ALLGO*(Y[4]-Y[1]);

        QPAWGB=FAWGB*ALAWGB*(Y[14]-Y[11]);
        QPAWLB=FAWLB*ALAWLB*(Y[15]-Y[12]);
        QPTWGB=FTWGB*ALTWGB*(Y[16]-Y[11]);
        QPTWLB=FTWLB*ALTWLB*(Y[17]-Y[12]);
        QPLGB=FFB*ALLGB*(Y[12]-Y[11]);

        /*******************************************************************/
        /*                                                                 */
        /*    Sum of heat fluxes (from outer and sep. walls) towards       */
        /*    gas/fluid phases in each compartment.                        */
        /*                                                                 */
        /*******************************************************************/
        SUQPLO=QPAWLO+QPTWLO-QPLGO;
        SUQPGO=QPAWGO+QPTWGO+QPLGO;
        SUQPLB=QPAWLB+QPTWLB-QPLGB;
        SUQPGB=QPAWGB+QPTWGB+QPLGB;

        /*******************************************************************/
        /*                                                                 */
        /*    Computation of pressure gas enthalpies for both compartments */
        /*                                                                 */
        /*******************************************************************/
        HHEINO=CPHE*THEINO;
        HHEINB=CPHE*THEINB;

        /** Volume fluxes of ox. and fuel to engine **********************/

        VOLOUT=-1.*F[18]/DICHLO;
        VBLOUT=-1.*F[19]/DICHLB;

        /*******************************************************************/
        /*                                                                 */
        /*    Computation of oxidizer covered / resp. fuel covered         */
        /*    tank compartment outer wall aereas resp separation           */
        /*    Bwall areas.                                                 */
        /*                                                                 */
        /*******************************************************************/
        MAWGO=FMAWO*FAWGO;
        MAWLO=FMAWO*FAWLO;
        MTWGO=FMTW*FTWGO/2;
        MTWLO=FMTW*FTWLO/2;

        MAWGB=FMAWB*FAWGB;
        MAWLB=FMAWB*FAWLB;
        MTWGB=FMTW*FTWGB/2;
        MTWLB=FMTW*FTWLB/2;

        if(X==0) {
            MAWGOA=MAWGO;
            MTWGOA=MTWGO;
            MAWGBA=MAWGB;
            MTWGBA=MTWGB;
        }

        MPAWGO=(MAWGO-MAWGOA);
        MPAWGO=MPAWGO/(X-ZEITA);
        MPTWGO=(MTWGO-MTWGOA);
        MPTWGO=MPTWGO/(X-ZEITA);
        MPAWGB=(MAWGB-MAWGBA);
        MPAWGB=MPAWGB/(X-ZEITA);
        MPTWGB=(MTWGB-MTWGBA);
        MPTWGB=MPTWGB/(X-ZEITA);

        /*******************************************************************/
        /*                                                                 */
        /*    Computing some intermediate helper variables                 */
        /*                                                                 */
        /*******************************************************************/
        if(BDFLAG==0) {
            KO=MDO*CVOG+Y[0]*CVHE;
            LO=HHEINO-Y[1]*CVHE+KO*Y[3]*Y[2]*RSPHE
                    / Math.pow((RSPMO*(Y[0]+MDO)),2);
            VPTGBR=DB*VLBR*SUQPLB/(Y[19]*CPLB*DICHLB)+VBLOUT;
            FB=VPTGBR*Y[11]/Y[13];
        }

        /*******************************************************************/
        /*                                                                 */
        /*    Computation of derivatives vector for DEQ system             */
        /*                                                                 */
        /*******************************************************************/
        if(BDFLAG==0) {
            F[4]=SUQPLO/(Y[18]*CPLO);
            F[2]=VOLOUT+(VLOX*F[4]*(D+2*E*Y[4]))/DICHLO;
            F[0]=Y[3]*F[2]-SUQPGO+F[2]*Y[3]*KO/(RSPMO*(Y[0]+MDO));
            F[0]=F[0]/LO;
            F[1]=(F[0]*(HHEINO-CVHE*Y[1])+SUQPGO-Y[3]*F[2])/KO;
            F[3]=0;
            F[5]=(-QPAWGO+SPWKO*(Y[6]-Y[5])*MPAWGO)/(MAWGO*SPWKO);
            F[6]=-QPAWLO/(MAWLO*SPWKO);
            F[7]=(-QPTWGO+SPWKO*(Y[8]-Y[7])*MPTWGO)/(MTWGO*SPWKO);
            if (MTWLO > 0.)
                F[8]=-QPTWLO/(MTWLO*SPWKO);
            else
                F[8]=0.;
            F[9]=0;
            F[10]=(-1.*SUQPGB+Y[10]*CVHE*FB+VPTGBR*Y[9])/HHEINB;


            F[11]=-F[10]*Y[11]/Y[10]+FB;
            F[12]=SUQPLB/(CPLB*Y[19]);
            F[13]=F[12]*DB*VLBR/DICHLB+VBLOUT;
            F[14]=(-QPAWGB+SPWKB*(Y[15]-Y[14])*MPAWGB)/(MAWGB*SPWKB);
            if (MAWLB > 0.)
                F[15]=-QPAWLB/(MAWLB*SPWKB);
            else
                F[15]=0.;
            F[16]=(-QPTWGB+SPWKB*(Y[17]-Y[16])*MPTWGB)/(MTWGB*SPWKB);
            F[17]=-QPTWLB/(MTWLB*SPWKB);
        } else {
            F[0]=0;
            F[4]=SUQPLO/(Y[18]*CPLO);
            F[2]=VOLOUT+(VLOX*F[4]*(D+2*E*Y[4]))/DICHLO;
            F[1]=(SUQPGO-Y[3]*F[2])/((Y[0]+MDO)*CVGO);
            F[3]=(Y[0]+MDO)*RSPMO*(F[1]/Y[2]-F[2]*Y[1]/Math.pow(Y[2],2));
            F[5]=(-QPAWGO+SPWKO*(Y[6]-Y[5])*MPAWGO)/(MAWGO*SPWKO);
            F[6]=-QPAWLO/(MAWLO*SPWKO);
            F[7]=(-QPTWGO+SPWKO*(Y[8]-Y[7])*MPTWGO)/(MTWGO*SPWKO);
            if (MTWLO > 0.)
                F[8]=-QPTWLO/(MTWLO*SPWKO);
            else
                F[8]=0.;
            F[10]=0;
            F[12]=SUQPLB/(CPLB*Y[19]);
            F[13]=F[12]*DB*VLBR/DICHLB+VBLOUT;
            F[11]=(SUQPGB-Y[9]*F[13])/(Y[10]*CVHE);
            F[9]=Y[10]*RSPHE*(F[11]/Y[13]-F[13]*Y[11]/Math.pow(Y[13],2));
            F[14]=(-QPAWGB+SPWKB*(Y[15]-Y[14])*MPAWGB)/(MAWGB*SPWKB);
            if (MAWLB > 0.)
                F[15]=-QPAWLB/(MAWLB*SPWKB);
            else
                F[15]=0.;
            F[18]=(-QPTWGB+SPWKB*(Y[17]-Y[16])*MPTWGB)/(MTWGB*SPWKB);
            F[17]=-QPTWLB/(MTWLB*SPWKB);
        }
        return 0;
    }


    @Override
    public int timeStep(final double time, final double tStepSize) {
        int result;

        result = 0;

        LOG.info("% {} TimeStep-Computation", name);

        localNAckFlag = 0;

        MPKTLB = mfBoundFuel;
        MPKTLO = mfBoundOx;

        YK[9]  = inputPortFuelPressureGas.getPressure();
        YK[3]  = inputPortOxidizerPressureGas.getPressure();
        THEINB = inputPortFuelPressureGas.getTemperature();
        THEINO = inputPortOxidizerPressureGas.getTemperature();

        result = DEqSys.DEqSys(time, tStepSize, YK, 20, (time+tStepSize),
                SimHeaders.epsabs, SimHeaders.epsrel, IFMAX, IFANZ, IFEHL,
                this);

        if (result == 1) {
            LOG.info("Error in timestep integration of component '{}'", name);
            localNAckFlag = 1;
        }

        ZEITA=time;
        MAWGOA=MAWGO;
        MTWGOA=MTWGO;
        MAWGBA=MAWGB;
        MTWGBA=MTWGB;
        MPHEBR=Math.abs(MHEBRA-YK[10])/tStepSize;
        MPHEOX=Math.abs(MHEOXA-YK[0])/tStepSize;
        MHEBRA=YK[10];
        MHEOXA=YK[0];

        mfBoundFuelPress = MPHEBR;
        mfBoundOxPress   = MPHEOX;
        pBoundFuelPress  = YK[9];
        pBoundOxPress    = YK[3];

        if (localNAckFlag == 1) {
            return 1;
        }
        return 0;
    }


    @Override
    public int iterationStep() {
        String fluid;
        double errval;
        int    result;

        LOG.info("% {} IterationStep-Computation", name);

        pinFPG  = inputPortFuelPressureGas.getPressure();
        tinFPG  = inputPortFuelPressureGas.getTemperature();
        mfinFPG = inputPortFuelPressureGas.getMassflow();
        pinOPG  = inputPortOxidizerPressureGas.getPressure();
        tinOPG  = inputPortOxidizerPressureGas.getTemperature();
        mfinOPG = inputPortOxidizerPressureGas.getMassflow();

        result = 0;

        errval = Math.abs((mfinFPG - mfBoundFuelPress)
                / mfBoundFuelPress);
        if (errval > 0.02) {
            result = -1;
        }
        errval = Math.abs((mfinOPG - mfBoundOxPress)/mfBoundOxPress);
        if (errval > 0.02) {
            result = -1;
        }

        fluid     = fuel;
        mfoutFuel = mfBoundFuel;
        poutFuel  = YK[9];
        toutFuel  = YK[12];

        outputPortFuel.setFluid(fluid);
        outputPortFuel.setPressure(poutFuel);
        outputPortFuel.setTemperature(toutFuel);
        outputPortFuel.setMassflow(mfoutFuel);

        fluid         = oxidizer;
        mfoutOxidizer = mfBoundOx;
        poutOxidizer  = YK[3];
        toutOxidizer  = YK[4];

        //For printout in TabGenerator
        poxt  = YK[3] / 1.E5;
        tGOxT = YK[1];
        tLOxT = YK[4];
        PFuT  = YK[9] / 1.E5;
        tGFuT = YK[11];
        tLFuT = YK[12];

        outputPortOxidizer.setFluid(fluid);
        outputPortOxidizer.setPressure(poutOxidizer);
        outputPortOxidizer.setTemperature(toutOxidizer);
        outputPortOxidizer.setMassflow(mfoutOxidizer);

        return result;
    }


    @Override
    public int backIterStep() {
        int result;

        result = 0;

        LOG.info("% {} BackIteration-Computation", name);

        if (outputPortFuel.getBoundaryPressure() >= 0.0) {
            LOG.info("Error! Comp. '{}': Pressure request on output port fuel"
                    + " cannot be handled!", name);
            result = 1;
        }
        if (outputPortFuel.getBoundaryTemperature() >= 0.0) {
            LOG.info("Error! Comp. '{}': Temp. request on output port fuel"
                    + " cannot be handled!", name);
            result = 1;
        }
        if (outputPortOxidizer.getBoundaryPressure() >= 0.0) {
            LOG.info("Error! Comp. '{}': Pressure request on output port"
                    + " oxidizer cannot be handled!", name);
            result = 1;
        }
        if (outputPortOxidizer.getBoundaryTemperature() >= 0.0) {
            LOG.info("Error! Comp. '{}': Temp. request on output port oxidizer"
                    + " cannot be handled!", name);
            result = 1;
        }

        mfBoundFuel = outputPortFuel.getBoundaryMassflow();
        mfBoundOx   = outputPortOxidizer.getBoundaryMassflow();

        inputPortFuelPressureGas.setBoundaryFluid(fuPressGas);
        inputPortFuelPressureGas.setBoundaryPressure(-999999.99);
        inputPortFuelPressureGas.setBoundaryTemperature(-999999.99);
        inputPortFuelPressureGas.setBoundaryMassflow(mfBoundFuelPress);

        inputPortOxidizerPressureGas.setBoundaryFluid(oxPressGas);
        inputPortOxidizerPressureGas.setBoundaryPressure(-999999.99);
        inputPortOxidizerPressureGas.setBoundaryTemperature(-999999.99);
        inputPortOxidizerPressureGas.setBoundaryMassflow(mfBoundOxPress);

        return result;
    }


    @Override
    public int regulStep() {
        LOG.info("% {} RegulStep-Computation", name);
        return 0;
    }


    @Override
    public int save(final FileWriter outFile) throws IOException {
        outFile.write("TankT1: '" + name + "'" + SimHeaders.NEWLINE);
        return 0;
    }
    
    //-----------------------------------------------------------------------------------
    // Methods added for JMX monitoring	and setting initial properties via CDI Extensions

	@ManagedAttribute    
    public String getFuel() {
		return fuel;
	}
	public void setFuel(String fuel) {
		this.fuel = fuel;
	}
    
	@ManagedAttribute    
    public String getOxidizer() {
		return oxidizer;
	}
	public void setOxidizer(String oxidizer) {
		this.oxidizer = oxidizer;
	}
	@ManagedAttribute    
	public String getFuPressGas() {
		return fuPressGas;
	}
	public void setFuPressGas(String fuPressGas) {
		this.fuPressGas = fuPressGas;
	}
	@ManagedAttribute    
	public String getOxPressGas() {
		return oxPressGas;
	}
	public void setOxPressGas(String oxPressGas) {
		this.oxPressGas = oxPressGas;
	}
	@ManagedAttribute    
	public double getMfBoundFuelPress() {
		return mfBoundFuelPress;
	}
	public void setMfBoundFuelPress(double mfBoundFuelPress) {
		this.mfBoundFuelPress = mfBoundFuelPress;
	}
	@ManagedAttribute    
	public double getMfBoundOxPress() {
		return mfBoundOxPress;
	}
	public void setMfBoundOxPress(double mfBoundOxPress) {
		this.mfBoundOxPress = mfBoundOxPress;
	}
	@ManagedAttribute    
	public double getpBoundFuelPress() {
		return pBoundFuelPress;
	}
	public void setpBoundFuelPress(double pBoundFuelPress) {
		this.pBoundFuelPress = pBoundFuelPress;
	}
	@ManagedAttribute    
	public double getpBoundOxPress() {
		return pBoundOxPress;
	}
	public void setpBoundOxPress(double pBoundOxPress) {
		this.pBoundOxPress = pBoundOxPress;
	}
	@ManagedAttribute    
	public double getMfBoundFuel() {
		return mfBoundFuel;
	}
	public void setMfBoundFuel(double mfBoundFuel) {
		this.mfBoundFuel = mfBoundFuel;
	}
	@ManagedAttribute    
	public double getMfBoundOx() {
		return mfBoundOx;
	}
	public void setMfBoundOx(double mfBoundOx) {
		this.mfBoundOx = mfBoundOx;
	}
	@ManagedAttribute    
	public double getPinFPG() {
		return pinFPG;
	}
	public void setPinFPG(double pinFPG) {
		this.pinFPG = pinFPG;
	}
	@ManagedAttribute    
	public double getTinFPG() {
		return tinFPG;
	}
	public void setTinFPG(double tinFPG) {
		this.tinFPG = tinFPG;
	}
	@ManagedAttribute    
	public double getMfinFPG() {
		return mfinFPG;
	}
	public void setMfinFPG(double mfinFPG) {
		this.mfinFPG = mfinFPG;
	}
	@ManagedAttribute    
	public double getPinOPG() {
		return pinOPG;
	}
	public void setPinOPG(double pinOPG) {
		this.pinOPG = pinOPG;
	}
	@ManagedAttribute    
	public double getTinOPG() {
		return tinOPG;
	}
	public void setTinOPG(double tinOPG) {
		this.tinOPG = tinOPG;
	}
	@ManagedAttribute    
	public double getMfinOPG() {
		return mfinOPG;
	}
	public void setMfinOPG(double mfinOPG) {
		this.mfinOPG = mfinOPG;
	}
	@ManagedAttribute    
	public double getPoutFuel() {
		return poutFuel;
	}
	public void setPoutFuel(double poutFuel) {
		this.poutFuel = poutFuel;
	}
	@ManagedAttribute    
	public double getToutFuel() {
		return toutFuel;
	}
	public void setToutFuel(double toutFuel) {
		this.toutFuel = toutFuel;
	}
	@ManagedAttribute    
	public double getMfoutFuel() {
		return mfoutFuel;
	}
	public void setMfoutFuel(double mfoutFuel) {
		this.mfoutFuel = mfoutFuel;
	}
	@ManagedAttribute    
	public double getPoutOxidizer() {
		return poutOxidizer;
	}
	public void setPoutOxidizer(double poutOxidizer) {
		this.poutOxidizer = poutOxidizer;
	}
	@ManagedAttribute    
	public double getToutOxidizer() {
		return toutOxidizer;
	}
	public void setToutOxidizer(double toutOxidizer) {
		this.toutOxidizer = toutOxidizer;
	}
	@ManagedAttribute    
	public double getMfoutOxidizer() {
		return mfoutOxidizer;
	}
	public void setMfoutOxidizer(double mfoutOxidizer) {
		this.mfoutOxidizer = mfoutOxidizer;
	}
	@ManagedAttribute    
	public double getVTBR() {
		return VTBR;
	}
	public void setVTBR(double vTBR) {
		VTBR = vTBR;
	}
	@ManagedAttribute    
	public double getVTOX() {
		return VTOX;
	}
	public void setVTOX(double vTOX) {
		VTOX = vTOX;
	}
	@ManagedAttribute    
	public double getFAWO() {
		return FAWO;
	}
	public void setFAWO(double fAWO) {
		FAWO = fAWO;
	}
	@ManagedAttribute    
	public double getFTWO() {
		return FTWO;
	}
	public void setFTWO(double fTWO) {
		FTWO = fTWO;
	}
	@ManagedAttribute    
	public double getFAWB() {
		return FAWB;
	}
	public void setFAWB(double fAWB) {
		FAWB = fAWB;
	}
	@ManagedAttribute    
	public double getFTWB() {
		return FTWB;
	}
	public void setFTWB(double fTWB) {
		FTWB = fTWB;
	}
	@ManagedAttribute    
	public double getHGOX() {
		return HGOX;
	}
	public void setHGOX(double hGOX) {
		HGOX = hGOX;
	}
	@ManagedAttribute    
	public double getHGBR() {
		return HGBR;
	}
	public void setHGBR(double hGBR) {
		HGBR = hGBR;
	}
	@ManagedAttribute    
	public double getCHARMO() {
		return CHARMO;
	}
	public void setCHARMO(double cHARMO) {
		CHARMO = cHARMO;
	}
	@ManagedAttribute    
	public double getCHARMB() {
		return CHARMB;
	}
	public void setCHARMB(double cHARMB) {
		CHARMB = cHARMB;
	}
	@ManagedAttribute    
	public double getFMAWO() {
		return FMAWO;
	}
	public void setFMAWO(double fMAWO) {
		FMAWO = fMAWO;
	}
	@ManagedAttribute    
	public double getFMTW() {
		return FMTW;
	}
	public void setFMTW(double fMTW) {
		FMTW = fMTW;
	}
	@ManagedAttribute    
	public double getFMAWB() {
		return FMAWB;
	}
	public void setFMAWB(double fMAWB) {
		FMAWB = fMAWB;
	}
	@ManagedAttribute    
	public double getSPWKO() {
		return SPWKO;
	}
	public void setSPWKO(double sPWKO) {
		SPWKO = sPWKO;
	}
	@ManagedAttribute    
	public double getSPWKB() {
		return SPWKB;
	}
	public void setSPWKB(double sPWKB) {
		SPWKB = sPWKB;
	}
	@ManagedAttribute    
	public double getPTO() {
		return PTO;
	}
	public void setPTO(double pTO) {
		PTO = pTO;
	}
	@ManagedAttribute    
	public double getPENDOX() {
		return PENDOX;
	}
	public void setPENDOX(double pENDOX) {
		PENDOX = pENDOX;
	}
	@ManagedAttribute    
	public double getPTB() {
		return PTB;
	}
	public void setPTB(double pTB) {
		PTB = pTB;
	}
	@ManagedAttribute    
	public double getPENDBR() {
		return PENDBR;
	}
	public void setPENDBR(double pENDBR) {
		PENDBR = pENDBR;
	}
	@ManagedAttribute    
	public double getVANFOX() {
		return VANFOX;
	}
	public void setVANFOX(double vANFOX) {
		VANFOX = vANFOX;
	}
	@ManagedAttribute    
	public double getTANFOX() {
		return TANFOX;
	}
	public void setTANFOX(double tANFOX) {
		TANFOX = tANFOX;
	}
	@ManagedAttribute    
	public double getVANFBR() {
		return VANFBR;
	}
	public void setVANFBR(double vANFBR) {
		VANFBR = vANFBR;
	}
	@ManagedAttribute    
	public double getTANFBR() {
		return TANFBR;
	}
	public void setTANFBR(double tANFBR) {
		TANFBR = tANFBR;
	}
	@ManagedAttribute    
	public double getPVO() {
		return PVO;
	}
	public void setPVO(double pVO) {
		PVO = pVO;
	}
	@ManagedAttribute    
	public double getTHEINO() {
		return THEINO;
	}
	public void setTHEINO(double tHEINO) {
		THEINO = tHEINO;
	}
	@ManagedAttribute    
	public double getTHEINB() {
		return THEINB;
	}
	public void setTHEINB(double tHEINB) {
		THEINB = tHEINB;
	}
	@ManagedAttribute    
	public double getMAWGO() {
		return MAWGO;
	}
	public void setMAWGO(double mAWGO) {
		MAWGO = mAWGO;
	}
	@ManagedAttribute    
	public double getMTWGO() {
		return MTWGO;
	}
	public void setMTWGO(double mTWGO) {
		MTWGO = mTWGO;
	}
	@ManagedAttribute    
	public double getMAWGB() {
		return MAWGB;
	}
	public void setMAWGB(double mAWGB) {
		MAWGB = mAWGB;
	}
	@ManagedAttribute    
	public double getMTWGB() {
		return MTWGB;
	}
	public void setMTWGB(double mTWGB) {
		MTWGB = mTWGB;
	}
	@ManagedAttribute    
	public double getMPHEOX() {
		return MPHEOX;
	}
	public void setMPHEOX(double mPHEOX) {
		MPHEOX = mPHEOX;
	}
	@ManagedAttribute    
	public double getMPHEBR() {
		return MPHEBR;
	}
	public void setMPHEBR(double mPHEBR) {
		MPHEBR = mPHEBR;
	}
	@ManagedAttribute    
	public double getMAWGOA() {
		return MAWGOA;
	}
	public void setMAWGOA(double mAWGOA) {
		MAWGOA = mAWGOA;
	}
	@ManagedAttribute    
	public double getMTWGOA() {
		return MTWGOA;
	}
	public void setMTWGOA(double mTWGOA) {
		MTWGOA = mTWGOA;
	}
	@ManagedAttribute    
	public double getMAWGBA() {
		return MAWGBA;
	}
	public void setMAWGBA(double mAWGBA) {
		MAWGBA = mAWGBA;
	}
	@ManagedAttribute    
	public double getMTWGBA() {
		return MTWGBA;
	}
	public void setMTWGBA(double mTWGBA) {
		MTWGBA = mTWGBA;
	}
	@ManagedAttribute    
	public double getMLOX() {
		return MLOX;
	}
	public void setMLOX(double mLOX) {
		MLOX = mLOX;
	}
	@ManagedAttribute    
	public double getMLBR() {
		return MLBR;
	}
	public void setMLBR(double mLBR) {
		MLBR = mLBR;
	}
	@ManagedAttribute    
	public double getMPKTLO() {
		return MPKTLO;
	}
	public void setMPKTLO(double mPKTLO) {
		MPKTLO = mPKTLO;
	}
	@ManagedAttribute    
	public double getMPKTLB() {
		return MPKTLB;
	}
	public void setMPKTLB(double mPKTLB) {
		MPKTLB = mPKTLB;
	}
	@ManagedAttribute    
	public double getMDO() {
		return MDO;
	}
	public void setMDO(double mDO) {
		MDO = mDO;
	}
	@ManagedAttribute    
	public double getMHEOXA() {
		return MHEOXA;
	}
	public void setMHEOXA(double mHEOXA) {
		MHEOXA = mHEOXA;
	}
	@ManagedAttribute    
	public double getMHEBRA() {
		return MHEBRA;
	}
	public void setMHEBRA(double mHEBRA) {
		MHEBRA = mHEBRA;
	}
	@ManagedAttribute    
	public double[] getFuLevel() {
		return fuLevel;
	}
	public void setFuLevel(double[] fuLevel) {
		this.fuLevel = fuLevel;
	}
	@ManagedAttribute    
	public double[] getFuCOutWSfc() {
		return fuCOutWSfc;
	}
	public void setFuCOutWSfc(double[] fuCOutWSfc) {
		this.fuCOutWSfc = fuCOutWSfc;
	}
	@ManagedAttribute    
	public double[] getFuCSepWSfc() {
		return fuCSepWSfc;
	}
	public void setFuCSepWSfc(double[] fuCSepWSfc) {
		this.fuCSepWSfc = fuCSepWSfc;
	}
	@ManagedAttribute    
	public double[] getFuSfc() {
		return fuSfc;
	}
	public void setFuSfc(double[] fuSfc) {
		this.fuSfc = fuSfc;
	}
	@ManagedAttribute    
	public double[] getFuCOutWSfc2() {
		return fuCOutWSfc2;
	}
	public void setFuCOutWSfc2(double[] fuCOutWSfc2) {
		this.fuCOutWSfc2 = fuCOutWSfc2;
	}
	@ManagedAttribute    
	public double[] getFuCSepWSfc2() {
		return fuCSepWSfc2;
	}
	public void setFuCSepWSfc2(double[] fuCSepWSfc2) {
		this.fuCSepWSfc2 = fuCSepWSfc2;
	}
	@ManagedAttribute    
	public double[] getFuSfc2() {
		return fuSfc2;
	}
	public void setFuSfc2(double[] fuSfc2) {
		this.fuSfc2 = fuSfc2;
	}
	@ManagedAttribute    
	public double[] getOxLevel() {
		return oxLevel;
	}
	public void setOxLevel(double[] oxLevel) {
		this.oxLevel = oxLevel;
	}
	@ManagedAttribute    
	public double[] getOxCOutWSfc() {
		return oxCOutWSfc;
	}
	public void setOxCOutWSfc(double[] oxCOutWSfc) {
		this.oxCOutWSfc = oxCOutWSfc;
	}
	@ManagedAttribute    
	public double[] getOxCSepWSfc() {
		return oxCSepWSfc;
	}
	public void setOxCSepWSfc(double[] oxCSepWSfc) {
		this.oxCSepWSfc = oxCSepWSfc;
	}
	@ManagedAttribute    
	public double[] getOxSfc() {
		return oxSfc;
	}
	public void setOxSfc(double[] oxSfc) {
		this.oxSfc = oxSfc;
	}
	@ManagedAttribute    
	public double[] getOxCOutWSfc2() {
		return oxCOutWSfc2;
	}
	public void setOxCOutWSfc2(double[] oxCOutWSfc2) {
		this.oxCOutWSfc2 = oxCOutWSfc2;
	}
	@ManagedAttribute    
	public double[] getOxCSepWSfc2() {
		return oxCSepWSfc2;
	}
	public void setOxCSepWSfc2(double[] oxCSepWSfc2) {
		this.oxCSepWSfc2 = oxCSepWSfc2;
	}
	@ManagedAttribute    
	public double[] getOxSfc2() {
		return oxSfc2;
	}
	public void setOxSfc2(double[] oxSfc2) {
		this.oxSfc2 = oxSfc2;
	}
	@ManagedAttribute    
	public int getIFMAX() {
		return IFMAX;
	}
	public void setIFMAX(int iFMAX) {
		IFMAX = iFMAX;
	}
	@ManagedAttribute    
	public int getIFANZ() {
		return IFANZ;
	}
	public void setIFANZ(int iFANZ) {
		IFANZ = iFANZ;
	}
	@ManagedAttribute    
	public int getIFEHL() {
		return IFEHL;
	}
	public void setIFEHL(int iFEHL) {
		IFEHL = iFEHL;
	}
	@ManagedAttribute    
	public int getBDFLAG() {
		return BDFLAG;
	}
	public void setBDFLAG(int bDFLAG) {
		BDFLAG = bDFLAG;
	}
	@ManagedAttribute    
	public double getPoxt() {
		return poxt;
	}
	public void setPoxt(double poxt) {
		this.poxt = poxt;
	}
	@ManagedAttribute    
	public double gettGOxT() {
		return tGOxT;
	}
	public void settGOxT(double tGOxT) {
		this.tGOxT = tGOxT;
	}
	@ManagedAttribute    
	public double gettLOxT() {
		return tLOxT;
	}
	public void settLOxT(double tLOxT) {
		this.tLOxT = tLOxT;
	}
	@ManagedAttribute    
	public double getPFuT() {
		return PFuT;
	}
	public void setPFuT(double pFuT) {
		PFuT = pFuT;
	}
	@ManagedAttribute    
	public double gettGFuT() {
		return tGFuT;
	}
	public void settGFuT(double tGFuT) {
		this.tGFuT = tGFuT;
	}
	@ManagedAttribute    
	public double gettLFuT() {
		return tLFuT;
	}
	public void settLFuT(double tLFuT) {
		this.tLFuT = tLFuT;
	}
	@ManagedAttribute    
	public PureGasPort getInputPortFuelPressureGas() {
		return inputPortFuelPressureGas;
	}
	public void setInputPortFuelPressureGas(PureGasPort inputPortFuelPressureGas) {
		this.inputPortFuelPressureGas = inputPortFuelPressureGas;
	}
	@ManagedAttribute    
	public PureGasPort getInputPortOxidizerPressureGas() {
		return inputPortOxidizerPressureGas;
	}
	public void setInputPortOxidizerPressureGas(
			PureGasPort inputPortOxidizerPressureGas) {
		this.inputPortOxidizerPressureGas = inputPortOxidizerPressureGas;
	}
	@ManagedAttribute    
	public PureLiquidPort getOutputPortFuel() {
		return outputPortFuel;
	}
	public void setOutputPortFuel(PureLiquidPort outputPortFuel) {
		this.outputPortFuel = outputPortFuel;
	}
	@ManagedAttribute    
	public PureLiquidPort getOutputPortOxidizer() {
		return outputPortOxidizer;
	}
	public void setOutputPortOxidizer(PureLiquidPort outputPortOxidizer) {
		this.outputPortOxidizer = outputPortOxidizer;
	}
}
