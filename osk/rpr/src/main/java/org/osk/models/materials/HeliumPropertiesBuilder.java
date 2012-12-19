/*
 * Helium.java
 *
 * Created on 8. Juli 2007, 15:53
 *
 *  Computation of the thermodynamic properties of helium.
 *
 *  Model physics for rocket tank pressurization systems are taken from:
 *
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

package org.osk.models.materials;

/**
 * Computation of the thermodynamic properties of helium.
 *
 * @author J. Eickhoff
 * @author A. Brandt
 */
public final class HeliumPropertiesBuilder {
    private static double CV = 3146.5;
    private static double RSPEZ= 2077;
    private static double XI= 671741.8271;
    private static double ROKR= 69.45;
    private static double LR= 1.38;
    private static double GAMMA= 323.4165875;


    public static MaterialProperties build(double PK, double TLAUF) {
    // FIXME  PK  has also to be returned!
    	MaterialProperties MPHelium = new MaterialProperties();
        /**********************************************************************/
        /*                                                                    */
        /*    Berechnet die Stoffdaten von He fuer gegebene Temperatur und    */
        /*    gegebenen Druck.                                                */
        /*                                                                    */
        /*    Berechnet werden : Dynamische Viskositaet           ETA         */
        /*                       Kinematische Viskositaet         NUE         */
        /*                       Kompressibilitaetsfaktor         Z           */
        /*                       Waermeleitfaehigkeit             LAMBDA      */
        /*                                                                    */
        /*                                                                    */
        /*    Programm wird aufgerufen von: Hauptprogramm  SIMU               */
        /*                                  Subroutine     HETANK             */
        /*                                                 LEITUN             */
        /*                                                 HEFIL              */
        /*                                                 DREG               */
        /*                                                                    */
        /*    Vom Programm selbst aufgerufene Unterroutinen:                  */
        /*                                                                    */
        /*    Direkt im Aufruf uebergebene Daten:                             */
        /*                       Druck des Heliums         PK                 */
        /*                       Temperatur d. Heliums     TLAUF              */
        /*                       Dynamische Viskositaet    ETA                */
        /*                       Kinematische Viskositaet  NUE                */
        /*                       Kompressibilitaetsfaktor  Z                  */
        /*                       Dichte                    DICHTE             */
        /*                       Waermeleitfaehigkeit      LAMBDA             */
        /*                                                                    */
        /*    Uebergebene Datenbloecke (COMMON) :          keine              */
        /*                                                                    */
        /*                                                                    */
        /*                                                                    */
        /**********************************************************************/
        double NXIRO, NXIR, LAMBDO;
       //double CP = 0, CV = 0, RALLG = 0, RSPEZ = 0, XI = 0, LR = 0, GAMMA = 0;
        double A,B,C,D,E,F,TRED,PRED,FQ,FQO,ETAO,FAKTOR,RORED = 0;

        PK=PK/1E5;

        /**********************************************************************/
        /*                                                                    */
        /*    Berechnung der dynamischen Viskositaet Eta-Null bei p=1 bar     */
        /*                                                                    */
        /**********************************************************************/

        PRED=PK/2.27;
        TRED=TLAUF/5.19;
        NXIRO=(.807*Math.pow(TRED,.618))-(.357*(Math.exp(-.449*TRED)));
        NXIRO=NXIRO+.018;
        FQO=1.22*Math.pow(LR,.15);
        FQO=FQO*(1+(.00385*Math.pow(Math.pow((TRED-12),2),.2498126))
                * sign(1.,TRED-12.));
        ETAO=NXIRO*FQO/XI;

        /**********************************************************************/
        /*                                                                    */
        /*    Berechnung der dynamischen Viskositaet ETA bei Druck p          */
        /*                                                                    */
        /******************** *************************************************/

        A=(.001245*(Math.exp(5.1726*Math.pow(TRED,-.3286))))/TRED;
        B=A*(1.6553*TRED-1.2723);
        C=.4489/TRED;
        D=(1.7368*(Math.exp(2.231*Math.pow(TRED,-7.6351))))/TRED;
        E=1.3088;
        F=.9425*(Math.exp(-.1853*Math.pow(TRED,.4489)));

        NXIR=NXIRO*(1+(A*Math.pow(PRED,E))/(B*Math.pow(PRED,F)
                + Math.pow((1+ (C*Math.pow(PRED,D))),-1)));
        FQ=1+(FQO-1)*(Math.pow((NXIR/NXIRO),-1)
                - .007*Math.pow((Math.log(NXIR/NXIRO)),4));
        MPHelium.ETA=NXIR*FQ/XI;

        /**********************************************************************/
        /*                                                                    */
        /*    Berechnung des Kompressibilitaetsfaktors  Z                     */
        /*                                                                    */
        /**********************************************************************/

        FAKTOR=(1.913688E-3)-(8.520942E-6)*(TLAUF)+1.358845E-8
                * Math.pow(TLAUF,2);
        FAKTOR=FAKTOR-(4.595341E-12)*Math.pow(TLAUF,3);
        MPHelium.Z=1.0+FAKTOR* (PK);

        /**********************************************************************/
        /*                                                                    */
        /*    Berechnung der Dichte                                           */
        /*                                                                    */
        /**********************************************************************/

        MPHelium.DENSITY=(PK*1E5)/(RSPEZ* (TLAUF)*MPHelium.Z);

        /**********************************************************************/
        /*                                                                    */
        /*    Berechnung der Waermeleitfaehigkeit Lambda-null bei p=1 bar     */
        /*                                                                    */
        /**********************************************************************/

        LAMBDO=ETAO*2.5*CV*.99;

        /**********************************************************************/
        /*                                                                    */
        /*    Berechnung der Waermeleitfaehigkeit Lambda bei Druck=p          */
        /*                                                                    */
        /**********************************************************************/

        RORED=MPHelium.DENSITY/ROKR;
        MPHelium.LAMBDA=LAMBDO
                + (.121*(Math.exp(.15*RORED)-1))/(GAMMA*Math.pow(.301,5));

        /**********************************************************************/
        /*                                                                    */
        /*    Kinematische Viskositaet NUE                                    */
        /*                                                                    */
        /**********************************************************************/

        MPHelium.NUE=MPHelium.ETA/MPHelium.DENSITY;
        PK=PK*1E5;

        return MPHelium;
    }

    static double sign(final double X1, final double X2) {
        if(X2 >=0) {
            return(X1);
        } else {
            return(-X1);
        }
    }
    
}
