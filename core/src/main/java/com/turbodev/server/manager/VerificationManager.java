package com.turbodev.server.manager;

import java.util.Collection;
import java.util.Map;

import com.turbodev.server.model.Project;
import com.turbodev.server.util.Verification;

public interface VerificationManager {
	
	void saveVerification(Project project, String commit, String name, Verification verification);
	
	Map<String, Verification> getVerifications(Project project, String commit);
	
	Collection<String> getVerificationNames(Project project);
	
}
