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
package org.osk.models.t1;

import javax.inject.Inject;

import net.gescobar.jmx.annotation.ManagedAttribute;

import org.osk.config.SimHeaders;
import org.osk.models.BaseModel;
import org.osk.models.materials.HeliumJKC;
import org.osk.models.materials.HeliumPropertiesBuilder;
import org.osk.models.materials.MaterialProperties;
import org.osk.ports.FluidPort;
import org.slf4j.Logger;


public class HPBottleT1 extends BaseModel {
	@Inject Logger LOG; 
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

    public void init(String name) {
    	this.name = name;  
        final double ptotal_Pa = ptotal * 1.E5;
        final double radius = Math.pow((volume * 3 / (4 * Math.PI)), .33333);
        diam = radius * 2;
        surface = 4 * Math.PI * Math.pow(radius, 2);
        qHFlow = 0.0;
        pinit = ptotal_Pa; 
        twall = ttotal;
        MaterialProperties helium = HeliumPropertiesBuilder.build(ptotal, ttotal);
        mtotal = helium.DENSITY * volume;
    }
    
    public void calculateMassFlow(double timeStep) {
        // logState("% HPBottleT1 Start Conditions...");
        // ptotal = ptotal / 1E5;
        final double ptotal_Pa = ptotal * 1.E5;
        final double DTEMP = computeTempDifference(timeStep, 
        		ptotal, ttotal, mftotal, qHFlow, mtotal);
		final double dmass = mftotal * timeStep ;

        ttotal += DTEMP;
        mtotal -= dmass;
        ptotal = computePressure(ttotal, mtotal/volume); // bar
        qHFlow = computeWallHeatTransferFlow(ptotal, ttotal, twall);
        final double q=qHFlow*timeStep;
        twall -= q /(mass*specificHeatCapacity);
        // logState("% End Conditions...");
    }

	private double computeWallHeatTransferFlow(double ptotal, double ttotal, double twall) {

        /**********************************************************************/
        /*                                                                    */
        /*   Computation of heat transfer in vessel from wall to fluid.       */
        /*   Computation of Prandtl-, Nusselt, Grashof-numbers,               */
        /*   computation of heat transfer coeff. Alfa,                        */
        /*   and transferred heat using flow rate from previous timestep      */
        /*   (Euler method since press. & temps. all hav negat. gradients     */
        /*                                                                    */
        /**********************************************************************/
        MaterialProperties helium = HeliumPropertiesBuilder.build(ptotal, ttotal);
        final double CP = 5223.2;
        final double PRAN=CP*helium.ETA/helium.LAMBDA;
        // FIXME the gload value returned is gravity at sea level
        final double gload=GLoad.load();
        final double GRAS=Math.abs(gload*Math.pow(diam,3)*(twall-ttotal))
                                /(Math.pow(helium.NUE,2)*((twall+ttotal)/2));
        final double NU=.098*Math.pow((GRAS*PRAN),.345);
        final double ALFA=NU*helium.LAMBDA/diam;
        final double QLEIST=ALFA*surface*(twall-ttotal);
        return QLEIST;
	}

	private double computePressure(double ttotal, double density) {
        /**********************************************************************/
        /*                                                                    */
        /*   Computation of pressure after expansion considering non-ideal    */
        /*   gas effects:                                                     */
        /*   - assumption of a pressure value                                 */
        /*   - therewith computation of the compressibility factor Z          */
        /*   - comparison of assumed pressure with                            */
        /*     Z-factor derived one: p=Z*DENSITY*RSPEZ.*TEMP                   */
        /*   - correction of assumed pressure value and reiteration until     */
        /*     deviation < 5e-4                                               */
        /*                                                                    */
        /**********************************************************************/
       final double FAKTOR = 1.913688E-3 - 8.520942E-6 * ttotal
                + 1.358845E-8*Math.pow(ttotal,2) - 4.595341E-12*Math.pow(ttotal,3);

          final double RSPEZ = 2077;
          double PLANF=.7*density*RSPEZ*ttotal;
          double PLEND=pinit*1.1;
          double ST=1E6;
          double heliumZ=1.0+FAKTOR*PLANF/1E5;
          // FIXME: check for maximum 1000 iterations
          int i = 0;
          double P = heliumZ*density*RSPEZ*ttotal;;
          for(double PLAUF=PLANF; PLAUF<PLEND && i<1000; PLAUF+=ST, i++) {
              heliumZ = 1.0 + FAKTOR*PLAUF/1E5;
              P = heliumZ*density*RSPEZ*ttotal;
              final double Wert= Math.abs((PLAUF-P)/PLAUF);
              if (Wert<=0.01*SimHeaders.epsrel) break;
              if (PLAUF>=P)
                  if (PLAUF>P) {
                  PLANF=PLAUF-ST;
                  PLEND=PLAUF;
                  ST=ST/10;
                  PLAUF=PLANF;
                  }
          }
          if (i>=1000) {
          	LOG.warn("!!! Problem iterating the Pressure in HPTankT1");
          }
          return P;
	}

	private double computeTempDifference(final double timeStep, final double pressure,
			final double temp, final double mftotal, final double qHFlow, double mtotal) {
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

        final double CV = 3146.5;
		final double H = computeEntalphy(pressure, temp);
        final double DTEMP = (qHFlow /CV + mftotal * (temp - H/CV))
        		            * timeStep / mtotal;
		return DTEMP;
	}

	private double computeEntalphy(double pressure, double temp) {
        /**********************************************************************/
        /*                                                                    */
        /* Computation of specific enthalpy of gas in vessel by               */
        /* spline interpolations - please refer to [1] section 3.3.2.         */
        /*                                                                    */
        /**********************************************************************/
		int I = (int) (pressure / 40. + 1.);

        double P1, P2, param = 0;
        P1 = 0;
        P2 = 0;

        if (I <= 8) {
            for (int L = 0; L < 5; L++) {
                param = HeliumJKC.JKCParams(L, I-1);
                P1 += param * Math.pow(temp, L);
                param = HeliumJKC.JKCParams(L, I);
                P2 += param * Math.pow(temp, L);
            }
        } else {
            for(int L = 0; L < 5; L++) {
                param = HeliumJKC.JKCParams(L,7);
                P1 += param * Math.pow(temp ,L);
                param = HeliumJKC.JKCParams(L,8);
                P2 += param * Math.pow(temp ,L);
            }
        }
        final double JKC = -(P1+(P2-P1)*(pressure-(I-1)*40)/40);

        final double PBEZ = 5.0;
        final double DHTEMP = -JKC*(pressure-PBEZ);
        final double TBEZ=temp+DHTEMP;
        final double H = -19846.5 + 5732.967 * TBEZ - 2.42982 * Math.pow(TBEZ, 2)
                + 3.332099E-3 * Math.pow(TBEZ, 3);
		return H;
	}

	private void logState(String header) {
		LOG.info(header);
        LOG.info("ptotal : {}", ptotal);
        LOG.info("ttotal : {}", ttotal);
        LOG.info("mtotal : {}", mtotal);
        LOG.info("mftotal : {}", mftotal);
	}

	public FluidPort createInputPortIter() {
		return new FluidPort(name, fluid, ptotal, ttotal, mftotal);
	}

	public FluidPort getOutputPortStatus() {
		return new FluidPort(name, fluid, ptotal, ttotal, mftotal);
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
