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
public final class TestPacketDecoder implements PacketDecoder {
    private final ConsoleGUI frame;

    public TestPacketDecoder(final ConsoleGUI frame) {
        this.frame = frame;
    }

    public void decode(Packet packet) {
        ByteBuffer data = packet.getData();

        /* Here the decoding of the packet takes places. Currently it is
           mandatory to know were the data is located. This means that the
           user needs to look at the org/opensimkit/PackaetCreator.java file
           to know what data is written where. */
        frame.writeLog("c0-ptotal: " + data.getDouble(0) + "\r\n");
        frame.writeLog("c8-tout: " + data.getDouble(4) + "\r\n");
        frame.writeLog("c12-tout: " + data.getDouble(8) + "\r\n");
        frame.writeLog("c15-tout: " + data.getDouble(12) + "\r\n");
        frame.writeLog("c17-tin0: " + data.getDouble(16) + "\r\n");
        frame.writeLog("c17-tin1: " + data.getDouble(20) + "\r\n");
        frame.writeLog("c17-poxt: " + data.getDouble(24) + "\r\n");
        frame.writeLog("c17-tGOxT: " + data.getDouble(28) + "\r\n");
        frame.writeLog("c17-tLOxT: " + data.getDouble(32) + "\r\n");
        frame.writeLog("c17-PFuT: " + data.getDouble(36) + "\r\n");
        frame.writeLog("c17-tGFuT: " + data.getDouble(40) + "\r\n");
        frame.writeLog("c17-tLFuT: " + data.getDouble(44) + "\r\n");
    }

}
