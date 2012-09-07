package org.opensimkit.console;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.InterruptedIOException;

/**
 * This is the console viewer thread reading command echos from the simulator.
 *
 * @author J. Eickhoff
 */
public class ViewerThread extends Thread {
    private BufferedReader inputBufferReader;
    private String commandReply;
    private InputStream in;
    private ConsoleGUI frame;
    private boolean stoprequested;

    public ViewerThread(InputStream in, ConsoleGUI frame) {
        super("Viewer Thread");
        this.in = in;
        this.frame = frame;
        stoprequested = false;
    }

    public synchronized void requestStop() {
        stoprequested = true;
    }

    @Override
    public void run() {
        try {
            inputBufferReader = new BufferedReader(new InputStreamReader(in));
            while (!stoprequested) {
                try {
                    commandReply = inputBufferReader.readLine();
                    if (commandReply == null) {
                        System.out.println("Console lost simulator output "
                                + "connection...terminating...");
                        stoprequested = true;
                    } else {
                        System.out.println("commandReply:" + commandReply
                                + "#");
                        frame.writeEcho(commandReply + "\r\n");
                    }
                } catch (InterruptedIOException e) {
                    //nochmal versuchen
                }
            }
        } catch (IOException e) {
            System.err.println("ViewerThread: " + e.toString());
        }
        //System.out.println("ViewerThread: terminating");
    }
}
