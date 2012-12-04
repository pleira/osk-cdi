/*
 * OSKPacket.java
 *
 * Created on 10. December 2008, 18:32
 *
 *  The OSK Packet class implements the {@link org.osk.Packet} interface.
 *  This class is immutable.
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
package org.osk.oskpacket;

import java.nio.ByteBuffer;
import java.util.Formatter;
import org.osk.packet.Packet;
import org.osk.packet.PacketValidationException;

/**
 * The OSK Packet class implements the {@link org.osk.packet} interface.
 * This class is immutable.
 *
 * <table border="1">
 *   <tr>
 *     <th>Offset</th>
 *     <th>Size</th>
 *     <th>Name</th>
 *     <th>Remarks</th>
 *   </tr>
 *   <tr>
 *     <td>0</td>
 *     <td>1 byte</td>
 *     <td>Packet type</td>
 *     <td>Identifies a packet as telemetry or telecommand packet.</td>
 *   </tr>
 *   <tr>
 *     <td>1</td>
 *     <td>1 byte</td>
 *     <td>Application ID</td>
 *     <td>Used to address different applications. 256 different applications
 *         can be addressed.</td>
 *   </tr>
 *   <tr>
 *     <td>2</td>
 *     <td>1 byte</td>
 *     <td>Sequence counter</td>
 *     <td>A sequence counter to have a facility to detect lost packets.
 *         Each application shall have its own sequence counter.
 *     </td>
 *   </tr>
 *   <tr>
 *     <td>4</td>
 *     <td>2 bytes</td>
 *     <td>Packet length</td>
 *     <td>The packet length including header and footer (checksum). It can be
 *         equal to MAX_PACKET_SIZE at most.
 *     </td>
 *   </tr>
 *   <tr>
 *     <td>6</td>
 *     <td>8 bytes</td>
 *     <td>Simulated time</td>
 *     <td>The time of the simulation. This is in the default Java time format,
 *         which is Unix time with millisecond resolution.
 *     </td>
 *   </tr>
 *   <tr>
 *     <td>14</td>
 *     <td>8 bytes</td>
 *     <td>System time</td>
 *     <td>The time of the system. Normally this is local time. This is in the
 *         default Java time format, which is Unix time with millisecond
 *         resolution.
 *     </td>
 *   </tr>
 *   <tr>
 *     <td>22</td>
 *     <td>n bytes</td>
 *     <td>Data</td>
 *     <td>The data size ranges from 0 to a maximum of 2027 bytes.</td>
 *   </tr>
 *   <tr>
 *     <td>22 + n</td>
 *     <td>2 bytes</td>
 *     <td>Checksum</td>
 *     <td>The CRC16-CCITT checksum of the header and data. Calculated from
 *         position 0 to position (22 + n) - 2. In other words from the
 *         beginning of the packet to the end of the user data and thus omitting
 *         the footer.</td>
 *   </tr>
 * </table>
 *
 * @author OSK-J Team
 * @version 1.0
 * @since 2.5.1
 */
public final class OSKPacket implements Packet {
    /** Packet type denoting a TM packet. The current version 1 of the OSK
     * Packet specification specifies the TM packet type to be 0x0.*/
    public static final byte TMPACKET = 0;
    /** Packet type denoting a TC packet. The current version 1 of the OSK
     * Packet specification specifies the TC packet type to be 0x1.*/
    public static final byte TCPACKET = 1;
    /** The current version 1 of the OSK Packet
     * specification specifies the header size to 21 octets. */
    public static final int HEADER_SIZE = 22;
    /**The current version 1 of the OSK Packet
     * specification specifies the header size to 2 octets. */
    public static final int FOOTER_SIZE = 2;
    /** The current version 1 of the OSK Packet
     * specification limits the maximum packet size to 2048 octets. The packet
     * size includes the header, footer, and data size. */
    public static final int MAX_PACKET_SIZE = 2048;
   /** The minimum packet size is calculated by following formula:<br/>
     * MIN_PACKET_SIZE = HEADER_SIZE + FOOTER_SIZE */
    public static final int MIN_PACKET_SIZE = HEADER_SIZE + FOOTER_SIZE;
    /** The maximum data size is calculated by following formula:<br/>
     * MAX_DATA_SIZE = MAX_PACKET_SIZE - HEADER_SIZE - FOOTER_SIZE */
    public static final int MAX_DATA_SIZE
            = MAX_PACKET_SIZE - HEADER_SIZE - FOOTER_SIZE;
    /** The current version 1 of the OSK Packet
     * specification limits the minimum data size to 0 octets. */
    public static final int MIN_DATA_SIZE = 0;
    /** The offset of the packet type is 0. */
    private static final int OFFSET_PACKETTYPE = 0;
    /** The offset of the application ID is 1. */
    private static final int OFFSET_APPLICATIOND = 1;
    /** The offset of the sequence count is 2. */
    private static final int OFFSET_SEQUNCECOUNT = 2;
    /** The offset of the length is 4. */
    public static final int OFFSET_LENGTH = 4;
    /** The offset of the simulated time is 6. */
    private static final int OFFSET_SIMULATEDTIME = 6;
    /** The offset of the system time is 14. */
    private static final int OFFSET_SYSTEMTIME = 14;
    /** The offset of the data is 22. */
    private static final int OFFSET_DATA = 22;
    /** In this buffer all the data is stored. */
    private ByteBuffer rawData;
    /** Length of the data. */
    private final int dataLength;

    private OSKPacket(final byte packetType, final byte applicationID,
            final short sequenceCount, final long simulatedTime,
            final long systemTime, final byte[] data) {

        /* Allocate the buffer. */
        rawData = ByteBuffer.allocate(MAX_PACKET_SIZE);

        if (data.length > MAX_DATA_SIZE) {
            throw new PacketValidationException("Data size (" + data.length
                    + ") exceeds maximum packet data size (" + MAX_DATA_SIZE
                    + "!");
        }
        /* Calculate length from incoming data. */
        short length = (short) (HEADER_SIZE + data.length + FOOTER_SIZE);

        /* Set dataLength for use with getData(). */
        dataLength = data.length;

        rawData.put(OFFSET_PACKETTYPE, packetType);
        rawData.put(OFFSET_APPLICATIOND, applicationID);
        rawData.putShort(OFFSET_SEQUNCECOUNT, sequenceCount);
        rawData.putShort(OFFSET_LENGTH, length);
        rawData.putLong(OFFSET_SIMULATEDTIME, simulatedTime);
        rawData.putLong(OFFSET_SYSTEMTIME, systemTime);

        for (int i = 0; i < data.length; i++) {
            rawData.put(OFFSET_DATA + i, data[i]);
        }

         /* Calculate checksum from incoming data. */
        short checksum = CRC16CCITT.getValue(rawData.array(), 0,
                HEADER_SIZE + data.length);
        rawData.putShort(HEADER_SIZE + data.length, checksum);
    }

    private OSKPacket(final byte[] raw) {
        /* Allocate the buffer. */
        rawData = ByteBuffer.allocate(MAX_PACKET_SIZE);

        int length = raw.length;

        /* The basic validation needs to be done before the ByteBuffer is
           populated. */
        basicValidation(length);

        /* Copy the incoming buffer into the internal one. */
        for (int i = 0; i < length; i++) {
            rawData.put(i, raw[i]);
        }

        /* Set the dataLength. */
        dataLength = getLength() - HEADER_SIZE - FOOTER_SIZE;

        /* Now the complete Validation is possible. */
        completeValidation(length);
    }

    private OSKPacket(final ByteBuffer raw) {
        int length = raw.capacity();

        /* The basic validation needs to be done before the ByteBuffer is
           assigned. */
        basicValidation(length);

        /* Assign the incoming buffer to the internal one. */
        rawData = raw;

        /* Set the dataLength. */
        dataLength = getLength() - HEADER_SIZE - FOOTER_SIZE;

        /* Now the complete Validation is possible. */
        completeValidation(length);
    }

    /**
     * Factory method creating an OSK TM packet.
     *
     * @param applicationID     the application ID of this packet.
     * @param sequenceCount     the sequence count.
     * @param simulatedTime     the time inside the simulation.
     * @param systemTime        the time of the system running the simulation.
     * @param data              the user data.
     * @return                  the OSK TM packet filled with the above data.
     */
    public static OSKPacket createTMPacket(final byte applicationID,
            final short sequenceCount, final long simulatedTime,
            final long systemTime, final byte[] data) {

        return new OSKPacket(TMPACKET, applicationID, sequenceCount,
                simulatedTime, systemTime, data);
    }

    /**
     * Factory method creating an OSK TC packet.
     *
     * @param applicationID     the application ID of this packet.
     * @param sequenceCount     the sequence count.
     * @param simulatedTime     the time inside the simulation.
     * @param systemTime        the time of the system running the simulation.
     * @param data              the user data.
     * @return                  the OSK TC packet filled with the above data.
     */
    public static OSKPacket createTCPacket(final byte applicationID,
            final short sequenceCount, final long simulatedTime,
            final long systemTime, final byte[] data) {

        return new OSKPacket(TCPACKET, applicationID, sequenceCount,
                simulatedTime, systemTime, data);
    }

    /**
     * Factory method creating a raw OSK packet. The type can be set freely.
     * Use this method with care.
     *
     * @param type              the type of this packet.
     * @param applicationID     the application ID of this packet.
     * @param sequenceCount     the sequence count.
     * @param simulatedTime     the time inside the simulation.
     * @param systemTime        the time of the system running the simulation.
     * @param data              the user data.
     * @return                  the raw OSK packet filled with the above data.
     */
    public static OSKPacket createRawPacket(final byte type,
            final byte applicationID, final short sequenceCount,
            final long simulatedTime, final long systemTime,
            final byte[] data) {

        return new OSKPacket(type, applicationID, sequenceCount, simulatedTime,
                systemTime, data);
    }

    /**
     * Creates an OSK packet from a byte array. Several checks are performed to
     * ensure that the data inside the byte array is really a correct OSK
     * packet.
     * @param raw   the byte array containing an OSK packet.
     * @return      the OSK packet created from the byte array.
     */
    public static OSKPacket wrap(final byte[] raw) {
        return new OSKPacket(raw);
    }

    /**
     * Creates an OSK packet from a ByteBuffer. Several checks are performed to
     * ensure that the data inside the ByteBuffer is really a correct OSK
     * packet.
     * @param raw   the ByteBuffer containing an OSK packet.
     * @return      the OSK packet created from the ByteBuffer.
     */
    public static OSKPacket wrap(final ByteBuffer raw) {
        return new OSKPacket(raw);
    }

    /**
     * Performs basic validation (complete packet length) for the creation of
     * OSK packets from their binary representation.
     * @param inputLength    The complete length of the packet.
     */
    private void basicValidation(final int inputLength) {
        if (inputLength > MAX_PACKET_SIZE) {
            throw new PacketValidationException("Input data (" + inputLength
                    + ") is bigger than maximum packet size ("
                    + MAX_PACKET_SIZE + ").");
        } else if (inputLength < MIN_PACKET_SIZE) {
            throw new PacketValidationException("Input data (" + inputLength
                    + ") is smaller than minimum packet size ("
                    + MIN_PACKET_SIZE + ").");
        }
    }

    /**
     * Performs advanced validation (checksum, data length and packet type)
     * for the creation of OSK packets from their binary representation.
     * @param inputLength    The complete length of the packet.
     */
    private void completeValidation(final int inputLength) {
        /* Validate checksum. */
        short checksumRead = getChecksum();
        short checksumCalculated = CRC16CCITT.getValue(rawData.array(), 0,
                inputLength - FOOTER_SIZE);
        if (checksumRead != checksumCalculated) {
            throw new PacketValidationException("Checksum error! "
                    + "CRC read: " + checksumRead + " CRC calculated: "
                    + checksumCalculated + " dataLength: " + dataLength
                    + " inputLength:"
                    + (inputLength - HEADER_SIZE - FOOTER_SIZE));
        }

        /* Validate data length. */
        int lengthCalculated = inputLength - HEADER_SIZE - FOOTER_SIZE;
        if (dataLength > MAX_DATA_SIZE) {
            throw new PacketValidationException("Invalid data length ("
                    + dataLength + ") is bigger than maximum data size ("
                    + MAX_DATA_SIZE + ")!");
        } else if (dataLength < MIN_DATA_SIZE) {
            throw new PacketValidationException("Invalid data length ("
                    + dataLength + ") is smaller than minimum data size ("
                    + MIN_DATA_SIZE + ")!");
        } else if (dataLength != lengthCalculated) {
            throw new PacketValidationException("Invalid data length! "
                    + "The calculated length (" + lengthCalculated
                    + ") is different from the length written in the packet ("
                    + dataLength + ").");
        }

        /* Validate packet type. */
        byte packetType = getType();
        if ((packetType != TMPACKET) && (packetType != TCPACKET)) {
            throw new PacketValidationException("Invalid packet type ("
                    + packetType + ")! "
                    + "Packet is neither a TM packet nor a TC packet.");
        }
    }

//    public OSKPacket setPacketType(final byte packetType) {
//
//    }

    /**
     * Checks if this packet is a TM packet.
     * @return  True if this packet is a TM packet.
     */
    public boolean isTMPacket() {
        boolean result = false;

        if (getType() == TMPACKET) {
            result = true;
        }

        return result;
    }

    /**
     * Checks if this packet is a TC packet.
     * @return  True if this packet is a TC packet.
     */
    public boolean isTCPacket() {
        boolean result = false;

        if (getType() == TCPACKET) {
            result = true;
        }

        return result;
    }

    /**
     * Returns the type of the packet. It is different from the
     * isTMPacket/isTCPacket methods, as it returns the byte value of the type
     * field of the OSK packet.
     * @return  The raw value of the type field of the OSK packet.
     */
    public byte getType() {
        return rawData.get(OFFSET_PACKETTYPE);
    }

    /**
     * Returns the Application ID of the packet. The Application ID is used to
     * recognize 256 different TM as well as 256 different TC packets.
     * @return  The Application ID of the OSK packet.
     */
    public byte getApplicationID() {
        return rawData.get(OFFSET_APPLICATIOND);
    }

    /**
     * Returns the Sequence Count of the packet. The Sequence Count is used to
     * track the packets and to know when a packet is lost. Generally, the
     * Sequence Count will be incremented by one each packet (and each
     * Application ID). If a packet is lost the receiver will notice it by an
     * unexpected Sequence Count. It shall be computed seperatedly for each
     * Application ID.
     * @return  The Sequence Count of the OSK packet.
     */
    public short getSequenceCount() {
        return rawData.getShort(OFFSET_SEQUNCECOUNT);
    }

    /**
     * Returns the complete length (i.e. the sum of the HEADER_SIZE
     * + data length + FOOTER_SIZE) of the OSK packet.
     * @return The complete length of the OSK packet.
     */
    public int getLength() {
        return rawData.getShort(OFFSET_LENGTH);
    }

    /**
     * Returns the simulated time, e.g. the time inside the simulation.  The
     * time is encoded as an Java time, that is a long counting the milliseconds
     * from 1970-01-01T00:00:00Z.
     * @return  The time inside the simulation.
     */
    public long getSimulatedTime() {
        return rawData.getLong(OFFSET_SIMULATEDTIME);
    }

    /**
     * Returns the system time, e.g. the time of the computer runnining the
     * simulation. The time is encoded as an Java time, that is a long counting
     * the milliseconds from 1970-01-01T00:00:00Z.
     * @return  The time of the computer running the simulation.
     */
    public long getSystemTime() {
        return rawData.getLong(OFFSET_SYSTEMTIME);
    }

    /**
     * Returns the user data inside the OSK packet as a {@link ByteBuffer}.
     * @return  A ByteBuffer containing the user data of the OSK packet.
     */
    public ByteBuffer getData() {
//        byte[] rawArray = new byte[dataLength];
//
//        for (int i = 0; i <= dataLength - 1; i++) {
//            rawArray[i] = rawData.get(OFFSET_DATA + i);
//        }
//
//        ByteBuffer newBuffer
//                = ByteBuffer.wrap(rawArray, 0, dataLength);
//        return newBuffer;
        return getByteBuffer(OFFSET_DATA, dataLength);
    }

    /**
     * Returns the checksum of the OSK packet.
     * @return  The checksum of the OSK packet
     */
    public short getChecksum() {
        return rawData.getShort(HEADER_SIZE + dataLength);
    }

    /**
     * Returns the size of the user data of the OSK packet.
     * @return  The size of the user data of the OSK packet.
     */
    public int getDataSize() {
        return getLength();
    }

    /**
     * Returns a {@link java.lang.String} containing a hexadecimal
     * representation of the raw bytes of the packet.
     * @return  A hex string representation of the packet.
     */
    public String toHexString() {
        int size = HEADER_SIZE + dataLength + FOOTER_SIZE;
        StringBuilder sb = new StringBuilder(size);

        for (int i = 0; i < size; i++) {
            sb.append(Integer.toHexString(rawData.get(i)));
        }

        return sb.toString();
    }

    @Override
    public String toString() {
        int size = HEADER_SIZE + dataLength + FOOTER_SIZE;
        StringBuilder sb = new StringBuilder(size);
        Formatter formatter = new Formatter(sb);

        formatter.format("HEADER (%3d): ", HEADER_SIZE);
        for (int i = 0; i < HEADER_SIZE; i++) {
            sb.append(Integer.toHexString(rawData.get(i)));
        }
        sb.append("\n");

        formatter.format("  DATA (%3d): ", dataLength);
        for (int i = HEADER_SIZE; i < HEADER_SIZE + dataLength; i++) {
            sb.append(Integer.toHexString(rawData.get(i)));
        }
        sb.append("\n");

        formatter.format("FOOTER (%3d): ", FOOTER_SIZE);
        for (int i = HEADER_SIZE + dataLength;
             i < HEADER_SIZE + dataLength + FOOTER_SIZE; i++) {
            sb.append(Integer.toHexString(rawData.get(i)));
        }
        sb.append("\n");
        formatter.format("Type: %d, APID: %d, SC: %d, SimTime: %d, SysTime: %d, CRC: %s%n",
                getType(), getApplicationID(), getSequenceCount(),
                getSimulatedTime(), getSystemTime(),
                Integer.toHexString(getChecksum()));

        return sb.toString();
    }

    @Override
    public boolean equals(final Object object) {
        boolean result;

        if (object instanceof OSKPacket) {
            result = this.hashCode() == object.hashCode() ? true : false;
        } else {
            result = false;
        }

        return result;
    }

    @Override
    public int hashCode() {
        int hash = 7;

        hash = 37 * hash + getChecksum();

        return hash;
    }

    public byte[] array() {
        int size = HEADER_SIZE + dataLength + FOOTER_SIZE;
        byte[] rawArray = new byte[size];

        for (int i = 0; i < size; i++) {
            rawArray[i] = rawData.get(i);
        }

        return rawArray;
    }

    private ByteBuffer getByteBuffer(final int from, final int to) {
        int size = to - from;

        ByteBuffer buffer = ByteBuffer.allocate(size);

        int positionInNewBuffer = 0;
        for (int position = from; position < size; position++) {
            buffer.put(positionInNewBuffer, rawData.get(position));
            positionInNewBuffer++;
        }

        return buffer;
    }

    /**
     * Returns the complete OSK packet as a {@link ByteBuffer}.
     * @return  A ByteBuffer containing the complete OSK packet.
     */
    public ByteBuffer getRaw() {
        return getByteBuffer(0, HEADER_SIZE + dataLength + FOOTER_SIZE);
    }

   /**
     * Returns the complete size of the OSK packet.
     * @return  The complete size of the OSK packet.
     */
    public int getRawSize() {
        return HEADER_SIZE + dataLength + FOOTER_SIZE;
    }

    /**
     * Returns the header of the OSK packet as a {@link ByteBuffer}.
     * @return  A ByteBuffer containg the header of the OSK packet.
     */
    public ByteBuffer getHeader() {
        return getByteBuffer(0, HEADER_SIZE);
    }

    /**
     * Returns the footer of the OSK packet as a {@link java.nio.ByteBuffer}.
     * @return  A ByteBuffer containg the footer of the OSK packet.
     */
    public ByteBuffer getFooter() {
        return getByteBuffer(OFFSET_DATA + dataLength, FOOTER_SIZE);
    }

}
