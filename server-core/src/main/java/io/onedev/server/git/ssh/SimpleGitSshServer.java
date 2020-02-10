package io.onedev.server.git.ssh;

import java.io.IOException;
import java.security.PublicKey;
import java.text.MessageFormat;
import java.util.Collections;
import java.util.concurrent.ExecutorService;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.sshd.common.config.keys.KeyUtils;
import org.apache.sshd.common.keyprovider.KeyPairProvider;
import org.apache.sshd.common.util.threads.ThreadUtils;
import org.apache.sshd.server.SshServer;
import org.apache.sshd.server.shell.UnknownCommand;
import org.eclipse.jgit.transport.ReceivePack;
import org.eclipse.jgit.transport.RemoteConfig;
import org.eclipse.jgit.transport.UploadPack;

import io.onedev.server.entitymanager.ProjectManager;
import io.onedev.server.model.Project;
import io.onedev.server.persistence.dao.Dao;
import io.onedev.server.util.ServerConfig;

@Singleton
public class SimpleGitSshServer {

    private final SshServer server;

    private final ExecutorService executorService = ThreadUtils
            .newFixedThreadPool("SimpleGitServer", 4);

    private final Dao dao;

    private final ProjectManager projectManager;

    private final ServerConfig serverConfig;

    @Inject
    public SimpleGitSshServer(
            Dao dao,
            ProjectManager projectManager,
            KeyPairProvider keyPairProvider,
            ServerConfig serverConfig) {
        this.dao = dao;
        this.projectManager = projectManager;
        this.serverConfig = serverConfig;
        this.server = SshServer.setUpDefaultServer();
        
        this.server.setKeyPairProvider(keyPairProvider);
        this.server.setPort(serverConfig.getSshPort());
        
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
        
        server.setShellFactory(new WelcomeGitShell());
        server.setPasswordAuthenticator(null);
        server.setKeyboardInteractiveAuthenticator(null);
        server.setGSSAuthenticator(null);
        server.setHostBasedAuthenticator(null);
        server.setUserAuthFactories(null);
    }
    
    /**
     * Just check that public key is present inside OneDev
     * @param userName
     * @param publicKey
     * @return
     */
    private boolean checkUserKeys(String userName, PublicKey publicKey) {
        String fingerPrint = KeyUtils.getFingerPrint(SshKeyUtils.MD5_DIGESTER, publicKey);        
        return SshKeyUtils.loadKeyByDigest(fingerPrint, dao) != null;
    }
    
    public int start() throws IOException {
        server.start();
        return server.getPort();
    }

    public void stop() throws IOException {
        executorService.shutdownNow();
        server.stop(true);
    }
    
    private class GitUploadPackCommand extends AbstractProjectAwareGitCommand {

        protected GitUploadPackCommand(String command,
                ExecutorService executorService) {
            super(command, executorService, false);
        }

        @Override
        public void run() {
            String projectName = getGitProjectName();
            Project project = projectManager.find(projectName);
            
            if (project == null) {
                onExit(-1, "Porject not found!");
            }
            
            UploadPack uploadPack = new UploadPack(project.getRepository());
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

    private class GitReceivePackCommand extends AbstractProjectAwareGitCommand {

        protected GitReceivePackCommand(String command,
                ExecutorService executorService) {
            super(command, executorService, false);
        }

        @Override
        public void run() {
            String projectName = getGitProjectName();
            Project project = projectManager.find(projectName);
            
            if (project == null) {
                onExit(-1, "Porject not found!");
            }
            
            try {
                new ReceivePack(project.getRepository()).receive(getInputStream(),
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
}
