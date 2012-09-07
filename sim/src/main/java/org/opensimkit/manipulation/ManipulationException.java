/*
 * ManipulationException.java
 *
 * Created on 14. August 2009
 *
 * A special exception related to the OSK-J manipulation framework.
 *
 *-----------------------------------------------------------------------------
 * Modification History:
 *
 *  2009-08
 *      File created - A. Brandt:
 *      Initial version to help keeping the complexity of the simulator models
 *      low.
 *
 *-----------------------------------------------------------------------------
 *
 *      File under GPL - see OpenSimKit Documentation.
 *
 *      No warranty and liability for correctness by author.
 *
 */
package org.opensimkit.manipulation;

/**
 * A special exception related to the OSK-J manipulation framework.
 *
 * @author A. Brandt
 * @version 1.0
 * @since 2.8.0
 */
public class ManipulationException extends RuntimeException {
    private static final long serialVersionUID = 1;

    /**
     * Creates a new instance of <code>InjectionException</code> without detail
     * message.
     */
    public ManipulationException() {
    }


    /**
     * Constructs an instance of <code>InjectionException</code> with the
     * specified detail message.
     * @param msg the detail message.
     */
    public ManipulationException(final String msg) {
        super(msg);
    }
}
