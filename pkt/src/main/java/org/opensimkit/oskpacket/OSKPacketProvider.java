/*
 * OSKPacketProvider.java
 *
 * Created on 10. December 2008, 18:32
 *
 *  The OSK Packet Provider class implements the
 *  {@link org.opensimkit.packet.PacketProvider} interface.
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
package org.opensimkit.oskpacket;

import org.opensimkit.packet.PacketProvider;

/**
 *  The OSK Packet Provider class implements the
 *  {@link org.opensimkit.packet.PacketProvider} interface.
 * @author OSK-J Team
 * @version 1.0
 * @since 2.5.1
 */
public final class OSKPacketProvider implements PacketProvider {
    private final byte type;
    private final byte applicationID;
    private short sequenceCount;
    private long simulatedTime;
    private long systemTime;
// hier kommt eine Controller-Klasse hin, die den Status der versendeten Packete
// verwaltet.

    private OSKPacketProvider(final byte type, final byte applicationID,
            final short sequenceCount) {

        this.applicationID = applicationID;
        this.sequenceCount = sequenceCount;
        this.type = type;
    }

    public static OSKPacketProvider createTMProvider(final byte applicationID,
            final byte sequenceCount) {

        return new OSKPacketProvider(OSKPacket.TMPACKET, applicationID,
                sequenceCount);
    }

    public static OSKPacketProvider createTMProvider(final byte applicationID) {

        return new OSKPacketProvider(OSKPacket.TCPACKET, applicationID,
                (byte) 0);
    }

    public OSKPacket sendNext(final byte[] data,
            final long simulatedTime, final long systemTime) {

        /* Increase the sequence count. */
        sequenceCount++;
        this.simulatedTime = simulatedTime;
        this.systemTime = systemTime;

        return OSKPacket.createRawPacket(type, applicationID, sequenceCount,
                simulatedTime, systemTime, data);
    }

    public byte getType() {
        return type;
    }

    public byte getApplicationID() {
        return applicationID;
    }

    public short getSequenceCount() {
        return sequenceCount;
    }

    public long getLastSimulatedTime() {
        return simulatedTime;
    }

    public long getLastSystemTime() {
        return systemTime;
    }

    public PacketProvider createTMProvider() {
        return new OSKPacketProvider(OSKPacket.TMPACKET, applicationID,
                sequenceCount);
    }

    public PacketProvider createTCProvider() {
        return new OSKPacketProvider(OSKPacket.TCPACKET, applicationID,
                sequenceCount);
    }
}
