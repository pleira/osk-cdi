/*
 * HeliumJKC.java
 *
 * Created on 8. Juli 2007, 16:15
 *
 *  Computation of the polynomial coefficients for an approximation of the
 *  Joule-Kelvin-Coefficient of Helium as function of pressure and temperature.
 *
 *  Physics are taken from:
 *
 *    Eickhoff, J.:
 *    Erstellung und Programmierung eines Rechenverfahrens zur
 *    thermodynamischen Erfassung des Druckgas-Foerdersystems der
 *    ARIANE L5-Stufe und Berechnung des noetigen Heliumbedarfs zur
 *    Treibstoffoerderung.
 *    Studienarbeit am Institut fuer Thermodynamik der Luft- und Raumfahrt
 *    Universitaet Stuttgart, Pfaffenwaldring 31, 7000 Stuttgart 80, 1988
 *
 *    chapters 3.3.2 and 3.3.6.
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
 *  2008-05
 *      OpenSimKit V 2.4
 *      Ported from C++ to Java
 *      A. Brandt  alexander.brandt@gmail.com
 *
 *  2009-06
 *     Integrated logging support.
 *     T. Pieper
 *
 */
package org.opensimkit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *  Computation of the polynomial coefficients for an approximation of the
 *  Joule-Kelvin-Coefficient of Helium as function of pressure and temperature.
 *
 * @author J. Eickhoff
 * @author A. Brandt
 * @author T. Pieper
 * @version 1.1
 * @since 2.4.0
 */
public final class HeliumJKC {
    private static final Logger LOG = LoggerFactory.getLogger(HeliumJKC.class);

    /** Creates a new instance of HeliumJKC. */
    public HeliumJKC() {
    }

    static final double[][] KOEFF = {
            {2.423013E-2, 2.392126E-2, 2.398876E-2, 2.52027E-2, 2.554027E-2, 2.616685E-2, 2.680521E-2, 2.769804E-2, 2.786347E-2},
            {3.354024E-4, 3.46115E-4, 3.504475E-4, 3.36523E-4, 3.367276E-4, 3.32186E-4, 3.271426E-4, 3.175929E-4, 3.205922E-4},
            {-1.263466E-6, -1.3317E-6, -1.365671E-6, -1.304549E-6, -1.317771E-6, -1.306084E-6, -1.290667E-6, -1.250196E-6, -1.278945E-6},
            {2.31579E-9, 2.473935E-9, 2.55962E-9, 2.446718E-9, 2.490386E-9, 2.481965E-9, 2.464073E-9, 2.391366E-9, 2.469772E-9},
            {-1.661793E-12, -1.785971E-12, -1.857724E-12, -1.785917E-12, -1.828192E-12, -1.832863E-12, -1.829817E-12, -1.785697E-12, -1.854994E-12}};

    public static double JKCParams(int i, int j) {
        double value;

        if (i >= 0 && i < 5 && j >= 0 && j < 9) {
            value = KOEFF[i][j];
            return value;
        } else {
            if (i >= 5) {
                i = 4;
            }
            if (i < 0) {
                i = 0;
            }
            if (j >= 9) {
                j = 8;
            }
            if (j < 0) {
                j = 0;
            }

            LOG.warn("Indices out of bounds in JKCParams");
            LOG.info("Indices out of bounds in JKCParams");

            value = KOEFF[i][j];
            return value;
        }
    }
}
