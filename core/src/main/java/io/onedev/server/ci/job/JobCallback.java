package io.onedev.server.ci.job;

import javax.annotation.Nullable;

import io.onedev.server.ci.job.log.JobLogger;

public interface JobCallback {

	void jobFinished(JobResult result, @Nullable String resultMessage);
	
	JobLogger getJobLogger();
	
}
