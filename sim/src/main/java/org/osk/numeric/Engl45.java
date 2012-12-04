/*
 * Engl45.java
 *
 * Created on 7. Juli 2007, 14:37
 *
 *  Object oriented adaption of the DEQ integration routine ENGL45 taken from:
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
 *
 */
package org.osk.numeric;

/**
 *  Object oriented adaption of the DEQ integration routine ENGL45 taken from:
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
public final class Engl45 {

    /** Creates a new instance of Engl45. */
    public Engl45() {
    }

    public static void ENGL45(double X, double HK, double[] YK, int NN,
            double[] Y, double[] YT, DEQClient Client) {
            /*****************************************************************/
            /*                                                               */
            /*  Dieses Programm berechnet, ausgehend von der Naeherung Y     */
            /*  an der Stelle X , ueber die Einbettungsformel von ENGLAND    */
            /*  Naeherungen 4. und 5. Ordnung Y und YT an der Stelle X + H   */
            /*  des ueber "D G L" zur Verfuegung gestellten Differential-    */
            /*  gleichungssystems 1. Ordnung                                 */
            /*                 Y' = F(X,Y)                                   */
            /*  von  N gewoehnlichen Differentialgleichungen 1. Ordnung.     */
            /*                                                               */
            /*  EINGABEPARAMETER :                                           */
            /* ======================                                        */
            /*                                                               */
            /*  X       -Ausgangspunkt der unabhaengigen Variablen X         */
            /*  H       -Schrittweite                                        */
            /*  Y       -1-dimensionales Feld (1:N) ; Wert fuer die Loesung  */
            /*         des DGL-Systems an der Stelle X                       */
            /*  N       -Anzahl der Differentialgleichungen ( 1<= N <=20     */
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
            /*  AUSGABEPARAMETER:                                            */
            /* ======================                                        */
            /*                                                               */
            /*  Y     -1-dim. Feld (1:N) ; Naeherung 4. Ordnung fuer die     */
            /*         Loesung des DGL-Systems an der Stelle X+H .           */
            /*  YT    -1-dim. Feld (1:N) ; Naeherung 5. Ordnung fuer die     */
            /*         Loesung des DGL-Systems an der Stelle X+H .           */
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
        double[] K1    = new double[20];
        double[] K2    = new double[20];
        double[] K3    = new double[20];
        double[] K4    = new double[20];
        double[] K5    = new double[20];
        double[] K6    = new double[20];
        double[] YHILF = new double[20];
        int    i;

        // DIFFGL(X,YK,NN,K1,GEOM,FLUIDE,AKTUEL,STORE,FLAGS);
        Client.DEQDeriv(X,YK,NN,K1);
        for (int j = 0; j < YK.length ; j++)   {
        	if (Double.isNaN(YK[j])) {
        		System.out.println("In 45 After 2 Check NaN");
        		return;
        	}
        	if (Double.isNaN(K1[j])) {
        		System.out.println("In K 45 After 2 Check NaN");
        		return;
        	}
        }
        for(i=0;i<NN;i++)
            YHILF[i]=YK[i]+.5*HK*K1[i];
        // DIFFGL(X+.5*HK,YHILF,NN,K2,GEOM,FLUIDE,AKTUEL,STORE,FLAGS);
        Client.DEQDeriv(X+.5*HK,YHILF,NN,K2);
        for(i=0;i<NN;i++)
            YHILF[i]=YK[i]+.25*HK*(K1[i]+K2[i]);
        // DIFFGL(X+.5*HK,YHILF,NN,K3,GEOM,FLUIDE,AKTUEL,STORE,FLAGS);
        Client.DEQDeriv(X+.5*HK,YHILF,NN,K3);
        for(i=0;i<NN;i++)
            YHILF[i]=YK[i]+HK*(-K2[i]+2.0*K3[i]);
        // DIFFGL(X+HK,YHILF,NN,K4,GEOM,FLUIDE,AKTUEL,STORE,FLAGS);
        Client.DEQDeriv(X+HK,YHILF,NN,K4);
        for(i=0;i<NN;i++)
            YHILF[i]=YK[i]+HK/27.0*(7.0*K1[i]+10.0*K2[i]+K4[i]);
        // DIFFGL(X+2./3.*HK,YHILF,NN,K5,GEOM,FLUIDE,AKTUEL,STORE,FLAGS);
        Client.DEQDeriv(X+2./3.*HK,YHILF,NN,K5);
        for(i=0;i<NN;i++)
            YHILF[i]=YK[i]+.0016*HK*(28.*K1[i]-125.*K2[i]
                    +546.*K3[i]+54.*K4[i]-378.*K5[i]);
        // DIFFGL(X+.2*HK,YHILF,NN,K6,GEOM,FLUIDE,AKTUEL,STORE,FLAGS);
        Client.DEQDeriv(X+.2*HK,YHILF,NN,K6);
        for (int j = 0; j < YK.length ; j++)   {
        	if (Double.isNaN(YK[j])) {
        		System.out.println("In 45 After Check NaN");
        		return;
        	}
        	if (Double.isNaN(YT[j])) {
        		System.out.println("In 45 After Check NaN");
        		return;
        	}
        }
        for(i=0;i<NN;i++) {
            Y[i]=YK[i]+HK/6.*(K1[i]+4.*K3[i]+K4[i]);
            YT[i]=YK[i]+HK/336.*(14.*K1[i]+35.*K4[i]+162.*K5[i]+125.*K6[i]);
        }
        for (int j = 0; j < YK.length ; j++)   {
        	if (Double.isNaN(YK[j])) {
        		System.out.println("In 45 After 2 Check NaN");
        		return;
        	}
        	if (Double.isNaN(YT[j])) {
        		System.out.println("In YT 45 After 2 Check NaN");
        		return;
        	}
        }
    }
}
