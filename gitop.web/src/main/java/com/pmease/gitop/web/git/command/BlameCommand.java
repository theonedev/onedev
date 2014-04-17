package com.pmease.gitop.web.git.command;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.parboiled.common.Preconditions;

import com.google.common.base.Charsets;
import com.google.common.base.Objects;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.io.LineProcessor;
import com.google.common.io.Resources;
import com.pmease.commons.git.GitIdentity;
import com.pmease.commons.git.command.GitCommand;
import com.pmease.commons.util.execution.Commandline;
import com.pmease.commons.util.execution.LineConsumer;
import com.pmease.gitop.web.git.GitUtils;

public class BlameCommand extends GitCommand<List<BlameEntry>> {

	public BlameCommand(File repoDir) {
		super(repoDir);
	}

	private String objectId;
	private String fileName;
	
	public BlameCommand objectId(String objectId) {
		this.objectId = objectId;
		return this;
	}
	
	public BlameCommand fileName(String fileName) {
		this.fileName = fileName;
		return this;
	}
	
	@Override
	public List<BlameEntry> call() {
		Commandline cmd = cmd();
		cmd.addArgs("blame", "--incremental");
		cmd.addArgs(Preconditions.checkArgNotNull(objectId, "objectId"));
		cmd.addArgs("--encoding=UTF-8");
		cmd.addArgs("--", Preconditions.checkArgNotNull(fileName, "file name"));
		
		BlameOutputHandler out = new BlameOutputHandler();
		cmd.execute(out, new LineConsumer.ErrorLogger());
		
		return out.getResult();
	}

	static Pattern pEntryStart = Pattern.compile("([a-z0-9]{40}) (\\d+) (\\d+) (\\d+)");
	
	static class BlameOutputHandler extends LineConsumer implements LineProcessor<List<BlameEntry>> {
		List<BlameEntry> entries = Lists.newArrayList();
		List<String> lines = Lists.newArrayList();
		
		BlameEntry previous;
		
		@Override
		public void consume(String line) {
			if (Strings.isNullOrEmpty(line)) {
				onBlockFinished();
				return;
			}
			
			Matcher m = pEntryStart.matcher(line);
			if (m.matches()) {
				onBlockFinished();
			}
			
			lines.add(line);
		}
		
		private void onBlockFinished() {
			if (lines.isEmpty())
				return;
			
			String authorName = null, committerName = null;
			String committerEmail = null, authorEmail = null;
			Date authorDate = null, commitDate = null; 
			String summary = null;
			String hash = null;
			int sourceLine = 0, resultLine = 0, numLines = 0;
			
			for (String each : lines) {
				Matcher m = pEntryStart.matcher(each);
				if (m.matches()) {
					hash = m.group(1);
					sourceLine = Integer.valueOf(m.group(2));
					resultLine = Integer.valueOf(m.group(3));
					numLines = Integer.valueOf(m.group(4));
				} else if (each.startsWith("author ")) {
					authorName = each.substring("author ".length());
				} else if (each.startsWith("author-mail ")) {
					authorEmail = GitUtils.parseEmail(each.substring("author-mail ".length()));
				} else if (each.startsWith("author-time ")) {
					long time = Long.valueOf(each.substring("author-time ".length()));
					authorDate = new Date(time * 1000L);
				} else if (each.startsWith("author-tz ")) {
					// TODO: Add timezone info
					
				} else if (each.startsWith("committer ")) {
					committerName = each.substring("committer ".length());
				} else if (each.startsWith("committer-mail ")) {
					committerEmail = each.substring("committer-mail ".length());
				} else if (each.startsWith("committer-time ")) {
					long time = Long.valueOf(each.substring("committer-time ".length()));
					commitDate = new Date(time * 1000L);
				} else if (each.startsWith("committer-tz ")) {
					// TODO: add timezone info
					
				} else if (each.startsWith("summary ")) {
					summary = each.substring("summary ".length());
					
				}
			}
			
			BlameEntry.Builder builder = BlameEntry.builder();
			builder.sha(hash)
					.sourceLine(sourceLine)
					.resultLine(resultLine)
					.numLines(numLines);
			
			if (Strings.isNullOrEmpty(summary)) {
				// no commit summary, use previous
				Preconditions.checkState(previous != null 
						&& Objects.equal(previous.getCommit().getHash(), hash));
				
				builder.sha(hash)
						.author(previous.getCommit().getAuthor())
						.committer(previous.getCommit().getCommitter())
						.authorDate(previous.getCommit().getAuthorDate())
						.commitDate(previous.getCommit().getCommitDate())
						.summary(previous.getCommit().getSubject());
			} else {
				builder.committer(new GitIdentity(committerName, committerEmail))
						.author(new GitIdentity(authorName, authorEmail))
						.commitDate(commitDate).authorDate(authorDate)
						.summary(summary);
			}
			
			BlameEntry entry = builder.build();
			entries.add(entry);
			previous = entry;
			lines = Lists.newArrayList();
		}

		@Override
		public boolean processLine(String line) throws IOException {
			consume(line);
			return true;
		}

		@Override
		public List<BlameEntry> getResult() {
			if (!lines.isEmpty()) {
				onBlockFinished();
			}
			
			Collections.sort(entries);
			return entries;
		}
	}
	
	public static void main(String[] args) throws IOException {
		BlameOutputHandler output = new BlameOutputHandler();
		Resources.readLines(Resources.getResource(BlameCommand.class, "blame.txt"), Charsets.UTF_8, output);
		
		List<BlameEntry> entries = output.getResult();
		Collections.sort(entries);
		
		for (BlameEntry each : entries) {
			int startLine = each.getResultLine();
			int numLines = each.getNumLines();
			for (int i = 0; i < numLines; i++) {
				if (i == 0) {
					System.out.print(GitUtils.abbreviateSHA(each.getCommit().getHash()));
				} else {
					System.out.print(StringUtils.repeat(" ", 10));
				}
				
				System.out.print("\t" + (startLine + i));
				if (i == 0) {
					System.out.print("\t\t" + each.getCommit().getSubject());
				}
				System.out.print("\n");
			}
		}
	}
}
