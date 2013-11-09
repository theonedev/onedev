package com.pmease.commons.git.command;

import java.io.File;

import com.google.common.base.Preconditions;
import com.pmease.commons.util.execution.Commandline;
import com.pmease.commons.util.execution.LineConsumer;

public class IsBinaryCommand extends GitCommand<Boolean> {

	private String revision;
	
	private String file;
	
	public IsBinaryCommand revision(String revision) {
		this.revision = revision;
		return this;
	}
	
	public IsBinaryCommand file(String file) {
		this.file = file;
		return this;
	}
	
	public IsBinaryCommand(File repoDir) {
		super(repoDir);
	}

	@Override
	public Boolean call() {
		Preconditions.checkNotNull(revision, "revision has to be specified.");
		Preconditions.checkNotNull(file, "file has to be specified.");
		
		Commandline cmd = cmd();
		cmd.addArgs("diff-tree", "-p", "4b825dc642cb6eb9a060e54bf8d69288fbee4904", revision, "--", file);
		
		final boolean[] isBinary = new boolean[]{false};
		
		cmd.execute(new LineConsumer() {

			@Override
			public void consume(String line) {
				if (line.startsWith("Binary files"))
					isBinary[0] = true;
			}
			
		}, errorLogger()).checkReturnCode();
		
		return isBinary[0];
	}

}
