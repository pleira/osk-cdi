/*
 * DEQClients.java
 *
 * Created on 7. Juli 2007, 14:13
 *
 *  Class definition for objects that use the DEQ numeric procedures
 *  ENGL45 and DGLSYS taken from:
 *
 *  G. Engeln-Muellges, F.Reutter
 *  "Formelsammlung zur numerischen Mathematik mit Standard-FORTRAN-77-
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
 *   2008-05
 *      OpenSimKit V 2.4
 *      Ported from C++ to Java
 *      A. Brandt  alexander.brandt@gmail.com
 */

package org.osk.numeric;

/**
 * Interface definition for objects that use the DEQ numeric procedures
 *  ENGL45 and DGLSYS taken from:
 *
 *  G. Engeln-Muellges, F.Reutter
 *  "Formelsammlung zur numerischen Mathematik mit Standard-FORTRAN-77-
 *  Programmen", 5. Aufl.
 *  B.I. Wissenschaftsverlag , Bibliographisches Institut
 *  Mannheim/Wien/Zuerich, 1986
 *
 * @author J. Eickhoff
 * @author A. Brandt
 */
public interface DEQClient {
    int DEQDeriv(double X, double[] Y, int N, double[] F);
}
