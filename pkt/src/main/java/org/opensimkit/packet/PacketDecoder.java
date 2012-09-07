/*
 * PacketDecoder.java
 *
 * Created on 10. December 2008, 18:32
 *
 *  This is the Packet Decoder interface which is used to process received
 *  packets.
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
package org.opensimkit.packet;

/**
 * This is the Packet Decoder interface which is used to process received
 * packets.
 *
 * @author OSK-J Team
 * @version 1.0
 * @since 2.5.1
 */
public interface PacketDecoder {
    /** This method consumes a packet and decodes it. */
    void decode(Packet packet);
}
