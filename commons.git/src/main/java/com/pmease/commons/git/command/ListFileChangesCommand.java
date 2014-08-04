package com.pmease.commons.git.command;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.pmease.commons.git.FileChange;
import com.pmease.commons.util.execution.Commandline;
import com.pmease.commons.util.execution.LineConsumer;

public class ListFileChangesCommand extends GitCommand<List<FileChange>> {

	private static final Logger logger = LoggerFactory.getLogger(ListFileChangesCommand.class);
	
	private String fromRev;
	
	private String toRev;
	
	private String path;
	
	private boolean findRenames;
	
	public ListFileChangesCommand(final File repoDir) {
		super(repoDir);
	}
	
	public ListFileChangesCommand fromRev(final String fromRev) {
		this.fromRev = fromRev;
		return this;
	}
	
	public ListFileChangesCommand toRev(final String toRev) {
		this.toRev = toRev;
		return this;
	}
	
	public ListFileChangesCommand path(final String path) {
		this.path = path;
		return this;
	}
	
	public ListFileChangesCommand findRenames(final boolean findRenames) {
		this.findRenames = findRenames;
		return this;
	}
	
	@Override
	public List<FileChange> call() {
		Preconditions.checkNotNull(toRev, "toRev has to be specified.");
		Preconditions.checkNotNull(fromRev, "fromRev has to be specified.");
		
		final List<FileChange> fileChanges = new ArrayList<>();
		
		Commandline cmd = cmd();
		
		cmd.addArgs("diff", "--raw");
		
		if (findRenames)
			cmd.addArgs("--find-renames");
		
		cmd.addArgs(fromRev + ".." + toRev);
		
		if (path != null)
			cmd.addArgs("--", path);
		
		cmd.execute(new LineConsumer() {

			@Override
			public void consume(String line) {
				if (line.startsWith(":"))
					fileChanges.add(FileChange.parseRawLine(line));
			}
			
		}, new LineConsumer() {

			@Override
			public void consume(String line) {
				logger.error(line);
			}
			
		}).checkReturnCode();
		
		return fileChanges;
	}

}
