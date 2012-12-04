/*
 * SimulatorState.java
 *
 * Created on 21. February 2009
 *
 * Enum representing the possible simulator states.
 *
 *-----------------------------------------------------------------------------
 * Modification History:
 *
 *  2009-02-21
 *      File created - A. Brandt:
 *      Initial version.
 *
 *      File under GPL - see OpenSimKit Documentation.
 *
 *      No warranty and liability for correctness by author.
 *
 */

package org.opensimkit;

/**
 * Enum representing the possible simulator states.
 *
 * @author A. Brandt
 * @version 1.0
 * @since 2.5.1
 */
public enum SimulatorState {
    RUNNING,
    PAUSED,
    NOT_RUNNING,
    STOPPING;
}
