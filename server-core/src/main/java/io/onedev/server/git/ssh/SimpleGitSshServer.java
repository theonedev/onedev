package io.onedev.server.git.ssh;

import java.io.IOException;
import java.nio.file.Path;
import java.security.PublicKey;
import java.text.MessageFormat;
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
import org.apache.sshd.server.SshServer;
import org.apache.sshd.server.command.AbstractCommandSupport;
import org.apache.sshd.server.command.Command;
import org.apache.sshd.server.shell.UnknownCommand;
import org.apache.sshd.server.subsystem.sftp.SftpSubsystemFactory;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.transport.ReceivePack;
import org.eclipse.jgit.transport.RemoteConfig;
import org.eclipse.jgit.transport.UploadPack;
import io.onedev.server.entitymanager.ProjectManager;
import io.onedev.server.entitymanager.impl.DefaultUserManager;
import io.onedev.server.model.SshKey;
import io.onedev.server.model.User;
import io.onedev.server.persistence.dao.Dao;

@Singleton
public class SimpleGitSshServer {

    private final Repository repository;
    
    private final SshServer server;

    private final ExecutorService executorService = ThreadUtils
            .newFixedThreadPool("SimpleGitServer", 4);

    private DefaultUserManager userManager;

    private Dao dao;

    public static final BaseDigest MD5_DIGESTER = new BaseDigest("MD5", 512);

    @Inject
    public SimpleGitSshServer(ProjectManager projectManager,
            DefaultUserManager userManager,
            Dao dao,
            KeyPairProvider keyPairProvider) {
        this.userManager = userManager;
        this.dao = dao;
        this.repository = null;
        this.server = SshServer.setUpDefaultServer();
        
        this.server.setKeyPairProvider(keyPairProvider);
        this.server.setPort(40789);
        
        configureAuthentication();
        this.server.setCommandFactory(command -> {
            if (command.startsWith(RemoteConfig.DEFAULT_UPLOAD_PACK)) {
                return new GitUploadPackCommand(command, executorService);
            } else if (command.startsWith(RemoteConfig.DEFAULT_RECEIVE_PACK)) {
                return new GitReceivePackCommand(command, executorService);
            }
            return new UnknownCommand(command);
        });
        
        
    }
    
    private void configureAuthentication() {
        server.setPublickeyAuthenticator((userName, publicKey, session) -> {
            return checkUserKeys(userName, publicKey);
        });
    }
    
    
    private boolean checkUserKeys(String userName, PublicKey publicKey) {
        String fingerPrint = KeyUtils.getFingerPrint(MD5_DIGESTER, publicKey);
        User user = userManager.findByName(userName);
        List<SshKey> keys = SshUtils.loadUserKeys(user, dao);
        
        for (SshKey sshKey : keys) {
            if (fingerPrint.equals(sshKey.getDigest())) {
                return true;
            }
        }
        
        return false;
    }

    private List<NamedFactory<Command>> configureSubsystems() {
        server.setFileSystemFactory(new VirtualFileSystemFactory() {

            @Override
            protected Path computeRootDir(Session session) throws IOException {
                return SimpleGitSshServer.this.repository.getDirectory()
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
