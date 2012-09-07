/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.opensimkit.console;

import java.nio.ByteBuffer;
import org.opensimkit.packet.Packet;
import org.opensimkit.packet.PacketDecoder;

/**
 *
 * @author OSK-J Team
 */
public final class OSKPacketDecoder2 implements PacketDecoder {
    private final ConsoleGUI frame;

    public OSKPacketDecoder2(final ConsoleGUI frame) {
        this.frame = frame;
    }

    public void decode(Packet packet) {
        ByteBuffer data = packet.getData();

        /* Here the decoding of the packet takes places. Currently it is
           mandatory to know were the data is located. This means that the
           user needs to look at the org/opensimkit/PackaetCreator.java file
           to know what data is written where. */
        frame.writeLog("Decoder2: c0-mass: " + data.getDouble(0) + "\r\n");

    }

}
