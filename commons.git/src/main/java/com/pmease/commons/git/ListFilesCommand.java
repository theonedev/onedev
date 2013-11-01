package com.pmease.commons.git;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import com.google.common.base.Preconditions;
import com.pmease.commons.util.execution.Commandline;
import com.pmease.commons.util.execution.LineConsumer;

public class ListFilesCommand extends GitCommand<Collection<String>> {

	private String revision;
	
	public ListFilesCommand(final Git git) {
		super(git);
	}
	
	public ListFilesCommand revision(final String revision) {
		this.revision = revision;
		return this;
	}
	
	@Override
	public Collection<String> call() {
		Preconditions.checkNotNull(revision, "revision has to be specified.");
		
		final Set<String> changedFiles = new HashSet<String>();
		
		Commandline cmd = git().cmd();
		
		cmd.addArgs("ls-tree", "--name-only", "-r", revision);
		
		cmd.execute(new LineConsumer() {

			@Override
			public void consume(String line) {
				if (line.trim().length() != 0)
					changedFiles.add(line);
			}
			
		}, errorLogger()).checkReturnCode();
		
		return changedFiles;
	}

}
