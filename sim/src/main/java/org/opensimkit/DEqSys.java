/*
 * DEqSys.java
 *
 * Created on 7. Juli 2007, 14:17
 *
 *  Object oriented adaption of the DEQ integration routine DGLSYS taken from:
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
 *  Object oriented adaption of the DEQ integration routine DGLSYS taken from:
 *
 *  G. Engeln-Muellges, F.Reutter
 *  "Formelsammlung zur numerischen Mathematik mit Standard-FORTRAN-77-
 *  Programmen", 5. Aufl.
 *  B.I. Wissenschaftsverlag , Bibliographisches Institut
 *  Mannheim/Wien/Zuerich, 1986
 *
 * @author J. Eickhoff
 * @author A. Brandt
 * @author T. Pieper
 * @version 1.1
 * @since 2.4.6
 */
public class DEqSys {
    private static final Logger LOG = LoggerFactory.getLogger(DEqSys.class);
    private static int    LEPSI = 0;
    private static double EPSLON;
    private static double EPS1 = 0;
    private static double EPS2 = 0;

    /** Creates a new instance of DEqSys. */
    public DEqSys() {
    }

    /*****************************************************************/
    /*                                                               */
    /*  Dieses Programm berechnet, ausgehend von der Naeherung YK    */
    /*  fuer die Loesung Y des Systems gewoehnlicher Differential-   */
    /*  gleichungen 1. Ordnung                                       */
    /*                           Y' = F(X,Y)                         */
    /*  im Punkt XK, eine Naeherung fuer die Loesung Y im Punkt      */
    /*  XENDE. Dabei wird intern mit Schrittweitensteuerung so ge-   */
    /*  arbeitet, dass der Fehler der berechneten Naeherung absolut  */
    /*  oder relativ in der Groessenordnung der vorgegebenen Fehler- */
    /*  schranken EPSABSS und EPSREL liegt.                          */
    /*  (Fuer Variablentypen der Uebergabeparameter gelten die       */
    /*  Standard-FORTRAN-Konventionen .)                             */
    /*                                                               */
    /*  EINGABEPARAMETER:                                            */
    /* ===================                                           */
    /*  XK      -Ausgangspunkt der unabh. Variablen X                */
    /*  HK      -Prognostizierte Schrittweite fuer den naechsten     */
    /*         Schritt                                               */
    /*  YK      -1-dim. Feld (1:N) ; Wert fuer die Loesung der Dgl   */
    /*         an der Stelle XK                                      */
    /*  N       -Anzahl der Differentialgleichungen                  */
    /*                                                               */
    /*  DIFFGL  -Rechte Seite der Differentialgleichungen, die als   */
    /*         Unterprogramm der Form                                */
    /*              S U B R O U T I N E   D I F F G L (X,Y,N,F)      */
    /*         REAL Y(N),F(N)                                        */
    /*         F(1)=F1(X,Y(1)........Y(N))                           */
    /*          ..                  ..                               */
    /*         F(N)=FN(X,Y(1)........Y(N))                           */
    /*                                                               */
    /*         zur Verfuegung gestellt sein muss, wobei F(1) bis     */
    /*         F(N) die Werte der rechten Seiten der DGL's dar-      */
    /*         stellen.                                              */
    /*                                                               */
    /*  XENDE  -Stelle, an der die Loesung gewuenscht wird; XENDE    */
    /*        darf nicht kleiner als XK gewaehlt werden.             */
    /* EPSABS  -Fehlerschranke fuer die absolute Genauigkeit der zu  */
    /*        berechnenden Loesung. Es muss EPSABS>=0 sein; Fuer     */
    /*        EPSABS=0 wird nur die relative Genauigk. betrachtet.   */
    /* EPSREL  -Fehlerschranke fuer die relative Genauigkeit der zu  */
    /*        berechnenden Loesung. Es muss EPSREL>=0 sein; Fuer     */
    /*        EPSREL=0 wird nur die absolute Genauigk. betrachtet.   */
    /*  IFMAX  -Obere Schranke fuer die Anzahl der zulaessigen       */
    /*        Funktionsauswertungen der rechten Seite des DGL-       */
    /*        Systems.                                               */
    /*                                                               */
    /*  AUSGABEPARAMETER:                                            */
    /* ======================                                        */
    /* XK      -Stelle, die bei der Integration zuletzt erreicht     */
    /*        wurde. Im Fall IFEHL=0 ist normalerweise XK=XENDE      */
    /* HK      -Zuletzt verwendete lokale Schrittweite (sollte fuer  */
    /*        den naechsten Schritt unveraendert gelassen werden)    */
    /* YK      -Naeherungswert der Loesung an der neuen Stelle XK    */
    /* IFANZ   -Anzahl der tatsaechlich benoetigten Funktionsaufrufe */
    /*                                                               */
    /* IFEHL   - Fehlerparameter                                     */
    /*         =0      alles OK                                      */
    /*         =1      Beide Fehlerschranken relativ z. Rechen-      */
    /*                 genauigk. zu klein                            */
    /*         =2      XENDE<=XK innerh. der Rechengenauigkeit       */
    /*         =3      Schrittweite HK<=0 innerh. Rechengenauigk.    */
    /*         =4      N>20  oder N<=0                               */
    /*         =5      IFANF > IFMAX: Die Zahl der zulaessigen       */
    /*                 Funktionsauswertungen reicht nicht aus eine   */
    /*                 geeignete Naeherungsloesung mit der gefor-    */
    /*                 derten Genauigk. zu bestimmen. XK und HK      */
    /*                 enthalten die aktuellen Werte bei Abbruch.    */
    /*                                                               */
    /*  BENOETIGTE UNTERPROGRAMME :                                  */
    /* ==============================                                */
    /*                                                               */
    /*  REAL FUNCTION NORM                                           */
    /*  SUBROUTINE    ENGL45                                         */
    /*                                                               */
    /*  RECHNERABHAENGIGE, LOKALE VARIABLE :                         */
    /* ======================================                        */
    /*                                                               */
    /*  Maschinengenauigkeit EPSLON, EPS1, EPS2                      */
    /*                                                               */
    /*****************************************************************/
    /*                                                               */
    /*  Nach "Formelsammlung zur numerischen Mathematik mit Stan-    */
    /*  dard-FORTRAN-77-Programmen",  5. Aufl.                       */
    /*  G. Engeln-Muellges, F.Reutter                                */
    /*  B.I. Wissenschaftsverlag , Bibliographisches Institut,       */
    /*  Mannheim/Wien/Zuerich, 1986                                  */
    /*                                                               */
    /*****************************************************************/

    public static int DEqSys(double ZEIT1, double HK, double[] YK, int NN,
            double ZEIT2, double EPSABS, double EPSREL, int IFMAX, int IFANZ,
            int IFEHL, DEQClient Client) {
        double[] Y   = new double[20];
        double[] YT  = new double[20];
        double[] Y00 = new double[20];
        double VZ,YMAX,HHILF = 0,DIFF,S;
        int IEND;
        double XEND;
        int i;

        for (i = 0; i < 20; i++)
            Y00[i] = 0;

        /** EPSLON ist die Maschinengenauigkeit der benutzten Anlage      */
        /** d.h. die kleinste, positive Zahl, die 1+EPSLON > 1 erfuellt.  */
        /** EPS1 faengt eine moeglicherweise zu kleine Schrittweite HK am */
        /** Intervallende ab, EPS2 dient bei Abfragen auf Null.           */
        if (LEPSI == 0) {
            EPSLON = 1.0;
            do {
                EPSLON = .5 * EPSLON;
            }
            while((1.0 + EPSLON) != 1.0);
            EPSLON = 2.0 * EPSLON;
            EPS1 = Math.pow(EPSLON, .75);
            EPS2 = 100.0 * EPSLON;
            LEPSI = 1;
        }

        /** Vorbesetzen lokaler Groessen                                  */
        if(ZEIT2 >= 0)
            VZ = 1.0;
        else
            VZ = -1.0;
        XEND = (1.0 - VZ * EPS2) * ZEIT2;
        IFEHL = 0;
        IFANZ = 0;
        IEND = 1;

        /** Plausibilitaetskontrolle der Eingabeparameter                 */
        YMAX=Norm.NORM(YK,Y00,NN);
        if((EPSABS <= (EPS2*YMAX)) && (EPSREL <= EPS2)) {
            IFEHL=1;
            LOG.warn("Value for abs. and rel. accuracy in DEqSys() "
                + "below numeric accuracy.");
            SimHeaders.negativeAckFlag = 1;
            return(IFEHL);
        } else if(XEND < ZEIT1) {
            IFEHL=2;
            LOG.warn("Integration step end not reached.");
            SimHeaders.negativeAckFlag = 1;
            return(IFEHL);
        } else if(HK < (EPS2*Math.abs(ZEIT1))) {
            IFEHL=3;
            LOG.warn("Value for integration stepsize in DEqSys() "
                + "below numeric accuracy.");
            SimHeaders.negativeAckFlag = 1;
            return(IFEHL);
        } else if((NN <= 0)||(NN > 20)) {
            IFEHL=4;
            LOG.error("Error in DEqSys() : Number of equations "
                + "exceeds 20. ");
            LOG.error("                    Dimension must also to be"
                + " adapted in ENGL45() ");
            SimHeaders.negativeAckFlag = 1;
            return(IFEHL);
        }

        /******* STEUERUNGSALGORITHMUS*************************************/
        if((ZEIT1+HK) > XEND) {
            HK=ZEIT2-ZEIT1;
            HHILF=HK;
            IEND=0;
        }
        /** Integration auf dem Intervall [ZEIT1,XENDE] in angemessenen      */
        /** Schritten                                                        */

        /*50      CONTINUE*/
        do {
            /** Aufruf des Einschrittverfahrens                              */
            Engl45.ENGL45(ZEIT1,HK,YK,NN,Y,YT,Client);

            IFANZ+=6;
            DIFF=Norm.NORM(Y,YT,NN);
            if(DIFF < EPS2)
                S=2.0;
            else {
                YMAX=Norm.NORM(YT,Y00,NN);
                S=Math.sqrt(HK*(EPSABS+EPSREL*YMAX)/DIFF);
                S=Math.sqrt(S);
            }
            if(S > 1.0) {
                /** Der durchgefuehrte Schritt wird akzeptiert               */

                for(i=0;i<NN;i++)

                    YK[i]=YT[i];
                ZEIT1=ZEIT1+HK;
                /** Fall die Integrationsgrenze ZEIT2 erreicht wurde, oder
                 * falls schon mehr als die zulaessige Zahl von
                 * Funktionsauswertungen gemacht wurden:  Ruecksprung.       */
                if(IEND==0) {
                    HK=HHILF;
                    return(0);
                } else
                    if(IFANZ >IFMAX) {     /**FEHLER???IANZ ODER IFANZ **/
                    IFEHL=5;
                    LOG.error("Max. number of integration function "
                        + "calls exceeded in DEqSys().");
                    SimHeaders.negativeAckFlag = 1;
                    return(5);
                    }
               /** Die Schrittweite fuer den naechsten Schritt wird angemessen*/
               /** maximal um der Faktor 2 vergroessert.                      */
                if(2.0<(.98*S))
                    HK=HK*2.0;
                else
                    HK=HK*.98*S;
                if((ZEIT1+HK) >= XEND) {
                    HHILF=HK;
                    HK=ZEIT2-ZEIT1;
                    IEND=0;
                    /** Falls man schon sehr nahe bei ZEIT2 ist: Ruecksprung */
                    if(HK < (EPS1*Math.abs(ZEIT2))) {
                        HK=HHILF;
                        return(0);
                    }
                }
            } else {
            /** Der letzte Schritt wird nicht akzeptiert; die Schrittweite HK */
            /** wird vor Wiederholung des Schritts verkleinert, jedoch        */
            /** hoechstens halbiert.                                          */
                if(.5>(.98*S))
                    HK=HK*.5;
                else
                    HK=HK*.98*S;
                IEND=1;
            }
        }
        while(true);
    }
}
