/*
 * ClassIsNotAModelException.java
 *
 * Created on 17. Juli 2008, 21:32
 *
 * A special exception denoting a class is not implementing
 * org.opensimkit.Model.
 *
 *-----------------------------------------------------------------------------
 * Modification History:
 *
 *  2008-07-17
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
 * A special exception denoting a class is not implementing
 * org.opensimkit.Model.
 *
 * @author A. Brandt
 * @version 1.0
 * @since 2.4.4
 */
public class ClassIsNotAModelException extends RuntimeException {
    private static final long serialVersionUID = 1;

    /**
     * Creates a new instance of <code>ClassIsNotAModelException</code>
     * without detail message.
     */
    public ClassIsNotAModelException() {
        super();
    }


    /**
     * Constructs an instance of <code>ClassIsNotAModelException</code> with
     * the specified detail message.
     * @param msg the detail message.
     */
    public ClassIsNotAModelException(final String msg) {
        super(msg);
    }
}
