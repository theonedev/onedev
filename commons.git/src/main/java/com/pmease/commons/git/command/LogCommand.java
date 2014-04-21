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
import com.pmease.commons.git.GitContrib;
import com.pmease.commons.util.execution.Commandline;
import com.pmease.commons.util.execution.LineConsumer;

public class LogCommand extends GitCommand<List<Commit>> {

    private final DateTimeFormatter dateFormatter = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss Z");
    
    private String fromRev;
    
    private String toRev;
    
    private String path;
    
    private int maxCount;
    
    private int skip;

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
    
    public LogCommand maxCount(int maxCount) {
    	this.maxCount = maxCount;
    	return this;
    }

    public LogCommand skip(int skip) {
    	this.skip = skip;
    	return this;
    }
    
    @Override
    public List<Commit> call() {
        Commandline cmd = cmd();
        cmd.addArgs("log",
                        "--format=*** commit_begin ***%n%B%n*** commit_message_end ***%n%N"
                        + "*** commit_note_end ***%nhash:%H%nauthor:%aN%nauthorEmail:%aE%n"
                        + "committerEmail:%cE%ncommitter:%cN%nparents:%P%ncommitterDate:%cd %n"
                        + "authorDate:%ad %n*** commit_info_end ***",
                        "--name-status", "--find-renames", "--find-copies", "--date=iso");
        if (fromRev != null) {
        	if (toRev != null)
        		cmd.addArgs(fromRev + ".." + toRev);
        	else
        		cmd.addArgs(fromRev + "..");
        } else if (toRev != null) {
        	cmd.addArgs(toRev);
        }
        
        if (maxCount != 0)
        	cmd.addArgs("-" + maxCount);
        if (skip != 0)
        	cmd.addArgs("--skip=" + skip);
        
        cmd.addArgs("--");
        if (path != null)
        	cmd.addArgs(path);

        final List<Commit> commits = new ArrayList<>();
        
        final CommitBuilder commitBuilder = new CommitBuilder();
        
        final boolean[] commitNoteBlock = new boolean[1];
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
	            		commitBuilder.summary = null;
	            		commitBuilder.message = null;
	            		commitBuilder.note = null;
            		}
            		commitMessageBlock[0] = true;
            		fileChangesBlock[0] = false;
            	} else if (line.equals("*** commit_message_end ***")) {
            		commitMessageBlock[0] = false;
            		commitNoteBlock[0] = true;
            	} else if (line.equals("*** commit_note_end ***")) {
            		commitNoteBlock[0] = false;
            	} else if (line.equals("*** commit_info_end ***")) {
            		fileChangesBlock[0] = true;
            	} else if (commitMessageBlock[0]) {
            		if (commitBuilder.summary == null)
            			commitBuilder.summary = line;
            		else if (commitBuilder.message == null)
            			commitBuilder.message = line;
            		else 
            			commitBuilder.message += "\n" + line;
            	} else if (commitNoteBlock[0]) {
            		if (commitBuilder.note == null)
            			commitBuilder.note = line;
            		else
            			commitBuilder.note += "\n" + line;
            	} else if (fileChangesBlock[0]) {
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
            			String path = StringUtils.substringAfter(line, "\t").trim();
            			String path1;
            			String path2;
            			if (path.indexOf('\t') != -1) {
            				path1 = StringUtils.substringBefore(path, "\t").trim();
            				path2 = StringUtils.substringAfter(path, "\t").trim();
            			} else {
            				path1 = path2 = path;
            			}
            			FileChange fileChange = new FileChange(action, path1, path2);
            			commitBuilder.fileChanges.add(fileChange);
            		}
            	} else if (line.startsWith("subject:")) {
            		commitBuilder.summary = line.substring("subject:".length());
            	} else if (line.startsWith("hash:")) {
                	commitBuilder.hash = line.substring("hash:".length());
            	} else if (line.startsWith("author:")) {
                	commitBuilder.authorName = line.substring("author:".length());
            	} else if (line.startsWith("committer:")) {
                	commitBuilder.committerName = line.substring("committer:".length());
            	} else if (line.startsWith("authorDate:")) {
                	commitBuilder.authorDate = dateFormatter.parseDateTime(line.substring("authorDate:".length()).trim()).toDate();
            	} else if (line.startsWith("committerDate:")) {
                	commitBuilder.committerDate = dateFormatter.parseDateTime(line.substring("committerDate:".length()).trim()).toDate();
            	} else if (line.startsWith("authorEmail:")) {
                	commitBuilder.authorEmail = line.substring("authorEmail:".length());
            	} else if (line.startsWith("committerEmail:")) {
                	commitBuilder.committerEmail = line.substring("committerEmail:".length());
            	} else if (line.startsWith("parents:")) {
                	for (String each: StringUtils.split(line.substring("parents:".length()), " "))
                		commitBuilder.parentHashes.add(each);
                }
            }
            
        }, errorLogger).checkReturnCode();

        if (commitBuilder.hash != null)
        	commits.add(commitBuilder.build());

        return commits;
    }

    private static class CommitBuilder {
        
    	private Date committerDate;
    	
        private Date authorDate;
        
        private String authorName;
        
        private String committerName;
        
        private String authorEmail;
        
        private String committerEmail;
        
        private String hash;
        
        private String summary;
        
        private String message;
        
        private String note;
        
        private List<String> parentHashes = new ArrayList<>();
        
        private List<FileChange> fileChanges = new ArrayList<>();

    	private Commit build() {
    		return new Commit(
    				hash, 
    				new GitContrib(committerName, committerEmail, committerDate), 
    				new GitContrib(authorName, authorEmail, authorDate),
    				summary.trim(), 
    				StringUtils.isNotBlank(message)?message.trim():null, 
    				StringUtils.isNotBlank(note)?note.trim():null, 
    				parentHashes, fileChanges);
    	}
    }
}
