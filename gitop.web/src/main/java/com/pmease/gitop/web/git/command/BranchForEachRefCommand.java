package com.pmease.gitop.web.git.command;

import java.io.File;
import java.util.Map;

import org.eclipse.jgit.lib.PersonIdent;

import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import com.pmease.commons.git.BriefCommit;
import com.pmease.commons.util.execution.Commandline;
import com.pmease.gitop.web.git.GitUtils;

public class BranchForEachRefCommand extends ForEachRefCommand<Map<String, BriefCommit>, BranchForEachRefCommand> {

	public BranchForEachRefCommand(File repoDir) {
		super(repoDir);
	}

	private static final String FORMAT = 
			"%(refname:short)|" +		// ref name
			"%(objectname)|" +			// ref sha-1
			"%(committername)|%(committeremail)|%(committerdate:raw)|" + // committer part
			"%(authorname)|%(authoremail)|%(authordate:raw)" + // author part
			"%09%(contents:subject)"; // \tcommit subject
	
	@Override
	protected String getFormat() {
		return FORMAT;
	}
	
	@Override
	protected void applyArgs(Commandline cmd) {
		super.applyArgs(cmd);
		
		if (Strings.isNullOrEmpty(getSort())) {
			cmd.addArgs("--sort=-committerdate");
		}
		
		if (getPatterns().length == 0) {
			cmd.addArgs("refs/heads/");
		}
	}
	
	@Override
	protected ForEachRefOutputHandler<Map<String, BriefCommit>> getOutputHandler() {
		return new BranchOutputHandler();
	}

	static class BranchOutputHandler extends ForEachRefOutputHandler<Map<String, BriefCommit>> {
		Map<String, BriefCommit> branches = Maps.newLinkedHashMap();

		@Override
		public Map<String, BriefCommit> getResult() {
			return branches;
		}

		@Override
		public void consume(String line) {
			int pos = line.indexOf('\t');
			if (pos <= 0)
				return;
			
			String first = line.substring(0, pos);
			String subject = line.substring(pos + 1);
			String[] pieces = Iterables.toArray(Splitter.on('|').split(first), String.class);
			
			int i = 0;
			String refname = pieces[i++];
			String sha = pieces[i++];
			
			PersonIdent committer = new PersonIdent(pieces[i++], 
											  GitUtils.parseEmail(pieces[i++]), 
											  GitUtils.parseRawDate(pieces[i++]), 0);
			PersonIdent author = new PersonIdent(pieces[i++], 
										   GitUtils.parseEmail(pieces[i++]), 
										   GitUtils.parseRawDate(pieces[i++]), 0);
			
			BriefCommit commit = new BriefCommit(sha, committer, author, subject);
			branches.put(refname, commit);
		}
	}

	@Override
	protected BranchForEachRefCommand self() {
		return this;
	}
}
