/*
 * CalcStep.java
 *
 * Created on 6. Juli 2007, 01:32
 *
 * Class for modelling numeric computation step objects.
 *
 *-----------------------------------------------------------------------------
 * Modification History:
 *
 *  2004-12-05
 *      File created - J. Eickhoff:
 *
 *      Class architecture is a derivative from ObjectSim 2.0.3.,
 *      a simulation program published in:
 *
 *        Eickhoff, J.:
 *        Modulare Programmarchitektur fuer ein wissensbasiertes
 *        Simulationssystem mit erweiterter Anwendbarkeit in der
 *        Entwicklung und Betriebsueberwachung verfahrenstechnischer
 *        Anlagen.
 *        PhD thesis in Department Process Engineering of
 *        TU Hamburg-Harburg, 1996.
 *
 *      See also file history cited there and see historic relation of
 *      this OpenSimKit class to a.m. ObjectSim explained in
 *      OpenSimKit Documentation.
 *
 *      File under GPL - see OpenSimKit Documentation.
 *
 *      No warranty and liability for correctness by author.
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
 */
package org.opensimkit;

import java.util.LinkedList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class for modelling numeric computation step objects.
 *
 * @author J. Eickhoff
 * @author A. Brandt
 * @author T. Pieper
 * @version 1.2
 * @since 2.4.0
 */
public class CalcStep {
    private static final Logger LOG = LoggerFactory.getLogger(CalcStep.class);
    protected String name;
    public List<BaseModel> items = new LinkedList<BaseModel>();

    /**
     * Creates a new instance of CalcStep.
     * @param name   the name of the CalcStep object
     */
    public CalcStep(final String name) {
        this.name = name;
        LOG.debug(SimHeaders.DEBUG_SHORT, "Constructor {}", name);
    }

    /**
     *
     * @param model   the reference to the model
     */
    public void addItem(final BaseModel model) {
        LOG.debug(SimHeaders.DEBUG_SHORT, "CalcStep {} adding {}.", name,
                model.getName());
        items.add(model);
    }

    /**
     *
     * @return   the name of the CalcStep object
     */
    public String getName() {
        return name;
    }

}
