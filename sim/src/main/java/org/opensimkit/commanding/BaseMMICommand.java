/*
 * BaseMMICommand.java
 *
 * Created on 10. August 2008, 21:32
 *
 * Basic MMI command. Intended as a parent for MMI commands.
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

import java.util.Locale;
import java.util.ResourceBundle;

/**
 * Basic MMI command. Intended as a parent for MMI commands.
 *
 * @author A. Brandt
 * @version 1.1
 * @since 2.4.6
 */
public class BaseMMICommand {
    /** Index of the first token. */
    protected static final int FIRST_TOKEN = 0;
    /** Index of the second token. */
    protected static final int SECOND_TOKEN = 1;
    /** Index of the third token. */
    protected static final int THIRD_TOKEN = 2;
    /** Index of the fourth token. */
    protected static final int FOURTH_TOKEN = 3;
    /** Index of the fifth token. */
    protected static final int FIFTH_TOKEN = 4;
    protected static final String COMMAND_DELIMITER = " ";
    protected static final String COMMANDBUNDLE =
            "org.opensimkit.resources.commands";
    protected static final String SHORTDESCRIPTION = ".shortDescription";
    protected static final String LONGDESCRIPTION = ".longDescription";
    protected static final String WRONGNUMBEROFPARAMETERS =
            ".error.wrongNumberOfParameters";
    protected static final String UNRECOGNIZEDPARAMETER =
            ".error.unrecognizedParameter";
    protected static final String ARRAYINDEXOUTOFBOUNDS =
            ".error.arrayIndexOutOfBounds";
    protected static final String FIELDISNOTAMANIPULATABLE =
            ".error.fieldIsNotAManipulatable";
    protected static final String UNKNOWNINSTANCE =
            ".error.unknownInstance";
    protected static final String NOSUCHFIELD =
            ".error.noSuchField";
    protected static final String NOSUCHMETHOD =
            ".error.noSuchMethod";
    protected static final String METHODISNOTCALLABLE =
            ".error.methodIsNotCallable";
    protected static final String ILLEGALARGUMENT =
            ".error.illegalArgument";
    protected static final String SIMULATIONISALREADYRUNNING =
            ".error.simulationIsAlreadyRunning";
    protected static final String SIMULATIONISNOTPAUSED =
            ".error.simulationIsNotPaused";
    protected static final String SIMULATIONISNOTRUNNING =
            ".error.simulationIsNotRunning";
    protected static final String WRONGSTATE = ".error.wrongState";
    protected static final String COMMANDSUCCESSFUL = ".commandSuccessful";

    /** Name of the command. It is used by the MMICommandHandler to decide which
     * command to invoke. */
    protected String name;
    /** Short description of the command. It is used by the MMICommandHandler to
     * print a help screen. */
    protected String shortDescription;
    /** Long description of the command. It is used by the MMICommandHandler to
     * print a help screen. */
    protected String longDescription;

    protected ResourceBundle resourceBundle;

    protected String wrongNumberOfParameters;

    public BaseMMICommand(final String name) {
        this.name = name;
        resourceBundle =
               ResourceBundle.getBundle(COMMANDBUNDLE, Locale.ENGLISH);
        shortDescription =
               String.format(resourceBundle.getString(name + SHORTDESCRIPTION));
        longDescription =
               String.format(resourceBundle.getString(name + LONGDESCRIPTION));
    }

    /**
     * Basic method of any MMICommand. Used by the MMICommandHandler to delegate
     * the command invokation to the actual command.
     * @param command Complete command string as received from the MMI.
     * @return Output message from the command. Either an error message or a
     *         success message.
     */
    public String process(final String command) {
        String[] tokens = command.split(COMMAND_DELIMITER);

        return output("Command successful: \"" + tokens[0] + "\"");
    }

    /**
     * Returns the name of the command.
     * @return The command name.
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the Description of the command.
     * @return The description.
     */
    public String getDescription() {
        return shortDescription;
    }

    public String getLongDescription() {
        return longDescription;
    }

    /**
     * Formats the output by putting the command name in front of the message.
     * @param message Message to format.
     * @param args Arguments for the format string.
     * @return Message prefixed with command name.
     */
    public String output(final String message, final Object... args) {
        return "[" + name + "] " + String.format(message, args);
    }
}
