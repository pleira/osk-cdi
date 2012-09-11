/*
 * MMICommandHandler.java
 *
 * Created on 10. August 2008, 21:32
 *
 * A class to process all simulator commands from the MMI and to delegate them
 * to the corresponding commands.
 *
 *-----------------------------------------------------------------------------
 * Modification History:
 *
 *  2008-08-10
 *      File created - A. Brandt:
 *      Initial version of the command handling.
 *
 *  2009-06
 *     Integrated logging support.
 *     T. Pieper
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

import java.util.Locale;
import java.util.SortedMap;
import java.util.TreeMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.opensimkit.SimHeaders;

/**
 * A class to process all simulator commands from the MMI and to delegate them
 * to the corresponding commands.
 *
 * @author A. Brandt
 * @author T. Pieper
 * @version 1.1
 * @since 2.4.6
 */
public final class MMICommandHandler {
    private static final Logger LOG
            = LoggerFactory.getLogger(MMICommandHandler.class);
    private final SortedMap<String, BaseMMICommand> items =
        new TreeMap<String, BaseMMICommand>();

    public void registerCommand(final BaseMMICommand mmiCommand) {
        items.put(mmiCommand.getName(), mmiCommand);
    }

    public void deregisterCommand(final BaseMMICommand mmiCommand) {
        items.remove(mmiCommand.getName());
    }

    /**
     *
     * @param command The name of the command to be invoked.
     * @return The return of the command.
     */
    public String invokeCommand(final String command) {
        String result = null;

        String[] tokens = command.split(BaseMMICommand.COMMAND_DELIMITER);
        LOG.debug("CMD: {} tokens.", tokens.length);

        String commandName = tokens[0];

        for (String key : items.keySet()) {
            if (key.equalsIgnoreCase(commandName)) {
                LOG.debug(key);
                result = items.get(key).process(command);
            }
        }
        if (result == null) {
            result = "[CMDHandler] Unknown command: \"" + commandName
                + "\"";
        }

        LOG.debug(result);
        return result;
    }

    public String getDescriptionOfRegisteredCommands() {
        StringBuilder result = new StringBuilder();

        result.append("List of available simulator commands:");
        result.append(SimHeaders.NEWLINE);

        for (String key : items.keySet()) {
            result.append(" ");
            result.append(items.get(key).getName());
            result.append(": ");
            result.append(items.get(key).getDescription());
            result.append(SimHeaders.NEWLINE);
        }
        return result.toString();
    }

    /**
     * Returns true if a Command with the supplied name exists, otherwise it
     * returns false.
     * @param commandName The name of the command.
     * @return True if the Command with the name "name" exists.
     */
    public boolean isCommand(final String commandName) {
        return items.containsKey(commandName.toUpperCase(Locale.ENGLISH));
    }

    public String getLongCommandDescription(final String name) {
        return items.get(name.toUpperCase(Locale.ENGLISH)).getLongDescription();
    }
    
    
}
