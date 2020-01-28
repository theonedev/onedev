package io.onedev.server.git.server;

import java.io.IOException;
import java.nio.file.Path;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.sshd.common.NamedFactory;
import org.apache.sshd.common.config.keys.KeyUtils;
import org.apache.sshd.common.digest.BaseDigest;
import org.apache.sshd.common.file.virtualfs.VirtualFileSystemFactory;
import org.apache.sshd.common.keyprovider.KeyPairProvider;
import org.apache.sshd.common.session.Session;
import org.apache.sshd.common.util.threads.ThreadUtils;
import org.apache.sshd.server.ServerAuthenticationManager;
import org.apache.sshd.server.SshServer;
import org.apache.sshd.server.auth.UserAuth;
import org.apache.sshd.server.auth.hostbased.AcceptAllHostBasedAuthenticator;
import org.apache.sshd.server.auth.keyboard.DefaultKeyboardInteractiveAuthenticator;
import org.apache.sshd.server.command.AbstractCommandSupport;
import org.apache.sshd.server.command.Command;
import org.apache.sshd.server.session.ServerSession;
import org.apache.sshd.server.shell.UnknownCommand;
import org.apache.sshd.server.subsystem.sftp.SftpSubsystemFactory;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.transport.ReceivePack;
import org.eclipse.jgit.transport.RemoteConfig;
import org.eclipse.jgit.transport.UploadPack;

import io.onedev.server.entitymanager.ProjectManager;

@Singleton
public class SimpleGitServer {

    private final Repository repository;
    
    private final SshServer server;

    private final ExecutorService executorService = ThreadUtils
            .newFixedThreadPool("SimpleGitServer", 4);

    @Inject
    public SimpleGitServer(ProjectManager projectManager , KeyPairProvider keyPairProvider) {
        this.repository = null;
        this.server = SshServer.setUpDefaultServer();

        this.server.setKeyPairProvider(keyPairProvider);
        
        configureAuthentication();
        
        List<NamedFactory<Command>> subsystems = configureSubsystems();
        
        if (!subsystems.isEmpty()) {
            this.server.setSubsystemFactories(subsystems);
        }

        disableShell();

        this.server.setCommandFactory(command -> {
            if (command.startsWith(RemoteConfig.DEFAULT_UPLOAD_PACK)) {
                return new GitUploadPackCommand(command, executorService);
            } else if (command.startsWith(RemoteConfig.DEFAULT_RECEIVE_PACK)) {
                return new GitReceivePackCommand(command, executorService);
            }
            return new UnknownCommand(command);
        });
        
        
    }
    
    private void disableShell() {
        server.setShellFactory(null);
    }
    
    private void configureAuthentication() {
        server.setUserAuthFactories(getAuthFactories());
        server.setPasswordAuthenticator((user, pwd, session) -> {
            return true;
        });
        server.setKeyboardInteractiveAuthenticator(new DefaultKeyboardInteractiveAuthenticator() {
            @Override
            public boolean authenticate(ServerSession session, String username, List<String> responses)
                    throws Exception {
                // TODO Auto-generated method stub
                return true;
            }
        });
        server.setHostBasedAuthenticator(AcceptAllHostBasedAuthenticator.INSTANCE);
       
        server.setPublickeyAuthenticator((userName, publicKey, session) -> {
            System.out.println(KeyUtils.getFingerPrint(new BaseDigest("MD5", 512), publicKey));
            return true;
        });
    }
    
    
    private List<NamedFactory<UserAuth>> getAuthFactories() {
        List<NamedFactory<UserAuth>> authentications = new ArrayList<>();
       
        authentications.add(
                ServerAuthenticationManager.DEFAULT_USER_AUTH_PUBLIC_KEY_FACTORY);
        authentications.add(
                ServerAuthenticationManager.DEFAULT_USER_AUTH_KB_INTERACTIVE_FACTORY);
//        authentications.add(
//                ServerAuthenticationManager.DEFAULT_USER_AUTH_PASSWORD_FACTORY);
        return authentications;
    }
    
    private List<NamedFactory<Command>> configureSubsystems() {
        server.setFileSystemFactory(new VirtualFileSystemFactory() {

            @Override
            protected Path computeRootDir(Session session) throws IOException {
                return SimpleGitServer.this.repository.getDirectory()
                        .getParentFile().getAbsoluteFile().toPath();
            }
        });
        return Collections
                .singletonList((new SftpSubsystemFactory.Builder()).build());
    }
    
    public int start() throws IOException {
        server.start();
        return server.getPort();
    }

    public void stop() throws IOException {
        executorService.shutdownNow();
        server.stop(true);
    }
    
    private class GitUploadPackCommand extends AbstractCommandSupport {

        protected GitUploadPackCommand(String command,
                ExecutorService executorService) {
            super(command, executorService, false);
        }

        @Override
        public void run() {
            UploadPack uploadPack = new UploadPack(repository);
            String gitProtocol = getEnvironment().getEnv().get("GIT_PROTOCOL");
            if (gitProtocol != null) {
                uploadPack
                        .setExtraParameters(Collections.singleton(gitProtocol));
            }
            try {
                uploadPack.upload(getInputStream(), getOutputStream(),
                        getErrorStream());
                onExit(0);
            } catch (IOException e) {
                log.warn(
                        MessageFormat.format("Could not run {0}", getCommand()),
                        e);
                onExit(-1, e.toString());
            }
        }

    }

    private class GitReceivePackCommand extends AbstractCommandSupport {

        protected GitReceivePackCommand(String command,
                ExecutorService executorService) {
            super(command, executorService, false);
        }

        @Override
        public void run() {
            try {
                new ReceivePack(repository).receive(getInputStream(),
                        getOutputStream(), getErrorStream());
                onExit(0);
            } catch (IOException e) {
                log.warn(
                        MessageFormat.format("Could not run {0}", getCommand()),
                        e);
                onExit(-1, e.toString());
            }
        }

    }

    public int getPort() {
        return server.getPort();
    }
}
