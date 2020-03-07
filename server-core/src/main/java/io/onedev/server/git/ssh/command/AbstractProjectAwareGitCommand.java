package io.onedev.server.git.ssh.command;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.apache.sshd.common.session.Session;
import org.apache.sshd.common.session.SessionHolder;
import org.apache.sshd.server.Environment;
import org.apache.sshd.server.ExitCallback;
import org.apache.sshd.server.SessionAware;
import org.apache.sshd.server.command.Command;
import org.apache.sshd.server.session.ServerSession;
import org.apache.sshd.server.session.ServerSessionHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import io.onedev.server.util.concurrent.PrioritizedRunnable;
import io.onedev.server.util.work.WorkExecutor;

public abstract class AbstractProjectAwareGitCommand
        implements Command, SessionAware, SessionHolder<Session>, ServerSessionHolder {

    protected final Logger log;

    private static final int PRIORITY = 2;

    protected final String command;
    protected final WorkExecutor workExecutor;
    private InputStream inputStream;
    private OutputStream outputStream;
    private OutputStream errorStream;
    private ExitCallback exitCallBack;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private ServerSession session;


    public AbstractProjectAwareGitCommand(String command, WorkExecutor workExecutor) {
        this.command = command;
        this.workExecutor = workExecutor;

        this.log = LoggerFactory.getLogger(getClass());
    }

    protected String getGitProjectName() {
        String gitCommand = getCommand();
        int lastSegment = gitCommand.lastIndexOf('/');

        return gitCommand.substring(lastSegment + 1, gitCommand.length() - 1);
    }

    @Override
    public void start(Environment env) throws IOException {
        executor.submit(new PrioritizedRunnable(PRIORITY) {
            @Override
            public void run() {
                AbstractProjectAwareGitCommand.this.execute(env);
            }
        });
    }

    protected abstract void execute(Environment env);

    @Override
    public void destroy() throws Exception {

    }

    protected void onExit(int exitValue, String exitMessage) {
        ExitCallback cb = getExitCallBack();

        if (log.isDebugEnabled()) {
            log.debug("onExit({}) exiting - value={}, message={}", this, exitValue, exitMessage);
        }

        cb.onExit(exitValue, exitMessage);
    }

    @Override
    public void setInputStream(InputStream inputStream) {
        this.inputStream = inputStream;
    }

    @Override
    public void setOutputStream(OutputStream outputStream) {
        this.outputStream = outputStream;
    }

    @Override
    public void setErrorStream(OutputStream errorStream) {
        this.errorStream = errorStream;
    }

    @Override
    public void setExitCallback(ExitCallback exitCallBack) {
        this.exitCallBack = exitCallBack;
    }

    public String getCommand() {
        return command;
    }

    public InputStream getInputStream() {
        return inputStream;
    }

    public OutputStream getOutputStream() {
        return outputStream;
    }

    public OutputStream getErrorStream() {
        return errorStream;
    }

    public ExitCallback getExitCallBack() {
        return exitCallBack;
    }

    @Override
    public ServerSession getServerSession() {
        return session;
    }

    @Override
    public Session getSession() {
        return getServerSession();
    }

    @Override
    public void setSession(ServerSession session) {
        this.session = session;
    }
}
