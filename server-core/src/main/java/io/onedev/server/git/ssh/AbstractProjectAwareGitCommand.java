package io.onedev.server.git.ssh;

import java.util.concurrent.ExecutorService;

import org.apache.sshd.server.command.AbstractCommandSupport;

public abstract class AbstractProjectAwareGitCommand extends AbstractCommandSupport {

    protected AbstractProjectAwareGitCommand(String command, ExecutorService executorService, boolean shutdownOnExit) {
        super(command, executorService, shutdownOnExit);
    }

    protected String getGitProjectName() {
        String gitCommand = getCommand();
        int lastSegment = gitCommand.lastIndexOf('/');
        int postifPos = gitCommand.lastIndexOf(".git");
        
        return gitCommand.substring(lastSegment +1, postifPos);
    }

}