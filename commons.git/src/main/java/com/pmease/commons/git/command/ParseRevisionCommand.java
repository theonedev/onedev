package com.pmease.commons.git.command;

import java.io.File;

import com.google.common.base.Preconditions;
import com.pmease.commons.util.StringUtils;
import com.pmease.commons.util.execution.Commandline;
import com.pmease.commons.util.execution.LineConsumer;

public class ParseRevisionCommand extends GitCommand<String> {

    private String revision;
    
	public ParseRevisionCommand(File repoDir) {
		super(repoDir);
	}
	
	public ParseRevisionCommand revision(String revision) {
		this.revision = revision;
		return this;
	}
	
	@Override
	public String call() {
        Preconditions.checkNotNull(revision, "revision has to be specified.");

        Commandline cmd = cmd().addArgs("parse-rev", "--revs-only", revision);
		
        final String[] commit = new String[]{null};
		cmd.execute(new LineConsumer() {

			@Override
			public void consume(String line) {
				if (StringUtils.isNotBlank(line))
					commit[0] = line.trim();
			}
			
		}, errorLogger).checkReturnCode();

		return commit[0];
	}

}
