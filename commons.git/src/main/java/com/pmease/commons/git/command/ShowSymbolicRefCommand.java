package com.pmease.commons.git.command;

import java.io.File;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.pmease.commons.util.execution.Commandline;
import com.pmease.commons.util.execution.LineConsumer;

public class ShowSymbolicRefCommand extends GitCommand<String> {

	private static final Logger logger = LoggerFactory.getLogger(ShowSymbolicRefCommand.class);
	
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
			
		}, new LineConsumer() {

			@Override
			public void consume(String line) {
				logger.error(line);
			}
			
		}).checkReturnCode();
		
		Preconditions.checkNotNull(shortRefName[0]);
		
		return shortRefName[0];
	}

}
