package io.onedev.server.buildspec.job;

import io.onedev.server.buildspec.job.trigger.JobTrigger;

public class JobTriggerMatch {
	
	private final JobTrigger trigger;
	
	private final SubmitReason reason;
	
	public JobTriggerMatch(JobTrigger trigger, SubmitReason reason) {
		this.trigger = trigger;
		this.reason = reason;
	}

	public JobTrigger getTrigger() {
		return trigger;
	}

	public SubmitReason getReason() {
		return reason;
	}

}
