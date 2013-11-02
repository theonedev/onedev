package com.pmease.commons.git.command;

import java.io.File;
import java.util.Map;
import java.util.concurrent.Callable;

import javax.annotation.Nullable;

import org.apache.commons.lang3.exception.ExceptionUtils;

import com.pmease.commons.git.GitVersion;
import com.pmease.commons.util.FileUtils;
import com.pmease.commons.util.execution.Commandline;
import com.pmease.commons.util.execution.LineConsumer;

public abstract class GitCommand<V> implements Callable<V> {

	private static final String GIT_EXE = "git";
	
	private static final String MIN_VERSION = "1.0.0";
	
	protected final File repoDir;
	
	private final Map<String, String> environments;
	
	private final LineConsumer debugLogger = new LineConsumer.DebugLogger();
	
	private final LineConsumer infoLogger = new LineConsumer.InfoLogger();
	
	private final LineConsumer warnLogger = new LineConsumer.WarnLogger();
	
	private final LineConsumer errorLogger = new LineConsumer.ErrorLogger();
	
	private final LineConsumer traceLogger = new LineConsumer.TraceLogger();
	
	public GitCommand(File repoDir, @Nullable Map<String, String> environments) {
		this.repoDir = repoDir;
		this.environments = environments;
		
		if (!repoDir.exists())
		    FileUtils.createDir(repoDir);
	}

	public GitCommand(File repoDir) {
		this(repoDir, null);
	}

	/**
	 * Check if there are any errors with git command line. 
	 *
	 * @return
	 * 			error message if failed to check git command line, 
	 * 			or <tt>null</tt> otherwise
	 * 			
	 */
	public static String checkError() {
		try {
			final String[] version = new String[]{null};
			
			new Commandline(GIT_EXE).addArgs("--version").execute(new LineConsumer() {
	
				@Override
				public void consume(String line) {
					if (line.trim().length() != 0)
						version[0] = line.trim();
				}
				
			}, new LineConsumer.ErrorLogger()).checkReturnCode();
	
			if (version[0] == null)
				throw new RuntimeException("Unable to determine git version.");
			
			GitVersion gitVersion = new GitVersion(version[0]);
			
			if (gitVersion.isOlderThan(new GitVersion(MIN_VERSION)))
				throw new RuntimeException("Git version should be at least " + MIN_VERSION);
			
			return null;
			
		} catch (Exception e) {
			return ExceptionUtils.getMessage(e);
		}
	}
	
	protected LineConsumer debugLogger() {
		return debugLogger;
	}
	
	protected LineConsumer infoLogger() {
		return infoLogger;
	}
	
	protected LineConsumer warnLogger() {
		return warnLogger;
	}
	
	protected LineConsumer errorLogger() {
		return errorLogger;
	}
	
	protected LineConsumer traceLogger() {
		return traceLogger;
	}
	
	public Commandline cmd() {
		Commandline cmd = new Commandline(GIT_EXE).workingDir(repoDir);
		if (environments != null)
			cmd.environment(environments);
		return cmd;
	}

	@Override
	public abstract V call();
}
