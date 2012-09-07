/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.opensimkit.manipulation;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.SortedMap;
import org.opensimkit.SimHeaders;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * This class shall be used to print information about the loaded simualtion
 * models and/or classes into a file or to the screen. Create a new
 * InjectionPrinter object and call the gewuenschte methods. Finally call
 * toString() to get a String containing the gewuenschte information.
 *
 * @author A. Brandt
 * @version 1.0
 * @since 2.6.8
 */
public final class ManipulationPrinter {
    private static final Logger LOG
            = LoggerFactory.getLogger(ManipulationPrinter.class);
    private final StringBuilder result;
    private final SortedMap<String, Object> instances;
    private final SortedMap<String, ClassInformation> registeredClasses;

    public ManipulationPrinter(final Manipulator manipulator) {
        registeredClasses = manipulator.getRegisteredClasses();
        instances = manipulator.getRegisterdInstances();
        result = new StringBuilder();
    }

    /**
     * Overwritten toString() method. Needs to be called to get the information
     * of this print job from the InjectionPrinter.
     * @return A String containing the information gathered by this print job.
     */
    @Override
    public String toString() {
        return result.toString();
    }

    /**
     * Allows the insertion of any kind of text into the injection printing.
     * @param stringToAppend
     */
    public void append(final String stringToAppend) {
        result.append(stringToAppend);
    }

    /**
     * Prints an alphabetically sorted listing of all available model instances.
     */
    public void printAvailableModels() {
        for (Map.Entry<String, Object> instance : instances.entrySet()) {
            result.append(instance.getKey());
            result.append(" (");
            result.append(instance.getValue().getClass().toString());
            result.append(")");
            result.append(SimHeaders.NEWLINE);
        }
    }
//Available Models:
//00_HPBottle  (class org.opensimkit.models.rocketpropulsion.HPBottleT1)
//01_HPBottle  (class org.opensimkit.models.rocketpropulsion.HPBottleT1)
//02_Pipe  (class org.opensimkit.models.rocketpropulsion.PipeT1)
//03_Pipe  (class org.opensimkit.models.rocketpropulsion.PipeT1)
//04_Junction  (class org.opensimkit.models.rocketpropulsion.JunctionT1)
//05_Pipe  (class org.opensimkit.models.rocketpropulsion.PipeT1)
//06_Filter  (class org.opensimkit.models.rocketpropulsion.FilterT1)
//07_Pipe  (class org.opensimkit.models.rocketpropulsion.PipeT1)
//08_PReg  (class org.opensimkit.models.rocketpropulsion.PRegT1)
//09_Pipe  (class org.opensimkit.models.rocketpropulsion.PipeT1)
//10_Split  (class org.opensimkit.models.rocketpropulsion.SplitT1)
//11_Pipe  (class org.opensimkit.models.rocketpropulsion.PipeT1)
//12_PReg  (class org.opensimkit.models.rocketpropulsion.PRegT1)
//13_Pipe  (class org.opensimkit.models.rocketpropulsion.PipeT1)
//14_Pipe  (class org.opensimkit.models.rocketpropulsion.PipeT1)
//15_PReg  (class org.opensimkit.models.rocketpropulsion.PRegT1)
//16_Pipe  (class org.opensimkit.models.rocketpropulsion.PipeT1)
//17_Tank  (class org.opensimkit.models.rocketpropulsion.TankT1)

    /**
     * Prints an alphabetically sorted listing of all classes and all of their
     * injectable fields known by the attached manipulator instance.
     */
    public void printAllInjectables() {
        try {
            for (Map.Entry<String, ClassInformation> clazzes
                : registeredClasses.entrySet()) {

                    Class<?> clazz = Class.forName(clazzes.getKey());
                    result.append(clazz.getName());
                    result.append(SimHeaders.NEWLINE);
                    result.append("--------------------------------------");
                    result.append(SimHeaders.NEWLINE);
                    printInjectables(clazz);
                }
        } catch (ClassNotFoundException ex) {
            LOG.error("Exception: ", ex);
        }
    }

    public void printInjectables(final String fullyQualifiedName) {
        try {
            Class<?> clazz = Class.forName(fullyQualifiedName);

            printInjectables(clazz);

        } catch (ClassNotFoundException ex) {
            LOG.error("Exception:", ex);
        }
    }

    public void printInjectables(final Class<?> clazz) {
        ClassInformation classInformation
                = registeredClasses.get(clazz.getName());

        if (classInformation != null) {
            SortedMap<String, Field> fields
                    = classInformation.getInjectableFields();
            printFieldMap(clazz, fields);
        }
    }

    /**
     * Prints an alphabetically sorted listing of all classes and all of their
     * readable fields known by the attached manipulator instance.
     */
    public void printAllReadables() {
//        for (Map.Entry<String, ClassInformation> clazz
//                : registeredClasses.entrySet()) {
//                    printReadables(clazz.getKey());
//        }
        try {
            for (Map.Entry<String, ClassInformation> clazzes
                : registeredClasses.entrySet()) {

                    Class<?> clazz = Class.forName(clazzes.getKey());
                    result.append(clazz.getName());
                    result.append(SimHeaders.NEWLINE);
                    result.append("--------------------------------------");
                    result.append(SimHeaders.NEWLINE);
                    printReadables(clazz);
                }
        } catch (ClassNotFoundException ex) {
            LOG.error("Exception: ", ex);
        }
    }

    public void printReadables(final String fullyQualifiedName) {
        try {
            Class<?> clazz = Class.forName(fullyQualifiedName);

            printReadables(clazz);

        } catch (ClassNotFoundException ex) {
            LOG.error("Exception:", ex);
        }
    }

    public void printReadables(final Class<?> clazz) {
        ClassInformation classInformation
                = registeredClasses.get(clazz.getName());

        if (classInformation != null) {
            SortedMap<String, Field> fields
                    = classInformation.getReadableFields();
            printFieldMap(clazz, fields);
        }
    }

    public void printFieldMap(final Class<?> clazz,
            final SortedMap<String, Field> fieldMap) {
        ClassInformation classInformation
                = registeredClasses.get(clazz.getName());

        if (classInformation != null) {
//            result.append(clazz.getName());
//            result.append(SimHeaders.NEWLINE);
//            result.append("--------------------------------------");
//            result.append(SimHeaders.NEWLINE);

            for (Map.Entry<String, Field> field : fieldMap.entrySet()) {
                result.append(field.getKey());
                result.append(" (");
                result.append(field.getValue().getType().getName());
                result.append(")");
                result.append(SimHeaders.NEWLINE);
            }
            result.append(SimHeaders.NEWLINE);
        }
    }

//org.opensimkit.models.rocketpropulsion.FilterT1
//--------------------------------------
//description (java.lang.String)
//innerDiameter (double)
//length (double)
//referenceMassFlow (double)
//referencePressureLoss (double)
//specificHeatCapacity (double)
//specificMass (double)
//temperature (double)
//type (java.lang.String)


//org.opensimkit.models.rocketpropulsion.FilterT1
//--------------------------------------
//maxIntegStepSize (double)
//mfin (double)
//minIntegStepSize (double)
//name (java.lang.String)
//numSolverType (java.lang.String)
//pin (double)
//pout (double)
//rStep (int)
//tStep (int)
//tin (double)
//tout (double)

    /**
     * Prints an alphabetically sorted listing of all classes and all of their
     * callable methods known by the attached manipulator instance.
     */
    public void printAllCallableMethods() {
//        for (Map.Entry<String, ClassInformation> clazz
//                : registeredClasses.entrySet()) {
//                    printCallableMethods(clazz.getKey());
//        }
        try {
            for (Map.Entry<String, ClassInformation> clazzes
                : registeredClasses.entrySet()) {

                    Class<?> clazz = Class.forName(clazzes.getKey());
                    result.append(clazz.getName());
                    result.append(SimHeaders.NEWLINE);
                    result.append("--------------------------------------");
                    result.append(SimHeaders.NEWLINE);
                    printCallableMethods(clazz);
                }
        } catch (ClassNotFoundException ex) {
            LOG.error("Exception: ", ex);
        }
    }

    public void printCallableMethods(final String fullyQualifiedName) {
        try {
            Class<?> clazz = Class.forName(fullyQualifiedName);

            printCallableMethods(clazz);

        } catch (ClassNotFoundException ex) {
            LOG.error("Exception:", ex);
        }
    }

    public void printCallableMethods(final Class<?> clazz) {
        ClassInformation classInformation
                = registeredClasses.get(clazz.getName());

        if (classInformation != null) {
            SortedMap<String, Method> methods
                    = classInformation.getCallableMethods();
            printMethodMap(clazz, methods);
        }
    }

    public void printMethodMap(final Class<?> clazz,
            final SortedMap<String, Method> methodMap) {
        ClassInformation classInformation
                = registeredClasses.get(clazz.getName());

        if (classInformation != null) {
//            result.append(clazz.getName());
//            result.append(SimHeaders.NEWLINE);
//            result.append("--------------------------------------");
//            result.append(SimHeaders.NEWLINE);

            for (Map.Entry<String, Method> method : methodMap.entrySet()) {
                result.append(method.getValue().getReturnType().getName());
                result.append(" ");
                result.append(method.getKey());
                result.append(" (");
                printMethodParameters(method.getValue());
                result.append(")");
                result.append(SimHeaders.NEWLINE);
            }
            result.append(SimHeaders.NEWLINE);
        }
    }

    public void printMethodParameters(final Method method) {
        Class<?>[] parameters = method.getParameterTypes();

        if (parameters.length > 0) {
            for (Class<?> clazz : parameters) {
                result.append(clazz.getName());
                result.append(", ");
            }
            int lastColon = result.lastIndexOf(", ");
            result.delete(lastColon, lastColon + 1);
        }
    }

//    public String getCallableMethods(final Model model) {
//        StringBuilder result = new StringBuilder();
//        SortedMap<String, Method> methods =
//                new TreeMap<String, Method>(allCallableMethods.get(
//                model.getClass()));
//
//        for (String key : methods.keySet()) {
//                result.append("");
//                Class<?> returnType =
//                        methods.get(key).getReturnType();
//                if (returnType == null) {
//                    result.append("void");
//                } else {
//                    result.append(returnType);
//                }
//                result.append(" ");
//                result.append(key);
//                result.append("(");
//                Class<?>[] parameterTypes =
//                        methods.get(key).getParameterTypes();
//                int numberOfParameters = parameterTypes.length;
//                if (numberOfParameters > 0) {
//                    result.append("(");
//                    for (int i = 0; i < numberOfParameters; i++) {
//                        result.append(parameterTypes[i].getName());
//                        result.append(" ");
//                    }
//                    result.append(")");
//                }
//                result.append(")");
//                result.append(SimHeaders.newline);
//            }
//        return result.toString();
//    }

//org.opensimkit.models.rocketpropulsion.FilterT1
//--------------------------------------
//void init()

}
