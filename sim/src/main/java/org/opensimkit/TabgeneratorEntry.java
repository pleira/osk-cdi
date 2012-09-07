/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.opensimkit;

import java.util.Locale;

/**
 * Mutable class to hold the information of an output file entry.
 *
 * @author A. Brandt
 */
public final class TabgeneratorEntry {
    private static final char SEPARATION_CHAR = '-';
    private final Object instance;
    private final String variable;
    private final String header;
    private final String format;
    private       int    alignmentSpaces;

    public TabgeneratorEntry(final Object instance, final String name,
            final String variable, final String header, final String format,
            final int alignmentSpaces) {
        this.instance = instance;
        this.variable = variable;
        this.alignmentSpaces = alignmentSpaces;

        if (format != null) {
            this.format = format;
        } else {
            this.format = "%s";
        }

        if (header != null) {
            this.header   = header;
        } else  {
            StringBuffer column = new StringBuffer();

            column.append(name);
            column.append(SEPARATION_CHAR);
            column.append(variable);

            this.header = column.toString().toUpperCase(Locale.ENGLISH);
        }
    }

    public void setAlignmentSpaces(final int newAlignmentSpaces) {
        alignmentSpaces = newAlignmentSpaces;
    }

    public Object getInstance() {
        return instance;
    }

    public String getVariable() {
        return variable;
    }

    public String getHeader() {
        return header;
    }

    public String getFormat() {
        return format;
    }

    public int getAlignmentSpaces() {
        return alignmentSpaces;
    }
}
