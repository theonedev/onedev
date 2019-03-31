package io.onedev.server.ci;

import java.util.List;
import java.util.Map;

import io.onedev.server.model.Build2;
import io.onedev.server.model.Project;

public interface JobScheduler {
	
	void submit(Project project, String commitHash, String jobName, Map<String, List<String>> paramMatrix);
	
	void resubmit(Build2 build);
	
	void cancel(Build2 build);

}
