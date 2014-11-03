package com.pmease.commons.git.command;

import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.jgit.lib.PersonIdent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Splitter;
import com.pmease.commons.git.BriefCommit;
import com.pmease.commons.git.GitUtils;
import com.pmease.commons.util.execution.Commandline;
import com.pmease.commons.util.execution.LineConsumer;

public class ListHeadCommitCommand extends GitCommand<Map<String, BriefCommit>> {

	private static final Logger logger = LoggerFactory.getLogger(ListHeadCommitCommand.class);
	
	private static final String SEPARATOR = "#|@";
	
	public ListHeadCommitCommand(File repoDir) {
		super(repoDir);
	}
	
	public ListHeadCommitCommand(File repoDir, Map<String, String> environments) {
		super(repoDir, environments);
	}

	@Override
	public Map<String, BriefCommit> call() {
		Commandline cmd = cmd();
		
		cmd.addArgs("for-each-ref");
		
		String format = 
				"%(refname:short)" + SEPARATOR +
				"%(objectname)" + SEPARATOR	+	
				"%(committername)" + SEPARATOR + "%(committeremail)" + SEPARATOR + "%(committerdate:raw)" + SEPARATOR + 
				"%(authorname)" + SEPARATOR + "%(authoremail)" + SEPARATOR + "%(authordate:raw)" + 
				"%09%(contents:subject)";
		cmd.addArgs("--format=" + format);
		
		cmd.addArgs("refs/heads/");

		final Map<String, BriefCommit> branches = new HashMap<>();
		
		cmd.execute(new LineConsumer() {

			private String parseEmail(String mail) {
				if (mail.charAt(0) == '<')
					mail = mail.substring(1, mail.length() - 1);
				
				return mail;
			}
			
			@Override
			public void consume(String line) {
				int pos = line.indexOf('\t');
				String first = line.substring(0, pos);
				String subject = line.substring(pos + 1);
				Iterator<String> iterator = Splitter.on(SEPARATOR).split(first).iterator();
				
				String refname = iterator.next();
				String sha = iterator.next();
				
				PersonIdent committer = GitUtils.newPersonIdent(iterator.next(), 
						parseEmail(iterator.next()), GitUtils.parseRawDate(iterator.next()));
				PersonIdent author = GitUtils.newPersonIdent(iterator.next(), 
						parseEmail(iterator.next()), GitUtils.parseRawDate(iterator.next()));
				
				BriefCommit commit = new BriefCommit(sha, committer, author, subject);
				branches.put(refname, commit);
			}
			
		}, new LineConsumer() {

			@Override
			public void consume(String line) {
				logger.error(line);
			}
			
		});
		
		return branches;
	}

}
