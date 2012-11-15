/*
 *  ProviderSubscriber.java
 *
 *  Provider-Subscriber function class for permitting models to subscribe
 *  variables computed by other models.
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
 *      Integrated ProviderSubscriber prototype into OSK V3.7.0.
 *      Check for proper provider, subscriber and variable names added.
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

import java.lang.reflect.Array;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.opensimkit.manipulation.Manipulator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provider-Subscriber function class for permitting models to subscribe
 * variables computed by other model.
 * This mechanism is used for data interchange between models which have no
 * physical line connection (data line, power line, fluid pipe etc.)
 *
 * @author A. Brandt
 * @author J. Eickhoff
 * @version 1.1
 * @since 3.7.0
 */
// FIXME: This class should be replaced by a CDI event mechanism in the model.
// That is, an object would update a value, and ,
// it will fire a particular type of event to pass the new value to listeners
@ApplicationScoped
public class ProviderSubscriber {
    private static final Logger LOG
            = LoggerFactory.getLogger(ProviderSubscriber.class);
    @Inject Manipulator manipulator;
    private PSTable psTable = new PSTable(1);

//    public ProviderSubscriber(final Manipulator manipulator) {
//        this.manipulator = manipulator;
//    }

    public void add(final String name, final String providerModel,
            final String providerVariable, final String subscriberModel,
            final String subscriberVariable) {
        Object providerReference;
        Object subscriberReference;
        Object providerVariableReference;
        Object subscriberVariableReference;
        
        providerReference = null;
        subscriberReference = null;
        providerVariableReference = null;
        subscriberVariableReference = null;
        
        //System.out.println("Registering provider/subscriber table entries");
        //System.out.println("entry "+ name);
        //System.out.println("providerName "+ providerModel);
        //System.out.println("providerVariable "+ providerVariable);
        //System.out.println("subscriberName "+ subscriberModel);
        //System.out.println("subscriberVariable "+ subscriberVariable);
       
        providerReference = manipulator.getInstance(providerModel);
        if (providerReference == null) {
            LOG.error("Provider does not exist: {}", providerModel);
            return;
        }

            try {
        providerVariableReference = manipulator.get(providerReference, providerVariable);
            } catch (IllegalAccessException ex) {
                LOG.error("Exception:", ex);
            return;
            } catch (ClassNotFoundException ex) {
                LOG.error("Exception:", ex);
            return;
            } catch (NoSuchFieldException ex) {
                LOG.error("Provider variable does not exist: {}", providerVariable);
            return;
            }
            
        subscriberReference = manipulator.getInstance(subscriberModel);
        if (subscriberReference == null) {
            LOG.error("Subscriber does not exist: {}", subscriberModel);
            return;
        }

            try {
        subscriberVariableReference = manipulator.get(subscriberReference, subscriberVariable);
            } catch (IllegalAccessException ex) {
                LOG.error("Exception:", ex);
            return;
            } catch (ClassNotFoundException ex) {
                LOG.error("Exception:", ex);
            return;
            } catch (NoSuchFieldException ex) {
                LOG.error("Subscriber variable does not exist: {}", subscriberVariable);
            return;
            }

        checkTypes(providerVariableReference, subscriberVariableReference,
                providerVariable, subscriberVariable);
        psTable = psTable.addEntry(name,
                providerModel, providerVariable, subscriberModel,
                subscriberVariable);
    }


    public void calc() {
        String providerName;
        String providerVariable;
        Object providerReference;
        Object providerValue;
        String subscriberName;
        String subscriberVariable;
        Object subscriberReference;
        // If table is empty since in computed system all
        // variables are exchanged via ports -> nothing to be done here.
        if (psTable.getNumberOfEntries() == 0) {
          return;
        }
        
        for (int i = 0; i < psTable.getNumberOfEntries(); i++ ) {
        
            providerName = psTable.getProviderModel(i);
            providerVariable = psTable.getProviderVariable(i);
            subscriberName = psTable.getSubscriberModel(i);
            subscriberVariable = psTable.getSubscriberVariable(i);
        
            providerReference = manipulator.getInstance(providerName);
            try {
                providerValue = manipulator.get(providerReference, providerVariable);
                subscriberReference = manipulator.getInstance(subscriberName);
                manipulator.set(subscriberReference, subscriberVariable, providerValue);
            } catch (IllegalAccessException ex) {
                LOG.error("Exception: ", ex);
            } catch (ClassNotFoundException ex) {
                LOG.error("Exception: ", ex);
            } catch (NoSuchFieldException ex) {
                LOG.error("Exception: ", ex);
            }
        }
    }


    @Override
    public String toString() {
        return psTable.toString();
    }

    
    private void checkTypes(final Object providerVariableReference,
            final Object subscriberVariableReference,
            final String providerVariableName,
            final String subscriberVariableName) {
                
        Class<?> providerVariableClass = providerVariableReference.getClass();
        Class<?> subscriberVariableClass = subscriberVariableReference.getClass();
        if (providerVariableClass == subscriberVariableClass){
            if ((providerVariableClass.isArray() == true) && (subscriberVariableClass.isArray() == true)) {
                int providerArrayLength   = Array.getLength(providerVariableReference);
                int subscriberArrayLength = Array.getLength(subscriberVariableReference);
                
                if (providerArrayLength != subscriberArrayLength) {
                    LOG.error("The array sizes of both variables are not equal!\n"
                            + "Provider: {}; length={}\n"
                            + "Subscriber: {}; length={}",
                            new Object[]{providerVariableName, providerArrayLength,
                                subscriberVariableName, subscriberArrayLength});
                    System.exit(1);
                }
            } else if ((providerVariableClass.isArray() == false) && (subscriberVariableClass.isArray() == false)) {
                /* Do nothing, as everything is fine. */
            } else {
                LOG.error("One of the variables is an array and the other is not!\n"
                            + "Provider: {}; isArray={}\n"
                            + "Subscriber: {}; isArray={}",
                            new Object[]{providerVariableName, providerVariableClass.isArray(),
                                subscriberVariableName, subscriberVariableClass.isArray()});
                System.exit(1);
            }
        } else {
            LOG.error("The types of both variables are not equal!\n"
                            + "Provider: {}; type={}\n"
                            + "Subscriber: {}; type={}",
                            new Object[]{providerVariableName, providerVariableClass.toString(),
                                subscriberVariableName, subscriberVariableClass.toString()});
            System.exit(1);
        }
    }
}
