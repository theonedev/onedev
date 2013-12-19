package com.pmease.commons.git.command;

import java.io.File;
import java.util.Map;
import java.util.concurrent.Callable;

import javax.annotation.Nullable;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.pmease.commons.git.GitConfig;
import com.pmease.commons.git.GitVersion;
import com.pmease.commons.loader.AppLoader;
import com.pmease.commons.util.FileUtils;
import com.pmease.commons.util.execution.Commandline;
import com.pmease.commons.util.execution.LineConsumer;

public abstract class GitCommand<V> implements Callable<V> {

	protected static final Logger logger = LoggerFactory.getLogger(GitCommand.class);

	private static final String MIN_VERSION = "1.8.0";
	
	protected final File repoDir;
	
	private final Map<String, String> environments;
	
	protected static final LineConsumer debugLogger = new LineConsumer() {

		@Override
		public void consume(String line) {
			logger.debug(line);
		}
		
	};
	
	protected static final LineConsumer infoLogger = new LineConsumer() {

		@Override
		public void consume(String line) {
			logger.info(line);
		}
		
	};
	
	protected static final LineConsumer warnLogger = new LineConsumer() {

		@Override
		public void consume(String line) {
			logger.warn(line);
		}
		
	};
	
	protected static final LineConsumer errorLogger = new LineConsumer() {

		@Override
		public void consume(String line) {
			logger.error(line);
		}
		
	};
	
	protected static final LineConsumer traceLogger = new LineConsumer() {

		@Override
		public void consume(String line) {
			logger.trace(line);
		}
		
	};
	
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
	public static String checkError(String gitExe) {
		try {
			final String[] version = new String[]{null};
			
			new Commandline(gitExe).addArgs("--version").execute(new LineConsumer() {
	
				@Override
				public void consume(String line) {
					if (line.startsWith("git version "));
						version[0] = line.substring("git version ".length());
				}
				
			}, new LineConsumer.ErrorLogger()).checkReturnCode();

			if (version[0] == null)
				return "Unable to determine git version of '" + gitExe + "'";
			
			GitVersion gitVersion = new GitVersion(version[0]);
			
			if (gitVersion.isOlderThan(new GitVersion(MIN_VERSION)))
				return "Git version of '" + gitExe + "' is " + gitVersion + ". Gitop requires at least " + MIN_VERSION;
			
			return null;
			
		} catch (Exception e) {
			if (e.getMessage() != null && e.getMessage().contains("CreateProcess error=2"))
				return "Unable to find git command: " + gitExe;
			else
				return ExceptionUtils.getMessage(e);
		}
	}
	
	public Commandline cmd() {
		String gitExe = AppLoader.getInstance(GitConfig.class).getExecutable();
		Commandline cmd = new Commandline(gitExe).workingDir(repoDir);
		if (environments != null)
			cmd.environment(environments);
		return cmd;
	}
	
	@Override
	public abstract V call();
	
	protected void debug(String line) {
		logger.debug(line);
	}
	
	protected void trace(String line) {
		logger.trace(line);
	}

	protected void info(String line) {
		logger.info(line);
	}

	protected void warn(String line) {
		logger.warn(line);
	}

	protected void error(String line) {
		logger.error(line);
	}
	
}
