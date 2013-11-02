package com.pmease.commons.git.command;

import java.io.File;

import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import com.google.common.base.Preconditions;
import com.pmease.commons.git.Commit;
import com.pmease.commons.util.execution.Commandline;
import com.pmease.commons.util.execution.LineConsumer;

public class GetCommitCommand extends GitCommand<Commit> {

    private final DateTimeFormatter dateFormatter = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss Z");
    
    private String revision;

    public GetCommitCommand(File repoDir) {
        super(repoDir);
    }

    public GetCommitCommand revision(String revision) {
        this.revision = revision;
        return this;
    }

    @Override
    public Commit call() {
        Preconditions.checkNotNull(revision, "revision has to be specified.");

        Commandline cmd = cmd().addArgs("show",
                        "--format=\"##commit_message_begin##%n%B%n##commit_message_end%nhash:%H%nauthor:%an%ncommitter:%cn%ndate:%cd\"",
                        "--quiet", "--date=iso");
        cmd.addArgs(revision);

        final Commit commit = new Commit();
        
        final boolean[] commitMessageBlock = new boolean[]{false};
        cmd.execute(new LineConsumer() {

            @Override
            public void consume(String line) {
            	if (commitMessageBlock[0]) {
            		if (commit.getSubject() == null)
            			commit.setSubject(line);
            		else if (commit.getBody() == null)
            			commit.setBody(line);
            		else 
            			commit.setBody(commit.getBody() + "\n" + line);
            	} else if (line.equals("##commit_message_begin##"))
            		commitMessageBlock[0] = true;
            	else if (line.equals("##commit_message_end##"))
            		commitMessageBlock[0] = false;
            	else if (line.startsWith("subject:"))
                    commit.setSubject(line.substring("subject:".length()));
                else if (line.startsWith("hash:"))
                    commit.setHash(line.substring("hash:".length()));
                else if (line.startsWith("author:"))
                    commit.setAuthor(line.substring("author:".length()));
                else if (line.startsWith("committer:"))
                    commit.setCommitter(line.substring("committer:".length()));
                else if (line.startsWith("date:"))
                    commit.setDate(dateFormatter.parseDateTime(line.substring("date:".length())).toDate());
            }
            
        }, errorLogger()).checkReturnCode();

        return commit;
    }

}
