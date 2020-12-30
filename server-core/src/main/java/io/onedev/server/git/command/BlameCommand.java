package io.onedev.server.git.command;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import javax.annotation.Nullable;

import org.apache.commons.collections4.map.AbstractReferenceMap.ReferenceStrength;
import org.apache.commons.collections4.map.ReferenceMap;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.jgit.lib.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;

import io.onedev.commons.utils.LinearRange;
import io.onedev.commons.utils.command.Commandline;
import io.onedev.commons.utils.command.ExecutionResult;
import io.onedev.commons.utils.command.LineConsumer;
import io.onedev.server.git.BlameBlock;
import io.onedev.server.git.BlameCommit;
import io.onedev.server.git.GitUtils;

public class BlameCommand extends GitCommand<Collection<BlameBlock>> {

	private static final Logger logger = LoggerFactory.getLogger(BlameCommand.class);
	
	private static final ReferenceMap<String, Collection<BlameBlock>> cache = 
			new ReferenceMap<>(ReferenceStrength.HARD, ReferenceStrength.SOFT);
	
	private static final int CACHE_THRESHOLD = 1000;
	
	private String commitHash;
	
	private String file;
	
	private LinearRange range;
	
	public BlameCommand(File gitDir) {
		super(gitDir);
	}

	public BlameCommand commitHash(String commitHash) {
		this.commitHash = commitHash;
		return this;
	}
	
	public BlameCommand file(String file) {
		this.file = file;
		return this;
	}
	
	/**
	 * Calculate blames of specified range
	 * @param range
	 * 			0-indexed and inclusive from and to
	 * @return
	 */
	public BlameCommand range(@Nullable LinearRange range) {
		this.range = range;
		return this;
	}
	
	private Commandline buildCmd() {
		Commandline cmd = cmd().addArgs("blame", "--porcelain");
		if (range != null)
			cmd.addArgs("-L" + (range.getFrom()+1) + "," + (range.getTo()+1));
		cmd.addArgs(commitHash, "--", file);
		return cmd;
	}
	
	@Override
	public Collection<BlameBlock> call() {
		Preconditions.checkArgument(commitHash!=null && ObjectId.isId(commitHash), "commit hash has to be specified.");
		Preconditions.checkNotNull(file, "file parameter has to be specified.");

		String cacheKey = commitHash + ":" + file + ":" + (range!=null?range.toString():"");
		
		Collection<BlameBlock> cached = cache.get(cacheKey);
		if (cached != null)
			return cached;
		
		Commandline cmd = buildCmd();
		
		Map<String, BlameBlock> blocks = new HashMap<>();
		Map<String, BlameCommit> commitMap = new HashMap<>();
		
		AtomicReference<BlameCommit> commitRef = new AtomicReference<>(null);
		CommitBuilder commitBuilder = new CommitBuilder();
		
		AtomicBoolean endOfFile = new AtomicBoolean(false);
		
		AtomicInteger beginLine;
		AtomicInteger endLine;
		
		if (range != null) {
			beginLine = new AtomicInteger(range.getFrom());
			endLine = new AtomicInteger(range.getFrom());
		} else {
			beginLine = new AtomicInteger(0);
			endLine = new AtomicInteger(0);
		}
		
		long time = System.currentTimeMillis();
		
		Logger effectiveLogger = logger!=null?logger:BlameCommand.logger;
		ExecutionResult result = cmd.execute(new LineConsumer() {

			@Override
			public void consume(String line) {
				if (line.startsWith("\t")) {
					if (commitRef.get() == null)
						commitRef.set(commitMap.get(commitBuilder.hash));
					endLine.getAndIncrement();
					commitBuilder.hash = null;
				} else if (commitBuilder.hash == null) {
					commitBuilder.hash = StringUtils.substringBefore(line, " ");
					BlameCommit commit = commitRef.get();
					if (commit != null && !commitBuilder.hash.equals(commit.getHash())) {
						BlameBlock block = blocks.get(commit.getHash());
						if (block == null) {
							block = new BlameBlock(commit, new ArrayList<>());
							blocks.put(commit.getHash(), block);
						}
						block.getRanges().add(new LinearRange(beginLine.get(), endLine.get()-1));
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
					effectiveLogger.trace(line.substring("fatal: ".length()));
				} else {
					effectiveLogger.error(line);
				}
			}
			
		});
		
		if (!endOfFile.get())
			result.checkReturnCode();
		
		if (endLine.get() > beginLine.get()) {
			BlameCommit commit = commitRef.get();
			BlameBlock block = blocks.get(commit.getHash());
			if (block == null) {
				block = new BlameBlock(commit, new ArrayList<LinearRange>());
				blocks.put(commit.getHash(), block);
			}
			block.getRanges().add(new LinearRange(beginLine.get(), endLine.get()-1));
		}
		
		if (System.currentTimeMillis()-time > CACHE_THRESHOLD)
			cache.put(cacheKey, blocks.values());
		
		return blocks.values();
	}

    private static class CommitBuilder {
        
    	private Date committerDate;
    	
        private Date authorDate;
        
        private String author;
        
        private String committer;
        
        private String authorEmail;
        
        private String committerEmail;
        
        private String hash;
        
        private String summary;
        
    	private BlameCommit build() {
    		return new BlameCommit(
    				hash, 
    				GitUtils.newPersonIdent(committer, committerEmail, committerDate), 
    				GitUtils.newPersonIdent(author, authorEmail, authorDate), 
    				summary.trim());
    	}
    }
}
