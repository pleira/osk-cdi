/*
 * Mesh.java
 *
 * Created on 6. Juli 2007, 20:09
 *
 * Implementation of a class for modelling meshes in seqentially modular
 * solvers for loosely coupled DEQ systems, like in system simulation.
 *
 *-----------------------------------------------------------------------------
 * Modification History:
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
 *  2008-05
 *      OpenSimKit V 2.4
 *      Ported from C++ to Java
 *      A. Brandt  alexander.brandt@gmail.com
 *
 *  2009-06
 *     Integrated logging support.
 *     T. Pieper
 *
 *  2009-07
 *     Fixed a number of mesh iteration loop bugs,
 *     particularly for non initial iteration of sub-meshes.
 *     J. Eickhoff
 *
 */
package org.opensimkit;

import java.io.FileWriter;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation of a class for modelling meshes in seqentially modular
 * solvers for loosely coupled DEQ systems, like in system simulation.
 *
 * @author J. Eickhoff
 * @author A. Brandt
 * @author T. Pieper
 * @version 1.3
 * @since 2.4.0
 */
public final class Mesh extends BaseModel {
    private static final Logger LOG = LoggerFactory.getLogger(Mesh.class);
    private static final String TOP_LEVEL = "top";
    private static final String SUB_LEVEL = "sub";
    private static final String MESH      = "Mesh";
    private static final String TYPE      = "Mesh";
    private static final String SOLVER    = "none";
    private static final double MAXTSTEP  = 0.0;
    private static final double MINTSTEP  = 0.0;
    private static final int    REGULSTEP = 0;
    private static final int    TIMESTEP  = 0;
    private final List<Model> items = new LinkedList<Model>();
    private final int mode;
    private int topLevel;

    public Mesh(final String string, final String level) {
        super(string, TYPE, SOLVER, MAXTSTEP, MINTSTEP, TIMESTEP, REGULSTEP);
        setLevel(level);
        mode = 0;
    }

    @Override
    public int save(final FileWriter outFile) {
        return 0;
    }

    private void setLevel(final String level) {
        /* AB: Necessary to change (!strcmp(s1.c_str()) to s1.equals
         *(without negation!). */
        if (level.equals(TOP_LEVEL)) {
            topLevel = 1;
        } else if (level.equals(SUB_LEVEL)) {
            topLevel = 0;
        } else {
            LOG.error("Wrong level!");
        }
    }

    public void initMeshModel(final String modelName,
            final ComHandler modelHandler) {
        Model model;

        model = modelHandler.getItemKey(modelName);
        if (model == null) {
            /* Model not found. */
            LOG.error("Model '{}' does not exist.", modelName);
            localNAckFlag = 1;
        }
        items.add(model);
    }

    public void initMeshMesh(final String modelName,
            final MeshHandler meshHandler) {
        BaseModel model;
        model = meshHandler.getItemKey(modelName);
        if (model == null) {
            /* Model not found. */
            LOG.error("Mesh '{}' doesn't exist.", modelName);
            localNAckFlag = 1;
        }
        items.add(model);
    }

    /**
     *
     * @return error code
     */
    public int isInit() {
        LOG.debug("ISInit");
        if (LOG.isDebugEnabled()) {
            for (Model model: items) {
                LOG.debug("Items: {}", model.getName());
            }
        }
        if (topLevel == 1) {
            /* return 1 here does not mean an error situation. */
            return 1;
        }
        return 0;
    }

    public int timeStep(final float time, final float tStepSize) {
        return 0;
    }

    @Override
    public int iterationStep() {
        Model model;
        int i;
        int maxCount = 20;
        int compRtn;
        int meshConvFlag;

        LOG.debug("\n Mesh iteration ....");

        for (i = 0; i < maxCount; i++) {
            // Each iteration step computation begins with a backward iteration
            // to propagate the initial boundary values (or new boundary values
            // computed in previous timestep) up to the fulfilling models.
            LOG.debug("\n Mesh '{}' - backiteration pass: {}", name, i);
            ListIterator<Model> backit = items.listIterator(items.size());
            while (backit.hasPrevious()) {
                model = backit.previous();

                if (model.getType().equals(MESH)) {
                    LOG.debug("Sub-Mesh initial iteration: {}",
                            model.getName());
                    if (model.iterationStep() == 1) {
                       /* A model found an error in computation. */
                        LOG.error("Mesh '{}', model '{}' - backiteration step "
                                + "error in initial pass", name,
                                model.getName());
                        SimHeaders.negativeAckFlag = 1;
                        return 1;
                    }
                } else {
                 LOG.debug("Mesh backiteration: {}", model.getName());
                    if (model.backIterStep() == 1) {
                        /* A model found an error in computation. */
                        LOG.error("Mesh '{}', model '{}' - backiteration step "
                                + "error in initial pass", name,
                                model.getName());
                        SimHeaders.negativeAckFlag = 1;
                       return 1;
                    }
                }
            }

            /**
             * The the forward iteration takes place, observing whether any
             * error value model complains about not fulfilled hydraulic or
             * boundary condition. */
            /* Initially assuming that mesh iteration converges. */
            meshConvFlag = 0;
            LOG.debug("Mesh '{}' - forwarditeration pass: {}", name, i);
            Iterator<Model> it = items.iterator();
            while (it.hasNext()) {
                model = it.next();
                LOG.debug("Mesh '{}' - forwarditeration: {}", name,
                        model.getName());
                compRtn = 0;
                if (!model.getType().equals(MESH)) {
                    compRtn = model.iterationStep();
                    LOG.debug("Model'{}' compRtn: {}",model.getName(), compRtn);
                }

                if (compRtn == 1) { // A model found an error in computation
                LOG.error("Mesh '{}', model '{}' - forwarditeration step error "
                            + "in pass: {}",
                            new Object[]{name, model.getName(), i});
                    SimHeaders.negativeAckFlag = 1;
                    return 1;
                } else if (compRtn == -1) {
                    meshConvFlag = -1;
                    /* A model in mesh did not consider all boundary conditions
                       fulfilled (hydraulic, electric or similar). */
                }
            }

            if (meshConvFlag == -1) {
                /* Another backward iteration of the mesh is necessary. */
                LOG.debug("Another iteration of the mesh is necessary!\n");
            } else {
                /* Mesh iteration successful. Leave this mesh's iteration step.
                 */
                LOG.debug("Mesh '{}' - Iteration converged. \n", name);
                return 0;
            }
        }
        LOG.debug("Maximum number of iterations of mesh '{}' exceeded!", name);
        LOG.error("Maximum number of iterations of mesh '{}' exceeded!", name);
        SimHeaders.negativeAckFlag = 1;

        return 0;
    }

    @Override
    public int regulStep() {
        Model model;
        int regStepCompRtn;

        Iterator<Model> rstepiter = items.iterator();
        while (rstepiter.hasNext()) {
            model = rstepiter.next();
            LOG.debug("Mesh '{}' - RegulStep: {}", name, model.getName());
            regStepCompRtn = 0;
            regStepCompRtn = model.iterationStep();

            if (regStepCompRtn == 1) {
                /* A model found an error in computation. */
                LOG.debug("Mesh '{}' - RegulStep error in model: {}",
                name, model.getName());
                return 1;
            }
        }
        return 0;
    }
}
