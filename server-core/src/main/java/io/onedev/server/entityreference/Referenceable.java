package io.onedev.server.entityreference;

import io.onedev.server.model.Issue;
import io.onedev.server.model.Project;

import javax.annotation.Nullable;

public interface Referenceable {
	
	Project getProject();
	
	long getNumber();
	
	String getType();

	public static String asReference(Referenceable referenceable) {
		return asReference(referenceable, null);
	}

	public static String asReference(Referenceable referenceable, @Nullable Project currentProject) {
		var reference= "#" + referenceable.getNumber();
		if (!referenceable.getProject().equals(currentProject)) 
			reference = referenceable.getProject().getPath() + reference;
		if (!(referenceable instanceof Issue))
			reference = referenceable.getType() + " " + reference;
		return reference;
	}
	
}
