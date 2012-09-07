/*
 * SimCmdThread.java
 *
 * Created on 17. Juli 2008, 21:32
 *
 * A thread to receive commands from the MMI.
 *
 *-----------------------------------------------------------------------------
 * Modification History:
 *
 *  2008-07-17
 *      File created - A. Brandt:
 *      Initial version to help keeping the complexity of the simulator models
 *      low.
 *
 *      File under GPL - see OpenSimKit Documentation.
 *
 *      No warranty and liability for correctness by author.
 *
 *
 *  2009-06
 *     Integrated logging support.
 *     T. Pieper
 *
 *
 *  2010-07-22
 *     Preliminary fix of bug leading to simulator crash when shutdown is 
 *     being commanded from console and console also shuts down.
 *     A. Brandt
 *
 *-----------------------------------------------------------------------------
 *
 *      File under GPL  see OpenSimKit Documentation.
 *
 *      No warranty and liability for correctness by authors.
 *
 *
 *-----------------------------------------------------------------------------
*/
package org.opensimkit;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.opensimkit.commanding.CallCommand;
import org.opensimkit.commanding.DisconnectCommand;
import org.opensimkit.commanding.GetACommand;
import org.opensimkit.commanding.GetCommand;
import org.opensimkit.commanding.HelpCommand;
import org.opensimkit.commanding.MMICommandHandler;
import org.opensimkit.commanding.PauseCommand;
import org.opensimkit.commanding.ResumeCommand;
import org.opensimkit.commanding.SetACommand;
import org.opensimkit.commanding.SetCommand;
import org.opensimkit.commanding.ShutdownCommand;
import org.opensimkit.commanding.StartCommand;
import org.opensimkit.commanding.StopCommand;

/**
 * A thread to receive commands from the MMI.
 *
 * @author J. Eickhoff
 * @author A. Brandt
 * @author T. Pieper
 * @version 1.2
 * @since 2.4.6
 */
public final class SimCmdThread extends Thread {
    private static final Logger LOG
            = LoggerFactory.getLogger(SimCmdThread.class);
    private final MMICommandHandler commandHandler;
    private final Kernel            kernel;
    private int    myShutFlag;
    private int    pos;
    private String commandMsg;
    private String msg;
    private String cmd;
    private String objName;
    private String attName;
    private String value;
    private ComputeThread  cThread;
    private BufferedReader inputBufferReader;
    private OutputStream   out;
    private Socket         socket;

    public SimCmdThread(final ComputeThread cmpThread, final Socket socket,
        final Kernel kernel) {
        super();

        this.setName("CommandThread");
        this.cThread = cmpThread;
        this.socket  = socket;
        myShutFlag   = 0;
        this.kernel  = kernel;

        commandHandler = new MMICommandHandler();
        commandHandler.registerCommand(new CallCommand(kernel));
        commandHandler.registerCommand(new DisconnectCommand());
        commandHandler.registerCommand(new GetCommand(kernel));
        commandHandler.registerCommand(new GetACommand(kernel));
        commandHandler.registerCommand(new HelpCommand(commandHandler, kernel));
        commandHandler.registerCommand(new PauseCommand(kernel));
        commandHandler.registerCommand(new ResumeCommand(kernel));
        commandHandler.registerCommand(new SetCommand(kernel));
        commandHandler.registerCommand(new SetACommand(kernel));
        commandHandler.registerCommand(new ShutdownCommand());
        commandHandler.registerCommand(new StartCommand(kernel));
        commandHandler.registerCommand(new StopCommand(kernel));
    }

    public int getstatus() {
        return myShutFlag;
    }

    @Override
    public void run() {
        LOG.info("SimCmdThread: Cmd/ctrl connection established...");
        try {
            InputStream in = socket.getInputStream();
            out = socket.getOutputStream();
            inputBufferReader = new BufferedReader(new InputStreamReader(in));
            while (true) {
                commandMsg = inputBufferReader.readLine();
                if (commandMsg == null) {
                    LOG.error(
                        "Simulator lost cmd/ctrl connection ...terminating...");
                    myShutFlag = 2;
                    cThread.terminate();
                    break;
                }
                parseCommandMessage(commandMsg);

                // Parsing Cmd
                pos = commandMsg.indexOf(' ');
                if (pos == -1) {
                    cmd = commandMsg;
                    commandMsg = ("");
                } else {
                    if (pos < commandMsg.length()) {
                        cmd = commandMsg.substring(0, pos);
                        commandMsg = commandMsg.substring(pos + 1);
                    }
                }
                LOG.debug("Cmd:{}", cmd);

                // Parsing Oject
                pos = commandMsg.indexOf(' ');
                if (pos == -1)  {
                    objName = commandMsg;
                    commandMsg = ("");
                } else {
                    if (pos < commandMsg.length()) {
                        objName = commandMsg.substring(0, pos);
                        commandMsg = commandMsg.substring(pos + 1);
                    }
                }
                LOG.debug("Object:{}", objName);

                // Parsing Attribute
                pos = commandMsg.indexOf(' ');
                if (pos == -1)  {
                    attName = commandMsg;
                    commandMsg = ("");
                } else {
                    if (pos < commandMsg.length()) {
                        attName = commandMsg.substring(0, pos);
                        commandMsg = commandMsg.substring(pos + 1);
                    }
                }
                LOG.debug("Attribute:{}", attName);

                // Parsing Value
                value = commandMsg;
                LOG.debug("Value:{}#", value);
                LOG.debug("------------");

                // Processing Commands
            }
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                LOG.error("Exception:", e);
            }
            socket.close();
        } catch (IOException e) {
            LOG.error("Exception:", e);
        }
    }

    private void printReply(final String commandName, final String message)
        throws IOException {

        out.write((message + SimHeaders.NEWLINE).getBytes());
    }

    public String output(final String name, final String message) {
        return "[" + name + "] " + message;
    }

    public void parseCommandMessage(final String message) throws IOException {
        String returnValue = null;
        String commandName = null;
        LOG.debug("Cmd:{}", message);

        printReply("T", commandHandler.invokeCommand(message));
    }
}
