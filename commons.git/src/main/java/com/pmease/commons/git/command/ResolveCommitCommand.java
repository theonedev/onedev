package com.pmease.commons.git.command;

import java.io.File;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import com.google.common.base.Preconditions;
import com.pmease.commons.git.Commit;
import com.pmease.commons.util.execution.Commandline;
import com.pmease.commons.util.execution.LineConsumer;

public class ResolveCommitCommand extends GitCommand<Commit> {

    private final DateTimeFormatter dateFormatter = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss Z");
    
    private String revision;

    public ResolveCommitCommand(File repoDir) {
        super(repoDir);
    }

    public ResolveCommitCommand revision(String revision) {
        this.revision = revision;
        return this;
    }

    @Override
    public Commit call() {
        Preconditions.checkNotNull(revision, "revision has to be specified.");

        Commandline cmd = cmd().addArgs("show",
                        "--format=%B%n*** commit_message_end ***%nhash:%H%nauthor:%an%ncommitter:%cn%nparents:%P%ndate:%cd",
                        "--quiet", "--date=iso");
        cmd.addArgs(revision);

        final CommitBuilder commitBuilder = new CommitBuilder();
        
        final boolean[] commitMessageBlock = new boolean[]{true};
        cmd.execute(new LineConsumer() {

            @Override
            public void consume(String line) {
            	if (commitMessageBlock[0]) {
            		if (commitBuilder.getSubject() == null)
            			commitBuilder.setSubject(line);
            		else if (commitBuilder.getBody() == null)
            			commitBuilder.setBody(line);
            		else 
            			commitBuilder.setBody(commitBuilder.getBody() + "\n" + line);
            	} else if (line.equals("*** commit_message_end ***"))
            		commitMessageBlock[0] = false;
            	else if (line.startsWith("subject:"))
            		commitBuilder.setSubject(line.substring("subject:".length()));
                else if (line.startsWith("hash:"))
                	commitBuilder.setHash(line.substring("hash:".length()));
                else if (line.startsWith("author:"))
                	commitBuilder.setAuthor(line.substring("author:".length()));
                else if (line.startsWith("committer:"))
                	commitBuilder.setCommitter(line.substring("committer:".length()));
                else if (line.startsWith("date:"))
                	commitBuilder.setDate(dateFormatter.parseDateTime(line.substring("date:".length())).toDate());
                else if (line.startsWith("parents:")) {
                	for (String each: StringUtils.split(line.substring("parents:".length()), " "))
                		commitBuilder.getParentHashes().add(each);
                }
            }
            
        }, errorLogger()).checkReturnCode();

        return commitBuilder.build();
    }

}
