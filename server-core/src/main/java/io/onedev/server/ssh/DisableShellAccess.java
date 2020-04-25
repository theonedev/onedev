package io.onedev.server.ssh;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import org.apache.sshd.common.Factory;
import org.apache.sshd.server.Environment;
import org.apache.sshd.server.ExitCallback;
import org.apache.sshd.server.SessionAware;
import org.apache.sshd.server.command.Command;
import org.apache.sshd.server.session.ServerSession;
import org.eclipse.jgit.lib.Constants;

public class DisableShellAccess implements Factory<Command> {

    @Override
    public Command create() {
        return new WelcomeMessage();
    }
    
    private static class WelcomeMessage implements Command, SessionAware {

        private InputStream in;
        private OutputStream out;
        private OutputStream err;
        private ExitCallback callback;

        @Override
        public void start(Environment env) throws IOException {
            err.write(Constants.encode(generateWelcomeMessage()));
            err.flush();
            callback.onExit(0);

            in.close();
            out.close();
            err.close();
        }

        private String generateWelcomeMessage() {
            return "Hi! This server doesn't provide shell access.\n\r";
        }

        @Override
        public void destroy() throws Exception {
        }

        @Override
        public void setInputStream(InputStream in) {
            this.in = in;
            
        }

        @Override
        public void setOutputStream(OutputStream out) {
            this.out = out;
            
        }

        @Override
        public void setErrorStream(OutputStream err) {
            this.err = err;
            
        }

        @Override
        public void setExitCallback(ExitCallback callback) {
            this.callback = callback;
        }

        @Override
        public void setSession(ServerSession session) {
        }
        
    }
}
