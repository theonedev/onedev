package com.pmease.commons.git.command;

import java.io.File;
import java.io.UnsupportedEncodingException;

import com.google.common.base.Preconditions;
import com.pmease.commons.util.execution.Commandline;
import com.pmease.commons.util.execution.ExecuteResult;
import com.pmease.commons.util.execution.LineConsumer;

public class IsTreeLinkCommand extends GitCommand<Boolean> {

	private String revision;
	
	private String symlink;
	
	public IsTreeLinkCommand revision(String revision) {
		this.revision = revision;
		return this;
	}
	
	public IsTreeLinkCommand symlink(String symlink) {
		this.symlink = symlink;
		return this;
	}
	
	public IsTreeLinkCommand(File repoDir) {
		super(repoDir);
	}

	@Override
	public Boolean call() {
		Preconditions.checkNotNull(revision, "revision has to be specified.");
		Preconditions.checkNotNull(symlink, "symlink has to be specified.");
		
		ShowCommand show = new ShowCommand(repoDir).path(symlink).revision(revision);
		String targetPath;
		try {
			targetPath = new String(show.call(), "UTF8");
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
		
		Commandline cmd = cmd().addArgs("ls-tree", revision + ":" + targetPath);
		final boolean[] notTreeObject = new boolean[]{false};
		ExecuteResult result = cmd.execute(traceLogger, new LineConsumer() {

			@Override
			public void consume(String line) {
				debug(line);
				if (line.equals("fatal: not a tree object") || line.startsWith("fatal: Not a valid object name "))
					notTreeObject[0] = true;
			}
			
		});
		
		if (notTreeObject[0])
			return false;
		
		result.checkReturnCode();
		
		return true;
	}

}
