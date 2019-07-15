package io.onedev.server.git.command;

import java.io.File;
import java.util.Map;

import javax.annotation.Nullable;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.onedev.commons.launcher.loader.AppLoader;
import io.onedev.commons.utils.FileUtils;
import io.onedev.commons.utils.command.Commandline;
import io.onedev.commons.utils.command.LineConsumer;
import io.onedev.server.git.GitVersion;
import io.onedev.server.git.config.GitConfig;

public abstract class GitCommand<V> {

	private static final Logger logger = LoggerFactory.getLogger(GitCommand.class);
	
	private static final String MIN_VERSION = "2.15.0";
	
	protected final File gitDir;
	
	private final Map<String, String> environments;
	
	public GitCommand(File gitDir, @Nullable Map<String, String> environments) {
		this.gitDir = gitDir;
		this.environments = environments;
		
		if (!gitDir.exists())
		    FileUtils.createDir(gitDir);
	}

	public GitCommand(File gitDir) {
		this(gitDir, null);
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
				
			}, new LineConsumer() {

				@Override
				public void consume(String line) {
					logger.error(line);
				}
				
			}).checkReturnCode();

			if (version[0] == null)
				return "Unable to determine git version of '" + gitExe + "'";
			
			GitVersion gitVersion = new GitVersion(version[0]);
			
			if (gitVersion.isOlderThan(new GitVersion(MIN_VERSION)))
				return "Version of git is " + gitVersion + ". OneDev requires at least " + MIN_VERSION;
			
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
		Commandline cmd = new Commandline(gitExe).workingDir(gitDir);
		if (environments != null)
			cmd.environments(environments);
		return cmd;
	}
	
	protected String getGitExe() {
		return AppLoader.getInstance(GitConfig.class).getExecutable();
	}
	
	public abstract V call();
	
}
