/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.opensimkit.ports;

import org.opensimkit.Model;

/**
 *
 * @author A. Brandt
 */
public interface Port {
    void connectWith(final Model fromModel, final Model toModel);
    String getName();

}
