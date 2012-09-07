/*
 * PauseCommand.java
 *
 * Created on 05. January 2009, 18:57
 *
 * Command handling the pause of the simulation.
 *
 *-----------------------------------------------------------------------------
 * Modification History:
 *
 *  2009-01-05
 *      File created - A. Brandt:
 *      Initial version of the pause command.
 *
 *-----------------------------------------------------------------------------
 *
 *      File under GPL - see OpenSimKit Documentation.
 *
 *      No warranty and liability for correctness by author.
 *
 */
package org.opensimkit.commanding;

import org.opensimkit.Kernel;
import org.opensimkit.SimulatorState;

/**
 * Command handling the pause of the simulation.
 *
 * @author A. Brandt
 * @version 1.1
 * @since 2.5.1
 */
public final class PauseCommand extends BaseMMICommand {
    private final Kernel kernel;

    public PauseCommand(final Kernel kernel) {
        super("PAUSE");
        this.kernel = kernel;
    }

    @Override
    public String process(final String command) {
        String result = "";
        String[] tokens = command.split(COMMAND_DELIMITER);
        if (tokens.length == 1) {

            if (kernel.getState() == SimulatorState.RUNNING) {
                kernel.pauseSimulation();
                result
                        = output(resourceBundle.getString(COMMANDSUCCESSFUL),
                        tokens[FIRST_TOKEN]);
            } else {
                result
                        = output(resourceBundle.getString(name
                + SIMULATIONISNOTRUNNING), tokens[FIRST_TOKEN]);
            }

        } else {
            result = output(resourceBundle.getString(
                    WRONGNUMBEROFPARAMETERS), tokens.length - 1);
        }
        return result;
    }
}
