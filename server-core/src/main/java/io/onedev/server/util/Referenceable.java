package io.onedev.server.util;

import io.onedev.server.model.Project;

public interface Referenceable {
	
	Project getProject();
	
	long getNumber();
	
	String getPrefix();
	
}
