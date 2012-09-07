/*
 *  PSTable.java
 *
 *  Provider-Subscriber-Table class for permitting models to subscribe
 *  variables computed by other model.
 *  This mechanism is used for data interchange between models which have no
 *  physical line connection (data line, power line, fluid pipe etc.)
 *
 *  Demonstrator created on 16. February 2009
 *
 *
 *-----------------------------------------------------------------------------
 *  Modification History:
 *
 *  2009-02-20
 *      File created by A. Brandt
 *
 *  2011-01
 *      Integrated PSTable into OSK V3.7.0.
 *      J. Eickhoff
 *
 *-----------------------------------------------------------------------------
 *
 *      File under GPL  see OpenSimKit Documentation.
 *
 *      No warranty and liability for correctness by authors.
 *
 *
 *-----------------------------------------------------------------------------
*/

package org.opensimkit.providerSubscriber;

import java.util.Arrays;


/**
 * Provider-Subscriber-Table class for permitting models to subscribe
 * variables computed by other model.
 * This mechanism is used for data interchange between models which have no
 * physical line connection (data line, power line, fluid pipe etc.)
 *
 * @author A. Brandt
 * @version 1.0
 * @since 3.7.0
 */

public final class PSTable {
    private static final int INCREMENT = 10;
    private final int size;
    private final String names[];
    private final String providerModels[];
    private final String providerVariables[];
    private final String subscriberModels[];
    private final String subscriberVariables[];
    private int position = 0;

    public PSTable(final int size) {
        if (size < 1) {
            throw new RuntimeException("The size (" + size
                    + ") cannot be smaller than one!");
        }
        this.size                = size;
        this.names               = new String[size];
        this.providerModels      = new String[size];
        this.providerVariables   = new String[size];
        this.subscriberModels    = new String[size];
        this.subscriberVariables = new String[size];
    }

    private PSTable(final int newSize, final PSTable oldTable) {
        if (newSize < oldTable.size) {
            throw new RuntimeException("The new size (" + newSize
                    + ") is smaller than the old one (" + oldTable.size
                    + ")!");
        }
        size = newSize;
        names = Arrays.copyOf(oldTable.names, newSize);
        providerModels = Arrays.copyOf(oldTable.providerModels, newSize);
        providerVariables = Arrays.copyOf(oldTable.providerVariables, newSize);
        subscriberModels = Arrays.copyOf(oldTable.subscriberModels, newSize);
        subscriberVariables = Arrays.copyOf(oldTable.subscriberVariables, newSize);
        position = oldTable.position;
    }

    public int getNumberOfEntries() {
        return position;
    }

    private void add(final String name, final String providerModel,
            final String providerVariable, final String subscriberModel,
            final String subscriberVariable) {
        names[position]               = name;
        providerModels[position]      = providerModel;
        providerVariables[position]   = providerVariable;
        subscriberModels[position]    = subscriberModel;
        subscriberVariables[position] = subscriberVariable;
        position++;
    }

    public PSTable addEntry(final String name, final String providerModel,
            final String providerVariable, final String subscriberModel,
            final String subscriberVariable) {
        add(name, providerModel, providerVariable, subscriberModel,
                subscriberVariable);
        if ((position + 1) >= size) {
            return new PSTable(size + INCREMENT, this);
        } else {
            return this;
        }
    }

    private void checkGetPosition(final int positionToGet) {
        if (positionToGet < 0) {
            throw new RuntimeException("Position (" + positionToGet
                    + ") cannot be negative!");
        }
        if (positionToGet > position) {
            throw new RuntimeException("Position (" + positionToGet
                    + ") cannot be greater than number of entries ("
                    + position +")!");
        }
    }

    public String getName(final int position) {
        checkGetPosition(position);
        return names[position];
    }

    public String getProviderModel(final int position) {
        checkGetPosition(position);
        return providerModels[position];
    }

    public String getProviderVariable(final int position) {
        checkGetPosition(position);
        return providerVariables[position];
    }

    public String getSubscriberModel(final int position) {
        checkGetPosition(position);
        return subscriberModels[position];
    }

    public String getSubscriberVariable(final int position) {
        checkGetPosition(position);
        return subscriberVariables[position];
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < position; i++) {
            sb.append(i);
            sb.append(" ");
            sb.append(names[i]);
            sb.append(" ");
            sb.append(providerModels[i]);
            sb.append(" ");
            sb.append(providerVariables[i]);
            sb.append(" ");
            sb.append(subscriberModels[i]);
            sb.append(" ");
            sb.append(subscriberVariables[i]);
            sb.append("\n");
        }

        return sb.toString();
    }
}
