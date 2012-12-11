/*
 * Packet.java
 *
 * Created on 10. December 2008, 18:32
 *
 *  This is the Packet interface defining a subset which each implementation
 *  must support.
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

import java.nio.ByteBuffer;

/**
 * This is the Packet interface defining a subset which each implementation must
 * support.
 *
 * @author OSK-J Team
 * @version 1.0
 * @since 2.5.1
 */
public interface Packet {
    /** Needed for testing if the packet is actually a telemetry (TM) packet. */
    boolean isTMPacket();
    /** Needed for testing if the packet is actually a telecommand (TC) packet.
     */
    boolean isTCPacket();
    /** Returns a ByteBuffer which contains the whole user data. The header and
       footer of the packet are omitted. */
    ByteBuffer getData();
    /** Returns a ByteBuffer which contains the whole packet data. The header
       and footer of the packet are included. */
    ByteBuffer getRaw();
    /** Returns the size of the user data, e.g. PACKET_SIZE - HEADER_SIZE
       - FOOTER_SIZE. */
    int getDataSize();
    /** Returns the size of the complete packet. */
    int getRawSize();
    /** Returns a ByteBuffer which contains the packet header. The data
       and footer of the packet are omitted. */
    ByteBuffer getHeader();
    /** Returns a ByteBuffer which contains the packet footer. The header
       and data of the packet are omitted. */
    ByteBuffer getFooter();
}
