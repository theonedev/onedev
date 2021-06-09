package io.onedev.server.buildspec.job;

import java.io.File;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import org.eclipse.jgit.lib.ObjectId;

import io.onedev.k8shelper.CacheInstance;
import io.onedev.server.model.Build;
import io.onedev.server.model.Project;
import io.onedev.server.util.SimpleLogger;

public interface JobManager {
	
	void schedule(Project project);
	
	Build submit(Project project, ObjectId commitId, String jobName, 
			Map<String, List<String>> paramMap, SubmitReason reason);
	
	void resubmit(Build build, Map<String, List<String>> paramMap, String reason);
	
	void cancel(Build build);
	
	JobContext getJobContext(String jobToken, boolean mustExist);
	
	Map<CacheInstance, String> allocateJobCaches(String jobToken, Date currentTime, 
			Map<CacheInstance, Date> cacheInstances);
	
	void reportJobCaches(String jobToken, Collection<CacheInstance> cacheInstances);
	
	@Nullable
	Map<String, byte[]> runServerStep(String jobToken, List<Integer> stepPosition, 
			File filesDir, Map<String, String> placeholderValues, SimpleLogger logger);
	
}
