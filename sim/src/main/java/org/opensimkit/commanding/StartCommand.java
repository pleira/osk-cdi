/*
 * StartCommand.java
 *
 * Created on 10. August 2008, 21:32
 *
 * Command handling the start of the simulation.
 *
 *-----------------------------------------------------------------------------
 * Modification History:
 *
 *  2008-08-10
 *      File created - A. Brandt:
 *      Initial version of the command handling.
 *
 *-----------------------------------------------------------------------------
 *
 *      File under GPL - see OpenSimKit Documentation.
 *
 *      No warranty and liability for correctness by author.
 *
 */
package org.opensimkit.commanding;

import org.opensimkit.InteractiveMain;
import org.opensimkit.Kernel;
import org.opensimkit.SimulatorState;

/**
 * Command handling the start of the simulation.
 *
 * @author A. Brandt
 * @version 1.0
 * @since 2.4.6
 */
public final class StartCommand extends BaseMMICommand {
    private final Kernel kernel;

    public StartCommand(final Kernel kernel) {
        super("RUN");

        this.kernel = kernel;
    }

    @Override
    public String process(final String command) {
        String result = "";
        String[] tokens = command.split(COMMAND_DELIMITER);
        if (tokens.length == 1) {

            if (kernel.getState() == SimulatorState.NOT_RUNNING) {
                InteractiveMain.startSimulation();
                result = output(resourceBundle.getString(COMMANDSUCCESSFUL),
                        tokens[FIRST_TOKEN]);
            } else {
                result = output(resourceBundle.getString(name
                + SIMULATIONISALREADYRUNNING), tokens[FIRST_TOKEN]);
            }
        } else {
            result = output(resourceBundle.getString(
                    WRONGNUMBEROFPARAMETERS), tokens.length - 1);
        }
        return result;
    }
}
