package com.pmease.commons.git.command;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import com.google.common.base.Preconditions;
import com.pmease.commons.git.Blame;
import com.pmease.commons.git.BriefCommit;
import com.pmease.commons.git.GitContribInfo;
import com.pmease.commons.util.execution.Commandline;
import com.pmease.commons.util.execution.ExecuteResult;
import com.pmease.commons.util.execution.LineConsumer;

public class BlameCommand extends GitCommand<List<Blame>> {

	private String revision;
	
	private String file;
	
	private int start = -1;
	
	private int end = -1;
	
	public BlameCommand(File repoDir) {
		super(repoDir);
	}

	public BlameCommand revision(String revision) {
		this.revision = revision;
		return this;
	}
	
	public BlameCommand file(String file) {
		this.file = file;
		return this;
	}
	
	public BlameCommand start(int start) {
		this.start = start;
		return this;
	}
	
	public BlameCommand end(int end) {
		this.end = end;
		return this;
	}
	
	private Commandline buildCmd() {
		Commandline cmd = cmd().addArgs("blame", "--porcelain");
		if (start != -1) {
			if (end != -1)
				cmd.addArgs("-L", "" + start + "," + end);
			else
				cmd.addArgs("-L", "" + start);
		} else if (end != -1) {
			cmd.addArgs("-L", "," + end);
		}
		
		cmd.addArgs(revision, "--", file);
		return cmd;
	}
	
	@Override
	public List<Blame> call() {
		Preconditions.checkNotNull(file, "file parameter has to be specified.");
		Preconditions.checkNotNull(revision, "revision has to be specified.");
		
		Commandline cmd = buildCmd();
		
		final List<Blame> blames = new ArrayList<>();
		final Map<String, BriefCommit> commitMap = new HashMap<>();
		
		final BlameBuilder blameBuilder = new BlameBuilder();
		final BriefCommitBuilder commitBuilder = new BriefCommitBuilder();
		
		final boolean endOfFile[] = new boolean[]{false};
		
		while (true) {
			ExecuteResult result = cmd.execute(new LineConsumer() {
	
				@Override
				public void consume(String line) {
					if (line.startsWith("\t")) {
						if (blameBuilder.commit == null)
							blameBuilder.commit = commitMap.get(commitBuilder.hash);
						blameBuilder.lines.add(line.substring(1));
						commitBuilder.hash = null;
					} else if (commitBuilder.hash == null) {
						commitBuilder.hash = StringUtils.substringBefore(line, " ");
						if (blameBuilder.commit != null && !commitBuilder.hash.equals(blameBuilder.commit.getHash())) {
							blames.add(blameBuilder.build());
							blameBuilder.commit = null;
							blameBuilder.lines.clear();
						}
					} else if (line.startsWith("author ")) {
						commitBuilder.author = line.substring("author ".length());
					} else if (line.startsWith("author-mail ")) {
						line = StringUtils.substringAfter(line, "<");
						commitBuilder.authorEmail = StringUtils.substringBeforeLast(line, ">");
					} else if (line.startsWith("author-time ")) {
						commitBuilder.authorDate = new Date(1000L * Long.parseLong(line.substring("author-time ".length())));
					} else if (line.startsWith("committer ")) {
						commitBuilder.committer = line.substring("committer ".length());
					} else if (line.startsWith("committer-mail ")) {
						line = StringUtils.substringAfter(line, "<");
						commitBuilder.committerEmail = StringUtils.substringBeforeLast(line, ">");
					} else if (line.startsWith("committer-time ")) {
						commitBuilder.committerDate = new Date(1000L * Long.parseLong(line.substring("committer-time ".length())));
					} else if (line.startsWith("summary ")) {
						commitBuilder.summary = line.substring("summary ".length());
						commitMap.put(commitBuilder.hash, commitBuilder.build());
					} 
				}
				
			}, new LineConsumer() {
				
				@Override
				public void consume(String line) {
					if (line.startsWith("fatal: file ") && line.contains("has only ")) {
						endOfFile[0] = true;
						debug(line.substring("fatal: ".length()));
					} else {
						error(line);
					}
				}
				
			});
			
			if (endOfFile[0]) {
				if (end != -1) {
					end = -1;
					endOfFile[0] = false;
					cmd = buildCmd();
				} else {
					break;
				}
			} else {
				result.checkReturnCode();
				break;
			}
		}
		
		if (!blameBuilder.lines.isEmpty())
			blames.add(blameBuilder.build());
		
		return blames;
	}

	private static class BlameBuilder {
		private BriefCommit commit;
		
		private List<String> lines = new ArrayList<>();
		
		private Blame build() {
			return new Blame(commit, lines);
		}
	}

    private static class BriefCommitBuilder {
        
    	private Date committerDate;
    	
        private Date authorDate;
        
        private String author;
        
        private String committer;
        
        private String authorEmail;
        
        private String committerEmail;
        
        private String hash;
        
        private String summary;
        
    	private BriefCommit build() {
    		return new BriefCommit(
    				hash, 
    				new GitContribInfo(committer, committerEmail, committerDate), 
    				new GitContribInfo(author, authorEmail, authorDate), 
    				summary.trim());
    	}
    }
}
