/*
 * GetCommand.java
 *
 * Created on 10. August 2008, 21:32
 *
 * Command handling retrieving variable values of simulator models.
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
 *  2009-08
 *     Removed the  model dependency and made the manipulator use more generic.
 *     A. Brandt
 *
 *-----------------------------------------------------------------------------
 *
 *      File under GPL - see OpenSimKit Documentation.
 *
 *      No warranty and liability for correctness by author.

 */
package org.opensimkit.commanding;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.opensimkit.manipulation.ClassIsNotAModelException;
import org.opensimkit.manipulation.FieldIsNotManipulatableException;
import org.opensimkit.manipulation.Manipulator;
import org.opensimkit.Kernel;

/**
 * Command handling retrieving variable values of simulator models.
 *
 * @author A. Brandt
 * @author T. Pieper
 * @version 1.2
 * @since 2.4.6
 */
public class GetCommand extends BaseMMICommand {
    private static final Logger LOG = LoggerFactory.getLogger(GetCommand.class);
    @Inject Manipulator manipulator;

    public GetCommand(final Kernel kernel) {
        super("GET");
        // manipulator = kernel.getManipulator();
    }

    @Override
    public String process(final String command) {
        String result = "";
        String[] tokens = command.split(COMMAND_DELIMITER);
        if (tokens.length == 3) {
            String receivedName = tokens[FIRST_TOKEN];
            String instanceName = tokens[SECOND_TOKEN];
            String field        = tokens[THIRD_TOKEN];
            Object instance     = manipulator.getInstance(instanceName);

            if (instance == null) {
                result = output(resourceBundle.getString(
                        UNKNOWNINSTANCE), instanceName);
            } else {
                try {
                    String value = manipulator.getAsString(instance, field);
                    result = output(resourceBundle.getString(name
                        + COMMANDSUCCESSFUL), receivedName, instanceName, field,
                        value);
                } catch (NoSuchFieldException ex) {
                    result = output(resourceBundle.getString(
                        NOSUCHFIELD), field, instance.getClass().getName());
                } catch (IllegalArgumentException ex) {
                    LOG.error("Exception:", ex);
                } catch (IllegalAccessException ex) {
                    LOG.error("Exception:", ex);
                } catch (ClassNotFoundException ex) {
                    LOG.error("Exception:", ex);
                } catch (FieldIsNotManipulatableException ex) {
                    result = output(resourceBundle.getString(
                        FIELDISNOTAMANIPULATABLE), ex.getMessage());
                } catch (ClassIsNotAModelException ex) {
                    LOG.error("Exception:", ex);
                }
            }
        } else {
            result = output(resourceBundle.getString(name
                    + WRONGNUMBEROFPARAMETERS), tokens.length - 1);
        }
        return result;
    }
}
