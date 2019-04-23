package io.onedev.server.ci.job;

import java.util.List;
import java.util.Map;

import io.onedev.server.model.Build;
import io.onedev.server.model.Project;

public interface JobScheduler {
	
	void submit(Project project, String commitHash, String jobName, Map<String, List<String>> paramMatrix);
	
	void resubmit(Build build);
	
	void cancel(Build build);

}
