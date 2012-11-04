/*
 * MaterialProperties.java
 *
 * Created on 8. Juli 2007, 21:32
 *
 *  Defining data structure for fluid properties used in OpenSim models.
 *
 *-----------------------------------------------------------------------------
 * Modification History:
 *
 *  2004-12-05
 *      File created - J. Eickhoff:
 *
 *      Struct architecture is a derivative from ObjectSim 2.0.3.,
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

package org.opensimkit;

/**
 * Defining data structure for fluid properties used in OpenSimKit models.
 *
 * @author J. Eickhoff
 * @author A. Brandt
 * @version 1.0
 * @since 2.4.0
 */
public final class MaterialProperties {
    /* Wärmeleitfähigkeit. */
    public double LAMBDA;
    /* Dynamische Viskosität. */
    public double ETA;
    /* Kinematische Viskosität. */
    public double NUE;
    /* Dichte. */
    public double DICHTE;
    /* Kompressibilitätsfaktor. */
    public double Z;

    /** Creates a new instance of MaterialProperties. */
    public MaterialProperties() {
    }
}
