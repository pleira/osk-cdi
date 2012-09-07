package org.opensimkit;

import javax.enterprise.context.ApplicationScoped;

import org.apache.deltaspike.cdise.api.CdiContainer;
import org.apache.deltaspike.cdise.api.CdiContainerLoader;
import org.apache.deltaspike.cdise.api.ContextControl;

public class MainCDI {
	
    public static void main(String[] args)
    {
        CdiContainer cdiContainer = CdiContainerLoader.getCdiContainer();
        cdiContainer.boot();

//        ContextControl contextControl = cdiContainer.getContextControl();
//        contextControl.startContext(ApplicationScoped.class);

        cdiContainer.shutdown();

    }

}
