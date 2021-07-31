package io.onedev.server.ssh;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.sshd.server.Environment;
import org.apache.sshd.server.ExitCallback;
import org.apache.sshd.server.channel.ChannelSession;
import org.apache.sshd.server.command.Command;
import org.apache.sshd.server.session.ServerSession;
import org.apache.sshd.server.session.ServerSessionAware;
import org.apache.sshd.server.shell.ShellFactory;
import org.eclipse.jgit.lib.Constants;

public class DisableShellAccess implements ShellFactory {

    @Override
    public Command createShell(ChannelSession channel) {
        return new WelcomeMessage();
    }
    
    private static class WelcomeMessage implements Command, ServerSessionAware {

        private InputStream in;
        private OutputStream out;
        private OutputStream err;
        private ExitCallback callback;

        @Override
        public void start(ChannelSession channel, Environment env) throws IOException {
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
        public void destroy(ChannelSession channel) throws Exception {
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
