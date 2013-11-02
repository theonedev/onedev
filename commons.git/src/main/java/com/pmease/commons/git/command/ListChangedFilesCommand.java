package com.pmease.commons.git.command;

import java.io.File;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import com.google.common.base.Preconditions;
import com.pmease.commons.util.execution.Commandline;
import com.pmease.commons.util.execution.LineConsumer;

public class ListChangedFilesCommand extends GitCommand<Collection<String>> {

	private String fromRev;
	
	private String toRev;
	
	public ListChangedFilesCommand(final File repoDir) {
		super(repoDir);
	}
	
	public ListChangedFilesCommand fromRev(final String fromRev) {
		this.fromRev = fromRev;
		return this;
	}
	
	public ListChangedFilesCommand toRev(final String toRev) {
		this.toRev = toRev;
		return this;
	}
	
	@Override
	public Collection<String> call() {
		Preconditions.checkNotNull(toRev, "toRev has to be specified.");
		Preconditions.checkNotNull(fromRev, "fromRev has to be specified.");
		
		final Set<String> changedFiles = new HashSet<String>();
		
		Commandline cmd = cmd();
		
		cmd.addArgs("diff", "--name-only", fromRev + ".." + toRev);
		
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
