package com.pmease.commons.git.command;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.google.common.base.Preconditions;
import com.pmease.commons.git.RefInfo;
import com.pmease.commons.util.execution.Commandline;
import com.pmease.commons.util.execution.ExecuteResult;
import com.pmease.commons.util.execution.LineConsumer;

public class ShowRefCommand extends GitCommand<List<RefInfo>> {

    private String pattern;
    
	public ShowRefCommand(File repoDir) {
		super(repoDir);
	}
	
	public ShowRefCommand pattern(String pattern) {
		this.pattern = pattern;
		return this;
	}
	
	@Override
	public List<RefInfo> call() {
        Preconditions.checkNotNull(pattern, "pattern has to be specified.");

        Commandline cmd = cmd().addArgs("show-ref", pattern);
		
        final List<RefInfo> refs = new ArrayList<RefInfo>();
        final boolean[] hasError = new boolean[]{false};
		ExecuteResult result = cmd.execute(new LineConsumer() {

			@Override
			public void consume(String line) {
				String commitHash = StringUtils.substringBefore(line, " ");
				String refName = StringUtils.substringAfter(line, " ");
				refs.add(new RefInfo(refName, commitHash));
			}
			
		}, new LineConsumer() {

			@Override
			public void consume(String line) {
				error(line);
				hasError[0] = true;
			}
			
		});

		// When no refs are found, git returns error code 1, but we do not want 
		// to treat this as an error. Instead only when there are error messages
		// being printed to stderr, we then check the return code.
		if (hasError[0])
			result.checkReturnCode();
		
		return refs;
	}

}
