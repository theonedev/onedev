package com.gitplex.server.manager;

import java.util.Collection;
import java.util.Map;

import com.gitplex.server.model.Project;
import com.gitplex.server.util.Verification;

public interface VerificationManager {
	
	void saveVerification(Project project, String commit, String context, Verification verification);
	
	Map<String, Verification> getVerifications(Project project, String commit);
	
	Collection<String> getVerificationContexts(Project project);
	
}
