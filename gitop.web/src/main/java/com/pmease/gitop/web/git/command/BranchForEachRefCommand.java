package com.pmease.gitop.web.git.command;

import java.io.File;
import java.util.Date;
import java.util.Map;

import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import com.pmease.commons.git.BriefCommit;
import com.pmease.commons.git.UserInfo;
import com.pmease.commons.util.execution.Commandline;

public class BranchForEachRefCommand extends ForEachRefCommand<Map<String, BriefCommit>, BranchForEachRefCommand> {

	public BranchForEachRefCommand(File repoDir) {
		super(repoDir);
	}

	private static final String DEFAULT_FORMAT = 
			"%(refname:short)|" +		// ref name
			"%(objectname)|" +			// ref sha-1
			"%(committername)|%(committeremail)|%(committerdate:raw)|" + // committer part
			"%(authorname)|%(authoremail)|%(authordate:raw)" + // author part
			"%09%(contents:subject)"; // \tcommit subject
	
	@Override
	protected void applyArgs(Commandline cmd) {
		super.applyArgs(cmd);
		
		if (Strings.isNullOrEmpty(getSort())) {
			cmd.addArgs("--sort=-committerdate");
		}
		
		if (Strings.isNullOrEmpty(getFormat())) {
			cmd.addArgs("--format=" + DEFAULT_FORMAT);
		}
		
		if (getPatterns().length == 0) {
			cmd.addArgs("refs/heads/");
		}
	}
	
	@Override
	public BranchForEachRefCommand format(String format) {
		throw new UnsupportedOperationException();
	}
	
	@Override
	protected ForEachRefOutputHandler<Map<String, BriefCommit>> getOutputHandler() {
		return new BranchOutputHandler();
	}

	static class BranchOutputHandler extends ForEachRefOutputHandler<Map<String, BriefCommit>> {
		Map<String, BriefCommit> branches = Maps.newLinkedHashMap();

		@Override
		public Map<String, BriefCommit> getOutput() {
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
			
			UserInfo committer = new UserInfo(pieces[i++], parseEmail(pieces[i++]), parseDate(pieces[i++]));
			UserInfo author = new UserInfo(pieces[i++], parseEmail(pieces[i++]), parseDate(pieces[i++]));
			
			BriefCommit commit = new BriefCommit(sha, committer, author, subject);
			branches.put(refname, commit);
		}
	}

	static String parseEmail(String mail) {
		if (mail.charAt(0) == '<')
			mail = mail.substring(1, mail.length() - 1);
		
		return mail;
	}
	
	static Date parseDate(String str) {
		String[] pieces = Iterables.toArray(Splitter.on(" ").split(str), String.class);
		long when = Long.valueOf(pieces[0]) * 1000;
		return new Date(when);
	}
	
	@Override
	protected BranchForEachRefCommand self() {
		return this;
	}
	
	public static void main(String[] args) {
		String line = "master|6c355beafdbd0a62add3a3d89825ca87cf8ecec0|Linus Torvalds|<torvalds@linux-foundation.org>|1372550568 -0700|Linus Torvalds|<torvalds@linux-foundation.org>|1372550568 -0700	Merge branch 'merge' of git://git.kernel.org/pub/scm/linux/kernel/git/benh/powerpc";
		int pos = line.indexOf('\t');
		if (pos <= 0)
			return;
		
		String first = line.substring(0, pos);
		String subject = line.substring(pos + 1);
		String[] pieces = Iterables.toArray(Splitter.on('|').split(first), String.class);
		
		int i = 0;
		String refname = pieces[i++];
		String sha = pieces[i++];
		
		UserInfo committer = new UserInfo(pieces[i++], parseEmail(pieces[i++]), parseDate(pieces[i++]));
		UserInfo author = new UserInfo(pieces[i++], parseEmail(pieces[i++]), parseDate(pieces[i++]));
		System.out.println(refname);
		System.out.println(sha);
		System.out.println(committer);
		System.out.println(author);
		System.out.println(subject);
	}
}
