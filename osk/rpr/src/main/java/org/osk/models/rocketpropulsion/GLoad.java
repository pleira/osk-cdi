/*
 * GLoad.java
 *
 * Created on 8. Juli 2007, 15:50
 *
 *  Computation of the polynomial approximation of the gravity acceleration
 *  as function of time. Polynomial coefficients are loaded at program
 *  initialization.
t this.name = name;  
 *
 *-----------------------------------------------------------------------------
 * Modification History:
 *
 *  2004-12-05
 *      File created - J. Eickhoff:
 *
 *      Class architecture is a derivative from ObjectSim 2.0.3.,
 *      a simulation program published in:
 *
 *        Eickhoff, J.:
 *        Modulare Programmarchitektur fuer ein wissensbasiertes
 *        Simulationssystem mit erweiterter Anwendbarkeit in der
 *        Entwicklung und Betriebsueberwachung verfahrenstechnischer
 *        Anlagen.
 *        PhD thesis in Department Process Engineering of
 *        TU Hamburg-Harburg, 1996.
 *
 *      See also file history cited there and see historic relation of
 *      this OpenSimKit class to a.m. ObjectSim explained in
 *      OpenSimKit Documentation.
 *
 *      File under GPL - see OpenSimKit Documentation.
 *
 *      No warranty and liability for correctness by author.
 *
 *   2008-05
 *      OpenSimKit V 2.4
 *      Ported from C++ to Java
 *      A. Brandt  alexander.brandt@gmail.com
 *
 */

package org.osk.models.rocketpropulsion;

/**
 * Computation of the polynomial approximation of the gravity acceleration
 *  as function of time. Polynomial coefficients are loaded at program
 *  initialization.
 *
 * @author J. Eickhoff
 * @author A. Brandt
 */
public final class GLoad {

    /** Creates a new instance of GLoad. */
    public GLoad() {
    }

    /** No Gload variation here for simulation of ground tests.
     *
     * @return Gravity acceleration.
     */
    public static double load() {
        return 9.81;
    }
}
