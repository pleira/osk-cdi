/*
 * SimHeaders.java
 *
 * Created on 3. Juli 2007, 22:40
 *
 *  Header file with definition of a min. set of global
 *  control variables & references used in OpenSim models.
 *
 *-----------------------------------------------------------------------------
 *
 *  2004-12-05
 *      File created  J. Eickhoff:
 *
 *      File is a derivative from ObjectSim 2.0.3.,
 *      a simulation program published in:
 *
 *        Eickhoff, J.:
 *        Modulare Programmarchitektur fuer ein wissensbasiertes
 *        Simulationssystem mit erweiterter Anwendbarkeit in der
 *        Entwicklung und Betriebsberwachung verfahrenstechnischer
 *        Anlagen.
 *        PhD thesis in Department Process Engineering of
 *        TU Hamburg-Harburg, 1996.
 *
 *      See also file history cited there and see historic relation of
 *      this OpenSimKit class to a.m. ObjectSim explained in
 *      OpenSimKit Documentation.
 *
 *      File under GPL  see OpenSimKit Documentation.
 *
 *      No warranty and liability for correctness by author.
 *
 *  2006-03
 *      OpenSimKit V 2.3
 *      Modifications entered for cleaner handling of globals.
 *      Modifications enterd for I/O file handling via cmd line arguments.
 *      J. Eickhoff
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

import java.io.FileWriter;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

/**
 * Header file with definition of a min. set of global control variables &
 * references used in OpenSim models.
 *
 * @author J. Eickhoff
 * @author A. Brandt
 * @author T. Pieper
 * @version 1.1
 * @since 2.4.0
 */
public final class SimHeaders {

    /* ---------- Flags ----------------*/
    /** Negative acknowledge flag - error. */
    public static int negativeAckFlag;

    /** if >0 comps write to logfile. */
    public static boolean newDebugFlag;

    /* -------- File Stuff -------------*/
    /* The log file all objects have    */
    /* access to during computation.    */
    /* Opened and closed by Main        */
    public static FileWriter logFile;

    /* String for Input File Name       */
    public static String myInFileName;

    /* String for Output Table Filename  */
    public static String myOutFileName = "mysimulation.txt"; 

    /* ----- Precision Settings --------*/
    /* relative accuracy of computation */
    public static double epsrel;

    /* absolute accuracy of computation */
    public static double epsabs;

    /* Get the line seperator of the current OS. */
    public static final String NEWLINE = System.getProperty("line.separator");

    /* Marker for short debug messages, to be formatted as "% classname msg". */
    public static final Marker DEBUG_SHORT
            = MarkerFactory.getMarker("DBGSHORT");

    /** Creates a new instance of Globals. */
    private SimHeaders() {
    }
}
