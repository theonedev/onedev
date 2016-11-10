package com.gitplex.commons.git.command;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.commons.collections4.map.AbstractReferenceMap.ReferenceStrength;
import org.apache.commons.collections4.map.ReferenceMap;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gitplex.commons.git.Blame;
import com.gitplex.commons.git.BriefCommit;
import com.gitplex.commons.git.GitUtils;
import com.gitplex.commons.util.Range;
import com.gitplex.commons.util.execution.Commandline;
import com.gitplex.commons.util.execution.ExecuteResult;
import com.gitplex.commons.util.execution.LineConsumer;
import com.google.common.base.Preconditions;

public class BlameCommand extends GitCommand<Map<String, Blame>> {

	private static final Logger logger = LoggerFactory.getLogger(BlameCommand.class);
	
	private static final ReferenceMap<String, Map<String, Blame>> cache = 
			new ReferenceMap<>(ReferenceStrength.HARD, ReferenceStrength.SOFT);
	
	private static final int CACHE_THRESHOLD = 1000;
	
	private String commitHash;
	
	private String file;
	
	public BlameCommand(File repoDir) {
		super(repoDir);
	}

	public BlameCommand commitHash(String commitHash) {
		this.commitHash = commitHash;
		return this;
	}
	
	public BlameCommand file(String file) {
		this.file = file;
		return this;
	}
	
	private Commandline buildCmd() {
		Commandline cmd = cmd().addArgs("blame", "--porcelain");
		cmd.addArgs(commitHash, "--", file);
		return cmd;
	}
	
	@Override
	public Map<String, Blame> call() {
		Preconditions.checkArgument(commitHash!=null && GitUtils.isHash(commitHash), "commit hash has to be specified.");
		Preconditions.checkNotNull(file, "file parameter has to be specified.");

		String cacheKey = commitHash + ":" + file;
		
		Map<String, Blame> cached = cache.get(cacheKey);
		if (cached != null)
			return cached;
		
		Commandline cmd = buildCmd();
		
		final Map<String, Blame> blames = new HashMap<>();
		final Map<String, BriefCommit> commitMap = new HashMap<>();
		
		final AtomicReference<BriefCommit> commitRef = new AtomicReference<>(null);
		final BriefCommitBuilder commitBuilder = new BriefCommitBuilder();
		
		final AtomicBoolean endOfFile = new AtomicBoolean(false);
		final AtomicInteger beginLine = new AtomicInteger(0);
		final AtomicInteger endLine = new AtomicInteger(0);
		
		long time = System.currentTimeMillis();
		
		ExecuteResult result = cmd.execute(new LineConsumer() {

			@Override
			public void consume(String line) {
				if (line.startsWith("\t")) {
					if (commitRef.get() == null)
						commitRef.set(commitMap.get(commitBuilder.hash));
					endLine.getAndIncrement();
					commitBuilder.hash = null;
				} else if (commitBuilder.hash == null) {
					commitBuilder.hash = StringUtils.substringBefore(line, " ");
					BriefCommit commit = commitRef.get();
					if (commit != null && !commitBuilder.hash.equals(commit.getHash())) {
						Blame blame = blames.get(commit.getHash());
						if (blame == null) {
							blame = new Blame(commit, new ArrayList<Range>());
							blames.put(commit.getHash(), blame);
						}
						blame.getRanges().add(new Range(beginLine.get(), endLine.get()));
						commitRef.set(null);
						beginLine.set(endLine.get());
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
					endOfFile.set(true);
					logger.debug(line.substring("fatal: ".length()));
				} else {
					logger.error(line);
				}
			}
			
		});
		
		if (!endOfFile.get())
			result.checkReturnCode();
		
		if (endLine.get() > beginLine.get()) {
			BriefCommit commit = commitRef.get();
			Blame blame = blames.get(commit.getHash());
			if (blame == null) {
				blame = new Blame(commit, new ArrayList<Range>());
				blames.put(commit.getHash(), blame);
			}
			blame.getRanges().add(new Range(beginLine.get(), endLine.get()));
		}
		
		if (System.currentTimeMillis()-time > CACHE_THRESHOLD)
			cache.put(cacheKey, blames);
		
		return blames;
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
    				GitUtils.newPersonIdent(committer, committerEmail, committerDate), 
    				GitUtils.newPersonIdent(author, authorEmail, authorDate), 
    				summary.trim());
    	}
    }
}
