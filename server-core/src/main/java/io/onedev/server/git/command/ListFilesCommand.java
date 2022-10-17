package io.onedev.server.git.command;

import java.io.File;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.jgit.util.QuotedString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.onedev.commons.utils.command.Commandline;
import io.onedev.commons.utils.command.LineConsumer;
import io.onedev.server.git.CommandUtils;

public class ListFilesCommand {

	private static final Logger logger = LoggerFactory.getLogger(ListFilesCommand.class);
	
	private final File workingDir;
	
	private final String revision;
	
	public ListFilesCommand(File workingDir, String revision) {
		this.workingDir = workingDir;
		this.revision = revision;
	}

	protected Commandline newGit() {
		return CommandUtils.newGit();
	}
	
	public Collection<String> run() {
		Set<String> files = new HashSet<String>();
		
		Commandline git = newGit().workingDir(workingDir);
		
		git.addArgs("ls-tree", "--name-only", "-r", revision);
		
		git.execute(new LineConsumer() {

			@Override
			public void consume(String line) {
				if (line.trim().length() != 0)
					files.add(QuotedString.GIT_PATH.dequote(line));
			}
			
		}, new LineConsumer() {

			@Override
			public void consume(String line) {
				logger.error(line);
			}
			
		}).checkReturnCode();
		
		return files;
	}

}
