package io.onedev.server.plugin.provisioner.shell;

import static io.onedev.k8shelper.KubernetesHelper.cloneRepository;
import static io.onedev.k8shelper.KubernetesHelper.installGitLfs;
import static io.onedev.k8shelper.KubernetesHelper.setupGitForRemoteAccess;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Files;
import java.util.concurrent.CountDownLatch;

import javax.validation.constraints.NotEmpty;

import org.apache.commons.lang3.SystemUtils;
import org.jspecify.annotations.Nullable;

import io.onedev.agent.AgentUtils;
import io.onedev.commons.bootstrap.Bootstrap;
import io.onedev.commons.utils.ExplicitException;
import io.onedev.commons.utils.FileUtils;
import io.onedev.commons.utils.TaskLogger;
import io.onedev.commons.utils.command.Commandline;
import io.onedev.server.OneDev;
import io.onedev.server.annotation.Code;
import io.onedev.server.annotation.Editable;
import io.onedev.server.annotation.OmitName;
import io.onedev.server.git.CommandUtils;
import io.onedev.server.git.GitUtils;
import io.onedev.server.git.hook.HookUtils;
import io.onedev.server.model.Workspace;
import io.onedev.server.model.support.administration.workspaceprovisioner.WorkspaceProvisioner;
import io.onedev.server.plugin.provisioner.shell.ShellProvisioner.TestData;
import io.onedev.server.terminal.CommandlineShell;
import io.onedev.server.terminal.Shell;
import io.onedev.server.terminal.Terminal;
import io.onedev.server.web.util.Testable;
import io.onedev.server.workspace.WorkspaceContext;
import io.onedev.server.workspace.WorkspaceRuntime;

@Editable(order=ShellProvisioner.ORDER, name="Shell Provisioner", description="" +
		"This provisioner creates workspaces with OneDev server's shell facility.<br>" +
		"<b class='text-danger'>WARNING</b>: Workspaces created by this provisioner has same " +
		"permission as OneDev server process. Make sure it can only be used by trusted projects")
public class ShellProvisioner extends WorkspaceProvisioner implements Testable<TestData> {

	private static final long serialVersionUID = 1L;
	
	static final int ORDER = 400;
			
	private void checkApplicable() {
		if (OneDev.getK8sService() != null) {
			throw new ExplicitException(""
					+ "OneDev running inside kubernetes cluster does not support server shell provider. "
					+ "Please use kubernetes provider instead");
		} else if (Bootstrap.isInDocker()) {
			throw new ExplicitException("Server shell provider is only supported when OneDev is installed "
					+ "directly on bare metal/virtual machine");
		}
	}

	@Override
	public void test(TestData testData, TaskLogger jobLogger) {
		checkApplicable();
	}
	
	@Editable(name="Specify Shell/Batch Commands to Run")
	public static class TestData implements Serializable {

		private static final long serialVersionUID = 1L;

		private String commands;

		@Editable
		@OmitName
		@Code(language=Code.SHELL)
		@NotEmpty
		public String getCommands() {
			return commands;
		}

		public void setCommands(String commands) {
			this.commands = commands;
		}
		
	}

	@Override
	public WorkspaceRuntime provision(WorkspaceContext context, TaskLogger logger) {
		checkApplicable();
		
		var localServer = getClusterService().getLocalServerAddress();
		logger.log(String.format("Provisioning workspace (provisioner: %s, server: %s)...", 
				getName(), localServer));

		var workDir = Workspace.getWorkDir(context.getProjectId(), context.getWorkspaceNumber());
		var workspaceDir = workDir.getParentFile();
		FileUtils.createDir(workDir);

		var cloneInfo = context.getCloneInfo();

		var infoLogger = AgentUtils.newInfoLogger(logger);
		var errorLogger = AgentUtils.newWarningLogger(logger);

		var git = CommandUtils.newGit();
		git.workingDir(workDir);

		var userDir = new File(workspaceDir, "user");
		FileUtils.createDir(userDir);

		logger.log("Populating user configs...");
		
		for (var entry : context.getUserConfigs().entrySet()) {
			var configFile = new File(userDir, entry.getKey());
			FileUtils.createDir(configFile.getParentFile());
			try {
				Files.writeString(configFile.toPath(), entry.getValue());
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}

		setupGitForRemoteAccess(git, userDir, Bootstrap.getTrustCertsDir(),
				new File(workspaceDir, "trust-certs.pem"),
				cloneInfo, infoLogger, errorLogger);				

		git.addArgs("config", "--global", "user.name", context.getUserName());
		git.execute(infoLogger, errorLogger).checkReturnCode();
		git.clearArgs();

		git.addArgs("config", "--global", "pull.rebase", "false");
		git.execute(infoLogger, errorLogger).checkReturnCode();
		git.clearArgs();

		git.addArgs("config", "--global", "user.email", context.getUserEmail());
		git.execute(infoLogger, errorLogger).checkReturnCode();
		git.clearArgs();

		boolean repositoryCloned = false;

		if (new File(workDir, ".git").exists()) {
			git.addArgs("status");
			var statusResult = git.execute(infoLogger, errorLogger);
			git.clearArgs();
	
			if (statusResult.getReturnCode() == 0) 
				repositoryCloned = true;
		}

		if (repositoryCloned) {
			// Fix origin url in case this is a backup replica just getting active 
			git.addArgs("remote", "set-url", "origin", cloneInfo.getCloneUrl());
			git.execute(infoLogger, errorLogger).checkReturnCode();
			git.clearArgs();

			installGitLfs(git,	infoLogger, errorLogger);

			logger.log("Repository already exists, skipping clone");
		} else {
			logger.log("Cloning repository...");
			if (context.getBranch() != null) {
				cloneRepository(git, context.getProjectGitDir(), cloneInfo.getCloneUrl(),
						GitUtils.branch2ref(context.getBranch()), null, 
						context.isRetrieveLfs(), false, 0, 
						infoLogger, errorLogger);
			} else {
				git.addArgs("config", "--global", "init.defaultBranch", "main");
				git.execute(infoLogger, errorLogger).checkReturnCode();
				git.clearArgs();

				git.addArgs("clone", cloneInfo.getCloneUrl(), ".");
				git.execute(infoLogger, errorLogger).checkReturnCode();
				git.clearArgs();

				installGitLfs(git,	infoLogger, errorLogger);
			}
		}

		HookUtils.setupWorkspacePostCommitHook(new File(workDir, ".git"), context.isRetrieveLfs());

		context.getEnvVars().put("HOME", userDir.getAbsolutePath());
		context.getEnvVars().putAll(HookUtils.getWorkspacePostCommitHookEnvs(context.getToken()));
		
		return new WorkspaceRuntime() {

			private String getShellCmd(@Nullable String shell) {
				if (shell != null)
					return shell;
				else if (SystemUtils.IS_OS_WINDOWS)
					return "cmd";
				else
					return "sh";
			}
		
			@Override
			public Shell doOpenShell(Terminal terminal) {
				var shellCmd = getShellCmd(context.getShell());
				var workDir = Workspace.getWorkDir(context.getProjectId(), context.getWorkspaceNumber());
				FileUtils.createDir(workDir);
				var cmdline = new Commandline(shellCmd).workingDir(workDir).environments(context.getEnvVars());
				return new CommandlineShell(terminal, cmdline);
			}

			@Override
			public void await() {
				try {
					new CountDownLatch(1).await();
				} catch (InterruptedException e) {
					throw new RuntimeException(e);
				}
			}
			
		};
	}

}
