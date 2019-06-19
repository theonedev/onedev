package io.onedev.server.model.support;

import java.io.File;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import org.slf4j.Logger;

import io.onedev.server.ci.job.cache.JobCache;
import io.onedev.server.util.patternset.PatternSet;

public abstract class JobExecutionContext {
	
	private final String environment;
	
	private final File workspace;
	
	private final Map<String, String> envVars;
	
	private final List<String> commands;
	
	private final @Nullable SourceSnapshot snapshot;
	
	private final Collection<JobCache> caches; 
	
	private final PatternSet collectFiles;
	
	private final Logger logger;	
	
	public JobExecutionContext(String environment, File workspace, Map<String, String> envVars, 
			List<String> commands, @Nullable SourceSnapshot snapshot, Collection<JobCache> caches, 
			PatternSet collectFiles, Logger logger) {
		this.environment = environment;
		this.workspace = workspace;
		this.envVars = envVars;
		this.commands = commands;
		this.snapshot = snapshot;
		this.caches = caches;
		this.collectFiles = collectFiles;
		this.logger = logger;
	}

	public String getEnvironment() {
		return environment;
	}

	public File getWorkspace() {
		return workspace;
	}

	public Map<String, String> getEnvVars() {
		return envVars;
	}

	public List<String> getCommands() {
		return commands;
	}

	@Nullable
	public SourceSnapshot getSnapshot() {
		return snapshot;
	}

	public Collection<JobCache> getCaches() {
		return caches;
	}

	public PatternSet getCollectFiles() {
		return collectFiles;
	}

	public Logger getLogger() {
		return logger;
	}

	public abstract void notifyJobRunning();
	
}
