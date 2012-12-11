package org.osk.models.rocketpropulsion;

public class NumericalUtils {
    /***************************************************************************
    *                                                                          *
    *    Calculate nozzle exit pressure pe from chamber pressure pc            *
    *    assuming adiabatic isentropic 1D-flow                                 *
    *    Iterative Newton-method                                               *
    *                                                                          *
    ***************************************************************************/
    public static double newton(final double k, final double areaRatio,
                         final double pc) {
        double newp;
        double oldp = 0.0;
        double t1, t2;      /*t1, t2 temporary variables                */
        double F;           /*Formulae for nozzle area ratio            */
        /* Source: Space Propulsion Analysis and Design p.102 3.100     */

        double dF;          /*Derivative of F to (pe/pc): dF/d(pe/pc)   */
        int i = 0;

        newp = 1E3 / pc ; //Start value for iteration
        while ( (Math.abs(oldp - newp) > 0.00001 ) || ( i == 0 ) )
        {
            i = i + 1;
            oldp = newp;
            t1 = Math.pow(( 2 / ( k + 1 )),( 1 / ( k - 1 )))
                    * Math.pow((( k + 1 )/( k - 1 )),( -0.5 ));
            t2 = Math.pow(oldp,( - 1 / k ))*
                    Math.pow(( 1 - Math.pow( oldp, (( k - 1 )/ k ))),( -0.5 ));
            F = areaRatio / t1 - t2;

            t2 = ( - 1 / k)*Math.pow(oldp,( -1 - 1 / k))*
                    Math.pow(( 1 - Math.pow(oldp,(( k - 1 ) / k))),( -0.5 ));
            dF = areaRatio / t1 - t2 + Math.pow(oldp,( -1 / k)) *
                    ( - (( k - 1 ) / ( 2 * k ))
                    *Math.pow(oldp,((( k - 1 ) / k ) - 1 ))*
                    Math.pow(( 1 - Math.pow(oldp,(( k - 1 ) / k ))),( -1.5 )));
              newp = oldp - F / dF;
              if (i >= 100 || newp < 0.0 ) {
                  newp=1E2/pc; /*Assuming 1mbar pe*/
//                  break; /* oder abbrechen return 0*/
                  return 0.0;
              }
        }        
        return newp * pc;
    }


}
