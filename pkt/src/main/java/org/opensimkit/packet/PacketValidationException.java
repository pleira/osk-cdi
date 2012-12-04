/*
 * PacketValidationException.java
 *
 * Created on 10. December 2008, 18:32
 *
 *  This exception is used for the validation process of the packets.
 *
 *-----------------------------------------------------------------------------
 * Modification History:
 *
 *  2008-12-10
 *      File created - OSK-J Team:
 *      Initial version.
 *
 *      File under GPL - see OpenSimKit Documentation.
 *
 *      No warranty and liability for correctness by author.
 *
 */
package org.osk.packet;

/**
 * This exception is used for the validation process of the packets.
 *
 * @author OSK-J Team
 * @version 1.0
 * @since 2.5.1
 */
public class PacketValidationException extends RuntimeException {

    /**
     * Creates a new instance of <code>PacketValidationException</code> without
     * detail message.
     */
    public PacketValidationException() {
    }


    /**
     * Constructs an instance of <code>OSKPacketValidationException</code> with
     * the specified detail message.
     * @param msg the detail message.
     */
    public PacketValidationException(final String msg) {
        super(msg);
    }
}
