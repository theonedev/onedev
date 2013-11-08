package com.pmease.commons.git.command;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.google.common.base.Preconditions;
import com.pmease.commons.git.FileChange;
import com.pmease.commons.git.FileChange.Action;
import com.pmease.commons.git.FileChangeWithDiffs;
import com.pmease.commons.util.diff.DiffUtils;
import com.pmease.commons.util.execution.Commandline;
import com.pmease.commons.util.execution.LineConsumer;

public class DiffCommand extends GitCommand<List<FileChangeWithDiffs>> {

	private String fromRev;
	
	private String toRev;
	
	private String path;
	
	public DiffCommand(File repoDir) {
		super(repoDir);
	}
	
	public DiffCommand fromRev(String fromRev) {
		this.fromRev = fromRev;
		return this;
	}
	
	public DiffCommand toRev(String toRev) {
		this.toRev = toRev;
		return this;
	}

	public DiffCommand path(String path) {
		this.path = path;
		return this;
	}

	@Override
	public List<FileChangeWithDiffs> call() {
		Preconditions.checkNotNull(fromRev, "fromRev has to be specified.");
		Preconditions.checkNotNull(toRev, "toRev has to be specified.");
		
		Commandline cmd = cmd();
		cmd.addArgs("diff", fromRev + ".." + toRev);
		if (path != null)
			cmd.addArgs("--", path);
		
		final List<FileChangeWithDiffs> fileChanges = new ArrayList<FileChangeWithDiffs>();
		final ChangeBuilder changeBuilder = new ChangeBuilder();
		
		cmd.execute(new LineConsumer() {

			@Override
			public void consume(String line) {
				if (line.startsWith("diff --git")) {
					if (changeBuilder.path != null) 
						fileChanges.add(changeBuilder.buildFileChange());

					changeBuilder.action = FileChange.Action.MODIFY;
					changeBuilder.binary = false;
					changeBuilder.diffLines.clear();
					
					changeBuilder.path = StringUtils.substringBefore(line.substring("diff --git a/".length()), " ");
				} else if (line.startsWith("deleted file")) {
					changeBuilder.action = FileChange.Action.DELETE;
				} else if (line.startsWith("new file")) {
					changeBuilder.action = FileChange.Action.ADD;
				} else if (line.startsWith("Binary files")) {
					changeBuilder.binary = true;
				} else if (line.startsWith("@@") || line.startsWith("+") || line.startsWith("-") 
						|| line.startsWith(" ") || line.startsWith("\\")) {
					changeBuilder.diffLines.add(line);
				}
					
			}
			
		}, errorLogger()).checkReturnCode();

		if (changeBuilder.path != null)
			fileChanges.add(changeBuilder.buildFileChange());
		
		return fileChanges;
	}

	private static class ChangeBuilder {
		private Action action;
		
		private String path;
		
		private boolean binary;
		
		private List<String> diffLines = new ArrayList<>();
		
		private FileChangeWithDiffs buildFileChange() {
			return new FileChangeWithDiffs(path, action, binary, DiffUtils.parseUnifiedDiff(diffLines));
		}
	}
}
