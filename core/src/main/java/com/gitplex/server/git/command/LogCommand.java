package com.gitplex.server.git.command;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jgit.util.QuotedString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gitplex.server.git.GitUtils;
import com.gitplex.server.util.execution.Commandline;
import com.gitplex.server.util.execution.LineConsumer;

public abstract class LogCommand extends GitCommand<Void> {

	private static final Logger logger = LoggerFactory.getLogger(LogCommand.class); 
	
    private List<String> revisions = new ArrayList<>();
    
    public LogCommand(File gitDir) {
        super(gitDir);
    }

	public List<String> revisions() {
		return revisions;
	}

	public LogCommand revisions(List<String> revisions) {
		this.revisions = revisions;
		return this;
	}

	@Override
    public Void call() {
        Commandline cmd = cmd();
        cmd.addArgs("log",
                        "--format=*** commit_begin ***%nhash:%H%nauthor:%aN%nauthorEmail:%aE%n"
                        + "committerEmail:%cE%ncommitter:%cN%nparents:%P%ncommitterDate:%cd %n"
                        + "authorDate:%ad %n",
                        "--date=raw");
        
        if (!revisions.isEmpty()) {
        	for (String revision: revisions)
        		cmd.addArgs(revision);
        } else {
        	cmd.addArgs("--branches");
        }
  
        cmd.addArgs("--name-only", "--no-renames");
        
        LogCommit.Builder commitBuilder = new LogCommit.Builder();
        
        AtomicBoolean changedFilesBlock = new AtomicBoolean();
        
        cmd.execute(new LineConsumer() {

            @Override
            public void consume(String line) {
            	if (line.equals("*** commit_begin ***")) {
            		if (commitBuilder.hash!= null)
	            		LogCommand.this.consume(commitBuilder.build());
            		commitBuilder.hash = null;
            		commitBuilder.committerName = null;
            		commitBuilder.committerEmail = null;
            		commitBuilder.committerDate = null;
            		commitBuilder.authorName = null;
            		commitBuilder.authorEmail = null;
            		commitBuilder.authorDate = null;
            		commitBuilder.parentHashes = new ArrayList<>();
            		commitBuilder.changedFiles = new ArrayList<>();
            		changedFilesBlock.set(false);
            	} else if (changedFilesBlock.get()) {
    				if (line.trim().length() != 0 && commitBuilder.changedFiles != null)
    					commitBuilder.changedFiles.add(QuotedString.GIT_PATH.dequote(line));
            	} else if (line.startsWith("hash:")) {
                	commitBuilder.hash = line.substring("hash:".length());
            	} else if (line.startsWith("author:")) {
                	commitBuilder.authorName = line.substring("author:".length());
            	} else if (line.startsWith("committer:")) {
                	commitBuilder.committerName = line.substring("committer:".length());
            	} else if (line.startsWith("authorEmail:")) {
                	commitBuilder.authorEmail = line.substring("authorEmail:".length());
            	} else if (line.startsWith("committerEmail:")) {
                	commitBuilder.committerEmail = line.substring("committerEmail:".length());
            	} else if (line.startsWith("parents:")) {
                	for (String each: StringUtils.split(line.substring("parents:".length()), " "))
                		commitBuilder.parentHashes.add(each);
            	} else if (line.startsWith("committerDate:")) {
                	commitBuilder.committerDate = GitUtils.parseRawDate(line.substring("committerDate:".length()).trim());
            	} else if (line.startsWith("authorDate:")) {
	            	commitBuilder.authorDate = GitUtils.parseRawDate(line.substring("authorDate:".length()).trim());
	            	if (commitBuilder.changedFiles != null)
	            		changedFilesBlock.set(true);
            	}
            }
            
        }, new LineConsumer() {

			@Override
			public void consume(String line) {
				logger.error(line);
			}
        	
        }).checkReturnCode();

        if (commitBuilder.hash != null)
        	consume(commitBuilder.build());
        
        return null;
    }
	
	protected abstract void consume(LogCommit commit);
	
}
