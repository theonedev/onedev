package com.pmease.commons.git;

import java.util.ArrayList;
import java.util.List;

import com.google.common.base.Preconditions;
import com.pmease.commons.util.execution.Commandline;
import com.pmease.commons.util.execution.LineConsumer;

public class ListChangedFilesCommand extends GitCommand<List<String>> {

	private String fromRev;
	
	private String toRev;
	
	public ListChangedFilesCommand(final Git git) {
		super(git);
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
	public List<String> call() {
		Preconditions.checkNotNull(toRev, "toRev has to be specified.");
		Preconditions.checkNotNull(fromRev, "fromRev has to be specified.");
		
		final List<String> changedFiles = new ArrayList<String>();
		
		Commandline cmd = git().cmd();
		
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
