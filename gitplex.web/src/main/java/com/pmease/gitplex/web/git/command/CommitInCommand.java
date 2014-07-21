package com.pmease.gitplex.web.git.command;

import java.io.File;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.parboiled.common.Preconditions;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.pmease.commons.git.command.GitCommand;
import com.pmease.commons.util.execution.Commandline;
import com.pmease.commons.util.execution.LineConsumer;

public class CommitInCommand extends GitCommand<List<String>> {

	public static enum RefType {
		BRANCH, TAG
	}
	
	private RefType refType = RefType.BRANCH;
	
	private String commit;
	
	public CommitInCommand(File repoDir) {
		super(repoDir);
	}

	public CommitInCommand in(RefType refType) {
		this.refType = refType;
		return this;
	}
	
	public CommitInCommand inBranch() {
		return in(RefType.BRANCH);
	}
	
	public CommitInCommand inTag() {
		return in(RefType.TAG);
	}
	
	public CommitInCommand commit(String commit) {
		this.commit = Preconditions.checkNotNull(commit);
		return this;
	}
	
	@Override
	public List<String> call() {
		Commandline cmd = cmd();
		cmd.addArgs(refType.name().toLowerCase());
		cmd.addArgs("--contains", commit);
		
		final List<String> lines = Lists.newArrayList();
		
		cmd.execute(new LineConsumer() {

			@Override
			public void consume(String line) {
				String l = StringUtils.stripStart(line, "*").trim();
				if (!Strings.isNullOrEmpty(l)) {
					lines.add(l);
				}
			}
			
		}, errorLogger);
		
		return lines;
	}
}
