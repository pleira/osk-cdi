/*
 * Model.java
 *
 * Created on 10. August 2008, 21:32
 *
 *  Interface including the basic methods to qualify a class as simulator
 *  model.
 *
 *-----------------------------------------------------------------------------
 * Modification History:
 *
 *  2008-08-10
 *      File created - A. Brandt:
 *      Initial version to help keeping the complexity of the simulator models
 *      low.
 *
 *      File under GPL - see OpenSimKit Documentation.
 *
 *      No warranty and liability for correctness by author.
 *
 */
package org.osk.models;


/**
 * Interface including the basic methods to qualify a class as simulator
 * model.
 *
 * @author A. Brandt
 */
public interface Model {
//    public int initCalcSteps(CTimeStep ctime, CRegulStep cregul);
    public String getName();
    public String getType();
    public void init();
    public int iterationStep();
    public int backIterStep();
    public int timeStep(final double d1, final double d2);
}
