package io.onedev.server.job;

import io.onedev.commons.utils.TaskLogger;
import io.onedev.k8shelper.CacheAllocationRequest;
import io.onedev.k8shelper.CacheInstance;
import io.onedev.server.cluster.ClusterRunnable;
import io.onedev.server.model.*;
import io.onedev.server.terminal.Shell;
import io.onedev.server.terminal.Terminal;
import io.onedev.server.terminal.WebShell;
import org.eclipse.jgit.lib.ObjectId;

import javax.annotation.Nullable;
import java.io.File;
import java.util.List;
import java.util.Map;

public interface JobManager {
	
	void schedule(Project project, boolean recursive);
	
	void unschedule(Long projectId);
	
	Build submit(Project project, ObjectId commitId, String jobName,
				 Map<String, List<String>> paramMap, String pipeline,
				 String refName, User submitter, @Nullable PullRequest request,
				 @Nullable Issue issue, String reason);
	
	void resubmit(Build build, String reason);
	
	void cancel(Build build);
	
	void resume(Build build);

	void runJob(String server, ClusterRunnable runnable);

	void runJob(JobContext jobContext, JobRunnable runnable);
	
	WebShell openShell(Long buildId, Terminal terminal);
	
	JobContext getJobContext(String jobToken, boolean mustExist);
	
	@Nullable
	Shell getShell(String sessionId);
	
	void reportJobWorkspace(JobContext jobContext, String jobWorkspace);
	
	@Nullable
	JobContext getJobContext(Long buildId);
	
	Map<CacheInstance, String> allocateCaches(JobContext jobContext, CacheAllocationRequest request);

	void copyDependencies(JobContext jobContext, File targetDir);
	
	Map<String, byte[]> runServerStep(JobContext jobContext, List<Integer> stepPosition, File inputDir, 
			Map<String, String> placeholderValues, boolean callByAgent, TaskLogger logger);
	
}
