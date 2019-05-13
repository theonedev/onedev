package io.onedev.server.web.page.project.blob.render.renderers.cispec.job;

import io.onedev.server.ci.job.Job;

public interface JobAware {
	
	Job getJob();
	
}
