package io.onedev.server.ssh;

import com.google.common.collect.Lists;
import io.onedev.server.ServerConfig;
import io.onedev.server.cluster.ClusterManager;
import io.onedev.server.entitymanager.SettingManager;
import io.onedev.server.event.Listen;
import io.onedev.server.event.system.SystemStarted;
import io.onedev.server.event.system.SystemStopping;
import io.onedev.server.model.User;
import org.apache.sshd.client.SshClient;
import org.apache.sshd.client.keyverifier.RequiredServerKeyVerifier;
import org.apache.sshd.client.session.ClientSession;
import org.apache.sshd.common.config.keys.KeyUtils;
import org.apache.sshd.common.keyprovider.KeyIdentityProvider;
import org.apache.sshd.common.keyprovider.KeyPairProvider;
import org.apache.sshd.common.session.SessionContext;
import org.apache.sshd.server.SshServer;
import org.apache.sshd.server.auth.pubkey.CachingPublicKeyAuthenticator;
import org.apache.sshd.server.command.Command;
import org.apache.sshd.server.shell.UnknownCommand;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Set;

@Singleton
public class DefaultSshManager implements SshManager {

	private static final int CLIENT_VERIFY_TIMEOUT = 5000;
	
	private static final int CLIENT_AUTH_TIMEOUT = 5000;
    
    private final SettingManager settingManager;
    
    private final ClusterManager clusterManager;
	
	private final ServerConfig serverConfig;
    
    private final SshAuthenticator authenticator;
    
    private final Set<CommandCreator> commandCreators;
    
    private volatile SshServer server;
	
	private volatile SshClient client;
    
    @Inject
    public DefaultSshManager(SettingManager settingManager, SshAuthenticator authenticator, 
							 Set<CommandCreator> commandCreators, ClusterManager clusterManager, 
							 ServerConfig serverConfig) {
    	this.settingManager = settingManager;
        this.authenticator = authenticator;
        this.commandCreators = commandCreators;
        this.clusterManager = clusterManager;
		this.serverConfig = serverConfig;
    }
    
    @Listen
    public void on(SystemStarted event) {
        server = SshServer.setUpDefaultServer();

        server.setPort(serverConfig.getSshPort());
        
        PrivateKey privateKey = settingManager.getSshSetting().getPrivateKey();
        PublicKey publicKey;
		try {
			publicKey = KeyUtils.recoverPublicKey(privateKey);
		} catch (GeneralSecurityException e) {
			throw new RuntimeException(e);
		}
        
        server.setKeyPairProvider(new KeyPairProvider() {

			@Override
			public Iterable<KeyPair> loadKeys(SessionContext session) 
					throws IOException, GeneralSecurityException {
	            return Lists.newArrayList(new KeyPair(publicKey, privateKey));
			}
        	
        });
        
        server.setShellFactory(new DisableShellAccess());
        
        server.setPublickeyAuthenticator(new CachingPublicKeyAuthenticator(authenticator));
        server.setKeyboardInteractiveAuthenticator(null);
        
        server.setCommandFactory((channel, commandString) -> {
        	for (CommandCreator creator: commandCreators) {
        		Command command = creator.createCommand(commandString, channel.getEnvironment().getEnv());
        		if (command != null)
        			return command;
        	}
            return new UnknownCommand(commandString);
        });

        try {
			server.start();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
        
		client = SshClient.setUpDefaultClient();
		client.setKeyIdentityProvider(KeyIdentityProvider.wrapKeyPairs(
				new KeyPair(publicKey, privateKey)));
		client.setServerKeyVerifier(new RequiredServerKeyVerifier(publicKey));
		client.start();
	}

    @Listen
    public void on(SystemStopping event) throws IOException {
		if (client != null) {
			if (client.isStarted())
				client.stop();
			client.close();
			client = null;
		}
		if (server != null) {
			if (server.isStarted())			
				server.stop();
			server.close();
			server = null;
		}
    }
    
	@Override
	public ClientSession ssh(String server) {
		try {
			String serverHost = clusterManager.getServerHost(server);
			int serverPort = clusterManager.getSshPort(server);
			ClientSession session = client.connect(User.SYSTEM_NAME, serverHost, serverPort)
					.verify(CLIENT_VERIFY_TIMEOUT).getSession();
			session.auth().verify(CLIENT_AUTH_TIMEOUT);
			return session;
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
}
