/*
 * ComHandler.java
 *
 * Created on 3. Juli 2007, 22:24
 *
 *  This Class is a Subclass of the Class Handler. Functions for Initialization
 *  of the Models were added. The Attributes and Methods were parameterized
 *  interited from Handler, so the Class ComHandler is not a template class.
 *
 *-----------------------------------------------------------------------------
 *  Modification History
 *
 *  2004-12-05
 *      File created  J. Eickhoff:
 *
 *      Class architecture is a derivative from ObjectSim 2.0.3.,
 *      a simulation program published in:
 *
 *        Eickhoff, J.:
 *        Modulare Programmarchitektur fuer ein wissensbasiertes
 *        Simulationssystem mit erweiterter Anwendbarkeit in der
 *        Entwicklung und Betriebsberwachung verfahrenstechnischer
 *        Anlagen.
 *        PhD thesis in Department Process Engineering of
 *        TU Hamburg-Harburg, 1996.
 *
 *      See also file history cited there and see historic relation of
 *      this OpenSimKit class to a.m. ObjectSim explained in
 *      OpenSimKit Documentation.
 *
 *      File under GPL  see OpenSimKit Documentation.
 *
 *      No warranty and liability for correctness by author.
 *
 *
 *  2005-09
 *      OpenSimKit V 2.2
 *      Modifications enterd for XML input file parsing by
 *      Peter Heinrich  peterhe@student.ethz.ch
 *
 *  2006-03
 *      OpenSimKit V 2.3
 *      Modifications entered for I/O file handling via cmd line arguments by
 *      J. Eickhoff
 *
 *  2008-05
 *      OpenSimKit V 2.4
 *      Ported from C++ to Java
 *      A. Brandt  alexander.brandt@gmail.com
 *
 *  2009-06
 *     Integrated logging support.
 *     T. Pieper
 *
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

package org.opensimkit;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This Class is a Subclass of the Class Handler. Functions for Initialization
 * of the Models were added. The Attributes and Methods were parameterized
 * interited from Handler, so the Class ComHandler is not a template class.
 *
 * @author J. Eickhoff
 * @author P. Heinrich
 * @author A. Brandt
 * @author T. Pieper
 * @version 1.1
 * @since 2.4.0
 */
//@ApplicationScoped
public class ComHandler {
    private static final Logger LOG = LoggerFactory.getLogger(ComHandler.class);
//    private final Kernel kernel;
//    private   int     localNAckFlag;
    private String  name = "Model-Collection";
//    private int     id;
    private String  comment = "none";
//    @Inject @Named("ALL_ITEMS_MAP")
    private SortedMap<String, Model> items; // = new TreeMap<String, Model>();

    /**
     *
     * @param outFile   a reference to a FileWriter
     * @return error code
     * @throws IOException
     */
    public int save(final FileWriter outFile) throws IOException {
        Iterator it = items.keySet().iterator();
        while (it.hasNext()) {
            BaseModel model = (BaseModel) it.next();
            if (model.save(outFile) != 0) {
                // Error message already submitted by model
                return 1;
            }
        }
        return 0;
    }

    /**
     *
     * @param ctime
     * @param cregul
     * @return error code
     */
//    public int calcStepInit(final CTimeStep ctime, final CRegulStep cregul) {
//        Iterator it = items.keySet().iterator();
//        while (it.hasNext()) {
//            Model model = items.get((String) it.next());
//            if (model.initCalcSteps(ctime, cregul) == 1) {
//                // Error message not necessary
//                return 1;
//            }
//        }
//        return 0;
//    }

    /**
     *
     * @param compList
     * @return error code
     */
    public int getCompList(String compList) {
        Iterator it = items.keySet().iterator();
        String   myWorkString;

        myWorkString = "";
        myWorkString.concat("(");
        while (it.hasNext()) {
            BaseModel model = (BaseModel) it.next();
            myWorkString.concat(model.getName());
            myWorkString.concat(" ");
        }

        myWorkString.concat(")");

        LOG.debug("myWorkString is: {}", myWorkString);

        //FIXME: Evil magic number
        if (myWorkString.length() < 1024) {
            compList = myWorkString;

            LOG.debug("compList is: {}", compList);
        } else {
            LOG.error("Model list too long for internal string!");
            return 1;
        }
        return 0;
    }

    public Set<String> getModelNames() {
        return items.keySet();
    }

    /**
     *
     * @param model
     * @return error code
     */
    public int addItem(final Model model) {
        items.put(model.getName(), model);
        return 1;
    }

    /**
     *
     * @param key
     * @return the corresponding Model
     */
    public Model getItemKey(final String key) {
        if (items.get(key) == null) {
            return null;
        } else {
            return items.get(key);
        }
    }

    /**
     *
     * @param key
     * @return error code
     */
    public int removeItemKey(final String key) {
        items.remove(key);
        LOG.info("> Removed '" + key + "' from " + name);
        return 1;
    }

    /**
     *
     * @param model
     * @return error code
     */
    public int removeItemValue(final BaseModel model) {
        Iterator<String> it = items.keySet().iterator();
        while (it.hasNext()) {
            String temporary = it.next();
            if (items.get(temporary) == model) {
                items.remove(temporary);
            }
        }
        return 1;
    }

    /**
     * Returns true if a Model with the supplied name exists, otherwise it
     * returns false.
     * @param name Name to be checked.
     * @return True if the Model with the name "name" exists.
     */
    public boolean isModel(final String name) {
        return items.containsKey(name);
    }

    public Map<String, Model> getModelTypes() {
        Map<String, Model> result = new TreeMap<String, Model>();

        for (Map.Entry<String, Model> entry: items.entrySet()) {
            result.put(entry.getValue().getClass().getName(), entry.getValue());
        }

        return result;
    }
}
