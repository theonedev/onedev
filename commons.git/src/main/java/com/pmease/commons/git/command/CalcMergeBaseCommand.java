package com.pmease.commons.git.command;

import java.io.File;

import javax.annotation.Nullable;

import com.google.common.base.Preconditions;
import com.pmease.commons.util.execution.Commandline;
import com.pmease.commons.util.execution.ExecuteResult;
import com.pmease.commons.util.execution.LineConsumer;

public class CalcMergeBaseCommand extends GitCommand<String> {

	private String rev1;
	
	private String rev2;
	
	public CalcMergeBaseCommand(final File repoDir) {
		super(repoDir);
	}
	
	public CalcMergeBaseCommand rev1(final String rev1) {
		this.rev1 = rev1;
		return this;
	}
	
	public CalcMergeBaseCommand rev2(final String rev2) {
		this.rev2 = rev2;
		return this;
	}
	
	@Override
	public @Nullable String call() {
		Preconditions.checkNotNull(rev1, "rev1 has to be specified.");
		Preconditions.checkNotNull(rev2, "rev2 has to be specified.");
		
		final String commonAncestor[] = new String[]{null};
		
		Commandline cmd = cmd();
		
		cmd.addArgs("merge-base", rev1, rev2);
		
		final boolean[] revExists = new boolean[]{true};
		ExecuteResult result = cmd.execute(new LineConsumer() {

			@Override
			public void consume(String line) {
				if (line.trim().length() != 0)
					commonAncestor[0] = line.trim();
			}
			
		}, new LineConsumer() {

			@Override
			public void consume(String line) {
				if (line.contains("Not a valid object name"))
					revExists[0] = false;
				else
					LineConsumer.logger.error(line);
			}
			
		});

		if (!revExists[0])
			return null;
		else 
			result.checkReturnCode();
		
		return commonAncestor[0];
	}

}
