package io.onedev.server.ssh;

import java.io.IOException;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.sshd.common.keyprovider.KeyPairProvider;
import org.apache.sshd.server.SshServer;
import org.apache.sshd.server.auth.pubkey.CachingPublicKeyAuthenticator;
import org.apache.sshd.server.command.Command;
import org.apache.sshd.server.shell.UnknownCommand;

import io.onedev.commons.launcher.loader.Listen;
import io.onedev.server.event.system.SystemStarted;
import io.onedev.server.event.system.SystemStopping;
import io.onedev.server.util.ServerConfig;

@Singleton
public class SshServerLauncher {

    private final ServerConfig serverConfig;
    
    private final KeyPairProvider keyPairProvider;
    
    private final SshAuthenticator authenticator;
    
    private final Set<SshCommandCreator> commandCreators;
    
    private SshServer server;

    @Inject
    public SshServerLauncher(KeyPairProvider keyPairProvider, ServerConfig serverConfig, 
    		SshAuthenticator authenticator, Set<SshCommandCreator> commandCreators) {
    	this.keyPairProvider = keyPairProvider;
        this.serverConfig = serverConfig;
        this.authenticator = authenticator;
        this.commandCreators = commandCreators;
    }
    
    @Listen
    public void on(SystemStarted event) {
        server = SshServer.setUpDefaultServer();

        server.setPort(serverConfig.getSshPort());
        server.setKeyPairProvider(keyPairProvider);
        server.setShellFactory(new DisableShellAccess());
        
        server.setPublickeyAuthenticator(new CachingPublicKeyAuthenticator(authenticator));
        server.setKeyboardInteractiveAuthenticator(null);
        
        server.setCommandFactory(command -> {
        	for (SshCommandCreator creator: commandCreators) {
        		Command sshCommand = creator.createCommand(command);
        		if (sshCommand != null)
        			return sshCommand;
        	}
            return new UnknownCommand(command);
        });

        try {
			server.start();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
    }

    @Listen
    public void on(SystemStopping event) throws IOException {
    	if (server != null && server.isStarted())
    		server.stop(true);
    }
    
}
