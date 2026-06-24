package io.onedev.server.plugin.provisioner.servershell;

import static io.onedev.agent.AgentUtils.testCommands;
import static io.onedev.agent.workspace.WorkspaceUtils.setupShellProvisioned;
import static io.onedev.agent.workspace.WorkspaceUtils.testTmuxAvailability;
import static io.onedev.k8shelper.WorkspaceHelper.buildEnvVars;
import static io.onedev.server.workspace.ServerProvisionerUtils.getWorkDir;
import static io.onedev.server.workspace.ServerProvisionerUtils.getWorkspaceDir;
import static io.onedev.server.workspace.ServerProvisionerUtils.persistServerAddress;
import static io.onedev.server.workspace.ServerProvisionerUtils.setupRepository;
import static java.util.stream.Collectors.toMap;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Future;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;

import org.apache.commons.io.FilenameUtils;
import org.jspecify.annotations.Nullable;

import io.onedev.agent.workspace.FileData;
import io.onedev.agent.workspace.GitExecutionResult;
import io.onedev.commons.codeassist.InputSuggestion;
import io.onedev.commons.utils.ExplicitException;
import io.onedev.commons.utils.FileUtils;
import io.onedev.commons.utils.TaskLogger;
import io.onedev.commons.utils.command.Commandline;
import io.onedev.commons.utils.match.WildcardUtils;
import io.onedev.k8shelper.CacheProvisioner;
import io.onedev.server.OneDev;
import io.onedev.server.annotation.Editable;
import io.onedev.server.annotation.Patterns;
import io.onedev.server.cache.ServerWorkspaceCacheProvisioner;
import io.onedev.server.cluster.ClusterTask;
import io.onedev.server.git.GitUtils;
import io.onedev.server.model.Project;
import io.onedev.server.model.support.administration.workspaceprovisioner.WorkspaceProvisioner;
import io.onedev.server.model.support.workspace.spec.EnvVar;
import io.onedev.server.terminal.CommandlineShell;
import io.onedev.server.terminal.Shell;
import io.onedev.server.terminal.Terminal;
import io.onedev.server.web.util.SuggestionUtils;
import io.onedev.server.web.util.Testable;
import io.onedev.server.workspace.ServerProvisionerUtils;
import io.onedev.server.workspace.WorkspaceContext;
import io.onedev.server.workspace.WorkspaceRuntime;

@Editable(order=ServerShellProvisioner.ORDER, name="Server Shell Provisioner", description="""
		This provisioner creates workspaces with OneDev server's shell facility, and requires 
		tmux to be installed on OneDev server""")
public class ServerShellProvisioner extends WorkspaceProvisioner implements Testable<Testable.None> {

	private static final long serialVersionUID = 1L;
	
	static final int ORDER = 400;

	private String tmuxExecutable;

	private Integer concurrency;

	@Editable(order=1000, placeholder = "CPU cores", description = """
			Specify max number of workspaces this provisioner can handle concurrently.
			Leave empty to set as CPU cores""")
	@Min(1)
	public Integer getConcurrency() {
		return concurrency;
	}

	public void setConcurrency(Integer concurrency) {
		this.concurrency = concurrency;
	}

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
	public void test(None testData, TaskLogger logger) {
		checkApplicable();
		
		testCommands(logger);
		testTmuxAvailability(newTmux(), logger);
	}

	public Commandline newTmux() {
		var tmux = getTmuxExecutable();
		if (tmux == null) 
			tmux = "tmux";
		return new Commandline(tmux);
	}

	@Editable(order=10000, description="""
			Workspaces created by this provisioner have same privilege as OneDev process. 			
			Use '**', '*' or '?' for <a href='https://docs.onedev.io/appendix/path-wildcard' target='_blank'>path wildcard match</a>.
			Multiple projects should be separated by space.<br>
			<b class='text-danger'>WARNING</b>: Workspaces created by this provisioner have same privilege as OneDev process. 
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
	public WorkspaceRuntime provision(WorkspaceContext context, TaskLogger workspaceLogger) {
		checkApplicable();

		if (context.getSpec().isRunInContainer()) 
			throw new ExplicitException("This workspace can only be provisioned by docker provisioner");

		var serverAddress = getClusterService().getLocalServerAddress();
		workspaceLogger.log("Provisioning workspace on server '" + serverAddress + "'...");
		persistServerAddress(context.getWorkspaceId(), serverAddress);		
		
		var workspaceDir = getWorkspaceDir(context);
		FileUtils.createDir(workspaceDir);
		setupRepository(context, workspaceDir.getAbsolutePath(), workspaceLogger);

		var trustCertsFile = new File(workspaceDir, "trust-certs.pem");

		var workDir = getWorkDir(context);
		var envVars = buildEnvVars(
				context.getSpec().getEnvVars().stream()
						.collect(toMap(EnvVar::getName, it -> it.isSecret() ? it.getSecretValue() : it.getValue())),
				context.getServerUrl(), context.getToken(), 
				trustCertsFile.exists()? trustCertsFile.getAbsolutePath(): null,
				workDir.getAbsolutePath());

		workspaceLogger.log("Setting up cache...");

		var cacheProvisioners = new ArrayList<CacheProvisioner>();
		var cacheConfigIndex = 1;
		for (var cacheConfig : context.getSpec().getCacheConfigs()) {
			for (var path : cacheConfig.getPaths()) {
				if (FilenameUtils.getPrefixLength(path) > 0)
					throw new ExplicitException("Shell provisioner does not allow absolute cache path: " + path);
			}
			var cacheProvisioner = new ServerWorkspaceCacheProvisioner(cacheConfig.getFacade(), cacheConfigIndex++, context);
			cacheProvisioner.download(workspaceDir, workspaceLogger);
			cacheProvisioners.add(cacheProvisioner);
		}

		if (context.getSetupScriptConfig() != null)
			setupShellProvisioned(context.getSetupScriptConfig(), workspaceDir, envVars, workspaceLogger);

		return new WorkspaceRuntime() {

			@Override
			public GitExecutionResult executeGitCommand(String[] gitArgs) {
				var git = GitUtils.newGit();
				git.workingDir(workDir);
				git.args(Arrays.asList(gitArgs));

				var stdoutStream = new ByteArrayOutputStream();
				var stderrStream = new ByteArrayOutputStream();
				var returnCode = git.execute(stdoutStream, stderrStream).getReturnCode();
				return new GitExecutionResult(stdoutStream.toByteArray(), stderrStream.toByteArray(), returnCode);
			}

			@Override
			@Nullable
			public FileData readFileData(String path) {
				try {
					return FileData.from(new File(workDir, path));
				} catch (IOException e) {
					throw new UncheckedIOException(e);
				}
			}

			@Override
			public Shell doOpenShell(String shellId, Terminal terminal) {
				// Use a dedicated tmux server (unique socket) per shell so that tearing down one
				// shell can never kill the tmux server shared by other shells/workspaces on this host
				var tmuxSocket = "onedev-" + shellId;
				var tmux = newTmux();
				tmux.addArgs("-L", tmuxSocket, "new-session");
				for (var envVar : envVars.entrySet()) 
					tmux.addArgs("-e", envVar.getKey() + "=" + envVar.getValue());
				tmux.addArgs(context.getSpec().getShell().getFacility().getExecutable());
				tmux.workingDir(workDir);
				return new CommandlineShell(terminal, tmux, null);
			}

			@Override
			public void await() {
				try {
					new CountDownLatch(1).await();
				} catch (InterruptedException e) {
					throw new RuntimeException(e);
				} finally {					
					try {
						for (var cacheProvisioner : cacheProvisioners) 
							cacheProvisioner.upload(workspaceDir, workspaceLogger);
					} finally {
						for (var shellId : getShellLabels().keySet())
							terminateShell(shellId);
					}
				}
			}

			@Override
			public String getPortHost() {
				throw new UnsupportedOperationException();
			}
			
		};
	}

	protected int getConcurrencyNumber() {
		return concurrency != null ? concurrency : 0;
	}

	@Override
	public <T> Future<T> submitTask(@Nullable String pinnedServerAddress, @Nullable Long pinnedAgentId,
						 ClusterTask<T> task, TaskLogger logger) {
		if (pinnedAgentId != null) {
			throw new ExplicitException("""
				This workspace is provisioned on agent previously, \
				and cannot be reprovisioned via server shell""");
		}
		logger.log("Pending resource allocation...");
		return getResourceService().submitServerTask(
				pinnedServerAddress, getName(), getConcurrencyNumber(), 1, task);
	}

	@Override
	public void deleteWorkspace(Long projectId, Long workspaceNumber, String pinnedServer, Long pinnedAgentId) {
		ServerProvisionerUtils.deleteWorkspace(projectId, workspaceNumber, pinnedServer);
	}

	@Override
	public boolean isApplicable(Project project) {
		return WildcardUtils.matchPath(getApplicableProjects(), project.getPath());
	}

}
