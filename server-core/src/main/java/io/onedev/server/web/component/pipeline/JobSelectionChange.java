package io.onedev.server.web.component.pipeline;

import org.apache.wicket.ajax.AjaxRequestTarget;

import io.onedev.server.buildspec.job.Job;
import io.onedev.server.web.util.AjaxPayload;

public class JobSelectionChange extends AjaxPayload {

	private final Job job;
	
	public JobSelectionChange(AjaxRequestTarget target, Job job) {
		super(target);
		this.job = job;
	}

	public Job getJob() {
		return job;
	}

}