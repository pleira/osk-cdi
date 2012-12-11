/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.osk.console;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.channels.SocketChannel;
import org.osk.oskpacket.OSKPacketReceiver;
import org.osk.packet.PacketDecoder;

/**
 * This is the console logger thread reading simulator result data for display
 * from the simulator.
 *
 * @author J. Eickhoff
 */
public class LoggerThread extends Thread {
    private BufferedReader inputBufferReader;
    private String simData;
    private InputStream in;
    private ConsoleGUI frame;
    private boolean stoprequested;
    private final PacketDecoder packetDecoder1;
    private final OSKPacketReceiver packetReceiver = new OSKPacketReceiver();

    public LoggerThread(final SocketChannel channel, InputStream in,
            ConsoleGUI frame) {
        super("Logger Thread");
        this.in = in;
        this.frame = frame;
        packetDecoder1 = new TestPacketDecoder(frame);
        stoprequested = false;

        packetReceiver.addDecoder((byte) 0x00, packetDecoder1);
        packetReceiver.addDecoder((byte) 0x01, new OSKPacketDecoder2(frame));
    }

    public synchronized void requestStop() {
        stoprequested = true;
    }

    @Override
    public void run() {
//    try {
        inputBufferReader = new BufferedReader(new InputStreamReader(in));

        while (!stoprequested) {
            try {
    			Thread.sleep(20);
    		} catch (InterruptedException e) {
    			// TODO Auto-generated catch block
    			e.printStackTrace();
    		}
        	packetReceiver.decode(in);

//        try {
////          simData = inputBufferReader.readLine();
//          if(simData == null) {
//            System.out.println("Console lost simulator data stream connection...terminating...");
//            stoprequested = true;
//          }
//          else {
//            System.out.println("simulator result:" + simData +"#");
//            frame.writeLog(simData + "\r\n");
//          }
//        }
//        catch (Exception e) {
//          //nochmal versuchen
//        }
        }
//    } catch (IOException e) {
//      System.err.println("LoggerThread: " + e.toString());
//    }
        //System.out.println("LoggerThread: terminating");
    }
}
