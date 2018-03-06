package io.onedev.server.manager;

import java.util.Collection;
import java.util.Map;

import io.onedev.server.model.Project;
import io.onedev.server.util.Verification;

public interface VerificationManager {
	
	void saveVerification(Project project, String commit, String name, Verification verification);
	
	Map<String, Verification> getVerifications(Project project, String commit);
	
	Collection<String> getVerificationNames(Project project);
	
}
