/*
 * Manipulatable.java
 *
 * Created on 17. Juli 2008, 21:32
 *
 *  A class to manipulate special declared fields in the OpenSimKit models.
 *
 *-----------------------------------------------------------------------------
 * Modification History:
 *
 *  2008-07-17
 *      File created - A. Brandt:
 *      Initial version to help keeping the complexity of the simulator models
 *      low.
 *
 *      File under GPL - see OpenSimKit Documentation.
 *
 *      No warranty and liability for correctness by author.
 *
 */
package org.opensimkit.manipulation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marker annotation to denote a field of a {@link org.opensimkit.Model} as
 * manipulatable.
 *
 * @author A. Brandt
 * @version 1.0
 * @since 2.4.4
 */
    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.FIELD})
    public @interface Manipulatable {

    }
