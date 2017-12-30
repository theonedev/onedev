package com.gitplex.server.git.command;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.jgit.util.QuotedString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gitplex.server.git.GitUtils;
import com.gitplex.utils.command.Commandline;
import com.gitplex.utils.command.LineConsumer;
import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;

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
		Preconditions.checkArgument(!revisions.isEmpty(), "Log revisions have to be specified");
		
        Commandline cmd = cmd();

        String format = ""
                + "hash:%H %n"
                + "author:%aN %n"
                + "authorEmail:%aE %n"
                + "committer:%cN %n"
                + "committerEmail:%cE %n"
                + "parents:%P %n"
                + "committerDate:%cd %n"
                + "authorDate:%ad %n";

        cmd.addArgs("-c", "diff.renameLimit=1000", "log", "--format=" + format, "--date=raw", "--numstat", 
        		"--find-renames");
        
    	for (String revision: revisions)
    		cmd.addArgs(revision);
        
        AtomicReference<LogCommit.Builder> commitBuilderRef = new AtomicReference<>();
        cmd.execute(new LineConsumer() {

            @Override
            public void consume(String line) {
            	if (line.startsWith("hash:")) {
            		if (commitBuilderRef.get() != null)
	            		LogCommand.this.consume(commitBuilderRef.get().build());
            		commitBuilderRef.set(new LogCommit.Builder());
                	commitBuilderRef.get().hash = line.substring("hash:".length()).trim();
            	} else if (line.startsWith("author:")) {
            		commitBuilderRef.get().authorName = line.substring("author:".length()).trim();
            	} else if (line.startsWith("committer:")) {
            		commitBuilderRef.get().committerName = line.substring("committer:".length()).trim();
            	} else if (line.startsWith("authorEmail:")) {
            		commitBuilderRef.get().authorEmail = line.substring("authorEmail:".length()).trim();
            	} else if (line.startsWith("committerEmail:")) {
            		commitBuilderRef.get().committerEmail = line.substring("committerEmail:".length()).trim();
            	} else if (line.startsWith("parents:")) {
            		Splitter splitter = Splitter.on(" ").omitEmptyStrings().trimResults();
                	for (String each: splitter.split(line.substring("parents:".length())))
                		commitBuilderRef.get().parentHashes.add(each);
            	} else if (line.startsWith("committerDate:")) {
            		commitBuilderRef.get().committerDate = 
            				GitUtils.parseRawDate(line.substring("committerDate:".length()).trim());
            	} else if (line.startsWith("authorDate:")) {
            		commitBuilderRef.get().authorDate = 
            				GitUtils.parseRawDate(line.substring("authorDate:".length()).trim());
            	} else if (line.trim().length() != 0) {
            		StringTokenizer tokenizer = new StringTokenizer(line, "\t");
            		String additionsToken = tokenizer.nextToken();
            		int additions = additionsToken.equals("-")?0:Integer.parseInt(additionsToken);
            		String deletionsToken = tokenizer.nextToken();
            		int deletions = deletionsToken.equals("-")?0:Integer.parseInt(deletionsToken);
            		
            		String path = tokenizer.nextToken();
            		int renameSignIndex = path.indexOf(" => ");
            		if (renameSignIndex != -1) {
            			int leftBraceIndex = path.indexOf("{");
            			int rightBraceIndex = path.indexOf("}");
            			if (leftBraceIndex != -1 && rightBraceIndex != -1 && leftBraceIndex<renameSignIndex
            					&& rightBraceIndex>renameSignIndex) {
            				String leftCommon = path.substring(0, leftBraceIndex);
            				String rightCommon = path.substring(rightBraceIndex+1);
            				String oldPath = leftCommon + path.substring(leftBraceIndex+1, renameSignIndex) 
            						+ rightCommon;
            				String newPath = leftCommon + path.substring(renameSignIndex+4, rightBraceIndex) 
            						+ rightCommon;
                			commitBuilderRef.get().fileChanges.add(
                					new FileChange(oldPath, newPath, additions, deletions));
            			} else {
            				String oldPath = QuotedString.GIT_PATH.dequote(path.substring(0, renameSignIndex));
            				String newPath = QuotedString.GIT_PATH.dequote(path.substring(renameSignIndex+4));
                			commitBuilderRef.get().fileChanges.add(
                					new FileChange(oldPath, newPath, additions, deletions));
            			}
            		} else {
            			path = QuotedString.GIT_PATH.dequote(path);
            			commitBuilderRef.get().fileChanges.add(new FileChange(null, path, additions, deletions));
            		}
            	}
            }
            
        }, new LineConsumer() {

			@Override
			public void consume(String line) {
				logger.error(line);
			}
        	
        }).checkReturnCode();

        if (commitBuilderRef.get() != null)
        	consume(commitBuilderRef.get().build());
        
        return null;
    }
	
	protected abstract void consume(LogCommit commit);
	
}
