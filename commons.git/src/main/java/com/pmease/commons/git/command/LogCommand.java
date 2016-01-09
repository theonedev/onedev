package com.pmease.commons.git.command;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.eclipse.jgit.util.QuotedString;
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
    
    private String after;
    
    private String before;
    
    private int count;
    
    private int skip;
    
    private boolean ignoreCase;
    
    private boolean listChangedFiles;
    
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

	public String after() {
		return after;
	}

	public LogCommand after(String after) {
		this.after = after;
		return this;
	}
	
	public LogCommand after(Date after) {
		this.after = DateFormatUtils.ISO_DATE_FORMAT.format(after);
		return this;
	}

	public String before() {
		return before;
	}

	public LogCommand before(String before) {
		this.before = before;
		return this;
	}
	
	public LogCommand before(Date before) {
		this.before = DateFormatUtils.ISO_DATE_FORMAT.format(before);		
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
	
	public LogCommand listChangedFiles(boolean listChangedFiles) {
		this.listChangedFiles = listChangedFiles;
		return this;
	}

	public boolean ignoreCase() {
		return ignoreCase;
	}
	
	public LogCommand ignoreCase(boolean ignoreCase) {
		this.ignoreCase = ignoreCase;
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
	
	public void run(final CommitConsumer consumer) {
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
        
        if (listChangedFiles)
        	cmd.addArgs("--name-only");
        
        if (ignoreCase)
        	cmd.addArgs("-i");
        
        cmd.addArgs("--");
        
        for (String path: paths)
        	cmd.addArgs(path);

        final Commit.Builder commitBuilder = Commit.builder();
        
        final AtomicBoolean commitMessageBlock = new AtomicBoolean();
        final AtomicBoolean commitNoteBlock = new AtomicBoolean();
        final AtomicBoolean changedFilesBlock = new AtomicBoolean();
        
        cmd.execute(new LineConsumer() {

            @Override
            public void consume(String line) {
            	if (line.equals("*** commit_begin ***")) {
            		if (commitBuilder.hash!= null)
	            		consumer.consume(commitBuilder.build());
            		commitBuilder.parentHashes.clear();
            		commitBuilder.subject = null;
            		commitBuilder.body = null;
            		commitBuilder.note = null;
            		commitMessageBlock.set(true);
            		changedFilesBlock.set(false);
            		if (listChangedFiles)
            			commitBuilder.changedFiles = new ArrayList<>();
            		else
            			commitBuilder.changedFiles = null;
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
        	consumer.consume(commitBuilder.build());
	}
	
	@Override
    public List<Commit> call() {
        final List<Commit> commits = new ArrayList<>();
        
        run(new CommitConsumer() {

			@Override
			public void consume(Commit commit) {
				commits.add(commit);
			}
        		
        });
        return commits;
    }
	
}
