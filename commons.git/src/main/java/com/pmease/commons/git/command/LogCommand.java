package com.pmease.commons.git.command;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.pmease.commons.git.Commit;
import com.pmease.commons.git.GitUtils;
import com.pmease.commons.util.execution.Commandline;
import com.pmease.commons.util.execution.LineConsumer;

public class LogCommand extends GitCommand<List<Commit>> {

	private static final Logger logger = LoggerFactory.getLogger(LogCommand.class); 
	
    private List<String> revisions = new ArrayList<>();
    
    private List<String> paths = new ArrayList<>();
    
    private Date after;
    
    private Date before;
    
    private int count;
    
    private int skip;
    
    private boolean parentRewriting;
    
    private List<String> messages = new ArrayList<>();
    
    private List<String> authors = new ArrayList<>();
    
    private List<String> committers = new ArrayList<>();
    
    public LogCommand(File repoDir) {
        super(repoDir);
    }

	public List<String> revisions() {
		return revisions;
	}

	public LogCommand revisions(List<String> revisions) {
		this.revisions = revisions;
		return this;
	}

	public List<String> paths() {
		return paths;
	}

	public LogCommand paths(List<String> paths) {
		this.paths = paths;
		return this;
	}

	public Date after() {
		return after;
	}

	public LogCommand after(Date after) {
		this.after = after;
		return this;
	}

	public Date before() {
		return before;
	}

	public LogCommand before(Date before) {
		this.before = before;
		return this;
	}

	public int count() {
		return count;
	}

	public LogCommand count(int count) {
		this.count = count;
		return this;
	}

	public int skip() {
		return skip;
	}

	public LogCommand skip(int skip) {
		this.skip = skip;
		return this;
	}

	public List<String> messages() {
		return messages;
	}

	public LogCommand messages(List<String> messages) {
		this.messages = messages;
		return this;
	}

	public List<String> authors() {
		return authors;
	}
	
	public LogCommand authors(List<String> authors) {
		this.authors = authors;
		return this;
	}
	
	public List<String> committers() {
		return committers;
	}
	
	public LogCommand commmitters(List<String> committers) {
		this.committers = committers;
		return this;
	}
	
	public boolean parentRewriting() {
		return parentRewriting;
	}
	
	public LogCommand parentRewriting(boolean parentRewriting) {
		this.parentRewriting = parentRewriting;
		return this;
	}
	
	@Override
    public List<Commit> call() {
        Commandline cmd = cmd();
        cmd.addArgs("log",
                        "--format=*** commit_begin ***%n%B%n*** commit_message_end ***%n%N"
                        + "*** commit_note_end ***%nhash:%H%nauthor:%aN%nauthorEmail:%aE%n"
                        + "committerEmail:%cE%ncommitter:%cN%nparents:%P%ncommitterDate:%cd %n"
                        + "authorDate:%ad %n",
                        "--date=raw");
        
        if (!revisions.isEmpty()) {
        	for (String revision: revisions)
        		cmd.addArgs(revision);
        } else {
        	cmd.addArgs("--branches");
        }
        
        if (before != null) 
        	cmd.addArgs("--before").addArgs(DateFormatUtils.ISO_DATE_FORMAT.format(before));
        
        if (after != null)
        	cmd.addArgs("--after").addArgs(DateFormatUtils.ISO_DATE_FORMAT.format(after));
        
        if (count != 0)
        	cmd.addArgs("-" + count);
        if (skip != 0)
        	cmd.addArgs("--skip=" + skip);

        for (String message: messages)
        	cmd.addArgs("--grep=" + message);
        
        if (parentRewriting)
        	cmd.addArgs("--parents");
        
        cmd.addArgs("--");
        
        for (String path: paths)
        	cmd.addArgs(path);

        final List<Commit> commits = new ArrayList<>();
        
        final Commit.Builder commitBuilder = Commit.builder();
        
        final AtomicBoolean commitMessageBlock = new AtomicBoolean();
        final AtomicBoolean commitNoteBlock = new AtomicBoolean();
        
        cmd.execute(new LineConsumer() {

            @Override
            public void consume(String line) {
            	if (line.equals("*** commit_begin ***")) {
            		if (commitBuilder.hash!= null) {
	            		commits.add(commitBuilder.build());
	            		commitBuilder.parentHashes.clear();
	            		commitBuilder.subject = null;
	            		commitBuilder.body = null;
	            		commitBuilder.note = null;
            		}
            		commitMessageBlock.set(true);
            	} else if (line.equals("*** commit_message_end ***")) {
            		commitMessageBlock.set(false);
            		commitNoteBlock.set(true);
            	} else if (line.equals("*** commit_note_end ***")) {
            		commitNoteBlock.set(false);
            	} else if (commitMessageBlock.get()) {
            		if (commitBuilder.subject == null)
            			commitBuilder.subject = line;
            		else if (commitBuilder.body == null)
            			commitBuilder.body = line;
            		else 
            			commitBuilder.body += "\n" + line;
            	} else if (commitNoteBlock.get()) {
            		if (commitBuilder.note == null)
            			commitBuilder.note = line;
            		else
            			commitBuilder.note += "\n" + line;
            	} else if (line.startsWith("hash:")) {
                	commitBuilder.hash = line.substring("hash:".length());
            	} else if (line.startsWith("author:")) {
                	commitBuilder.authorName = line.substring("author:".length());
            	} else if (line.startsWith("committer:")) {
                	commitBuilder.committerName = line.substring("committer:".length());
            	} else if (line.startsWith("authorDate:")) {
                	commitBuilder.authorDate = GitUtils.parseRawDate(line.substring("authorDate:".length()).trim());
            	} else if (line.startsWith("committerDate:")) {
                	commitBuilder.committerDate = GitUtils.parseRawDate(line.substring("committerDate:".length()).trim());
            	} else if (line.startsWith("authorEmail:")) {
                	commitBuilder.authorEmail = line.substring("authorEmail:".length());
            	} else if (line.startsWith("committerEmail:")) {
                	commitBuilder.committerEmail = line.substring("committerEmail:".length());
            	} else if (line.startsWith("parents:")) {
                	for (String each: StringUtils.split(line.substring("parents:".length()), " "))
                		commitBuilder.parentHashes.add(each);
                }
            }
            
        }, new LineConsumer() {

			@Override
			public void consume(String line) {
				logger.error(line);
			}
        	
        }).checkReturnCode();

        if (commitBuilder.hash != null)
        	commits.add(commitBuilder.build());

        return commits;
    }
    
}
