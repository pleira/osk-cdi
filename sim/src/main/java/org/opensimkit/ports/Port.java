package org.opensimkit.ports;

import org.opensimkit.models.Model;

/**
 *
 * @author A. Brandt
 */
public interface Port {
    void connectWith(final Model fromModel, final Model toModel);
    String getName();
}
