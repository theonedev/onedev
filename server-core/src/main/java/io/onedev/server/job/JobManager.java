package io.onedev.server.job;

import io.onedev.commons.utils.TaskLogger;
import io.onedev.k8shelper.ServerStepResult;
import io.onedev.server.cluster.ClusterTask;
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
	
	Build submit(Project project, ObjectId commitId, String jobName,
				 Map<String, List<String>> paramMap, String refName, 
				 User submitter, @Nullable PullRequest request,
				 @Nullable Issue issue, String reason);
	
	void resubmit(Build build, String reason);
	
	void cancel(Build build);
	
	void resume(Build build);

	boolean runJob(String server, ClusterTask<Boolean> runnable);

	boolean runJob(JobContext jobContext, JobRunnable runnable);
	
	WebShell openShell(Build build, Terminal terminal);
	
	JobContext getJobContext(String jobToken, boolean mustExist);
	
	@Nullable
	Shell getShell(String sessionId);
	
	void reportJobWorkspace(JobContext jobContext, String workspacePath);
	
	@Nullable
	JobContext getJobContext(Long buildId);

	void copyDependencies(JobContext jobContext, File targetDir);
	
	ServerStepResult runServerStep(JobContext jobContext, List<Integer> stepPosition, File inputDir,
								   Map<String, String> placeholderValues, boolean callByAgent,
								   TaskLogger logger);
	
}
