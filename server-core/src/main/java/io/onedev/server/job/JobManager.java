package io.onedev.server.job;

import java.io.File;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import org.eclipse.jgit.lib.ObjectId;

import io.onedev.commons.utils.TaskLogger;
import io.onedev.k8shelper.CacheAllocationRequest;
import io.onedev.k8shelper.CacheInstance;
import io.onedev.server.buildspec.job.SubmitReason;
import io.onedev.server.model.Build;
import io.onedev.server.model.Project;
import io.onedev.server.terminal.Shell;
import io.onedev.server.terminal.Terminal;
import io.onedev.server.terminal.WebShell;

public interface JobManager {
	
	void schedule(Project project);
	
	void unschedule(Project project);
	
	@Nullable
	Build submit(Project project, ObjectId commitId, String jobName, 
			Map<String, List<String>> paramMap, String pipeline, 
			SubmitReason reason);
	
	void resubmit(Build build, String reason);
	
	void cancel(Build build);
	
	void resume(Build build);
	
	WebShell openShell(Long buildId, Terminal terminal);
	
	JobContext getJobContext(String jobToken, boolean mustExist);
	
	@Nullable
	Shell getShellLocal(String sessionId);
	
	void reportJobWorkspace(JobContext jobContext, String jobWorkspace);
	
	@Nullable
	JobContext getJobContext(Long buildId);
	
	Map<CacheInstance, String> allocateCaches(JobContext jobContext, CacheAllocationRequest request);

	void copyDependencies(JobContext jobContext, File targetDir);
	
	Map<String, byte[]> runServerStep(JobContext jobContext, List<Integer> stepPosition, File inputDir, 
			Map<String, String> placeholderValues, TaskLogger logger);
	
}
