package io.onedev.server.git.ssh;

import java.io.IOException;
import java.security.PublicKey;
import java.text.MessageFormat;
import java.util.Collections;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.apache.sshd.common.config.keys.KeyUtils;
import org.apache.sshd.common.keyprovider.KeyPairProvider;
import org.apache.sshd.server.Environment;
import org.apache.sshd.server.SshServer;
import org.apache.sshd.server.auth.AsyncAuthException;
import org.apache.sshd.server.auth.pubkey.CachingPublicKeyAuthenticator;
import org.apache.sshd.server.auth.pubkey.PublickeyAuthenticator;
import org.apache.sshd.server.session.ServerSession;
import org.apache.sshd.server.shell.UnknownCommand;
import org.eclipse.jgit.transport.ReceivePack;
import org.eclipse.jgit.transport.RemoteConfig;
import org.eclipse.jgit.transport.UploadPack;
import io.onedev.server.entitymanager.ProjectManager;
import io.onedev.server.entitymanager.SshKeyManager;
import io.onedev.server.git.ssh.command.AbstractProjectAwareGitCommand;
import io.onedev.server.git.ssh.util.SshServerUtils;
import io.onedev.server.model.Project;
import io.onedev.server.model.SshKey;
import io.onedev.server.model.User;
import io.onedev.server.persistence.SessionManager;
import io.onedev.server.persistence.dao.Dao;
import io.onedev.server.security.permission.ReadCode;
import io.onedev.server.security.permission.WriteCode;
import io.onedev.server.util.ServerConfig;
import io.onedev.server.util.work.WorkExecutor;

@Singleton
public class SimpleGitSshServer {

    private final SshServer server;
    
    private final ProjectManager projectManager;

    private final SshKeyManager sshKeyManager;

    private final SessionManager sessionManager;

    @Inject
    public SimpleGitSshServer(
            Dao dao,
            ProjectManager projectManager,
            KeyPairProvider keyPairProvider,
            ServerConfig serverConfig,
            WorkExecutor workExecutor,
            SshKeyManager sshKeyManager,
            SessionManager sessionManager) {
        this.projectManager = projectManager;
        this.sshKeyManager = sshKeyManager;
        this.sessionManager = sessionManager;
        this.server = SshServer.setUpDefaultServer();
        
        this.server.setKeyPairProvider(keyPairProvider);
        this.server.setPort(serverConfig.getSshPort());
        
        configureAuthentication();
        this.server.setCommandFactory(command -> {
            if (command.startsWith(RemoteConfig.DEFAULT_UPLOAD_PACK)) {
                return new GitUploadPackCommand(command, workExecutor, projectManager);
            } else if (command.startsWith(RemoteConfig.DEFAULT_RECEIVE_PACK)) {
                return new GitReceivePackCommand(command, workExecutor, projectManager);
            }
            return new UnknownCommand(command);
        });
    }
    
    private void configureAuthentication() {
        CachingPublicKeyAuthenticator cachingAuthenticator = 
                new CachingPublicKeyAuthenticator(new OneDevPublickeyAuthenticator());
        server.setPublickeyAuthenticator(cachingAuthenticator);
        
        server.setShellFactory(new WelcomeGitShell());
        server.setPasswordAuthenticator(null);
        server.setKeyboardInteractiveAuthenticator(null);
        server.setGSSAuthenticator(null);
        server.setHostBasedAuthenticator(null);
        server.setUserAuthFactories(null);
    }
    
    public int start() {
        try {
            server.start();
            return server.getPort();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void stop() throws IOException {
        server.stop(true);
    }
    
    private class OneDevPublickeyAuthenticator implements PublickeyAuthenticator {
        @Override
        public boolean authenticate(String userName, PublicKey publicKey, ServerSession session)
                throws AsyncAuthException {
            return checkUserKeys(userName, publicKey, session);
        }
        
        /**
         * Check that public key is present inside OneDev and retrieve the owner user
         * @param userName
         * @param publicKey
         * @param session 
         * @return
         */
        protected boolean checkUserKeys(String userName, PublicKey publicKey, ServerSession session) {
            String fingerPrint = KeyUtils.getFingerPrint(SshKeyUtils.MD5_DIGESTER, publicKey);        
            SshKey sshKey = sshKeyManager.loadKeyByDigest(fingerPrint);
            
            if (sshKey == null) {
                return false;
            }
            
            User owner = sshKey.getOwner();
            session.setAttribute(SshServerUtils.SESSION_USER_ID, owner.getId());

            return true;
        }
    }

    private class GitUploadPackCommand extends AbstractProjectAwareGitCommand {


        public GitUploadPackCommand(String command, WorkExecutor workExecutor, ProjectManager projectManager) {
            super(command, workExecutor, projectManager);
        }

        @Override
        protected void execute(Environment env) {
            String projectName = getGitProjectName();
            
            sessionManager.openSession(); 

            Project project = projectManager.find(projectName);
            try {
                if (project == null) {
                    onExit(-1, "Project not found!");
                    return;
                }
                
                if (!isUserAllowed(project, new ReadCode())) {
                    onExit(-1, "User is not allowed to use this project!");
                    return;
                }
            } finally {                
                sessionManager.closeSession();
            }
            
            UploadPack uploadPack = new UploadPack(project.getRepository());
            String gitProtocol = env.getEnv().get("GIT_PROTOCOL");
            if (gitProtocol != null) {
                uploadPack
                        .setExtraParameters(Collections.singleton(gitProtocol));
            }
            try {
                uploadPack.upload(getInputStream(), getOutputStream(),
                        getErrorStream());
                onExit(0, "Ok");
            } catch (IOException e) {
                log.warn(
                        MessageFormat.format("Could not run {0}", getCommand()),
                        e);
                onExit(-1, e.toString());
            }
        }

    }

    private class GitReceivePackCommand extends AbstractProjectAwareGitCommand {

        
        public GitReceivePackCommand(String command, WorkExecutor workExecutor, ProjectManager projectManager) {
            super(command, workExecutor, projectManager);
        }

        @Override
        protected void execute(Environment env) {
            String projectName = getGitProjectName();
            
            sessionManager.openSession(); 

            Project project = projectManager.find(projectName);
            try {
                if (project == null) {
                    onExit(-1, "Project not found!");
                    return;
                }
                
                if (!isUserAllowed(project, new WriteCode())) {
                    onExit(-1, "User is not allowed to use this project!");
                    return;
                }
            } finally {                
                sessionManager.closeSession();
            }
            
            try {
                new ReceivePack(project.getRepository()).receive(getInputStream(),
                        getOutputStream(), getErrorStream());
                onExit(0, "Ok");
            } catch (IOException e) {
                log.warn(
                        MessageFormat.format("Could not run {0}", getCommand()),
                        e);
                onExit(-1, e.toString());
            }
        }

    }
}
