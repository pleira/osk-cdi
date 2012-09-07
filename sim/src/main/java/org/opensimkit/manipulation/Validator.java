/*
 * Validator.java
 *
 * Created on 20. September 2008, 15:32
 *
 *  A class to validate input for the Manipulator.
 *
 *-----------------------------------------------------------------------------
 * Modification History:
 *
 *  2008-09-20
 *      File created - A. Brandt:
 *      Initial version.
 *
 *      File under GPL - see OpenSimKit Documentation.
 *
 *      No warranty and liability for correctness by author.
 *
 */
package org.opensimkit.manipulation;

/**
 * A class to validate input for the Manipulator.
 * @author A. Brandt
 * @version 1.1
 * @since 2.4.6
 */
public final class Validator {

    /**
     * This class shall never be instantiated, because it consists only of
     * static methods.
     */
    private Validator() {

    }

    public static boolean validateBooleanInput(final String booleanInput) {
        boolean result = false;

        if ((booleanInput.equalsIgnoreCase("true"))
                || (booleanInput.equalsIgnoreCase("false"))) {
            result = true;
        } else {
            throw new IllegalArgumentException("The argument \"" + booleanInput
                    + " \" for the boolean field is illegal! The only "
                    + "legal values are \"true\" and \"false\".");
        }

        return result;
    }

    public static boolean validateByteInput(final String byteInput) {
        boolean result = false;

        /* To check for non-number strings use validateIntInput()! */
        validateIntInput(byteInput);
        int i = Integer.parseInt(byteInput);
        if (i < Byte.MIN_VALUE || i > Byte.MAX_VALUE) {
            throw new NumberFormatException(
                    "Byte value out of range. Value: \"" + byteInput
                    + "\" must be between \"" + Byte.MIN_VALUE
                    + "\" and \"" + Byte.MAX_VALUE + "\"!");
        } else {
            result = true;
        }

        return result;
    }

    public static boolean validateCharInput(final String charInput) {
        boolean result = false;

        if (charInput.length() > 1) {
            throw new IllegalArgumentException("The char is too long! It has "
                    + charInput.length() + " characters, but must only have 1"
                    + " character!");
        } else {
            result = true;
        }
        return result;
    }

    public static boolean validateShortInput(final String shortInput) {
        boolean result = false;

        /* To check for non-number strings use validateIntInput()! */
        validateIntInput(shortInput);
        int i = Integer.parseInt(shortInput);
    if (i < Short.MIN_VALUE || i > Short.MAX_VALUE) {
            throw new NumberFormatException(
                    "Short value out of range. Value: \"" + shortInput
                    + "\" must be between \"" + Short.MIN_VALUE
                    + "\" and \"" + Short.MAX_VALUE + "\"!");
        } else {
            result = true;
        }

        return result;
    }

    public static boolean validateIntInput(final String intInput) {
        int result = 0;
        int radix = 10;
        boolean negative = false;
        int i = 0, max = intInput.length();
        int limit;
        int multmin;
        int digit;

        if (max > 0) {
            if (intInput.charAt(0) == '-') {
                negative = true;
                limit = Integer.MIN_VALUE;
                i++;
            } else {
                limit = -Integer.MAX_VALUE;
            }
            multmin = limit / radix;
            if (i < max) {
                digit = Character.digit(intInput.charAt(i++), radix);
                if (digit < 0) {
                    throwNumberFormatException(intInput);
                } else {
                    result = -digit;
                }
            }
            while (i < max) {
                // Accumulating negatively avoids surprises near MAX_VALUE
                digit = Character.digit(intInput.charAt(i++), radix);
                if (digit < 0) {
                    throwNumberFormatException(intInput);
                }
                if (result < multmin) {
                    throwNumberFormatException(intInput);
                }
                result *= radix;
                if (result < limit + digit) {
                    throwNumberFormatException(intInput);
                }
                result -= digit;
            }
        } else {
            throwNumberFormatException(intInput);
        }
        if (negative) {
            if (i > 1) {
                return true;
            } else {    /* Only got "-" */
                throwNumberFormatException(intInput);
            }
        } else {
            return true;
        }
            return true;
    }

    //TODO Extend the Validator to validate also ints completely and longs,
    //floats and doubles

    private static void throwNumberFormatException(final String string) {
        String message = "The string \"" + string + "\" is not a number!";
        throw new NumberFormatException(message);
    }
}
