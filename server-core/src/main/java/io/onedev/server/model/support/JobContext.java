package io.onedev.server.model.support;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.ObjectId;

import io.onedev.commons.utils.ExceptionUtils;
import io.onedev.server.ci.job.cache.JobCache;
import io.onedev.server.git.command.CheckoutCommand;
import io.onedev.server.git.command.FetchCommand;
import io.onedev.server.util.JobLogger;
import io.onedev.server.util.patternset.PatternSet;

public abstract class JobContext {
	
	private final String projectName;
	
	private final File gitDir;
	
	private final String environment;
	
	private final File serverWorkspace;
	
	private final Map<String, String> envVars;
	
	private final List<String> commands;
	
	private final boolean cloneSource;
	
	private final ObjectId commitId;
	
	private final Collection<JobCache> caches; 
	
	private final PatternSet collectFiles;
	
	private final JobLogger logger;	
	
	public JobContext(String projectName, File gitDir, String environment, File workspace, 
			Map<String, String> envVars, List<String> commands, boolean cloneSource, 
			ObjectId commitId,  Collection<JobCache> caches, PatternSet collectFiles, 
			JobLogger logger) {
		this.projectName = projectName;
		this.gitDir = gitDir;
		this.environment = environment;
		this.serverWorkspace = workspace;
		this.envVars = envVars;
		this.commands = commands;
		this.cloneSource = cloneSource;
		this.commitId = commitId;
		this.caches = caches;
		this.collectFiles = collectFiles;
		this.logger = logger;
	}

	public String getProjectName() {
		return projectName;
	}

	public String getEnvironment() {
		return environment;
	}

	public File getServerWorkspace() {
		return serverWorkspace;
	}

	public Map<String, String> getEnvVars() {
		return envVars;
	}

	public List<String> getCommands() {
		return commands;
	}

	public ObjectId getCommitId() {
		return commitId;
	}

	public boolean isCloneSource() {
		return cloneSource;
	}

	public Collection<JobCache> getCaches() {
		return caches;
	}

	public PatternSet getCollectFiles() {
		return collectFiles;
	}

	public JobLogger getLogger() {
		return logger;
	}
	
	private void fetchAndCheckout(File checkoutDir) {	
		new FetchCommand(checkoutDir).depth(1).from(gitDir.getAbsolutePath()).refspec(commitId.name()).call();
		new CheckoutCommand(checkoutDir).refspec(commitId.name()).call();
	}
	
	public void checkoutSource(File dir) {
		if (new File(dir, ".git").exists()) {
			try (Git git = Git.open(dir)) {
				fetchAndCheckout(dir);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		} else {
	        try (Git git = Git.init().setDirectory(dir).call()) {
				fetchAndCheckout(dir);
			} catch (GitAPIException e) {
				throw ExceptionUtils.unchecked(e);
			}
		}
	}

	public abstract void notifyJobRunning();
	
}
