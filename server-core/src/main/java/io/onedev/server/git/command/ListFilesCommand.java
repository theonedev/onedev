package io.onedev.server.git.command;

import java.io.File;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.jgit.util.QuotedString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;

import io.onedev.commons.utils.command.Commandline;
import io.onedev.commons.utils.command.LineConsumer;

public class ListFilesCommand extends GitCommand<Collection<String>> {

	private static final Logger logger = LoggerFactory.getLogger(ListFilesCommand.class);
	
	private String revision;
	
	public ListFilesCommand(File gitDir) {
		super(gitDir);
	}
	
	public ListFilesCommand revision(String revision) {
		this.revision = revision;
		return this;
	}
	
	@Override
	public Collection<String> call() {
		Preconditions.checkNotNull(revision, "revision has to be specified.");
		
		Set<String> files = new HashSet<String>();
		
		Commandline cmd = cmd();
		
		cmd.addArgs("ls-tree", "--name-only", "-r", revision);
		
		cmd.execute(new LineConsumer() {

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
