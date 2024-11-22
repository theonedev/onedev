package io.onedev.server.model.support.code;

import java.util.Collection;

public class BuildRequirement {
	
	private final Collection<String> requiredJobs;
	
	private final boolean strictMode;
	
	public BuildRequirement(Collection<String> requiredJobs, boolean strictMode) {
		this.requiredJobs = requiredJobs;
		this.strictMode = strictMode;
	}

	public Collection<String> getRequiredJobs() {
		return requiredJobs;
	}

	public boolean isStrictMode() {
		return strictMode;
	}
	
}
