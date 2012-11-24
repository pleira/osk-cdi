/*
 * Manipulator.java
 *
 * Created on 17. Juli 2008, 21:32
 *
 * A class to access special declared fields and methods in OpenSimKit models
 * and other classes.
 *
 *-----------------------------------------------------------------------------
 * Modification History:
 *
 *  2008-07-17
 *     File created - A. Brandt:
 *     Initial version to help keeping the complexity of the simulator models
 *     low.
 *
 *  2009-02-21
 *     Major changes to make the manipulator more generic.
 *     A. Brandt
 *
 *  2009-06
 *     Integrated logging support.
 *     T. Pieper
 *
 *  2009-08
 *     Added support of non-primitive fields.
 *     A. Brandt
 *
 *  2011-01
 *     Added generic set function needed for Provider-Subscriber mechanism.
 *     A. Brandt
 *
 *-----------------------------------------------------------------------------
 *
 *      File under GPL - see OpenSimKit Documentation.
 *
 *      No warranty and liability for correctness by author.
 *
 *-----------------------------------------------------------------------------
 */

package org.opensimkit.manipulation;

import java.lang.annotation.Annotation;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A class to access special declared fields in OpenSimKit models and other
 * classes.
 *
 * @author A. Brandt
 * @author T. Pieper
 * @version 1.4
 * @since 2.4.4
 */
@ApplicationScoped
public class Manipulator {
    /** Reference to the Logger. */
    private final Logger LOG
            = LoggerFactory.getLogger(Manipulator.class);
    /** SortedMap of all instances. Each instance is registered by a name. */
    private final SortedMap<String, Object> instances
            = new TreeMap<String, Object>();
    /** SortedMap of all registered Classes. */
    private final SortedMap<String, ClassInformation> registeredClasses
            = new TreeMap<String, ClassInformation>();

    /**************************************************************************
     *                Private general methods                                 *
     **************************************************************************/
    private void updateClassInformation(
            final Class<?> clazz,
            final String name,
            final Object instance) {
        SortedMap<String, Field>  readableFields;
        SortedMap<String, Field>  injectableFields;
        SortedMap<String, Method> callableMethods;
        ClassInformation          newClassInformation;
        int                       useCount;
        String                    clazzName = clazz.getName();

        if (registeredClasses.containsKey(clazzName) == false) {
            readableFields   = getAllReadableFieldsFromClass(instance, name);
            injectableFields = getAllInjectableFieldsFromClass(instance, name);
            callableMethods  = getAllCallableMethodsFromClass(clazz);
            useCount = 1;
        } else {
            ClassInformation oldClassInformation
                    = registeredClasses.get(clazzName);

            readableFields   = oldClassInformation.getReadableFields();
            injectableFields = oldClassInformation.getInjectableFields();
            callableMethods  = oldClassInformation.getCallableMethods();

            int oldUseCount = oldClassInformation.getUseCount();
            useCount = oldUseCount++;
        }

        newClassInformation
                    = new ClassInformation(readableFields, injectableFields,
                    callableMethods, useCount);
        registeredClasses.put(clazzName, newClassInformation);
    }

    private void removeClassInformation(final Class<?> clazz) {
        if (registeredClasses.containsKey(clazz.getName()) == false) {
            LOG.info("The class '{}' is not registered!");
        } else {
            ClassInformation oldClassInformation
                    = registeredClasses.get(clazz.getName());
            int oldUseCount = oldClassInformation.getUseCount();

            if (oldUseCount == 1) {
                registeredClasses.remove(clazz.getName());
            } else {
                SortedMap<String, Field>  readableFields;
                SortedMap<String, Field>  injectableFields;
                SortedMap<String, Method> callableMethods;

                readableFields = oldClassInformation.getReadableFields();
                injectableFields
                        = oldClassInformation.getInjectableFields();
                callableMethods = oldClassInformation.getCallableMethods();
                int  useCount = oldUseCount--;

                ClassInformation newClassInformation
                    = new ClassInformation(readableFields, injectableFields,
                     callableMethods, useCount);
                registeredClasses.put(clazz.getName(), newClassInformation);
            }
        }
    }

    /**
     * Updates the given instance. This is usefull if there are non-primitive
     * Manipulatable or Readable Fields which were previously null. Example:
     *  Port inputPort;
     * During the first registering of the Class the Port is null as it is not
     * yet injected by the input XML parser. Later it will be injected and an
     * update registers the inputPort of the instances.
     * @param instance
     */
    public void updateInstance(final Object instance) {
        final Class<?> clazz = instance.getClass();

        String name = getName(instance);

        getAllReadableFieldsFromClass(instance, name);
        getAllInjectableFieldsFromClass(instance, name);
        getAllCallableMethodsFromClass(clazz);
    }

    /**
      * Returns a SortedMap with all fields of the class clazz which are
      * annotated with @org.opensimkit.Manipulatable.
      * @param clazz
      * @return SortedMap with the name of the field as key and the reference to
      * the field as value.
      */
    public SortedMap<String, Field> getAnnotatedFieldsFromClass(
            final Class<?> clazz,
            final Class<? extends Annotation> annotation,
            final String name,
            final Object instance) {

        SortedMap<String, Field> result = new TreeMap<String, Field>();

        Field[] fields = clazz.getDeclaredFields();
        if (fields.length > 0) {
            for (Field field : fields) {
                if (field.isAnnotationPresent(annotation) == true) {
                    if (isFieldOfPrimitiveOrStringType(field)) {
                        result.put(field.getName(), field);
                    } else {
                        try {
                            LOG.info("Field {} is neither primitive nor"
                                    + " String, it is {}.", field.getName(),
                                    field.getType());
                            String newName = name + "." + field.getName();
                            field.setAccessible(true);
                            Object instance2 = field.get(instance);
                            if (instance2 != null) {
                                registerInstance(newName, instance2);
                            }
                        } catch (IllegalArgumentException ex) {
                            LOG.error("Exception", ex);
                        } catch (IllegalAccessException ex) {
                            LOG.error("Exception", ex);
                        }
                    }
                }
            }
        }
        return result;
    }

   /**
     * Returns all annotated fields from a given class.
     * @param instance The instance which is scanned for annotations.
     * @param annotation The annotation for which the class is scanned.
     * @return All annotated fields of the class.
     */
    private SortedMap<String, Field> getAllAnnotatedFieldsFromClass(
            final Object instance,
            final Class<? extends Annotation> annotation,
            final String name) {

        SortedMap<String, Field> result = new TreeMap<String, Field>();

        Class<?> clazz = instance.getClass();
        result = getAnnotatedFieldsFromClass(clazz, annotation, name, instance);
        Class<?> clazz2 = clazz;
        while (clazz2.getSuperclass() != Object.class) {
            clazz2 = clazz2.getSuperclass();
            result.putAll(getAnnotatedFieldsFromClass(
                    clazz2, annotation, name, instance));
        }
        LOG.info("Fields of class \"{}\" annotated with \"{}\": {}",
                new Object[] {clazz.getName(), annotation.getName(),
                result.keySet()});

        return result;
    }

//    /**
//      * Returns a SortedMap with all fields of the class clazz which are
//      * annotated with @org.opensimkit.Manipulatable.
//      * @param clazz
//      * @return SortedMap with the name of the field as key and the reference
    //to
//      * the field as value.
//      */
//    private SortedMap<String, Field> getAnnotatedFieldsFromClass2(
//            final String name,
//            final Class<?> clazz,
//            final Class<? extends Annotation> annotation) {
//
//        SortedMap<String, Field> result = new TreeMap<String, Field>();
//
//        Field[] fields = clazz.getDeclaredFields();
//        if (fields.length > 0) {
//            for (Field field : fields) {
//                if (field.isAnnotationPresent(annotation) == true) {
//                    if (isFieldOfPrimitiveOrStringType(field)) {
//                        result.put(field.getName(), field);
//                    } else {
//                        LOG.info("Field {} is neither primitive nor String,
    //it is {}.",
//                                field.getName(), field.getType());
//
//                    }
//                }
//            }
//        }
//        return result;
//    }

    private SortedMap<String, Field> getAllObjectFieldsFromClass(
            final Class<?> clazz) {
        SortedMap<String, Field> result = new TreeMap<String, Field>();

        Field[] fields = clazz.getDeclaredFields();
        for (Field field: fields) {
            if (isFieldOfPrimitiveOrStringType(field) == false) {
                result.put(field.getName(), field);
            }
        }

        return result;
    }

    private SortedMap<String, Field> getAllInjectableFieldsFromClass(
            final Object instance,
            final String name) {
        return getAllAnnotatedFieldsFromClass(
                instance, Manipulatable.class, name);
    }

    private SortedMap<String, Field> getAllReadableFieldsFromClass(
            final Object instance,
            final String name) {
        return getAllAnnotatedFieldsFromClass(
                instance, Readable.class, name);
    }

    /**************************************************************************
     *                Private array handling methods                          *
     **************************************************************************/

        private Field getAnnotatedArrayField(final Class<?> clazz,
            final String fieldName,
            final SortedMap<String, Field> annotatedFields)
            throws ClassNotFoundException, NoSuchFieldException {

        Field result;

        Field field = getAnnotatedField(clazz, fieldName, annotatedFields);
        Class<?> classOfField = field.getType();
        if (classOfField.isArray()) {
            result = field;
        } else {
            throw new IllegalArgumentException("Field \"" + fieldName
                    + "\" of Class \"" + clazz.getName()
                    + "\" is not an Array!");
        }
        return result;
    }

    private Field getInjectableArrayField(final Object instance,
            final String fieldName) throws ClassNotFoundException,
            NoSuchFieldException {

        Class<?> clazz = instance.getClass();
        return getAnnotatedArrayField(clazz, fieldName,
                registeredClasses.get(clazz.getName()).getInjectableFields());
    }

    private Field getReadableArrayField(final Object instance,
            final String fieldName) throws ClassNotFoundException,
            NoSuchFieldException {

        Field result = null;
        Class<?> clazz = instance.getClass();
        Field field = getReadableField(clazz, fieldName);
        Class<?> classOfField = field.getType();
        if (classOfField.isArray()) {
            result = field;
        } else {
            throw new IllegalArgumentException("Field \"" + fieldName
                    + "\" of Class \"" + clazz.getName()
                    + "\" is not an Array!");
        }
        return result;
    }

    /**************************************************************************
     *                Private method handling methods                         *
     **************************************************************************/

    private SortedMap<String, Method> getAllCallableMethodsFromClass(
            final Class<?> clazz) {

        SortedMap<String, Method> result = new TreeMap<String, Method>();

        result = getCallableMethodsFromClass(clazz);
        Class<?> clazz2 = clazz;
        while (clazz2.getSuperclass() != Object.class) {
            clazz2 = clazz2.getSuperclass();
            result.putAll(getCallableMethodsFromClass(clazz2));
        }
        LOG.info("Callable methods of class \"{}\": {}", clazz, result);

        return result;
    }

    private SortedMap<String, Method> getCallableMethodsFromClass(
            final Class<?> clazz) {

        SortedMap<String, Method> callableMethods
                = new TreeMap<String, Method>();

        Method[] method = clazz.getDeclaredMethods();
        if (method.length != 0) {
            for (Method m : method) {
                if (m.isAnnotationPresent(Callable.class) == true) {
                    callableMethods.put(m.getName(), m);
                }
            }
        }
        return callableMethods;
    }

    private Method getMethod(final Object instance, final String methodName)
            throws ClassNotFoundException, NoSuchMethodException {
        Class<?> clazz = instance.getClass();

        return getAnnotatedMethod(clazz, methodName,
                registeredClasses.get(clazz.getName()).getCallableMethods());
    }

    private Method getAnnotatedMethod(final Class<?> clazz,
            final String methodName,
            final SortedMap<String, Method> annotatedMethods)
            throws ClassNotFoundException, NoSuchMethodException {

        Method result = null;

        if (callableMethodExists(clazz, methodName)) {
            Method methodToCall = annotatedMethods.get(methodName);
            if (methodToCall != null) {
                methodToCall.setAccessible(true);
            }
            result = methodToCall;
        }

        LOG.info("Result: {}", result.toString());
        return result;
    }

    private boolean callableMethodExists(final Class<?> clazz,
            final String methodName) throws NoSuchMethodException {

        boolean result = true;

        try {
            result = annotatedMethodExists(clazz,
                   methodName,
                   registeredClasses.get(clazz.getName()).getCallableMethods());
        } catch (MethodIsNotCallableException ex) {
            result = false;
        }

        return result;
    }

    private boolean annotatedMethodExists(final Class<?> clazz,
            final String methodName, final SortedMap<String, Method> methods)
            throws NoSuchMethodException {

        boolean result = false;

        /** Check if this class is registered. */
        if (registeredClasses.containsKey(clazz.getName())) {
            /** Check if this class has the wanted manipulatable method. */
            if (methods.containsKey(methodName)) {
                result = true;
            } else {
                /* The following order is mandatory! If there exists no method
                 named methodName in the class a NoSuchMethodException is thrown
                 and the following MethodIsNotCallableException is never
                 reached!
                 If the method exists in the class a
                 MethodIsNotCallableException is thrown. */
                testClassForMethod(clazz, methodName);
                throw new MethodIsNotCallableException("Method \""
                        + methodName + "\" in class \"" + clazz.getName()
                        + "\" is not annotated with Callable!");
            }
        } else {
            throw new ClassIsNotAModelException("Class \"" + clazz.getName()
                    + "\" is not registered!");
        }
        return result;
    }

    @Deprecated
    private Method getExistingMethod(final Class<?> clazz,
            final String methodName)
            throws ClassNotFoundException, NoSuchMethodException {

        Method result;

        /** Check if this class is registered. */
        if (registeredClasses.containsKey(clazz.getName())) {
            SortedMap<String, Method> methods
                  = registeredClasses.get(clazz.getName()).getCallableMethods();
            /** Check if this class has the wanted callable method. */
            if (methods.containsKey(methodName)) {
                result = methods.get(methodName);
            } else {
                /* The following order is mandatory! If there exists no method
                 named methodName in the class a NoSuchMethodException is thrown
                 and the following MethodIsNotCallableException is never
                 reached!
                 If the field exists in the class a
                 MethodIsNotCallableException is thrown. */
                testClassForMethod(clazz, methodName);
                throw new MethodIsNotCallableException("Method \"" + methodName
                        + "\" in class \"" + clazz.getName()
                        + "\" is not annotated with Callable!");
            }
        } else {
            throw new ClassIsNotAModelException("Class \"" + clazz.getName()
                    + "\" is not registerd with the manipulator!");
        }
        return result;
    }

    private void testClassForMethod(Class<?> clazz, final String methodName)
            throws NoSuchMethodException {
        int hasMethod = 0;

        try {
            hasMethod++;
            clazz.getMethod(methodName);
        } catch (NoSuchMethodException ex) {
            hasMethod--;
        } catch (SecurityException ex) {
            LOG.error("Exception: ", ex);
        }

        while (clazz.getSuperclass() != Object.class) {
                clazz = clazz.getSuperclass();
            try {
                hasMethod++;
                clazz.getMethod(methodName);
            } catch (NoSuchMethodException ex) {
                hasMethod--;
            } catch (SecurityException ex) {
                LOG.error("Exception: ", ex);
            }
        }

        if (hasMethod <= 0) {
            throw new NoSuchMethodException("Method \"" + methodName
                    + "\" of class \"" + clazz + "\" does not exist!");
        }
    }

    /**************************************************************************
     *                Private general methods                                 *
     **************************************************************************/

    private boolean annotatedFieldExists(final Class<?> clazz,
            final String fieldName, final SortedMap<String, Field> fields)
            throws FieldIsNotManipulatableException,
            ClassIsNotAModelException, NoSuchFieldException {

        boolean result = false;

        /** Check if this class is registered. */
        if (registeredClasses.containsKey(clazz.getName())) {
            /** Check if this class has the wanted manipulatable field. */
            if (fields.containsKey(fieldName)) {
                result = true;
            } else {
                /* The following order is mandatory! If there exists no field
                 named fieldName in the class a NoSuchFieldException is thrown
                 and the following FieldIsNotManipulatableException is never
                 reached!
                 If the field exists in the class a
                 FieldIsNotManipulatableException is thrown.*/
                testClassForField(clazz, fieldName);
                throw new FieldIsNotAnnotatedException("Field \""
                        + fieldName + "\" in class \"" + clazz.getName()
                        + "\" is not annotated!");
            }
        } else {
            throw new ClassIsNotAModelException("Class \"" + clazz.getName()
                    + "\" is not registered!");
        }
        return result;
    }

    private void testClassForField(final Class<?> clazz, final String fieldName)
            throws NoSuchFieldException {
        Class<?> clazz2;
        int hasField = 0;

        try {
            hasField++;
            clazz.getDeclaredField(fieldName);
        } catch (NoSuchFieldException ex) {
            hasField--;
        } catch (SecurityException ex) {
            LOG.error("Exception: ", ex);
        }

        clazz2 = clazz;
        while (clazz2.getSuperclass() != Object.class) {
                clazz2 = clazz2.getSuperclass();
            try {
                hasField++;
                clazz2.getDeclaredField(fieldName);
            } catch (NoSuchFieldException ex) {
                hasField--;
            } catch (SecurityException ex) {
                LOG.error("Exception: ", ex);
            }
        }

        if (hasField <= 0) {
            throw new NoSuchFieldException("Field \"" + fieldName
                    + "\" of class \"" + clazz.getName()
                    + "\" does not exist!");
        }
    }

    private boolean readableFieldExists(final Class<?> clazz,
            final String fieldName)
            throws FieldIsNotManipulatableException,
            ClassIsNotAModelException, NoSuchFieldException {

        boolean resultInjectatable = false;
        boolean resultReadable = false;
        boolean errorConditionInjectable = false;
        boolean errorConditionReadable = false;

        try {
            resultInjectatable = annotatedFieldExists(clazz,
                    fieldName,
                    registeredClasses.get(clazz.getName()).getReadableFields());
        } catch (FieldIsNotAnnotatedException ex) {
            errorConditionInjectable = true;
        }

        try {
            resultReadable = annotatedFieldExists(clazz,
                    fieldName,
                  registeredClasses.get(clazz.getName()).getInjectableFields());
        } catch (FieldIsNotAnnotatedException ex) {
            errorConditionReadable = true;
        }

        if (errorConditionInjectable == true) {
            if (errorConditionReadable == true) {
                throw new ManipulationException(
                        "Field \"" + fieldName + "\" in class \""
                        + clazz.getName()
                        + "\" is neither annotated with Manipulatable nor with "
                        + "Readable!");
            }
        }

        return resultInjectatable || resultReadable;
    }

    private Field getAnnotatedField(final Class<?> clazz,
            final String fieldName,
            final SortedMap<String, Field> annotatedFields)
            throws NoSuchFieldException {

        Field result = null;

        if (readableFieldExists(clazz, fieldName)) {
            Field fieldToInject = annotatedFields.get(fieldName);
            if (fieldToInject != null) {
                fieldToInject.setAccessible(true);
            }
            result = fieldToInject;
        }

        return result;
    }

    private Field getInjectableField(final Object instance,
            final String fieldName) throws NoSuchFieldException {

        Class<?> clazz = instance.getClass();
        return getAnnotatedField(clazz, fieldName,
                registeredClasses.get(clazz.getName()).getInjectableFields());
    }

    private Field getReadableField(final Class<?> clazz, final String fieldName)
            throws NoSuchFieldException {

        return getAnnotatedField(clazz, fieldName,
                registeredClasses.get(clazz.getName()).getReadableFields());
    }

    /**
     *
     * @param instance
     * @param fieldName
     * @return
     * @throws NoSuchFieldException
     */
    private Field getReadableOrInjectableField(final Object instance,
            final String fieldName) throws NoSuchFieldException {

        Field result = null;
        Field resultReadable = null;

        Class<?> clazz = instance.getClass();
        resultReadable = getReadableField(clazz, fieldName);
        if (resultReadable == null) {
            result = getInjectableField(instance, fieldName);
        } else {
            result = resultReadable;
        }

        return result;
    }

    private boolean isInstanceRegistered(final Object instance) {
        boolean result = false;

        if (instances.containsValue(instance)) {
            result = true;
        }

        return result;
    }

    private int getSizeOfArray(final Object instance, final String fieldName)
            throws IllegalAccessException, ClassIsNotAModelException,
            FieldIsNotManipulatableException, NoSuchFieldException,
            ClassNotFoundException {

        int result = 0;
        Class<?> clazz = instance.getClass();

        if (readableFieldExists(clazz, fieldName)) {
            SortedMap<String, Field> fields
                 = registeredClasses.get(clazz.getName()).getInjectableFields();
            Field fieldToRead = fields.get(fieldName);
            if (fieldToRead == null) {
                SortedMap<String, Field> readableFields
                   = registeredClasses.get(clazz.getName()).getReadableFields();
                fieldToRead = readableFields.get(fieldName);
            }
            Class<?> typeOfField = fieldToRead.getType();
            if (typeOfField.isArray()) {
                fieldToRead.setAccessible(true);
                result = Array.getLength(fieldToRead.get(instance));
            }
        }
        return result;
    }

    /**************************************************************************
     *                Public methods                                          *
     **************************************************************************/

    public void registerInstance(final String name, final Object instance) {
        if (name == null) {
            LOG.info("The name of the instance cannot be null!");
            throw new ManipulationException(
                    "The name of the instance cannot be null!");
        } else if (instances.containsKey(name)) {
            Object registeredInstance = instances.get(name);
            LOG.info("An instance {} of class {} named {} is already "
                    + "registered. "
                    + "Therefore the instance {} of class {} cannot be "
                    + "registered using the same name!",
                new Object[]{registeredInstance, registeredInstance.getClass(),
                name, instance, instance.getClass()});
        } else {
            LOG.info("Registering instance {} of class {}.",
                    instance, instance.getClass());
            updateClassInformation(instance.getClass(), name, instance);

            instances.put(name, instance);
        }
    }

    public void unRegisterInstance(final String name, final Object instance) {
        instances.remove(name);

        removeClassInformation(instance.getClass());
    }

    public boolean isRegisteredInstance(final Object instance) {
        return instances.containsValue(instance);
    }

    public boolean isRegisteredInstance(final String name) {
        return instances.containsKey(name);
    }

    public SortedMap<String, ClassInformation> getRegisteredClasses() {
        return Collections.unmodifiableSortedMap(registeredClasses);
    }

    public SortedMap<String, Object> getRegisterdInstances() {
        return Collections.unmodifiableSortedMap(instances);
    }

    public Object callMethod(final Object instance, final String methodName,
            final Object... parameters)
        throws ClassNotFoundException, IllegalAccessException,
        IllegalArgumentException, InvocationTargetException,
        NoSuchMethodException {

        return getMethod(instance, methodName).invoke(instance, parameters);
    }

    public Object get(final Object instance, final String fieldName)
            throws IllegalAccessException, ClassNotFoundException,
            NoSuchFieldException {

        return getReadableOrInjectableField(
                instance, fieldName).get(instance);
    }

    public void setPort(final Object instance, final String fieldName,
            final Object value)
            throws IllegalAccessException, ClassNotFoundException,
            NoSuchFieldException {

        Class<?> clazz = instance.getClass();
        Field field = clazz.getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(instance, value);
    }

    /**
     * Returns the value of a manipulatable boolean variable.
     * @param instance Instance which has the variable.
     * @param fieldName
     * @return The value of the manipulate variable with the name
     * <i>fieldName</i>.
     * @throws java.lang.IllegalArgumentException
     * @throws java.lang.IllegalAccessException
     * @throws NoSuchFieldException
     * @throws ClassNotFoundException
     */
    public boolean getBoolean(final Object instance, final String fieldName)
            throws IllegalAccessException, ClassNotFoundException,
            NoSuchFieldException {

        return getReadableOrInjectableField(
                instance, fieldName).getBoolean(instance);
    }

    public byte getByte(final Object instance, final String fieldName)
            throws IllegalAccessException, ClassNotFoundException,
            NoSuchFieldException {

        return getReadableOrInjectableField(
                instance, fieldName).getByte(instance);
    }

    public char getChar(final Object instance, final String fieldName)
            throws IllegalAccessException, ClassNotFoundException,
            NoSuchFieldException {

        return getReadableOrInjectableField(
                instance, fieldName).getChar(instance);
    }

    public short getShort(final Object instance, final String fieldName)
            throws IllegalAccessException, ClassNotFoundException,
            NoSuchFieldException {

        return getReadableOrInjectableField(
                instance, fieldName).getShort(instance);
    }

    public int getInt(final Object instance, final String fieldName)
            throws IllegalAccessException, ClassNotFoundException,
            NoSuchFieldException {

        return getReadableOrInjectableField(
                instance, fieldName).getInt(instance);
    }

    public long getLong(final Object instance, final String fieldName)
            throws IllegalAccessException, ClassNotFoundException,
            NoSuchFieldException {

        return getReadableOrInjectableField(
                instance, fieldName).getLong(instance);
    }

    public float getFloat(final Object instance, final String fieldName)
            throws IllegalAccessException, ClassNotFoundException,
            NoSuchFieldException {

        return getReadableOrInjectableField(
                instance, fieldName).getFloat(instance);
    }

    public double getDouble(final Object instance, final String fieldName)
            throws IllegalAccessException, ClassNotFoundException,
            NoSuchFieldException {

        return getReadableOrInjectableField(
                instance, fieldName).getDouble(instance);
    }

    public String getString(final Object instance, final String fieldName)
            throws IllegalAccessException, ClassNotFoundException,
            NoSuchFieldException {

        String result = null;

        Field field = getReadableOrInjectableField(instance, fieldName);
        Class<?> typeOfField = field.getType();
        if (typeOfField.equals(String.class)) {
            result = (String) field.get(instance);
        } else {
            throw new IllegalArgumentException("Field \"" + fieldName + "\""
                    + "of Class \"" + instance.getClass().getName()
                    + "is not a String!");
        }
        return result;
    }

    public String getAsString(final Object instance, final String fieldName)
            throws IllegalAccessException, ClassNotFoundException,
            NoSuchFieldException {

        String result = null;

        Field field = getReadableOrInjectableField(instance, fieldName);
        Class<?> typeOfField = field.getType();
        if (typeOfField.equals(boolean.class)) {
            result = Boolean.toString(getBoolean(instance,
                fieldName));
        } else if (typeOfField.equals(byte.class)) {
            result = Byte.toString(getByte(instance,
                fieldName));
        } else if (typeOfField.equals(char.class)) {
            result = Character.toString(getChar(instance,
                fieldName));
        } else if (typeOfField.equals(short.class)) {
            result = Short.toString(getShort(instance, fieldName));
        } else if (typeOfField.equals(int.class)) {
            result = Integer.toString(getInt(instance, fieldName));
        } else if (typeOfField.equals(long.class)) {
            result = Long.toString(getLong(instance, fieldName));
        } else if (typeOfField.equals(float.class)) {
            result = Float.toString(getFloat(instance, fieldName));
        } else if (typeOfField.equals(double.class)) {
            result = Double.toString(getDouble(instance,
                fieldName));
        } else if (typeOfField.equals(String.class)) {
            result = getString(instance, fieldName);
        } else {
            result = field.get(fieldName).toString();
        }
        return result;
    }

    public boolean getArrayBoolean(final Object instance,
            final String fieldName, final int index)
            throws IllegalAccessException, ClassNotFoundException,
            NoSuchFieldException {

        Field field = getReadableOrInjectableField(instance, fieldName);

        return Array.getBoolean(field.get(instance), index);
    }

    public byte getArrayByte(final Object instance,
            final String fieldName, final int index)
            throws IllegalAccessException, ClassNotFoundException,
            NoSuchFieldException {

        Field field = getReadableOrInjectableField(instance, fieldName);

        return Array.getByte(field.get(instance), index);
    }

    public char getArrayChar(final Object instance,
            final String fieldName, final int index)
            throws IllegalAccessException, ClassNotFoundException,
            NoSuchFieldException {

        Field field = getReadableOrInjectableField(instance, fieldName);

        return Array.getChar(field.get(instance), index);
    }

    public short getArrayShort(final Object instance,
            final String fieldName, final int index)
            throws IllegalAccessException, ClassNotFoundException,
            NoSuchFieldException {

        Field field = getReadableOrInjectableField(instance, fieldName);

        return Array.getShort(field.get(instance), index);
    }

    public int getArrayInt(final Object instance,
            final String fieldName, final int index)
            throws IllegalAccessException, ClassNotFoundException,
            NoSuchFieldException {

        Field field = getReadableOrInjectableField(instance, fieldName);

        return Array.getInt(field.get(instance), index);
    }

    public long getArrayLong(final Object instance,
            final String fieldName, final int index)
            throws IllegalAccessException, ClassNotFoundException,
            NoSuchFieldException {

        Field field = getReadableOrInjectableField(instance, fieldName);

        return Array.getLong(field.get(instance), index);
    }

    public float getArrayFloat(final Object instance,
            final String fieldName, final int index)
            throws IllegalAccessException, ClassNotFoundException,
            NoSuchFieldException {

        Field field = getReadableOrInjectableField(instance, fieldName);

        return Array.getFloat(field.get(instance), index);
    }

    public double getArrayDouble(final Object instance,
            final String fieldName, final int index)
            throws IllegalAccessException, ClassNotFoundException,
            NoSuchFieldException {

        Field field = getReadableOrInjectableField(instance, fieldName);

        return Array.getDouble(field.get(instance), index);
    }

    public String getArrayAsString(final Object instance,
            final String fieldName, final int index)
            throws IllegalAccessException, ClassNotFoundException,
            NoSuchFieldException {

        String result = null;

        Field field = getReadableOrInjectableField(instance, fieldName);
        Class<?> typeOfField = field.getType();
        if (typeOfField.equals(boolean.class)) {
            result = Boolean.toString(
                Array.getBoolean(field.get(instance), index));
        } else if (typeOfField.equals(byte[].class)) {
            result = Byte.toString(
                Array.getByte(field.get(instance), index));
        } else if (typeOfField.equals(char[].class)) {
            result = Character.toString(
                Array.getChar(field.get(instance), index));
        } else if (typeOfField.equals(short[].class)) {
            result = Short.toString(
                Array.getShort(field.get(instance), index));
        } else if (typeOfField.equals(int[].class)) {
            result = Integer.toString(
                Array.getInt(field.get(instance), index));
        } else if (typeOfField.equals(long[].class)) {
            result = Long.toString(
                Array.getLong(field.get(instance), index));
        } else if (typeOfField.equals(float[].class)) {
            result = Float.toString(
                Array.getFloat(field.get(instance), index));
        } else if (typeOfField.equals(double[].class)) {
            result = Double.toString(
                Array.getDouble(field.get(instance), index));
        } else {
            result = field.get(fieldName).toString();
        }
        return result;
    }

    /**
     * Sets a manipulatable boolean variable.
     *
     * @param instance Instance of model.
     * @param fieldName
     * @param value
     * @throws java.lang.ClassNotFoundException
     * @throws java.lang.IllegalArgumentException
     * @throws NoSuchFieldException
     * @throws java.lang.IllegalAccessException
     */
    public void setBoolean(final Object instance, final String fieldName,
            final boolean value) throws IllegalAccessException,
            ClassNotFoundException, NoSuchFieldException {

        getInjectableField(instance, fieldName).setBoolean(instance, value);
    }

    public void setByte(final Object instance, final String fieldName,
            final byte value) throws IllegalAccessException,
            ClassNotFoundException, NoSuchFieldException {

        getInjectableField(instance, fieldName).setByte(instance, value);
    }

    public void setChar(final Object instance, final String fieldName,
            final char value) throws IllegalAccessException,
            ClassNotFoundException, NoSuchFieldException {

        getInjectableField(instance, fieldName).setChar(instance, value);
    }

    public void setShort(final Object instance, final String fieldName,
            final short value) throws IllegalAccessException,
            ClassNotFoundException, NoSuchFieldException {

        getInjectableField(instance, fieldName).setShort(instance, value);
    }

    public void setInt(final Object instance, final String fieldName,
            final int value) throws IllegalAccessException,
            ClassNotFoundException, NoSuchFieldException {

        getInjectableField(instance, fieldName).setInt(instance, value);
    }

    public void setLong(final Object instance, final String fieldName,
            final long value) throws IllegalAccessException,
            ClassNotFoundException, NoSuchFieldException {

        getInjectableField(instance, fieldName).setLong(instance, value);
    }

    public void setFloat(final Object instance, final String fieldName,
            final float value) throws IllegalAccessException,
            ClassNotFoundException, NoSuchFieldException {

        getInjectableField(instance, fieldName).setFloat(instance, value);
    }

    public void setDouble(final Object instance, final String fieldName,
            final double value) throws IllegalAccessException,
            ClassNotFoundException, NoSuchFieldException {

        getInjectableField(instance, fieldName).setDouble(instance, value);
    }

    public void setString(final Object instance, final String fieldName,
            final String value)
            throws IllegalAccessException, ClassNotFoundException,
            NoSuchFieldException {

        Field field = getInjectableField(instance, fieldName);
        Class<?> typeOfField = field.getType();
        if (typeOfField.equals(String.class)) {
            field.set(instance, value);
        } else {
            throw new IllegalArgumentException("Field \"" + fieldName + "\""
                + "of Class \"" + instance.getClass().getName()
                + "is not a String!");
        }
    }

    public void setFromString(final Object instance, final String fieldName,
            final String value) throws IllegalAccessException,
            ClassNotFoundException, NoSuchFieldException,
            IllegalArgumentException {

        Class<?> typeOfField =
            getInjectableField(instance, fieldName).getType();
        if (typeOfField.equals(boolean.class)) {
            if (Validator.validateBooleanInput(value)) {
                setBoolean(instance, fieldName, Boolean.parseBoolean(value));
            }
        } else if (typeOfField.equals(byte.class)) {
            if (Validator.validateByteInput(value)) {
                setByte(instance, fieldName, Byte.parseByte(value));
            }
        } else if (typeOfField.equals(char.class)) {
            if (Validator.validateCharInput(value)) {
                setChar(instance, fieldName, value.charAt(0));
            }
        } else if (typeOfField.equals(short.class)) {
            if (Validator.validateCharInput(value)) {
                setShort(instance, fieldName, Short.parseShort(value));
            }
        } else if (typeOfField.equals(int.class)) {
            setInt(instance, fieldName, Integer.parseInt(value));
        } else if (typeOfField.equals(long.class)) {
            setLong(instance, fieldName, Long.parseLong(value));
        } else if (typeOfField.equals(float.class)) {
            setFloat(instance, fieldName, Float.parseFloat(value));
        } else if (typeOfField.equals(double.class)) {
             setDouble(instance, fieldName, Double.parseDouble(value));
        } else if (typeOfField.equals(String.class)) {
            setString(instance, fieldName, value);
        } else {
            throw new IllegalArgumentException("Value \"" + value
                + "\" cannot be set to field \""
                + getInjectableField(instance, fieldName) + "\"!");
        }
    }

    public void set(final Object instance, final String fieldName,
            final Object value) throws IllegalAccessException,
            ClassNotFoundException, NoSuchFieldException {

        getInjectableField(instance, fieldName).set(instance, value);
    }

    public void setArrayBoolean(final Object instance, final String fieldName,
            final boolean value, final int index)
            throws IllegalAccessException, ClassNotFoundException,
            NoSuchFieldException {

        Array.setBoolean(
            getInjectableArrayField(instance, fieldName).get(instance),
            index, value);
    }

    public void setArrayByte(final Object instance, final String fieldName,
            final byte value, final int index)
            throws IllegalAccessException, ClassNotFoundException,
            NoSuchFieldException {

        Array.setByte(
            getInjectableArrayField(instance, fieldName).get(instance),
            index,
            value);
    }

    public void setArrayChar(final Object instance, final String fieldName,
            final char value, final int index)
            throws IllegalAccessException, ClassNotFoundException,
            NoSuchFieldException {

        Array.setChar(
            getInjectableArrayField(instance, fieldName).get(instance),
            index,
            value);
    }

    public void setArrayShort(final Object instance, final String fieldName,
            final short value, final int index)
            throws IllegalAccessException, ClassNotFoundException,
            NoSuchFieldException {

        Array.setShort(
            getInjectableArrayField(instance, fieldName).get(instance),
            index,
            value);
    }

    public void setArrayInt(final Object instance, final String fieldName,
            final int value, final int index)
            throws IllegalAccessException, ClassNotFoundException,
            NoSuchFieldException {

        Array.setInt(
            getInjectableArrayField(instance, fieldName).get(instance),
            index,
            value);
    }

    public void setArrayLong(final Object instance, final String fieldName,
            final long value, final int index)
            throws IllegalAccessException, ClassNotFoundException,
            NoSuchFieldException {

        Array.setLong(
            getInjectableArrayField(instance, fieldName).get(instance),
            index,
            value);
    }

    public void setArrayFloat(final Object instance, final String fieldName,
            final float value, final int index)
            throws IllegalAccessException, ClassNotFoundException,
            NoSuchFieldException {

        Array.setFloat(
            getInjectableArrayField(instance, fieldName).get(instance),
            index,
            value);
    }

    public void setArrayDouble(final Object instance, final String fieldName,
            final double value, final int index)
            throws IllegalAccessException, ClassNotFoundException,
            NoSuchFieldException {

        Array.setDouble(
            getInjectableArrayField(instance, fieldName).get(instance),
            index,
            value);
    }

    /**
     * Works only with one-dimensional arrays!
     *
     * @param instance
     * @param fieldName
     * @param value
     * @param index
     * @throws java.lang.IllegalArgumentException
     * @throws java.lang.IllegalAccessException
     * @throws NoSuchFieldException
     * @throws java.lang.ClassNotFoundException
     * @throws FieldIsNotManipulatableException
     * @throws ClassIsNotAModelException
     */
    public void setArrayFromString(final Object instance,
            final String fieldName, final String value, final int index)
            throws IllegalAccessException, ClassNotFoundException,
            NoSuchFieldException {

        Field fieldToInject =
            getInjectableArrayField(instance, fieldName);

        Class<?> typeOfField = fieldToInject.getType();
        if (typeOfField.isArray()) {
            if (typeOfField.equals(boolean[].class)) {
                if (Validator.validateBooleanInput(value)) {
                    Array.setBoolean(fieldToInject.get(instance),
                            index,
                            Boolean.parseBoolean(value));
                }
            } else if (typeOfField.equals(byte[].class)) {
                Array.setByte(
                    fieldToInject.get(instance),
                    index,
                    Byte.parseByte(value));
            } else if (typeOfField.equals(char[].class)) {
                Array.setChar(
                    fieldToInject.get(instance),
                    index,
                    value.charAt(0));
            } else if (typeOfField.equals(short[].class)) {
                Array.setShort(
                    fieldToInject.get(instance),
                    index,
                    Short.parseShort(value));
            } else if (typeOfField.equals(int[].class)) {
                Array.setInt(
                    fieldToInject.get(instance),
                    index,
                    Integer.parseInt(value));
            } else if (typeOfField.equals(long[].class)) {
                Array.setLong(
                    fieldToInject.get(instance),
                    index,
                    Long.parseLong(value));
            } else if (typeOfField.equals(float[].class)) {
                Array.setFloat(
                    fieldToInject.get(instance),
                    index,
                    Float.parseFloat(value));
            } else if (typeOfField.equals(double[].class)) {
                Array.setDouble(
                    fieldToInject.get(instance),
                    index,
                    Double.parseDouble(value));
            } else {
                throw new IllegalArgumentException("Value is not a primitive"
                        + " type!");
            }
        }
    }

    /**
     * This method sets the value of an array. It is capable of separating the
     * value string into its pieces. It ignores line breaks and line feeds. The
     * delimeter is fixed to a space.
     * @param instance
     * @param fieldName
     * @param length
     * @param value
     * @throws java.lang.IllegalArgumentException
     * @throws java.lang.IllegalAccessException
     * @throws NoSuchFieldException
     * @throws java.lang.ClassNotFoundException
     */
    public void setArray(final Object instance, final String fieldName,
            String value, final int length)
            throws IllegalAccessException, ClassNotFoundException,
            NoSuchFieldException {

        /** Check if the length declared in the XML file and in the class is
         consistent. */
        int sizeOfArray = getSizeOfArray(instance, fieldName);
        if (sizeOfArray != length) {
            throw new IllegalArgumentException("Size of array mismatch!"
                + " XML file: " + length + ". Class: " + sizeOfArray);
        }
        /** The else is intentionally left out to trigger the additional
         * exceptions. */

        /** Filter out the line breaks and line feeds. */
        value = value.replace("\n", "");
        value = value.replace("\r", "");
        String[] tokens = value.split(" ");

        int currentPosition = 0;
        for (int i = 0; i < tokens.length; i++) {
            setArrayFromString(
                instance, fieldName, tokens[i], currentPosition);
            currentPosition++;
        }
    }

    public Object getInstance(final String name) {
        return instances.get(name);
    }

    public String getName(final Object instance) {
        Set<Entry<String, Object>> entries = instances.entrySet();
        String result = null;

        for (Entry<String, Object> entry : entries) {
            if (entry.getValue().equals(instance)) {
                result = entry.getKey();
            }
        }

        return result;
    }

//    private static boolean implementsInterface(final Class<?> clazz,
//            final Class<?> implementedInterface) {
//        boolean result = false;
//        List<Class<?>> interfacesList = new ArrayList<Class<?>>();
//
//        Class<?>[] interfaces = clazz.getInterfaces();
//        interfacesList.addAll(Arrays.asList(interfaces));
//        Class<?> clazz2 = clazz;
//        Class<?> superClass = clazz2.getSuperclass();
//        if (superClass != null) {
//            while (clazz2.getSuperclass() != Object.class) {
//                clazz2 = clazz2.getSuperclass();
//                interfaces = clazz2.getInterfaces();
//                interfacesList.addAll(Arrays.asList(interfaces));
//            }
//        }
//
//        if (interfacesList.isEmpty() == false) {
//            for (Class<?> interfaceClazz : interfacesList) {
//                if (interfaceClazz.equals(implementedInterface)) {
//                    result = true;
//                }
//            }
//        }
//
//        return result;
//    }
//
//    private static Field getFieldFromSuperClasses(final Class<?> superClazz,
//            final String fieldName) {
//
//        Field result = null;
//        try {
//            result = superClazz.getDeclaredField(fieldName);
//        } catch (NoSuchFieldException ex) {
//            Class<?> clazz = superClazz.getSuperclass();
//            result = getFieldFromSuperClasses(clazz, fieldName);
//        }
//
//        return result;
//    }

    public boolean isFieldOfPrimitiveOrStringType(final Field field) {
        boolean result = false;
        String nameOfType;

        Class<?> typeOfField = field.getType();

        if (typeOfField.isPrimitive()) {
            result = true;
        } else if (typeOfField.equals(String.class)) {
            result = true;
        } else if (typeOfField.isArray()) {
            nameOfType = typeOfField.getName();
            String strippedNameOfType = nameOfType.replaceAll("]", "");
            if (strippedNameOfType.indexOf("L") == 0) {
                LOG.info("The array {} is neither primitive nor String.",
                        nameOfType);
            } else {
                result = true;
            }
        }

        return result;
    }

}
