/*
 * ComputeThread.java
 *
 * Created on 10. August 2008, 21:32
 *
 *  A class which manages the simulation in a separate thread.
 *
 *-----------------------------------------------------------------------------
 * Modification History:
 *
 *  2008-08-10
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
 */
package org.opensimkit;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A class which manages the simulation in a separate thread.
 *
 * @author J. Eickhoff
 * @author A. Brandt
 * @author T. Pieper
 * @since 2.4.6
 * @version 1.1
 */
@ApplicationScoped
public class ComputeThread implements Runnable {
    private static final Logger LOG
            = LoggerFactory.getLogger(ComputeThread.class);
//    private final Socket socket;
    @Inject Kernel kernel;
    private int isTerminated = 0;

    public ComputeThread() {
        super();
//        this.setName("ComputeModel");
//        isTerminated = 0;
//        this.kernel = kernel;
    }

    public void terminate() {
        isTerminated = 1;
    }

    public int isterminated() {
        return isTerminated;
    }

    public void cont() {
        kernel.setIsSimulationRunning(true);
    }

    @Override
    public void run() {
        kernel.setIsSimulationRunning(true);
//        LOG.info(getName());
        LOG.info("Simulator: Output console connection established...\n");
        while (isTerminated == 0) {
            // Here the simulation computation takes place and
            // potential thread interrupt must be caught
            if (kernel.getIsSimulationRunning() == true) {
                try {
                    kernel.compute();
                } catch (IOException ex) {
                    LOG.error("Exception:", ex);
                }
            }
        }
    }
}
