package io.onedev.server.ci.job;

import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import org.eclipse.jgit.lib.ObjectId;

import io.onedev.server.model.Build;
import io.onedev.server.model.Project;
import io.onedev.server.model.User;
import io.onedev.server.model.support.JobContext;

public interface JobManager {
	
	public static final String JOB_TOKEN_HTTP_HEADER = "X-ONEDEV-JOB-TOKEN";
	
	Build submit(Project project, ObjectId commitId, String jobName, 
			Map<String, List<String>> paramMap, @Nullable User submitter);
	
	void resubmit(Build build, Map<String, List<String>> paramMap, @Nullable User submitter);
	
	void cancel(Build build, @Nullable User canceller);
	
	JobContext getJobContext(String jobToken);
	
}
