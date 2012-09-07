/*
 * ClassInformation.java
 *
 * Created on 21. April 2009
 *
 * Immutable class which acts as a container for use with the manipulator.
 *
 *-----------------------------------------------------------------------------
 * Modification History:
 *
 *  2009-04-21
 *      File created - A. Brandt:
 *      Initial version.
 *
 *-----------------------------------------------------------------------------
 *
 *      File under GPL - see OpenSimKit Documentation.
 *
 *      No warranty and liability for correctness by author.
 *
 */
package org.opensimkit.manipulation;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.SortedMap;

/**
 * Immutable class which acts as a container for use with the manipulator.
 *
 * @author A. Brandt
 * @version 1.1
 * @since 2.6.0
 */
public final class ClassInformation {
    private final SortedMap<String, Field> readableFields;
    private final SortedMap<String, Field> injectableFields;
    private final SortedMap<String, Method> callableMethods;
    private final int useCount;

    public ClassInformation(final SortedMap<String, Field> readableFields,
            final SortedMap<String, Field> injectableFields,
            final SortedMap<String, Method> callableMethods,
            final int useCount) {

        this.readableFields = readableFields;
        this.injectableFields = injectableFields;
        this.callableMethods = callableMethods;
        this.useCount = useCount;
    }

    public SortedMap<String, Field> getReadableFields() {
        return Collections.unmodifiableSortedMap(readableFields);
    }

    public SortedMap<String, Field> getInjectableFields() {
        return Collections.unmodifiableSortedMap(injectableFields);
    }

    public SortedMap<String, Method> getCallableMethods() {
        return Collections.unmodifiableSortedMap(callableMethods);
    }

    /**
     * Used for reference counting inside the manipulator.
     * @return Use count of this class.
     */
    public int getUseCount() {
        return useCount;
    }

}
