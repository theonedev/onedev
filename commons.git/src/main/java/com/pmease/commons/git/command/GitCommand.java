package com.pmease.commons.git.command;

import java.io.File;
import java.util.Map;
import java.util.concurrent.Callable;

import javax.annotation.Nullable;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;

import com.pmease.commons.git.GitConfig;
import com.pmease.commons.git.GitVersion;
import com.pmease.commons.loader.AppLoader;
import com.pmease.commons.util.FileUtils;
import com.pmease.commons.util.LogUtils;
import com.pmease.commons.util.execution.Commandline;
import com.pmease.commons.util.execution.LineConsumer;

public abstract class GitCommand<V> implements Callable<V> {

	private static final String MIN_VERSION = "1.8.0";
	
	protected final File repoDir;
	
	private final Map<String, String> environments;
	
	private static Logger getLogger() {
		return LogUtils.getLogger(3);
	}
	
	protected static final LineConsumer debugLogger = new LineConsumer() {

		@Override
		public void consume(String line) {
			getLogger().debug(line);
		}
		
	};
	
	protected static final LineConsumer infoLogger = new LineConsumer() {

		@Override
		public void consume(String line) {
			getLogger().info(line);
		}
		
	};
	
	protected static final LineConsumer warnLogger = new LineConsumer() {

		@Override
		public void consume(String line) {
			getLogger().warn(line);
		}
		
	};
	
	protected static final LineConsumer errorLogger = new LineConsumer() {

		@Override
		public void consume(String line) {
			getLogger().error(line);
		}
		
	};
	
	protected static final LineConsumer traceLogger = new LineConsumer() {

		@Override
		public void consume(String line) {
			getLogger().trace(line);
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
					if (line.startsWith("git version "))
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
			String message = ExceptionUtils.getMessage(e);
			if (message.contains("CreateProcess error=2"))
				return "Unable to find git command: " + gitExe;
			else if (message.contains("error launching git"))
				return "Unable to launch git command: " + gitExe;
			else
				return message;
		}
	}
	
	public Commandline cmd() {
		String gitExe = getGitExe();
		Commandline cmd = new Commandline(gitExe).workingDir(repoDir);
		if (environments != null)
			cmd.environment(environments);
		return cmd;
	}
	
	protected String getGitExe() {
		return AppLoader.getInstance(GitConfig.class).getExecutable();
	}
	
	@Override
	public abstract V call();
	
	protected void debug(String line) {
		getLogger().debug(line);
	}
	
	protected void trace(String line) {
		getLogger().trace(line);
	}

	protected void info(String line) {
		getLogger().info(line);
	}

	protected void warn(String line) {
		getLogger().warn(line);
	}

	protected void error(String line) {
		getLogger().error(line);
	}

}
