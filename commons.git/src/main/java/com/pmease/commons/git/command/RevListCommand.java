package com.pmease.commons.git.command;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.time.DateFormatUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.pmease.commons.util.execution.Commandline;
import com.pmease.commons.util.execution.LineConsumer;

public class RevListCommand extends GitCommand<List<String>> {

	private static final Logger logger = LoggerFactory.getLogger(RevListCommand.class); 
	
    private List<String> revisions = new ArrayList<>();
    
    private List<String> paths = new ArrayList<>();
    
    private String after;
    
    private String before;
    
    private int count;
    
    private int skip;
    
    private boolean ignoreCase;
    
    private List<String> messages = new ArrayList<>();
    
    private List<String> authors = new ArrayList<>();
    
    private List<String> committers = new ArrayList<>();
    
    public RevListCommand(File gitDir) {
        super(gitDir);
    }

	public List<String> revisions() {
		return revisions;
	}

	public RevListCommand revisions(List<String> revisions) {
		this.revisions = revisions;
		return this;
	}

	public List<String> paths() {
		return paths;
	}

	public RevListCommand paths(List<String> paths) {
		this.paths = paths;
		return this;
	}

	public String after() {
		return after;
	}

	public RevListCommand after(String after) {
		this.after = after;
		return this;
	}
	
	public RevListCommand after(Date after) {
		this.after = DateFormatUtils.ISO_DATE_FORMAT.format(after);
		return this;
	}

	public String before() {
		return before;
	}

	public RevListCommand before(String before) {
		this.before = before;
		return this;
	}
	
	public RevListCommand before(Date before) {
		this.before = DateFormatUtils.ISO_DATE_FORMAT.format(before);		
		return this;
	}
	
	public int count() {
		return count;
	}

	public RevListCommand count(int count) {
		this.count = count;
		return this;
	}

	public int skip() {
		return skip;
	}

	public RevListCommand skip(int skip) {
		this.skip = skip;
		return this;
	}
	
	public boolean ignoreCase() {
		return ignoreCase;
	}
	
	public RevListCommand ignoreCase(boolean ignoreCase) {
		this.ignoreCase = ignoreCase;
		return this;
	}
	
	public List<String> messages() {
		return messages;
	}

	public RevListCommand messages(List<String> messages) {
		this.messages = messages;
		return this;
	}

	public List<String> authors() {
		return authors;
	}
	
	public RevListCommand authors(List<String> authors) {
		this.authors = authors;
		return this;
	}
	
	public List<String> committers() {
		return committers;
	}
	
	public RevListCommand commmitters(List<String> committers) {
		this.committers = committers;
		return this;
	}
	
	@Override
    public List<String> call() {
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
        	cmd.addArgs("--before", before);
        
        if (after != null)
        	cmd.addArgs("--after", after);
        
        if (count != 0)
        	cmd.addArgs("-" + count);
        if (skip != 0)
        	cmd.addArgs("--skip=" + skip);

        for (String author: authors)
        	cmd.addArgs("--author=" + author);
        
        for (String committer: committers)
        	cmd.addArgs("--committer=" + committer);
        
        for (String message: messages)
        	cmd.addArgs("--grep=" + message);
        
        if (ignoreCase)
        	cmd.addArgs("-i");
        
        cmd.addArgs("--");
        
        for (String path: paths)
        	cmd.addArgs(path);

        List<String> commitHashes = new ArrayList<>();
        cmd.execute(new LineConsumer() {

            @Override
            public void consume(String line) {
            	commitHashes.add(line);
            }
            
        }, new LineConsumer() {

			@Override
			public void consume(String line) {
				logger.error(line);
			}
        	
        }).checkReturnCode();
        
        return commitHashes;
	}
	
}
