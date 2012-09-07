/*
 * MeshXMLSectionReader.java
 *
 * Created on 12. October 2008, 14:32
 *
 * Concrete implementation of the behaviour for reading the MeshDefs section of
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
 */
package org.opensimkit.xml;

import java.util.ArrayList;
import java.util.List;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;
import org.opensimkit.ComHandler;
import org.opensimkit.Kernel;
import org.opensimkit.Mesh;
import org.opensimkit.MeshHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Concrete implementation of the behaviour for reading the meshes section of
 * the XML input file.
 *
 * @author A. Brandt
 * @author T. Pieper
 * @version 1.1
 * @since 2.4.6
 */
public final class MeshXMLSectionReader
        extends AbstractTemplateXMLSectionReader {
    private static final Logger LOG
            = LoggerFactory.getLogger(MeshXMLSectionReader.class);
    /** Constant used as an attribute for Meshes. */
    private static final String LEVEL = "level";
    /** Constant for model ID. */
    private static final String CID = "model";
    /** Constant for mesh ID. */
    private static final String MID = "mesh";
    private final MeshHandler meshHandler;
    private final ComHandler  comHandler;
    /** List of all Mesh items. We need this list to initialize the Meshes
     *  after we have read the MeshDefs section. We cannot initialize earlier,
     *  because Meshes can include other Meshes. Additionally it is mandatory
     *  to use a LinkedList, because the order of the elements inside the Mesh
     *  is important for the correct functioning of the Simulator! The entries
     *  of the List have the form: Mesh name, Model name, Model type. */
    private final List<String[]> meshList = new ArrayList<String[]>();
    /** It is mandatory to know to which Mesh the items belong we are parsing.*/
    private String currentMeshName;

    public MeshXMLSectionReader(final String rootName, final Kernel kernel) {
        super(rootName);
        meshHandler = kernel.getMeshHandler();
        comHandler  = kernel.getComHandler();
    }

    public void foundArrayValue(final String elementName, final String value,
            final int length) {
        /** Intentionally left empty. */
    }

    public void foundCharacterData(final String elementName,
            final String value) {
        if (getHierarchyLevel() == LEVEL_3) {
            putItemIntoMeshList(value, elementName);
        }
    }

    public void foundEndElement(final EndElement endElement) {
        if (getHierarchyLevel() == LEVEL_0) {
            initMeshes();
        }
    }

    public void foundStartElement(final StartElement startElement) {
        if (getHierarchyLevel() == LEVEL_2) {
            String name = getMandatoryAttributeValue(startElement, NAME);
            String level = getMandatoryAttributeValue(startElement, LEVEL);
            createNewMesh(name, level);
        }
    }

    /**
     * Initializes one Mesh item.
     * @param meshName Name of the Mesh.
     * @param name Name of the Model.
     * @param type Type of the Model
     */
    private void initMeshItem(final String meshName, final String name,
            final String type) {
        Mesh mesh = meshHandler.getItemKey(meshName);
        if (type.equals(CID)) {
            mesh.initMeshModel(name, comHandler);
        } else if (type.equals(MID)) {
            mesh.initMeshMesh(name, meshHandler);
        } else {
            LOG.info("Invalid slot in /Mesh/Models of"
                    + " mesh " + currentMeshName + ".");
        }
    }

    /**
     * Creates a new entry into the meshList.
     * @param name of the Model.
     * @param type of the Model.
     */
    private void putItemIntoMeshList(final String name, final String type) {
        String[] item = new String[3];
        item[0] = currentMeshName;
        item[1] = name;
        item[2] = type;
        meshList.add(item);
    }

    private void createNewMesh(final String name, final String level) {
        meshHandler.createMesh(name, level);
        currentMeshName = name;
    }

    /**
     * Initializes the Meshes.
     */
    private void initMeshes() {
        for (int i = 0; i < meshList.size(); i++) {
         initMeshItem(getMeshName(i), getModelName(i), getModelType(i));
        }
    }

    /**
     * Convinience method for getting the Mesh name from the meshList.
     * @param index of meshList.
     * @return Mesh name.
     */
    private String getMeshName(final int index) {
        return meshList.get(index)[0];
    }

    /**
     * Convinience method for getting the Model name from the meshList.
     * @param index of meshList.
     * @return Model name.
     */
    private String getModelName(final int index) {
        return meshList.get(index)[1];
    }

    /**
     * Convinience method for getting the model type from the meshList.
     * @param index of meshList.
     * @return Model type.
     */
    private String getModelType(final int index) {
        return meshList.get(index)[2];
    }
}
