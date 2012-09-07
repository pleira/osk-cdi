/*
 * FieldIsNotManipulatableException.java
 *
 * Created on 17. Juli 2008, 21:32
 *
 * A special exception denoting a field is not annotated by Manipulatable.
 *
 *-----------------------------------------------------------------------------
 * Modification History:
 *
 *  2008-07-17
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
 * A special exception denoting a field is not annotated by Manipulatable.
 *
 * @author A. Brandt
 * @version 1.0
 * @since 2.4.4
 */
public final class FieldIsNotManipulatableException
        extends FieldIsNotAnnotatedException {
    private static final long serialVersionUID = 2;

    /**
     * Creates a new instance of <code>FieldIsNotManipulatableException</code>
     * without detail message.
     */
    public FieldIsNotManipulatableException() {
        super();
    }


    /**
     * Constructs an instance of <code>FieldIsNotManipulatableException</code>
     * with the specified detail message.
     * @param msg the detail message.
     */
    public FieldIsNotManipulatableException(final String msg) {
        super(msg);
    }
}
