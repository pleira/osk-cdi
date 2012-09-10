/*
 * SimVisThread.java
 *
 * Created on 17. March 2009
 *
 * A class which writes S/C position & attitude data 
 * to a separate thread for connecting to a visualization SW
 *
 *-----------------------------------------------------------------------------
 * Modification History:
 *
 *  2009-17-03
 *      File created - R.Witt:
 *      R. Witt
 *
 *
 *  2010-03/04
 *      File adapted to suitability for individual threads for each flying
 *      structure instance. Called by ScStructure now, not InteractiveMain.
 *      Cleaned off deprecated thread functions.
 *      Changed printouts to logger statements.
 *      J. Eickhoff
 *
 *-----------------------------------------------------------------------------
 *
 *      File under GPL - see OpenSimKit Documentation.
 *
 *      No warranty and liability for correctness by author.
 *
 *-----------------------------------------------------------------------------
 */
package org.opensimkit;

import java.io.IOException;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.Disposes;
import javax.enterprise.inject.Produces;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Visualization thread class for connection of OpenSimKit to Celestia.
 *
 * 
 *
 * @author R. Witt
 * @author J. Eickhoff
 * @version 1.2
 * @since 3.5.0
 */

public class SimVisThread  {
//	@Inject
//	private Logger LOG;
	
	private Socket socket;
    private OutputStream out;
    private int isRunning = 0;

//    private final ComHandler  comHandler;
//    @Inject Manipulator manipulator;
    private final String compName = "Celestia";
    private final int visSocketNumber = 1520;

    private static final Logger LOG = LoggerFactory.getLogger(SimVisThread.class);
    
    // TODO: someone has to fire sc data
    public void writeSimData(@Observes ScData data) {
//        connectToCelestia();

       LOG.info("Data received for celestia");
       if (isRunning == 1) {
            // Model strucModel = comHandler.getItemKey(compName);

            try {
                out.write(data.getCelestiaInfo());
                out.write('\n');
                out.flush();
            }

            catch (IOException e) {
                System.err.println(e.toString());
                try {
                    LOG.info("Closing socket, SimVisThread");
                    socket.close();
                } 
                catch (IOException e1) {
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
                }
            }
        }
//        disconnectFromCelestia();
    }

 
	public void disconnectFromCelestia(@Disposes @CelestiaConnection SimVisThread visThread) {
		isRunning = 0;
		LOG.debug("Start terminating SimVisThread");
        LOG.debug("Checking whether socket already was closed");
        try {
            if (socket!= null) socket.close();
            if (out != null) out.close();
        }
        catch (IOException e2) {
        }
        LOG.info("SimVisThread terminating");
	}

	@Produces @CelestiaConnection
	public SimVisThread connectToCelestia() {
		LOG.info("SimVisThread running for component " + compName);
        LOG.debug("Waiting for connection of Visualization software on port " + visSocketNumber);

        try {
            ServerSocket visualizationTMSocket = new ServerSocket(visSocketNumber);
            LOG.info("SimVisThread waiting...");
            socket = visualizationTMSocket.accept();
            LOG.info("Celestia connected to OSK");
            out = socket.getOutputStream();
            isRunning = 1;    		
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        return this;
	}
        
}
