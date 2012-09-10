/*
 * ModelsXMLSectionReader.java
 *
 * Created on 12. October 2008, 14:32
 *
 * Concrete implementation of the behaviour for reading the "models" section of
 * the XML input file.
 *
 *-----------------------------------------------------------------------------
 * Modification History:
 *
 *  2008-10-12
 *      File created - A. Brandt:
 *      Initial version.
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
 *-----------------------------------------------------------------------------
 */
package org.opensimkit.xml;

import java.lang.reflect.Constructor;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;

import org.opensimkit.ComHandler;
import org.opensimkit.Kernel;
import org.opensimkit.Model;
import org.opensimkit.manipulation.Manipulator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Concrete implementation of the behaviour for reading the Model section of
 * the XML input file.
 *
 * @author A. Brandt
 * @author T. Pieper
 * @version 1.2
 * @since 2.4.6
 */
@ApplicationScoped
public class ModelsXMLSectionReader
        extends AbstractTemplateXMLSectionReader {
    private static final Logger LOG
            = LoggerFactory.getLogger(ModelsXMLSectionReader.class);
    private static final String MODEL = "model";
    private static final String CLASS = "class";
    @Inject ComHandler         comHandler;
    @Inject Manipulator        manipulator;
    @Inject Kernel             kernel;
    private       Model model;
    
 
    public ModelsXMLSectionReader() {
        this.rootName = "models";
    }

    public void foundArrayValue(final String elementName,
            final String value, final int length) {

        String variableName
                = getMandatoryAttributeValue(getXMLEventAt(LEVEL_2), NAME);

        try {
            manipulator.setArray(model, variableName, value, length);
        } catch (IllegalAccessException ex) {
            LOG.error("Exception: ", ex);
        } catch (ClassNotFoundException ex) {
            LOG.error("Exception: ", ex);
        } catch (NoSuchFieldException ex) {
            LOG.error("Exception: ", ex);
        }
    }

    public void foundCharacterData(final String elementName,
            final String value) {

        String variableName
                = getMandatoryAttributeValue(getXMLEventAt(LEVEL_2), NAME);

        try {
            manipulator.setFromString(model, variableName, value);
        } catch (IllegalAccessException ex) {
            LOG.error("Exception: ", ex);
        } catch (ClassNotFoundException ex) {
            LOG.error("Exception: ", ex);
        } catch (NoSuchFieldException ex) {
            LOG.error("Exception: ", ex);
        } catch (IllegalArgumentException ex) {
            LOG.error("Exception: ", ex);
        }
    }

    public void foundEndElement(final EndElement endElement) {
        if (getHierarchyLevel() == LEVEL_1) {
            model.init();
        }
    }

    public void foundStartElement(final StartElement startElement) {
        if (getHierarchyLevel() == LEVEL_2) {
            /** Create model here. */
            createModel();
        }
    }

     /**
     * This method does instantiate the simulator models with the help
     * of reflection. Thus no "direct" instantiation (e.g. new FilterT1(s0)) is
     * applied. This removes the need to change this method, when a new
     * model is added to the simulator, as it can be dynamically
     * instantiated during run-time.
     */
    private void createModel() {
        String name;
        /** Get the attributes and look for the attribute which is called name
         *  and use its value to instantiate the Model. */
        //String modelType = getElementNameAt(LEVEL_1);
        String modelType
                = getMandatoryAttributeValue(getXMLEventAt(LEVEL_1), CLASS);

        name = getMandatoryAttributeValue(getXMLEventAt(LEVEL_1), NAME);

        try {
            /** Retrieve the Class reference for the name of the class.
             *  The name of the class is located in the variable "type". */
            Class<?> cls = Class.forName(modelType);
            /** Retrieve the constructor with the first argument of type String
             *  and the second argument of type org.opensimkit.Kernel. This
             *  must be done, because Model has no default constructor.*/
            Constructor<?> con = cls.getConstructor(String.class, Kernel.class);
            /** Execute this constructor to instantiate a new object from the
             *  originally given name. */
            model = (Model) con.newInstance(name, kernel);
            comHandler.addItem(model);
            //modelManipulator.update();
            manipulator.registerInstance(model.getName(), model);
         } catch (Exception ex) {
             LOG.info("Invalid type {} specified for model {}.",
                     modelType, name);
             LOG.debug("Exception: ", ex);
         }
    }
}
