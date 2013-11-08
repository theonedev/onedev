package com.pmease.commons.git.command;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import com.pmease.commons.git.Commit;
import com.pmease.commons.git.FileChange;
import com.pmease.commons.util.execution.Commandline;
import com.pmease.commons.util.execution.LineConsumer;

public class LogCommand extends GitCommand<List<Commit>> {

    private final DateTimeFormatter dateFormatter = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss Z");
    
    private String fromRev;
    
    private String toRev;
    
    private String path;
    
    private int maxCommits;

    public LogCommand(File repoDir) {
        super(repoDir);
    }

    public LogCommand fromRev(String fromRev) {
        this.fromRev = fromRev;
        return this;
    }

    public LogCommand toRev(String toRev) {
        this.toRev = toRev;
        return this;
    }
    
    public LogCommand path(String path) {
    	this.path = path;
    	return this;
    }
    
    public LogCommand maxCommits(int maxCommits) {
    	this.maxCommits = maxCommits;
    	return this;
    }

    @Override
    public List<Commit> call() {
        Commandline cmd = cmd();
        cmd.addArgs("log",
                        "--format=*** commit_begin ***%n%B%n*** commit_message_end ***%nhash:%H%nauthor:%an%ncommitter:%cn%nparents:%P%ndate:%cd %n*** commit_info_end ***",
                        "--name-status", "--no-renames", "--date=iso");
        if (fromRev != null) {
        	if (toRev != null)
        		cmd.addArgs(fromRev + ".." + toRev);
        	else
        		cmd.addArgs(fromRev + "..");
        } else if (toRev != null) {
        	cmd.addArgs(toRev);
        }
        
        if (maxCommits != 0)
        	cmd.addArgs("-" + maxCommits);
        
        if (path != null)
        	cmd.addArgs("--", path);

        final List<Commit> commits = new ArrayList<>();
        
        final CommitBuilder commitBuilder = new CommitBuilder();
        
        final boolean[] commitMessageBlock = new boolean[1];
        final boolean[] fileChangesBlock = new boolean[1];
        
        cmd.execute(new LineConsumer() {

            @Override
            public void consume(String line) {
            	if (line.equals("*** commit_begin ***")) {
            		if (commitBuilder.hash != null) {
	            		commits.add(commitBuilder.build());
	            		commitBuilder.parentHashes.clear();
	            		commitBuilder.fileChanges.clear();
	            		commitBuilder.subject = null;
	            		commitBuilder.body = null;
            		}
            		commitMessageBlock[0] = true;
            		fileChangesBlock[0] = false;
            	} else if (line.equals("*** commit_message_end ***")) {
            		commitMessageBlock[0] = false;
            	} else if (line.equals("*** commit_info_end ***")) {
            		fileChangesBlock[0] = true;
            	} else if (commitMessageBlock[0]) {
            		if (commitBuilder.subject == null)
            			commitBuilder.subject = line;
            		else if (commitBuilder.body == null)
            			commitBuilder.body = line;
            		else 
            			commitBuilder.body = commitBuilder.body + "\n" + line;
            	} else if (fileChangesBlock[0]) {
            		FileChange.Action action = null;
            		if (line.startsWith("A")) 
            			action = FileChange.Action.ADD;
            		else if (line.startsWith("M"))
            			action = FileChange.Action.MODIFY;
            		else if (line.startsWith("D"))
            			action = FileChange.Action.DELETE;
            		
            		if (action != null) {
            			String path = StringUtils.substringAfter(line, "\t").trim();
            			FileChange fileChange = new FileChange(path, action);
            			commitBuilder.fileChanges.add(fileChange);
            		}
            	} else if (line.startsWith("subject:")) {
            		commitBuilder.subject = line.substring("subject:".length());
            	} else if (line.startsWith("hash:")) {
                	commitBuilder.hash = line.substring("hash:".length());
            	} else if (line.startsWith("author:")) {
                	commitBuilder.author = line.substring("author:".length());
            	} else if (line.startsWith("committer:")) {
                	commitBuilder.committer = line.substring("committer:".length());
            	} else if (line.startsWith("date:")) {
                	commitBuilder.date = dateFormatter.parseDateTime(line.substring("date:".length()).trim()).toDate();
            	} else if (line.startsWith("parents:")) {
                	for (String each: StringUtils.split(line.substring("parents:".length()), " "))
                		commitBuilder.parentHashes.add(each);
                }
            }
            
        }, errorLogger()).checkReturnCode();

        if (commitBuilder.hash != null)
        	commits.add(commitBuilder.build());

        return commits;
    }

    private static class CommitBuilder {
        
        private Date date;
        
        private String author;
        
        private String committer;
        
        private String hash;
        
        private String subject;
        
        private String body;
        
        private List<String> parentHashes = new ArrayList<>();
        
        private List<FileChange> fileChanges = new ArrayList<>();

    	private Commit build() {
    		return new Commit(date, author, committer, hash, subject.trim(), 
    				body!=null?body.trim():null, parentHashes, fileChanges);
    	}
    }
}
