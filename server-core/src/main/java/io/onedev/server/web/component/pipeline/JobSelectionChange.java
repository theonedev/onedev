package io.onedev.server.web.component.pipeline;

import javax.annotation.Nullable;

import org.apache.wicket.ajax.AjaxRequestTarget;

import io.onedev.server.buildspec.job.Job;
import io.onedev.server.web.util.AjaxPayload;

public class JobSelectionChange extends AjaxPayload {

	private final Job job;
	
	public JobSelectionChange(AjaxRequestTarget target, @Nullable Job job) {
		super(target);
		this.job = job;
	}

	@Nullable
	public Job getJob() {
		return job;
	}

}