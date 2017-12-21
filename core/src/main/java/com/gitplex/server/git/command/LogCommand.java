package com.gitplex.server.git.command;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jgit.util.QuotedString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gitplex.server.git.GitUtils;
import com.gitplex.utils.command.Commandline;
import com.gitplex.utils.command.LineConsumer;

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
  
        cmd.addArgs("--name-status");
        
        AtomicReference<LogCommit.Builder> commitBuilderRef = new AtomicReference<>();
        AtomicBoolean inFileChangesBlock = new AtomicBoolean();
        cmd.execute(new LineConsumer() {

            @Override
            public void consume(String line) {
            	if (line.equals("*** commit_begin ***")) {
            		if (commitBuilderRef.get() != null)
	            		LogCommand.this.consume(commitBuilderRef.get().build());
            		commitBuilderRef.set(new LogCommit.Builder());
            		inFileChangesBlock.set(false);
            	} else if (inFileChangesBlock.get()) {
            		FileChange.Action action = null;
            		if (line.startsWith("A")) 
            			action = FileChange.Action.ADD;
            		else if (line.startsWith("M"))
            			action = FileChange.Action.MODIFY;
            		else if (line.startsWith("D"))
            			action = FileChange.Action.DELETE;
            		else if (line.startsWith("C"))
            			action = FileChange.Action.COPY;
            		else if (line.startsWith("R"))
            			action = FileChange.Action.RENAME;
            		else if (line.startsWith("T"))
            			action = FileChange.Action.TYPE;
            		
            		if (action != null) {
            			String path = StringUtils.substringAfter(line, "\t");
            			String path1;
            			String path2;
            			if (path.indexOf('\t') != -1) {
            				path1 = QuotedString.GIT_PATH.dequote(StringUtils.substringBefore(path, "\t"));
            				path2 = QuotedString.GIT_PATH.dequote(StringUtils.substringAfter(path, "\t"));
            			} else {
            				path1 = path2 = QuotedString.GIT_PATH.dequote(path);
            			}
            			FileChange fileChange = new FileChange(action, path1, path2);
        				commitBuilderRef.get().fileChanges.add(fileChange);
            		}
            	} else if (line.startsWith("hash:")) {
                	commitBuilderRef.get().hash = line.substring("hash:".length());
            	} else if (line.startsWith("author:")) {
            		commitBuilderRef.get().authorName = line.substring("author:".length());
            	} else if (line.startsWith("committer:")) {
            		commitBuilderRef.get().committerName = line.substring("committer:".length());
            	} else if (line.startsWith("authorEmail:")) {
            		commitBuilderRef.get().authorEmail = line.substring("authorEmail:".length());
            	} else if (line.startsWith("committerEmail:")) {
            		commitBuilderRef.get().committerEmail = line.substring("committerEmail:".length());
            	} else if (line.startsWith("parents:")) {
                	for (String each: StringUtils.split(line.substring("parents:".length()), " "))
                		commitBuilderRef.get().parentHashes.add(each);
            	} else if (line.startsWith("committerDate:")) {
            		commitBuilderRef.get().committerDate = 
            				GitUtils.parseRawDate(line.substring("committerDate:".length()).trim());
            	} else if (line.startsWith("authorDate:")) {
            		commitBuilderRef.get().authorDate = 
            				GitUtils.parseRawDate(line.substring("authorDate:".length()).trim());
	            	inFileChangesBlock.set(true);
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
