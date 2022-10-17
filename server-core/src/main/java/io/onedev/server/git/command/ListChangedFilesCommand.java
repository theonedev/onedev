package io.onedev.server.git.command;

import java.io.File;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.jgit.util.QuotedString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.onedev.commons.utils.command.Commandline;
import io.onedev.commons.utils.command.LineConsumer;
import io.onedev.server.git.CommandUtils;

public class ListChangedFilesCommand {

	private static final Logger logger = LoggerFactory.getLogger(ListChangedFilesCommand.class);
	
	private final File workingDir;
	
	private final String fromRev;
	
	private final String toRev;
	
	private final Map<String, String> envs;
	
	public ListChangedFilesCommand(File workingDir, String fromRev, String toRev, Map<String, String> envs) {
		this.workingDir = workingDir;
		this.fromRev = fromRev;
		this.toRev = toRev;
		this.envs = envs;
	}
	
	protected Commandline newGit() {
		return CommandUtils.newGit();
	}
	
	public Collection<String> run() {
		final Set<String> changedFiles = new HashSet<String>();
		
		Commandline git = newGit().workingDir(workingDir);
		git.environments().putAll(envs);
		
		git.addArgs("diff", "--name-only", "--no-renames", fromRev + ".." + toRev);
		
		git.execute(new LineConsumer() {

			@Override
			public void consume(String line) {
				if (line.trim().length() != 0)
					changedFiles.add(QuotedString.GIT_PATH.dequote(line));
			}
			
		}, new LineConsumer() {

			@Override
			public void consume(String line) {
				logger.error(line);
			}
			
		}).checkReturnCode();
		
		return changedFiles;
	}

}
