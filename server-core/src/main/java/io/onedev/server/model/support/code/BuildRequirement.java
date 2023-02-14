package io.onedev.server.model.support.code;

import java.util.Collection;

public class BuildRequirement {
	
	private final Collection<String> requiredJobs;
	
	private final boolean stictMode;
	
	public BuildRequirement(Collection<String> requiredJobs, boolean strictMode) {
		this.requiredJobs = requiredJobs;
		this.stictMode = strictMode;
	}

	public Collection<String> getRequiredJobs() {
		return requiredJobs;
	}

	public boolean isStictMode() {
		return stictMode;
	}
	
}
