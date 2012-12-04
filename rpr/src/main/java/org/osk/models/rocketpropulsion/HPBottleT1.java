/**
 *
 *  Model definition for a gas pressure vessel.
 *
 *
 *                            ++++
 *                         ++      ++
 *                       ++          ++
 *                      +      p,t     +
 *                     +              . +
 *                    +      .        m +--->-+  Output Port
 *                     +--> Q           +
 *                      +              +
 *                       ++          ++
 *                         ++      ++
 *                            ++++
 *
 *
 *  The pressure vessel object computes the following phenomena:
 *    - Expansion of gas (pure gas) including real gas effects.
 *    - Heat transfer from pressure regulator housing to fluid is included.
 *  Assumed is that the pressure vessel can be recognized adiabatic to
 *  the environment.
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
 *      File under GPL  see OpenSimKit Documentation.
 *
 *      No warranty and liability for correctness by author.
 */
package org.osk.models.rocketpropulsion;

import javax.annotation.PostConstruct;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.osk.SimHeaders;
import org.osk.materials.HeliumJKC;
import org.osk.materials.MaterialProperties;
import org.osk.models.BaseModel;
import org.osk.ports.FluidPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.org.glassfish.gmbal.ManagedAttribute;

public abstract class HPBottleT1 extends BaseModel {
	/** Logger instance for the HPBottleT1. */
	private static final Logger LOG = LoggerFactory.getLogger(HPBottleT1.class);
	/** Mass of pressure vessel. */
	private double mass;
	/** Volume of vessel. */
	private double volume;
	/** Specific. heat capacity of vessel. */
	private double specificHeatCapacity;
	/** Diameter of vessel. */
	private double diam;
	/** Surface of vessel (spherical bottle assumed). */
	private double surface;
	/** Pressure of gas in vessel. */
	private double ptotal;
	/** Temperature of gas in vessel. */
	private double ttotal;
	/** Vessel wall temperature. */
	private double twall;
	/** Value of pressure of gas in vessel from previous timestep. */
	private double pold;
	/** Value of temperature of gas in vessel from previous timestep. */
	private double told;
	/** Value of vessel wall temperature from previous timestep. */
	private double twold;
	/** Gradient of pressure of gas in vessel. */
	private double pgrad;
	/** Gradient of temperature of gas in vessel. */
	private double tgrad;
	/** Gradient of vessel wall temperature. */
	private double twgrad;
	/** Mass of gas in vessel. */
	private double mtotal;
	/** Mass flow of gas into pipe. */
	private double mftotal;
	/** Gas in vessel. */
	private String fluid;
	/** Heat flow from wall to fluid for pressure regul. elements. */
	private double qHFlow;
	/** Initial pressure of gas in vessel. */
	private double pinit;

	private static final String TYPE = "HPBottleT1";
	private static final String SOLVER = "Euler";
	
	
    public HPBottleT1() {
        super(TYPE, SOLVER);
    }

    @PostConstruct
    public void init() {
        MaterialProperties helium = new MaterialProperties();
        double radius;

        /* Computation of derived design parameters. */

        ptotal = ptotal * 1.E5;

        /* Initializing diameter. */
        radius = Math.pow((volume * 3 / (4 * 3.14159)), .33333);
        diam = radius * 2;

        /* Initializing surface. */
        surface = 4 * 3.14159 * Math.pow(radius, 2);
        /* Initializing heat flow. */
        qHFlow = 0.0;
        /* Initializing start pressure (to be saved). */
        pinit = ptotal;
        /* Initializing initial bottle wall temperature. */
        twall = ttotal;
        /* Initializing pressure gradient. */
        pgrad = 0.;
        /* Initializing temperature gradient. */
        tgrad = 0.;
        /* Initializing wall temperature gradient. */
        twgrad = 0.;
        /* Initializing pressure old value. */
        pold = ptotal;
        /* Initializing temperature old value. */
        told = ttotal;
        /* Initializing wall temperature old value. */
        twold = twall;
        /* Initializing initial mass in bottle. */
        /** TODO Look here, is this correct? */
        ptotal = org.osk.materials.Helium.HELIUM(ptotal, ttotal, helium);
        mtotal = helium.DICHTE * volume;

        /* Initializing default value for mass flow. */
        mftotal = 0.01;

        LOG.info("mass : {}", mass);
        LOG.info("volume : {}", volume);
        LOG.info("specificHeatCapacity : {}", specificHeatCapacity);
        LOG.info("ptotal : {}", ptotal);
        LOG.info("ttotal : {}", ttotal);
        LOG.info("fluid : {}", fluid);
    }
    

    public FluidPort iterationStep() {
        return newOutputPort();
    }

    public void timeStep(final double time, final double timeStep) {
        MaterialProperties helium = new MaterialProperties();
        double RALLG, RSPEZ, CP, CV;
        double PBEZ, DMASSE;
        double P1, P2, param = 0, JKC;
        int    I, L;
        double DTEMP, TBEZ, H;
        double FAKTOR, ST, PLANF, PLEND, PLAUF, P = 0, Wert;
        double gload, PRAN, GRAS, NU, ALFA, QLEIST, q;

        logState("% HPBottleT1 Start Conditions...");

        RALLG = 8314.3;
        RSPEZ = 2077;

        CP = 5223.2;
        CV = 3146.5;

        PBEZ = 5.0;

        DMASSE = mftotal * timeStep;
        helium.DICHTE = mtotal / volume;

        /**********************************************************************/
        /*                                                                    */
        /* Computation of specific enthalpy of gas in vessel by               */
        /* spline interpolations - please refer to [1] section 3.3.2.         */
        /*                                                                    */
        /**********************************************************************/

        ptotal = ptotal / 1E5;

        I = (int) (ptotal / 40. + 1.);

        P1 = 0;
        P2 = 0;

        if (I <= 8) {
            for (L = 0; L < 5; L++) {
                param = HeliumJKC.JKCParams(L, I-1);
                P1 = P1+ param * Math.pow(ttotal, L);
                param = HeliumJKC.JKCParams(L, I);
                P2 = P2 + param * Math.pow(ttotal, L);
            }
        } else {
            for(L = 0; L < 5; L++) {
                param = HeliumJKC.JKCParams(L,7);
                P1=P1+ param * Math.pow(ttotal ,L);
                param = HeliumJKC.JKCParams(L,8);
                P2=P2+ param * Math.pow(ttotal ,L);
            }
        }
        JKC=P1+(P2-P1)*(ptotal-(I-1)*40)/40;
        JKC= JKC*-1.0;

        DTEMP=-JKC*(ptotal-PBEZ);
        TBEZ=ttotal+DTEMP;
        H = -19846.5 + 5732.967 * TBEZ - 2.42982 * Math.pow(TBEZ, 2)
                + 3.332099E-3 * Math.pow(TBEZ, 3);

        /**********************************************************************/
        /*                                                                    */
        /*   Computation of resulting temp. difference in time interval dT    */
        /*   by solving energy equation:                                      */
        /*               .                 .                   .              */
        /*               Q                 mT                h*m              */
        /*   dT =     ------- dt    +  --------- dt    -   -------- dt        */
        /*             m  *c               m                 m * c            */
        /*                  V                                     V           */
        /*                                                                    */
        /**********************************************************************/

        DTEMP = qHFlow * timeStep /(mtotal * CV)
            + mftotal * ttotal * timeStep /mtotal
            - H * mftotal * timeStep/(mtotal * CV);

        /**********************************************************************/
        /*                                                                    */
        /*  Computation of new temperature and fluid mass in vessel           */
        /*  and computation of new fluid density after time interval dT       */
        /*                                                                    */
        /**********************************************************************/

        ttotal=ttotal+DTEMP;
        mtotal=mtotal-DMASSE;
        helium.DICHTE=mtotal/volume;

        /**********************************************************************/
        /*                                                                    */
        /*   Computation of pressure after expansion considering non-ideal    */
        /*   gas effects:                                                     */
        /*   - assumption of a pressure value                                 */
        /*   - therewith computation of the compressibility factor Z          */
        /*   - comparison of assumed pressure with                            */
        /*     Z-factor derived one: p=Z*DICHTE*RSPEZ.*TEMP                   */
        /*   - correction of assumed pressure value and reiteration until     */
        /*     deviation < 5e-4                                               */
        /*                                                                    */
        /**********************************************************************/

        FAKTOR=(1.913688E-3)-(8.520942E-6)*ttotal;
        FAKTOR=FAKTOR+1.358845E-8*Math.pow(ttotal,2);
        FAKTOR=FAKTOR-(4.595341E-12*Math.pow(ttotal,3));

        PLANF=.7*helium.DICHTE*RSPEZ*ttotal;
        PLEND=pinit*1.1;
        ST=1E6;
        // FIXME: check for maximum 10000 iterations
        int i = 0;
        for(PLAUF=PLANF;PLAUF<PLEND && i<10000;PLAUF+=ST, i++) {

            helium.Z=1.0+FAKTOR*PLAUF/1E5;
            P=helium.Z*helium.DICHTE*RSPEZ*ttotal;
            Wert=(PLAUF-P)/PLAUF;
            if(Wert<0) Wert=Wert*-1.;
            if (Wert<=0.01*SimHeaders.epsrel) break;
            if (PLAUF>=P)
                if (PLAUF>P) {
                PLANF=PLAUF-ST;
                PLEND=PLAUF;
                ST=ST/10;
                PLAUF=PLANF;
                }
        }
        if (i>=10000) {
        	LOG.warn("!!! Problem iterating the Pressure in HPTankT1");
        }
        //ptotal=P;

        /**********************************************************************/
        /*                                                                    */
        /*   Computation of fluid properties (Viskosity etc.)                 */
        /*   at current conditions (Temp. THEBEH and press- PHEBEH)           */
        /*                                                                    */
        /**********************************************************************/

        ptotal = org.osk.materials.Helium.HELIUM(P, ttotal, helium);

        /**********************************************************************/
        /*                                                                    */
        /*   Computation of heat transfer in vessel from wall to fluid.       */
        /*   Computation of Prandtl-, Nusselt, Grashof-numbers,               */
        /*   computation of heat transfer coeff. Alfa,                        */
        /*   and transferred heat using flow rate from previous timestep      */
        /*   (Euler method since press. & temps. all hav negat. gradients     */
        /*                                                                    */
        /**********************************************************************/

        PRAN=CP*helium.ETA/helium.LAMBDA;
        gload=GLoad.load();
        GRAS=Math.abs(gload*Math.pow(diam,3)*(twall-ttotal))
        /(Math.pow(helium.NUE,2)*((twall+ttotal)/2));
        NU=.098*Math.pow((GRAS*PRAN),.345);
        ALFA=NU*helium.LAMBDA/diam;
        QLEIST=ALFA*surface*(twall-ttotal);
        q=qHFlow*timeStep;

        /**********************************************************************/
        /*                                                                    */
        /*   Computation of vessel wall temp. change and storage of heat-     */
        /*   flow rate for next timestep computation.                         */
        /*                                                                    */
        /**********************************************************************/

        twall=twall-(q/(mass*specificHeatCapacity));
        qHFlow = QLEIST;

        pgrad = (ptotal - pold) / timeStep;
        tgrad = (ttotal - told) / timeStep;
        twgrad = (twall - twold) / timeStep;

        pold = ptotal;
        told = ttotal;
        twold = twall;
        logState("% End Conditions...");
         
    }

    public void backIterStep(FluidPort outputPort) {

        mftotal = outputPort.getBoundaryMassflow();
        if (outputPort.getBoundaryPressure() >= 0.0) {
            LOG.error("Pressure request on port 0 cannot be handled!");
        }
        if (outputPort.getBoundaryTemperature() >= 0.0) {
            LOG.error("Temp. request on port 0 cannot be handled!");
        }
        // TBC Do we need this: getBackIterEvent().fire(outputPort);
    }


	private void logState(String header) {
		LOG.info(header);
        LOG.info("ptotal : {}", ptotal);
        LOG.info("ttotal : {}", ttotal);
        LOG.info("mtotal : {}", mtotal);
        LOG.info("mftotal : {}", mftotal);
	}

	public ImmutablePair<Double, FluidPort> createInputPortIter(double timeStep) {
		return new ImmutablePair<Double, FluidPort> (timeStep, newOutputPort());
	}
	 

	private FluidPort newOutputPort() {
		FluidPort outputPort = new FluidPort();
        outputPort.setFluid(fluid);
        outputPort.setPressure(ptotal);
        outputPort.setTemperature(ttotal);
        outputPort.setMassflow(mftotal);
		return outputPort;
	}


    //-----------------------------------------------------------------------------------
    // Methods added for JMX monitoring	and setting initial properties via CDI Extensions

	@ManagedAttribute
	public double getMass() {
		return mass;
	}
	public void setMass(double mass) {
		this.mass = mass;
	}
	@ManagedAttribute	
	public double getVolume() {
		return volume;
	}
	public void setVolume(double volume) {
		this.volume = volume;
	}
	@ManagedAttribute
	public double getSpecificHeatCapacity() {
		return specificHeatCapacity;
	}
	public void setSpecificHeatCapacity(double specificHeatCapacity) {
		this.specificHeatCapacity = specificHeatCapacity;
	}
	@ManagedAttribute
	public double getPtotal() {
		return ptotal;
	}
	public void setPtotal(double ptotal) {
		this.ptotal = ptotal;
	}
	@ManagedAttribute
	public double getTtotal() {
		return ttotal;
	}
	public void setTtotal(double ttotal) {
		this.ttotal = ttotal;
	}
	@ManagedAttribute
	public String getFluid() {
		return fluid;
	}
	public void setFluid(String fluid) {
		this.fluid = fluid;
	}
	@ManagedAttribute
	public double getDiam() {
		return diam;
	}
	public void setDiam(double diam) {
		this.diam = diam;
	}
	@ManagedAttribute
	public double getSurface() {
		return surface;
	}
	public void setSurface(double surface) {
		this.surface = surface;
	}
	@ManagedAttribute
	public double getTwall() {
		return twall;
	}
	public void setTwall(double twall) {
		this.twall = twall;
	}
	@ManagedAttribute
	public double getPold() {
		return pold;
	}
	public void setPold(double pold) {
		this.pold = pold;
	}
	@ManagedAttribute
	public double getTold() {
		return told;
	}
	public void setTold(double told) {
		this.told = told;
	}
	@ManagedAttribute
	public double getTwold() {
		return twold;
	}
	public void setTwold(double twold) {
		this.twold = twold;
	}
	@ManagedAttribute
	public double getPgrad() {
		return pgrad;
	}
	public void setPgrad(double pgrad) {
		this.pgrad = pgrad;
	}
	@ManagedAttribute
	public double getTgrad() {
		return tgrad;
	}
	public void setTgrad(double tgrad) {
		this.tgrad = tgrad;
	}
	@ManagedAttribute
	public double getTwgrad() {
		return twgrad;
	}
	public void setTwgrad(double twgrad) {
		this.twgrad = twgrad;
	}
	@ManagedAttribute
	public double getMtotal() {
		return mtotal;
	}
	public void setMtotal(double mtotal) {
		this.mtotal = mtotal;
	}
	@ManagedAttribute
	public double getMftotal() {
		return mftotal;
	}
	public void setMftotal(double mftotal) {
		this.mftotal = mftotal;
	}
	@ManagedAttribute
	public double getqHFlow() {
		return qHFlow;
	}
	public void setqHFlow(double qHFlow) {
		this.qHFlow = qHFlow;
	}
	@ManagedAttribute
	public double getPinit() {
		return pinit;
	}
	public void setPinit(double pinit) {
		this.pinit = pinit;
	}

}
