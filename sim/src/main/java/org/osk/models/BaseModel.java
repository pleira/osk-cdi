package org.osk.models;

import java.io.FileWriter;
import java.io.IOException;
import java.io.Serializable;

import org.osk.SimHeaders;

import com.sun.org.glassfish.gmbal.ManagedAttribute;

/**
 * Implementation of an abstract model class.
 * 
 * @author J. Eickhoff
 * @author A. Brandt
 * @author T. Pieper
 */
public class BaseModel implements Serializable {
	protected String name;
	protected String type;
	protected String numSolverType;

	public BaseModel(final String type,
			final String numSolverType) {
		this.type = type;
		this.numSolverType = numSolverType;
	}

	public int save(final FileWriter f) throws IOException {
        f.write("Model Type: '" + type + "'" + SimHeaders.NEWLINE);
		return 0;
	}

	// -----------------------------------------------------------------------------------
	// Methods added for JMX monitoring and setting initial properties via CDI
	// Extensions

	@ManagedAttribute
	public String getType() {
		return type;
	}

	@ManagedAttribute
	public String getNumSolverType() {
		return numSolverType;
	}

}
