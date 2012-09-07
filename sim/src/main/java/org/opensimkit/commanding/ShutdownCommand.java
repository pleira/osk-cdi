/*
 * ShutdownCommand.java
 *
 * Created on 10. August 2008, 21:32
 *
 * Command handling the shutdown of the simulation and the simulator.
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
 *
 *-----------------------------------------------------------------------------
 */
package org.opensimkit.commanding;

import org.opensimkit.InteractiveMain;

/**
 * Command handling the shutdown of the simulation and the simulator.
 *
 * @author A. Brandt
 * @version 1.0
 * @since 2.4.6
 */
public final class ShutdownCommand extends BaseMMICommand {

    public ShutdownCommand() {
        super("SHUTDOWN");
    }

    @Override
    public String process(final String command) {
        String result = "";
        String[] tokens = command.split(COMMAND_DELIMITER);
        if (tokens.length == 1) {

            InteractiveMain.shutdownSimulation();

            result = output(resourceBundle.getString(name
                                + COMMANDSUCCESSFUL), tokens[FIRST_TOKEN]);
        } else {
            result = output(resourceBundle.getString(
                    WRONGNUMBEROFPARAMETERS), tokens.length - 1);
        }
        return result;
    }
}
