/*
 * SetACommand.java
 *
 * Created on 10. August 2008, 21:32
 *
 * Command setting an element of an array variable of a simulation model.
 *
 *-----------------------------------------------------------------------------
 * Modification History:
 *
 *  2008-08-10
 *      File created - A. Brandt:
 *      Initial version of the command handling.
 *
 *  2009-01-07
 *      Changed to work only when the simulator is not running. - A. Brandt
 *
 *  2009-06
 *     Integrated logging support.
 *     T. Pieper
 *
 *  2009-08
 *     Removed the model dependency and made the manipulator use more generic.
 *     A. Brandt
 *
 *-----------------------------------------------------------------------------
 *
 *      File under GPL - see OpenSimKit Documentation.
 *
 *      No warranty and liability for correctness by author.
 *
 */
package org.opensimkit.commanding;

import javax.inject.Inject;

import org.opensimkit.Kernel;
import org.opensimkit.SimulatorState;
import org.opensimkit.manipulation.ClassIsNotAModelException;
import org.opensimkit.manipulation.FieldIsNotManipulatableException;
import org.opensimkit.manipulation.Manipulator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Command setting an element of an array variable of a simulation model.
 *
 * @author A. Brandt
 * @author T. Pieper
 * @version 1.3
 * @since 2.4.6
 */
public class SetACommand extends BaseMMICommand {
    private static final Logger LOG
            = LoggerFactory.getLogger(SetACommand.class);
    @Inject Manipulator manipulator;
    private final Kernel kernel;

    public SetACommand(final Kernel kernel) {
        super("SETA");
        // manipulator = kernel.getManipulator();
        this.kernel = kernel;
    }

    @Override
    public String process(final String command) {
        String result = "";
        String[] tokens = command.split(COMMAND_DELIMITER);

        if (tokens.length == 5) {
            if (kernel.getState() != SimulatorState.RUNNING) {

                String receivedName = tokens[FIRST_TOKEN];
                String instanceName = tokens[SECOND_TOKEN];
                String field        = tokens[THIRD_TOKEN];
                String position     = tokens[FOURTH_TOKEN];
                String value        = tokens[FIFTH_TOKEN];
                Object instance     = manipulator.getInstance(instanceName);

                if (instance == null) {
                    result = output(resourceBundle.getString(
                            UNKNOWNINSTANCE), instanceName);
                } else {
                    try {
                        /* Please note that the value and position parameters
                           are switched between the SETA command and the
                           manipulator.setArrayFromString! */
                        manipulator.setArrayFromString(
                                instance, field, value,
                                Integer.valueOf(position));
                        result = output(resourceBundle.getString(name
                                + COMMANDSUCCESSFUL), receivedName,
                                instanceName, field, position, value);
                    } catch (NoSuchFieldException ex) {
                        result = output(resourceBundle.getString(
                            NOSUCHFIELD), field, instance.getClass().getName());
                    } catch (IllegalArgumentException ex) {
                        result = output(resourceBundle.getString(
                            ILLEGALARGUMENT), ex.getMessage());
                    } catch (IllegalAccessException ex) {
                        LOG.error("Exception:", ex);
                    } catch (ClassNotFoundException ex) {
                        LOG.error("Exception:", ex);
                    } catch (FieldIsNotManipulatableException ex) {
                        result = output(resourceBundle.getString(
                            FIELDISNOTAMANIPULATABLE), ex.getMessage());
                    } catch (ClassIsNotAModelException ex) {
                        LOG.error("Exception:", ex);
                    } catch (ArrayIndexOutOfBoundsException ex) {
                        result = output(resourceBundle.getString(
                            ARRAYINDEXOUTOFBOUNDS), position);
                    }
                }
            } else {
                result = output(resourceBundle.getString(WRONGSTATE));
            }
        } else {
            result = output(resourceBundle.getString(name
                    + WRONGNUMBEROFPARAMETERS), tokens.length - 1);
        }
        return result;
    }
}
