package io.onedev.server.plugin.provisioner.shell;

import static io.onedev.agent.AgentUtils.newErrorLogger;
import static io.onedev.agent.AgentUtils.newInfoLogger;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import javax.validation.constraints.NotEmpty;

import org.apache.commons.io.FilenameUtils;

import io.onedev.commons.codeassist.InputSuggestion;
import io.onedev.commons.utils.ExplicitException;
import io.onedev.commons.utils.FileUtils;
import io.onedev.commons.utils.TaskLogger;
import io.onedev.commons.utils.command.Commandline;
import io.onedev.commons.utils.match.WildcardUtils;
import io.onedev.k8shelper.KubernetesHelper;
import io.onedev.server.OneDev;
import io.onedev.server.annotation.Editable;
import io.onedev.server.annotation.Patterns;
import io.onedev.server.cache.WorkspaceCacheProvisioner;
import io.onedev.server.git.CommandUtils;
import io.onedev.server.git.hook.HookUtils;
import io.onedev.server.model.Project;
import io.onedev.server.model.Workspace;
import io.onedev.server.model.support.administration.workspaceprovisioner.WorkspaceProvisioner;
import io.onedev.server.terminal.CommandlineShell;
import io.onedev.server.terminal.Shell;
import io.onedev.server.terminal.Terminal;
import io.onedev.server.web.util.SuggestionUtils;
import io.onedev.server.web.util.Testable;
import io.onedev.server.workspace.GitExecutionResult;
import io.onedev.server.workspace.WorkspaceContext;
import io.onedev.server.workspace.WorkspaceRuntime;

@Editable(order=ShellProvisioner.ORDER, name="Shell Provisioner", description="""
		This provisioner creates workspaces with OneDev server's shell facility, and requires 
		tmux to be installed on OneDev server""")
public class ShellProvisioner extends WorkspaceProvisioner implements Testable<Testable.None> {

	private static final long serialVersionUID = 1L;
	
	static final int ORDER = 200;

	private String tmuxExecutable;

	@Editable(order=100, name="tmux Executable", placeholder="Use default", description="""
			Optionally specify <a href='https://github.com/tmux/tmux' target='_blank'>tmux</a> executable, 
			for instance <i>/usr/local/bin/tmux</i>. Leave empty to use tmux executable in PATH""")
	public String getTmuxExecutable() {
		return tmuxExecutable;
	}
	
	public void setTmuxExecutable(String tmuxExecutable) {
		this.tmuxExecutable = tmuxExecutable;
	}

	private void checkApplicable() {
		if (OneDev.getK8sService() != null) {
			throw new ExplicitException("OneDev running inside kubernetes cluster does not support workspaces yet");
		}
	}

	@Override
	public void test(None testData, TaskLogger jobLogger) {
		checkApplicable();
		
		KubernetesHelper.testTmuxAvailability(newTmux(), jobLogger);
		KubernetesHelper.testGitLfsAvailability(CommandUtils.newGit(), jobLogger);
	}

	public Commandline newTmux() {
		var tmux = getTmuxExecutable();
		if (tmux == null) 
			tmux = "tmux";
		return new Commandline(tmux);
	}

	@Editable(order=10000, description="""
			Workspaces created by this provisioner have same privilege as OneDev server process. 			
			Use '**', '*' or '?' for <a href='https://docs.onedev.io/appendix/path-wildcard' target='_blank'>path wildcard match</a>.
			Multiple projects should be separated by space.<br>
			<b class='text-danger'>WARNING</b>: Workspaces created by this provisioner have same privilege as OneDev server process. 
			Please make sure that only trusted projects can use this provisioner""")
	@Patterns(suggester="suggestProjects", path=true)
	@NotEmpty
	public String getApplicableProjects() {
		return applicableProjects;
	}

	public void setApplicableProjects(String applicableProjects) {
		this.applicableProjects = applicableProjects;
	}
	
	@SuppressWarnings("unused")
	private static List<InputSuggestion> suggestProjects(String matchWith) {
		return SuggestionUtils.suggestProjectPaths(matchWith);
	}

	@Override
	public WorkspaceRuntime provision(WorkspaceContext context, TaskLogger logger) {
		checkApplicable();

		if (context.getSpec().isRunInContainer()) 
			throw new ExplicitException("This workspace can only be provisioned by docker provisioner");

		for (var cacheConfig: context.getSpec().getCacheConfigs()) {
			for (var path: cacheConfig.getPaths()) {
				if (FilenameUtils.getPrefixLength(path) > 0)
					throw new ExplicitException("Shell provisioner does not allow absolute cache path: " + path);
			}
		}
		
		var workspaceDir = getWorkspaceDir(context);

		var envVars = setupRepository(context, workspaceDir.getAbsolutePath(), logger);

		envVars.put("TERM", "xterm-256color");
		envVars.put("LANG", "C.UTF-8");
		var workDir = new File(workspaceDir, "work");
		envVars.put("ONEDEV_WORKDIR", workDir.getAbsolutePath());

		logger.log("Setting up cache...");

		var cacheProvisioner = new WorkspaceCacheProvisioner(workspaceDir, context, logger);
		cacheProvisioner.setupCaches();

		var shell = context.getSpec().getShell();

		var setupCommands = shell.getSetupCommands();
		if (setupCommands != null) {
			logger.log("Running setup commands...");
			var commandDir = new File(workspaceDir, "command");
			FileUtils.createDir(commandDir);
			var scriptFile = new File(commandDir, "setup" + shell.getFacility().getScriptExtension());
			try {
				FileUtils.writeStringToFile(
						scriptFile,
						shell.getFacility().normalizeCommands(setupCommands),
						StandardCharsets.UTF_8);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
			var cmdline = new Commandline(shell.getFacility().getExecutable());
			cmdline.addArgs(shell.getFacility().getScriptOptions());

			cmdline.workingDir(workDir).envs(envVars);
			cmdline.addArgs(scriptFile.getAbsolutePath());
			cmdline.execute(newInfoLogger(logger), newErrorLogger(logger)).checkReturnCode();
		}

		return new WorkspaceRuntime() {

			@Override
			public GitExecutionResult executeGitCommand(String[] gitArgs) {
				var git = CommandUtils.newGit();
				git.workingDir(workDir);
				git.envs().putAll(HookUtils.getWorkspacePostCommitHookEnvs(context.getToken()));
				git.args(Arrays.asList(gitArgs));

				var stdoutStream = new ByteArrayOutputStream();
				var stderrStream = new ByteArrayOutputStream();
				var returnCode = git.execute(stdoutStream, stderrStream).getReturnCode();
				return new GitExecutionResult(stdoutStream.toByteArray(), stderrStream.toByteArray(), returnCode);
			}

			@Override
			public Shell doOpenShell(Terminal terminal) {
				var workDir = Workspace.getWorkDir(context.getProjectId(), context.getWorkspaceNumber());
				FileUtils.createDir(workDir);
				var tmux = newTmux();
				tmux.addArgs("new-session")
					.addArgs(context.getSpec().getShell().getFacility().getExecutable())
					.workingDir(workDir)
					.envs(envVars);
				return new CommandlineShell(terminal, tmux);
			}

			@Override
			public void await() {
				try {
					new CountDownLatch(1).await();
				} catch (InterruptedException e) {
					throw new RuntimeException(e);
				} finally {
					cacheProvisioner.uploadCaches();
				}
			}
			
		};
	}

	@Override
	public boolean isApplicable(Project project) {
		return WildcardUtils.matchPath(getApplicableProjects(), project.getPath());
	}

}
