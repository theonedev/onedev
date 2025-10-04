package io.onedev.server.ssh;

import static com.google.common.collect.Lists.newArrayList;

import java.io.IOException;
import java.io.ObjectStreamException;
import java.io.Serializable;
import java.security.GeneralSecurityException;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.sshd.client.SshClient;
import org.apache.sshd.client.keyverifier.RequiredServerKeyVerifier;
import org.apache.sshd.client.session.ClientSession;
import org.apache.sshd.common.config.keys.KeyUtils;
import org.apache.sshd.common.keyprovider.KeyIdentityProvider;
import org.apache.sshd.server.SshServer;
import org.apache.sshd.server.auth.pubkey.CachingPublicKeyAuthenticator;
import org.apache.sshd.server.command.Command;
import org.apache.sshd.server.shell.UnknownCommand;

import io.onedev.commons.loader.ManagedSerializedForm;
import io.onedev.server.ServerConfig;
import io.onedev.server.cluster.ClusterService;
import io.onedev.server.service.SettingService;
import io.onedev.server.event.Listen;
import io.onedev.server.event.entity.EntityPersisted;
import io.onedev.server.event.system.SystemStarted;
import io.onedev.server.event.system.SystemStopping;
import io.onedev.server.model.Setting;
import io.onedev.server.model.User;
import io.onedev.server.persistence.TransactionService;

@Singleton
public class DefaultSshService implements SshService, Serializable {

	private static final int CLIENT_VERIFY_TIMEOUT = 5000;
	
	private static final int CLIENT_AUTH_TIMEOUT = 5000;

	@Inject
    private SettingService settingService;

	@Inject
    private ClusterService clusterService;

	@Inject
	private ServerConfig serverConfig;

	@Inject
    private SshAuthenticator authenticator;

	@Inject
	private TransactionService transactionService;

	@Inject
	private Set<CommandCreator> commandCreators;
	
    private volatile SshServer server;
	
	private volatile SshClient client;

    @Listen
    public void on(SystemStarted event) {
		if (serverConfig.getSshPort() != 0)
			start();
	}
	
	private synchronized void start() {
		PrivateKey privateKey = settingService.getSshSetting().getPrivateKey();
		PublicKey publicKey;
		try {
			publicKey = KeyUtils.recoverPublicKey(privateKey);
		} catch (GeneralSecurityException e) {
			throw new RuntimeException(e);
		}

		if (server == null) {
			server = SshServer.setUpDefaultServer();
			
			var kexFactories = server.getKeyExchangeFactories();
			for (var it = kexFactories.iterator(); it.hasNext();) {
				var kexFactory = it.next();
				if (kexFactory.getName().equals("ecdh-sha2-nistp521") 
						|| kexFactory.getName().equals("ecdh-sha2-nistp384")
						|| kexFactory.getName().equals("ecdh-sha2-nistp256")) {
					it.remove();
				}
			}
			server.setKeyExchangeFactories(kexFactories);

			var sigFactories = server.getSignatureFactories();
			for (var it = sigFactories.iterator(); it.hasNext();) {
				var sigFactory = it.next();
				if (sigFactory.getName().equals("ssh-rsa"))
					it.remove();
			}
			server.setSignatureFactories(sigFactories);
			
			var macFactories = server.getMacFactories();
			for (var it = macFactories.iterator(); it.hasNext();) {
				var macFactory = it.next();
				if (macFactory.getName().equals("hmac-sha1-etm@openssh.com") 
						|| macFactory.getName().equals("hmac-sha1")) {
					it.remove();
				}
			}
			server.setMacFactories(macFactories);
			
			server.setPort(serverConfig.getSshPort());

			server.setKeyPairProvider(session -> newArrayList(new KeyPair(publicKey, privateKey)));

			server.setShellFactory(new DisableShellAccess());

			server.setPublickeyAuthenticator(new CachingPublicKeyAuthenticator(authenticator));
			server.setKeyboardInteractiveAuthenticator(null);

			server.setCommandFactory((channel, commandString) -> {
				for (CommandCreator creator : commandCreators) {
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
		}
        
		if (client == null) {
			client = SshClient.setUpDefaultClient();
			client.setKeyIdentityProvider(KeyIdentityProvider.wrapKeyPairs(
					new KeyPair(publicKey, privateKey)));
			client.setServerKeyVerifier(new RequiredServerKeyVerifier(publicKey));
			client.start();
		}
	}

    @Listen
    public void on(SystemStopping event) {
		if (serverConfig.getSshPort() != 0)
			stop();
    }
	
	private synchronized void stop() {
		try {
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
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
    
	@Override
	public ClientSession ssh(String server) {
		try {
			String serverHost = clusterService.getServerHost(server);
			int serverPort = clusterService.getSshPort(server);
			ClientSession session = client.connect(User.SYSTEM_NAME, serverHost, serverPort)
					.verify(CLIENT_VERIFY_TIMEOUT).getSession();
			session.auth().verify(CLIENT_AUTH_TIMEOUT);
			return session;
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Listen
	public void on(EntityPersisted event) {
		if (serverConfig.getSshPort() != 0 && event.getEntity() instanceof Setting) {
			var setting = (Setting) event.getEntity();
			if (setting.getKey() == Setting.Key.SSH) {
				transactionService.runAfterCommit(() -> clusterService.submitToAllServers(() -> {
					stop();
					start();
					return null;
				}));
			}
		}
	}

	public Object writeReplace() throws ObjectStreamException {
		return new ManagedSerializedForm(SshService.class);
	}

}
