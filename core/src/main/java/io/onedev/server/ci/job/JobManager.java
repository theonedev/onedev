package io.onedev.server.ci.job;

import java.util.List;
import java.util.Map;

import org.eclipse.jgit.lib.ObjectId;

import io.onedev.server.model.Build;
import io.onedev.server.model.Project;

public interface JobManager {
	
	Build submit(Project project, ObjectId commitId, String jobName, Map<String, List<String>> paramMap);
	
	void resubmit(Build build, Map<String, List<String>> paramMap);
	
	void cancel(Build build);
	
}
