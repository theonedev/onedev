package io.onedev.server.buildspec.job;

import io.onedev.server.buildspec.job.trigger.JobTrigger;

public class JobTriggerMatch {
	
	private final JobTrigger trigger;
	
	private final String reason;
	
	public JobTriggerMatch(JobTrigger trigger, String reason) {
		this.trigger = trigger;
		this.reason = reason;
	}

	public JobTrigger getTrigger() {
		return trigger;
	}

	public String getReason() {
		return reason;
	}
	
}
