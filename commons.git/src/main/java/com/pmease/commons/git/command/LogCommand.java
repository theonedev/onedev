package com.pmease.commons.git.command;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import com.pmease.commons.git.Commit;
import com.pmease.commons.util.execution.Commandline;
import com.pmease.commons.util.execution.LineConsumer;

public class LogCommand extends GitCommand<List<Commit>> {

    private final DateTimeFormatter dateFormatter = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss Z");
    
    private String fromRevision;
    
    private String toRevision;
    
    private String path;
    
    private int maxCommits;

    public LogCommand(File repoDir) {
        super(repoDir);
    }

    public LogCommand fromRevision(String fromRevision) {
        this.fromRevision = fromRevision;
        return this;
    }

    public LogCommand toRevision(String toRevision) {
        this.toRevision = toRevision;
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
                        "--format=%B%n*** commit_message_end ***%nhash:%H%nauthor:%an%ncommitter:%cn%nparents:%P%ndate:%cd %n*** commit_end ***",
                        "--quiet", "--date=iso");
        if (fromRevision != null) {
        	if (toRevision != null)
        		cmd.addArgs(fromRevision + ".." + toRevision);
        	else
        		cmd.addArgs(fromRevision + "..");
        } else if (toRevision != null) {
        	cmd.addArgs(toRevision);
        }
        
        if (maxCommits != 0)
        	cmd.addArgs("-" + maxCommits);
        
        if (path != null)
        	cmd.addArgs("--", path);

        final List<Commit> commits = new ArrayList<>();
        
        final CommitBuilder commitBuilder = new CommitBuilder();
        
        final boolean[] commitMessageBlock = new boolean[]{true};
        cmd.execute(new LineConsumer() {

            @Override
            public void consume(String line) {
            	if (line.equals("*** commit_end ***")) {
            		commits.add(commitBuilder.build());
            		commitBuilder.getParentHashes().clear();
            		commitBuilder.setSubject(null);
            		commitBuilder.setBody(null);
            		commitMessageBlock[0] = true;
            	} else if (line.equals("*** commit_message_end ***")) {
            		commitMessageBlock[0] = false;
            	} else if (commitMessageBlock[0]) {
            		if (commitBuilder.getSubject() == null)
            			commitBuilder.setSubject(line);
            		else if (commitBuilder.getBody() == null)
            			commitBuilder.setBody(line);
            		else 
            			commitBuilder.setBody(commitBuilder.getBody() + "\n" + line);
            	} else if (line.startsWith("subject:")) {
            		commitBuilder.setSubject(line.substring("subject:".length()));
            	} else if (line.startsWith("hash:")) {
                	commitBuilder.setHash(line.substring("hash:".length()));
            	} else if (line.startsWith("author:")) {
                	commitBuilder.setAuthor(line.substring("author:".length()));
            	} else if (line.startsWith("committer:")) {
                	commitBuilder.setCommitter(line.substring("committer:".length()));
            	} else if (line.startsWith("date:")) {
                	commitBuilder.setDate(dateFormatter.parseDateTime(line.substring("date:".length()).trim()).toDate());
            	} else if (line.startsWith("parents:")) {
                	for (String each: StringUtils.split(line.substring("parents:".length()), " "))
                		commitBuilder.getParentHashes().add(each);
                }
            }
            
        }, errorLogger()).checkReturnCode();

        return commits;
    }

}
