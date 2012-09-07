/*
 * CallCommand.java
 *
 * Created on 10. August 2008, 21:32
 *
 * Command handling method calls to simulation models.
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

import java.lang.reflect.InvocationTargetException;

import javax.inject.Inject;

import org.opensimkit.Kernel;
import org.opensimkit.manipulation.ClassIsNotAModelException;
import org.opensimkit.manipulation.Manipulator;
import org.opensimkit.manipulation.MethodIsNotCallableException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Command handling method calls to simulation models.
 *
 * @author A. Brandt
 * @author T. Pieper
 * @version 1.2
 * @since 2.4.6
 */
public class CallCommand extends BaseMMICommand {
    private static final Logger LOG
            = LoggerFactory.getLogger(CallCommand.class);
    @Inject
    private Manipulator manipulator;

    public CallCommand(final Kernel kernel) {
        super("CALL");
        //// manipulator = kernel.getManipulator();
    }

    @Override
    public String process(final String command) {
        String result = "";

        String[] tokens = command.split(COMMAND_DELIMITER);

        if (tokens.length == 3) {
            String receivedName = tokens[FIRST_TOKEN];
            String instanceName = tokens[SECOND_TOKEN];
            String method       = tokens[THIRD_TOKEN];
            Object instance     = manipulator.getInstance(instanceName);

            if (instance == null) {
                result = output(resourceBundle.getString(
                        UNKNOWNINSTANCE), instanceName);
            } else {
                try {
                    Object returned = manipulator.callMethod(instance, method);

                    if (returned == null) {
                        returned = "<void>";
                    }
                    result = output(resourceBundle.getString(name
                        + COMMANDSUCCESSFUL), receivedName, instanceName,
                        method, returned.toString());
                } catch (NoSuchMethodException ex) {
                    result = output(resourceBundle.getString(
                        NOSUCHMETHOD), method,
                        instance.getClass().getName());
                } catch (InvocationTargetException ex) {
                    LOG.error("Exception:", ex);
                } catch (IllegalArgumentException ex) {
                    LOG.error("Exception:", ex);
                } catch (IllegalAccessException ex) {
                    LOG.error("Exception:", ex);
                } catch (ClassNotFoundException ex) {
                    LOG.error("Exception:", ex);
                } catch (ClassIsNotAModelException ex) {
                    LOG.error("Exception:", ex);
                } catch (MethodIsNotCallableException ex) {
                    result = output(resourceBundle.getString(name
                        + METHODISNOTCALLABLE), method,
                        instance.getClass().getName());
                }
            }
        } else {
            result = output(resourceBundle.getString(name
                    + WRONGNUMBEROFPARAMETERS), tokens.length - 1);
        }
        return result;
    }
}
