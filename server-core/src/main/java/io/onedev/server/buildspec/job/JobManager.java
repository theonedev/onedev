package io.onedev.server.buildspec.job;

import java.io.File;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import org.eclipse.jgit.lib.ObjectId;

import io.onedev.commons.utils.TaskLogger;
import io.onedev.k8shelper.CacheAllocationRequest;
import io.onedev.k8shelper.CacheInstance;
import io.onedev.server.model.Build;
import io.onedev.server.model.Project;

public interface JobManager {
	
	void schedule(Project project);
	
	Build submit(Project project, ObjectId commitId, String jobName, 
			Map<String, List<String>> paramMap, String pipeline, SubmitReason reason);
	
	void resubmit(Build build, Map<String, List<String>> paramMap, String reason);
	
	void cancel(Build build);
	
	JobContext getJobContext(String jobToken, boolean mustExist);
	
	Map<CacheInstance, String> allocateJobCaches(String jobToken, CacheAllocationRequest request);
	
	@Nullable
	Map<String, byte[]> runServerStep(String jobToken, List<Integer> stepPosition, 
			File inputDir, Map<String, String> placeholderValues, TaskLogger logger);
	
}
