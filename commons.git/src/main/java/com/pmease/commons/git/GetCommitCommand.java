package com.pmease.commons.git;

import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import com.google.common.base.Preconditions;
import com.pmease.commons.util.execution.Commandline;
import com.pmease.commons.util.execution.LineConsumer;

public class GetCommitCommand extends GitCommand<Commit> {

    private final DateTimeFormatter dateFormatter = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss Z");
    
    private String revision;

    public GetCommitCommand(Git git) {
        super(git);
    }

    public GetCommitCommand revision(String revision) {
        this.revision = revision;
        return this;
    }

    @Override
    public Commit call() {
        Preconditions.checkNotNull(revision, "revision has to be specified.");

        Commandline cmd =
                git().cmd().addArgs("show",
                        "--format=\"subject:%s%nhash:%H%nauthor:%an%ncommitter:%cn%ndate:%cd\"",
                        "--quiet", "--date=iso");
        cmd.addArgs(revision);

        final Commit commit = new Commit();
        
        cmd.execute(new LineConsumer() {

            @Override
            public void consume(String line) {
                if (line.startsWith("subject:"))
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
