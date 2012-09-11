/*
 * HelpCommand.java
 *
 * Created on 10. August 2008, 21:32
 *
 * Command printing help about the MMI commands and the simulator models.
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
 */
package org.opensimkit.commanding;

import org.opensimkit.Kernel;
import org.opensimkit.SimHeaders;
import org.opensimkit.manipulation.ClassIsNotAModelException;
import org.opensimkit.manipulation.FieldIsNotManipulatableException;
import org.opensimkit.manipulation.ManipulationPrinter;
import org.opensimkit.manipulation.Manipulator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Command printing help about the MMI commands and the simulator models.
 *
 * @author A. Brandt
 * @author T. Pieper
 * @version 1.1
 * @since 2.4.6
 */
public class HelpCommand extends BaseMMICommand {
    private static final Logger LOG
            = LoggerFactory.getLogger(HelpCommand.class);
    private final MMICommandHandler mmiCommandHandler;
    Manipulator       manipulator;

    public HelpCommand(final MMICommandHandler mmiCommandHandler,
            final Kernel kernel) {
        super("HELP");
        this.mmiCommandHandler = mmiCommandHandler;
        manipulator = kernel.getManipulator();
    }

    // TODO Extend HelpCommand to accept parameters like "help run" to show a
    // more detailed description of the command.

    @Override
    public String process(final String command) {
        String result = "";
        String[] tokens = command.split(COMMAND_DELIMITER);
        if (tokens.length == 1) {
            result = output(resourceBundle.getString(name
                + COMMANDSUCCESSFUL), tokens[FIRST_TOKEN], getDefaultHelp());
        } else if (tokens.length == 2) {
            /** Name of the command. */
            String receivedName = tokens[FIRST_TOKEN];
            /** First parameter. */
            String parameter1 = tokens[SECOND_TOKEN];
            /** The following if-else-if is intended. If a Model has the
             * same name as a Command both the help of the Model and the
             * Command are displayed. */
            if (mmiCommandHandler.isCommand(parameter1)) {
                result = output(resourceBundle.getString(name
                    + COMMANDSUCCESSFUL), receivedName,
                    mmiCommandHandler.getLongCommandDescription(parameter1));
            } else if (manipulator.isRegisteredInstance(parameter1)) {
                result = output(resourceBundle.getString(name
                    + COMMANDSUCCESSFUL), receivedName,
                    getInstanceHelp(parameter1));
            } else if ("Models".equalsIgnoreCase(parameter1)) {
                ManipulationPrinter injectionPrinter
                        = new ManipulationPrinter(manipulator);
                injectionPrinter.append("All available models:");
                injectionPrinter.append(SimHeaders.NEWLINE);
                injectionPrinter.printAvailableModels();
                result = output(
                        resourceBundle.getString(name + COMMANDSUCCESSFUL),
                        receivedName,
                        injectionPrinter.toString());

            } else if ("Manipulatables".equalsIgnoreCase(parameter1)) {
                ManipulationPrinter injectionPrinter
                        = new ManipulationPrinter(manipulator);
                injectionPrinter.append("All manipulatable fields:");
                injectionPrinter.append(SimHeaders.NEWLINE);
                injectionPrinter.append(SimHeaders.NEWLINE);
                injectionPrinter.printAllInjectables();
                result = output(
                        resourceBundle.getString(name + COMMANDSUCCESSFUL),
                        receivedName,
                        injectionPrinter.toString());

            } else if ("Readables".equalsIgnoreCase(parameter1)) {
                ManipulationPrinter injectionPrinter
                        = new ManipulationPrinter(manipulator);
                injectionPrinter.append("All readable fields:");
                injectionPrinter.append(SimHeaders.NEWLINE);
                injectionPrinter.append(SimHeaders.NEWLINE);
                injectionPrinter.printAllReadables();
                result = output(
                        resourceBundle.getString(name + COMMANDSUCCESSFUL),
                        receivedName,
                        injectionPrinter.toString());

            } else if ("Callables".equalsIgnoreCase(parameter1)) {
                ManipulationPrinter injectionPrinter
                        = new ManipulationPrinter(manipulator);
                injectionPrinter.append("All callable methods:");
                injectionPrinter.append(SimHeaders.NEWLINE);
                injectionPrinter.append(SimHeaders.NEWLINE);
                injectionPrinter.printAllCallableMethods();
                result = output(
                        resourceBundle.getString(name + COMMANDSUCCESSFUL),
                        receivedName,
                        injectionPrinter.toString());

            } else if ("Version".equalsIgnoreCase(parameter1)) {
                String returned = "Simulator Version: "
                        + Kernel.OSK_NAME + " " + Kernel.OSK_VERSION;
                result = output(resourceBundle.getString(name
                    + COMMANDSUCCESSFUL), receivedName, returned);
            } else {
                result = output(resourceBundle.getString(name
                    + UNRECOGNIZEDPARAMETER), parameter1);
            }
        } else {
            result = output(resourceBundle.getString(name
                    + WRONGNUMBEROFPARAMETERS), tokens.length - 1);
        }
        return result;
    }

    private String getDefaultHelp() {
        String result = null;

        try {
            result = mmiCommandHandler.getDescriptionOfRegisteredCommands()
                    + SimHeaders.NEWLINE
                    + "Type \"help <command name>\" for detailed "
                    + "information about the command."
                    + SimHeaders.NEWLINE
                    + "Type \"help help\" for more information.";
        } catch (IllegalArgumentException ex) {
            LOG.error("Exception:", ex);
        } catch (FieldIsNotManipulatableException ex) {
            LOG.error("Exception:", ex);
        } catch (ClassIsNotAModelException ex) {
            LOG.error("Exception:", ex);
        }
        return SimHeaders.NEWLINE + result;
    }

    private String getInstanceHelp(final String parameter1) {
//        Model model = (Model) comHandler.getItemKey(parameter1);
        Object instance = manipulator.getInstance(parameter1);
//        StringBuilder stringBuilder = new StringBuilder();
//        stringBuilder.append("Instance \"" + model.getName()
//                + "\" of class " + model.getClass().getName());
//        stringBuilder.append(SimHeaders.NEWLINE);
//        stringBuilder.append("---------------------------------------");
//        stringBuilder.append(SimHeaders.NEWLINE);
//        stringBuilder.append("Manipulatables:");
//        stringBuilder.append(SimHeaders.NEWLINE);
//        stringBuilder.append(manipulator.getManipulatableFields(model));
//        stringBuilder.append(SimHeaders.NEWLINE);
//        stringBuilder.append("Readables:");
//        stringBuilder.append(SimHeaders.NEWLINE);
//        stringBuilder.append(manipulator.getReadableFields(model));
//        stringBuilder.append(SimHeaders.NEWLINE);
//        stringBuilder.append("Callables:");
//        stringBuilder.append(SimHeaders.NEWLINE);
//        stringBuilder.append(manipulator.getCallableMethods(model));

        ManipulationPrinter manipulationPrinter
                = new ManipulationPrinter(manipulator);
        manipulationPrinter.append("Instance \"");
        manipulationPrinter.append(parameter1);
        manipulationPrinter.append("\" of class ");
        manipulationPrinter.append(instance.getClass().getName());
        manipulationPrinter.append(":");
        manipulationPrinter.append(SimHeaders.NEWLINE);
        manipulationPrinter.append("---------------------------------------");
        manipulationPrinter.append(SimHeaders.NEWLINE);
        manipulationPrinter.append("Manipulatables:");
        manipulationPrinter.append(SimHeaders.NEWLINE);
        manipulationPrinter.printInjectables(instance.getClass());
        manipulationPrinter.append("Readables:");
        manipulationPrinter.append(SimHeaders.NEWLINE);
        manipulationPrinter.printReadables(instance.getClass());
        manipulationPrinter.append("Callables:");
        manipulationPrinter.append(SimHeaders.NEWLINE);
        manipulationPrinter.printCallableMethods(instance.getClass());

        return manipulationPrinter.toString();
//        return stringBuilder.toString();
    }
}
