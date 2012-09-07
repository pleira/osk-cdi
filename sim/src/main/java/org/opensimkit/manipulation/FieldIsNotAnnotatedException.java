/*
 * FieldIsNotAnnotatedException.java
 *
 * Created on 3. September 2008, 21:32
 *
 * A special exception denoting a field is not annotated.
 *
 *-----------------------------------------------------------------------------
 * Modification History:
 *
 *  2008-09-03
 *      File created - A. Brandt:
 *      Initial version to help keeping the complexity of the simulator models
 *      low.
 *
 *      File under GPL - see OpenSimKit Documentation.
 *
 *      No warranty and liability for correctness by author.
 *
 */
package org.opensimkit.manipulation;

/**
 * A special exception denoting a field is not annotated.
 *
 * @author A. Brandt
 * @version 1.0
 * @since 2.4.4
 */
public class FieldIsNotAnnotatedException extends RuntimeException {
    private static final long serialVersionUID = 1;

    /**
     * Creates a new instance of <code>FieldIsNotAnnotatedException</code>
     * without detail message.
     */
    public FieldIsNotAnnotatedException() {
        super();
    }


    /**
     * Constructs an instance of <code>FieldIsNotAnnotatedException</code>
     * with the specified detail message.
     * @param msg the detail message.
     */
    public FieldIsNotAnnotatedException(final String msg) {
        super(msg);
    }
}