/*
 * MeshHandler.java
 *
 * Created on 3. Juli 2007, 22:24
 *
 *  This is the implementation of the Class MeshHandler.
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
 *  2008-05
 *      OpenSimKit V 2.4
 *      Ported from C++ to Java
 *      A. Brandt  alexander.brandt@gmail.com
 *
 */
package org.opensimkit;

import java.util.ArrayList;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.opensimkit.steps.CIterationStep;

/**
 * This is the implementation of the Class MeshHandler.
 *
 * @author J. Eickhoff
 * @author A. Brandt
 * @author T. Pieper
 * @version 1.0
 * @since  2.4.0
 */
@ApplicationScoped
public class MeshHandler {
    protected String  name;
    protected int     id;
    protected String  comment;
    private List<Mesh> items = new ArrayList<Mesh>();

    /** Creates a new instance of MeshHandler. */
    @Inject
    public MeshHandler() {
        this.name = "Mesh-Collection";
        this.comment = "none";
    }

    public MeshHandler(final String name) {
        this.name = name;
        this.comment = "none";
    }

    public MeshHandler(final String name, final String comment) {
        this.name = name;
        this.comment = comment;
    }

    public void createMesh(final String name, final String level) {
        Mesh mesh = new Mesh(name, level);
        addItem(mesh);
    }

/*
int
MeshHandler::FillMeshSlots (Input& in) {
  map<string, Mesh*>::iterator meshIter=items.begin();
  Mesh* mesh;
  char thestring[MAXSTRINGLEN];

  while (meshIter!=items.end()) {
    mesh=meshIter.second;
    if (mesh.load(in)) {
      cout<<"Loading Data for '"<<mesh.getName()<<"' failed."<<
 "SimHeaders.newline";
      strcpy(thestring, (const char*)(mesh.getName()));
      negativeAckFlag = 1;
      return 1;
    }
    meshIter++;
  }
  return 0;
}
 */
    public int calcStepInit(final CIterationStep iStep) {
        Mesh mesh;

        for (int i = 0; i < items.size(); i++) {
            mesh = items.get(i);

            if (mesh.isInit() == 1) {
                iStep.addItem(mesh);
            }
        }
        return 0;
    }

    public int addItem(final Mesh key) {
        items.add(key);
        return 1;
    }

    public Mesh getItemKey(final String key) {
        Mesh result = null;
        for (int i = 0; i < items.size(); i++) {
            if (key.equals(items.get(i).getName())) {
                result = items.get(i);
            }
        }
        return result;
    }

}
