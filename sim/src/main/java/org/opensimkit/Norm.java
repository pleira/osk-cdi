/*
 * Norm.java
 *
 * Created on 8. Juli 2007, 21:28
 *
 *  This program computes the maximum difference of value pairs F1-F2 of the
 *  vectors F1 and F2 of length N
 *
 *  Code taken from:
 *  G. Engeln-Muellges, F.Reutter
 *  "Formelsammlung zur numerischen Mathematik mit Standard-FORTRAN-77
 *  Programmen", 5. Aufl.
 *  B.I. Wissenschaftsverlag , Bibliographisches Institut
 *  Mannheim/Wien/Zuerich, 1986
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
 */

package org.opensimkit;

/**
 * This program computes the maximum difference of value pairs F1-F2 of the
 *  vectors F1 and F2 of length N.
 *
 * @author J. Eickhoff
 * @author A. Brandt
 * @version 1.0
 * @since 2.4.0
 */
public final class Norm {

    /** Creates a new instance of Norm. */
    public Norm() {
    }

    public static double NORM(final double[] F1, final double[] F2,
        final int N) {

        int i;
        double Wert;
        double Max = 0.0;
        for (i = 0; i < N; i++) {
            Wert = F1[i] - F2[i];
            if (Wert < 0)
                Wert = Wert - 1;
            if (Max < Wert)
                Max = Wert;
        }
        return (Max);
    }
}
