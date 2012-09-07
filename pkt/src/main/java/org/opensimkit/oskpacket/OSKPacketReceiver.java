/*
 * OSKPacketReceiver.java
 *
 * Created on 10. December 2008, 18:32
 *
 * This class is meant to receive all OSK packets and to forward them to their
 * corresponding Packet Decoders.
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

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.opensimkit.packet.PacketDecoder;

/**
 * This class is meant to receive all OSK packets and to forward them to their
 * corresponding Packet Decoders.
 *
 * @author OSK-J Team
 * @version 1.0
 * @since 2.5.1
 */
public class OSKPacketReceiver {
    private Map<Byte, PacketDecoder> decoders
            = new HashMap<Byte, PacketDecoder>();

    public void decode(final InputStream inStream) {
        byte applicationID;
        PacketDecoder packetDecoder;
        OSKPacket packet = null;
        try {
            int length = inStream.available();
            //System.out.print(length + " ");
            //System.out.println(OSKPacket.MAX_PACKET_SIZE);
            byte[] buffer = ByteBuffer.allocate(OSKPacket.MAX_PACKET_SIZE).array();

            if (length > 0) {
                inStream.read(buffer);

                if (buffer.length < OSKPacket.MIN_PACKET_SIZE) {

                } else if (buffer.length > OSKPacket.MAX_PACKET_SIZE) {

                } else {
                    packet = OSKPacket.wrap(buffer);
                }
            }
        } catch (IOException ex) {
            Logger.getLogger(OSKPacketReceiver.class.getName()).log(
                    Level.SEVERE, null, ex);
        }

        if (packet != null) {
            applicationID = packet.getApplicationID();
            //System.out.println(applicationID);
            if (decoders.containsKey(applicationID) == true) {
                packetDecoder = decoders.get(applicationID);
                 packetDecoder.decode(packet);
            }
        }
    }

    public void decode(final ReadableByteChannel channel) {
        OSKPacket packet;
        PacketDecoder packetDecoder;
        short length;
        byte applicationID;
        int state = 0;

        try {
            while (state != -1) {
                ByteBuffer buffer
                        = ByteBuffer.allocate(OSKPacket.MAX_PACKET_SIZE);
                state = channel.read(buffer);
                length = buffer.getShort(OSKPacket.OFFSET_LENGTH);
                if (length < OSKPacket.MIN_PACKET_SIZE) {

                } else if (length > OSKPacket.MAX_PACKET_SIZE) {

                } else {
                    packet = OSKPacket.wrap(buffer);
                    if (packet != null) {
                        applicationID = packet.getApplicationID();
                        if (decoders.containsKey(applicationID) == true) {
                            packetDecoder = decoders.get(applicationID);
                           packetDecoder.decode(packet);
                        }
                    }
                }
            }
        } catch (IOException ex) {
            Logger.getLogger(OSKPacketReceiver.class.getName()).log(
                    Level.SEVERE, null, ex);
        }
    }

    public void addDecoder(final byte applicationID,
            final PacketDecoder packetDecoder) {

        if (decoders.containsKey(applicationID) == true) {
            throw new RuntimeException("ApplicationID (" + applicationID
                    + ") already bound to a decoder!");
        } else {
            decoders.put(applicationID, packetDecoder);
        }
    }

    public void removeDecoder(final byte applicationID) {
        if (decoders.containsKey(applicationID) == true) {
            decoders.remove(applicationID);
        } else {
            throw new RuntimeException("ApplicationID (" + applicationID
                    + ") is not bound to a decoder!");
        }
    }
}
