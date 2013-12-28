package com.pmease.commons.git.command;

import java.io.File;

import org.apache.commons.lang3.StringUtils;

import com.google.common.base.Preconditions;
import com.pmease.commons.util.execution.Commandline;
import com.pmease.commons.util.execution.LineConsumer;

public class ShowSymbolicRefCommand extends GitCommand<String> {

    private String symbolicRefName;

	public ShowSymbolicRefCommand(File repoDir) {
		super(repoDir);
	}
	
	public ShowSymbolicRefCommand symbolicRefName(String symbolicRefName) {
	    this.symbolicRefName = symbolicRefName;
		return this;
	}
	
	@Override
	public String call() {
	    Preconditions.checkNotNull(symbolicRefName, "symbolicRefName has to be specified.");
	    
		Commandline cmd = cmd().addArgs("symbolic-ref", symbolicRefName);

		final String shortRefName[] = new String[]{null};
		
		cmd.execute(new LineConsumer() {

			@Override
			public void consume(String line) {
				if (StringUtils.isNotBlank(line))
					shortRefName[0] = line;
			}
			
		}, errorLogger).checkReturnCode();
		
		Preconditions.checkNotNull(shortRefName[0]);
		
		return shortRefName[0];
	}

}
