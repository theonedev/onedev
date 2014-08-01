package com.pmease.commons.git.command;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.pmease.commons.git.FileChange;
import com.pmease.commons.util.execution.Commandline;
import com.pmease.commons.util.execution.LineConsumer;

public class ListFileChangesCommand extends GitCommand<Collection<FileChange>> {

	private static final Logger logger = LoggerFactory.getLogger(ListFileChangesCommand.class);
	
	private String fromRev;
	
	private String toRev;
	
	private String path;
	
	private boolean findRenames;
	
	private boolean findCopies;
	
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
	
	public ListFileChangesCommand findCopies(final boolean findCopies) {
		this.findCopies = findCopies;
		return this;
	}

	@Override
	public Collection<FileChange> call() {
		Preconditions.checkNotNull(toRev, "toRev has to be specified.");
		Preconditions.checkNotNull(fromRev, "fromRev has to be specified.");
		
		final Collection<FileChange> fileChanges = new ArrayList<>();
		
		Commandline cmd = cmd();
		
		cmd.addArgs("diff", "--name-status");
		
		if (findRenames)
			cmd.addArgs("--find-renames");
		if (findCopies)
			cmd.addArgs("--find-copies");
		
		cmd.addArgs(fromRev + ".." + toRev);
		
		if (path != null)
			cmd.addArgs("--", path);
		
		cmd.execute(new LineConsumer() {

			@Override
			public void consume(String line) {
        		FileChange.Action action = null;
        		if (line.startsWith("A")) 
        			action = FileChange.Action.ADD;
        		else if (line.startsWith("M"))
        			action = FileChange.Action.MODIFY;
        		else if (line.startsWith("D"))
        			action = FileChange.Action.DELETE;
        		else if (line.startsWith("C"))
        			action = FileChange.Action.COPY;
        		else if (line.startsWith("R"))
        			action = FileChange.Action.RENAME;
        		else if (line.startsWith("T"))
        			action = FileChange.Action.TYPE;
        		
        		if (action != null) {
        			String path = StringUtils.substringAfter(line, "\t").trim();
        			String path1;
        			String path2;
        			if (path.indexOf('\t') != -1) {
        				path1 = StringUtils.substringBefore(path, "\t").trim();
        				path2 = StringUtils.substringAfter(path, "\t").trim();
        			} else {
        				path1 = path2 = path;
        			}
        			FileChange fileChange = new FileChange(action, path1, path2);
        			fileChanges.add(fileChange);
        		}
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
