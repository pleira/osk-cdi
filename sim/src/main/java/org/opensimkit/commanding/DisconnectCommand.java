/*
 * DisconnectCommand.java
 *
 * Created on 10. August 2008, 21:32
 *
 * Command handling disconnecting the simulator from the MMI.
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

/**
 * Command handling disconnecting the simulator from the MMI.
 *
 * @author A. Brandt
 * @version 1.1
 * @since 2.4.6
 */
public final class DisconnectCommand extends BaseMMICommand {

    public DisconnectCommand() {
        super("DISCONNECT");
    }

    @Override
    public String process(final String command) {
        String result = "";
        String[] tokens = command.split(COMMAND_DELIMITER);
        if (tokens.length == 1) {

            InteractiveMain.shutdownSimulation();

            result = output(resourceBundle.getString(COMMANDSUCCESSFUL),
                    tokens[FIRST_TOKEN]);
        } else {
            result = output(resourceBundle.getString(
                    WRONGNUMBEROFPARAMETERS), tokens.length - 1);
        }
        return result;
    }
}
