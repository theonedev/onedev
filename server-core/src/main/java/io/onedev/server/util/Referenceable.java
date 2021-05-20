package io.onedev.server.util;

import io.onedev.server.model.Project;

public interface Referenceable {
	
	Project getProject();
	
	long getNumber();
	
	String getType();

	public static String asReference(Referenceable referenceable) {
		return String.format("%s %s#%d", referenceable.getType(), 
				referenceable.getProject().getName(), referenceable.getNumber());
	}
	
}
