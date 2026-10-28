package io.onedev.server.plugin.provisioner.serversandbox;

import java.io.Serial;
import java.io.Serializable;
import java.net.InetAddress;
import java.net.URI;
import java.net.UnknownHostException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;

import org.jspecify.annotations.Nullable;
import org.slf4j.LoggerFactory;

import io.onedev.agent.SandboxSettings;
import io.onedev.agent.workspace.FileData;
import io.onedev.agent.workspace.GitExecutionResult;
import io.onedev.agent.workspace.GitSettings;
import io.onedev.agent.workspace.ProvisionMode;
import io.onedev.agent.workspace.ProvisionSandboxWorkspaceData;
import io.onedev.agent.workspace.SandboxWorkspaceController;
import io.onedev.commons.bootstrap.Bootstrap;
import io.onedev.commons.codeassist.InputSuggestion;
import io.onedev.commons.utils.ExplicitException;
import io.onedev.commons.utils.TaskLogger;
import io.onedev.commons.utils.match.WildcardUtils;
import io.onedev.server.annotation.Editable;
import io.onedev.server.annotation.OmitName;
import io.onedev.server.annotation.Patterns;
import io.onedev.server.cluster.ClusterTask;
import io.onedev.server.model.Project;
import io.onedev.server.model.support.administration.workspaceprovisioner.WorkspaceProvisioner;
import io.onedev.server.terminal.CommandlineShell;
import io.onedev.server.terminal.Shell;
import io.onedev.server.terminal.Terminal;
import io.onedev.server.web.util.SuggestionUtils;
import io.onedev.server.web.util.Testable;
import io.onedev.server.workspace.ServerProvisionerUtils;
import io.onedev.server.workspace.WorkspaceContext;
import io.onedev.server.workspace.WorkspaceRuntime;

@Editable(order=ServerSandboxProvisioner.ORDER, name="Server Sandbox Provisioner", description="""
		Runs workspaces directly in a Docker Sandbox on the server. Setup commands and terminals run as the template image's default user.
		The sandbox is removed when the workspace is deleted.
		Select <b>Sandbox</b> as the workspace spec's provision mode. To use Docker in workspace commands,
		select a sandbox template that provides Docker in the workspace spec.
		Follow <a href='${docsite}/tutorials/sandbox/working-with-sandbox' target='_blank'>Working with Sandbox</a>
		to set up the server host. For untrusted workspaces, use a Remote Sandbox Provisioner on a dedicated agent host.""")
public class ServerSandboxProvisioner extends WorkspaceProvisioner implements Testable<ServerSandboxProvisioner.TestData> {

	@Serial
	private static final long serialVersionUID = 1L;

	public static final int ORDER = 75;

	private String sandboxExecutable = "sbx";

	private boolean alwaysPullTemplateImage = true;

	private Integer cpus;

	private String memory;

	private Integer concurrency;

	@Editable(order=120, name="CPUs", placeholder="Docker sandbox default")
	@Min(1)
	public Integer getCpus() {
		return cpus;
	}

	public void setCpus(Integer cpus) {
		this.cpus = cpus;
	}

	@Editable(order=130, placeholder="Docker sandbox default", description="Optionally specify sandbox memory, for example 2g")
	public String getMemory() {
		return memory;
	}

	public void setMemory(String memory) {
		this.memory = memory;
	}

	@Editable(order=610, group="More Settings", description="Whether to always pull the template image when creating a sandbox")
	public boolean isAlwaysPullTemplateImage() {
		return alwaysPullTemplateImage;
	}

	public void setAlwaysPullTemplateImage(boolean alwaysPullTemplateImage) {
		this.alwaysPullTemplateImage = alwaysPullTemplateImage;
	}

	@Editable(order=1000, placeholder="CPU cores", description="""
			Specify max number of workspaces this provisioner can handle concurrently.
			Leave empty to set as CPU cores""")
	@Min(1)
	public Integer getConcurrency() {
		return concurrency;
	}

	public void setConcurrency(Integer concurrency) {
		this.concurrency = concurrency;
	}

	protected int getConcurrencyNumber() {
		return concurrency != null ? concurrency : 0;
	}

	@Editable(order=10000, placeholder="Any project", description="Optionally specify projects applicable for this provisioner. "
			+ "Use '**', '*' or '?' for <a href='${docsite}/appendix/path-wildcard' target='_blank'>path wildcard match</a>. "
			+ "Multiple projects should be separated by space")
	@Patterns(suggester="suggestProjects", path=true)
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
	public boolean isApplicable(Project project) {
		return applicableProjects == null || WildcardUtils.matchPath(applicableProjects, project.getPath());
	}

	@Override
	public ProvisionMode getProvisionMode() {
		return ProvisionMode.SANDBOX;
	}

	@Editable(order=50100, group="More Settings", description="Specify the sandbox CLI executable")
	@NotEmpty
	public String getSandboxExecutable() {
		return sandboxExecutable;
	}

	public void setSandboxExecutable(String sandboxExecutable) {
		this.sandboxExecutable = sandboxExecutable;
	}

	protected SandboxSettings getSandboxSettings(@Nullable String template) {
		return new SandboxSettings(template, cpus, memory, alwaysPullTemplateImage, false,
				getSettingService().getSystemSetting().getServerUrl(), null, null, List.of());
	}

	protected ProvisionSandboxWorkspaceData getWorkspaceData(WorkspaceContext context) {
		var spec = context.getSpec();
		var git = new GitSettings(context.getUserName(), context.getUserEmail(), context.getCloneInfo(),
				context.getCommitHash(), context.getBranch(), spec.isRetrieveLfs(), spec.isRetrieveSubmodules());
		return new ProvisionSandboxWorkspaceData(context.getToken(),
				SandboxWorkspaceController.getSandboxName(context.getProjectId(), context.getWorkspaceNumber()), getSandboxSettings(spec.getTemplate()),
				git, context.getCacheConfigFacades(), context.getUserDataFacades(), context.getConfigFileFacades(),
				context.getScriptConfig(), context.getEnvVars(), spec.getExposedPorts());
	}

	@Override
	public void test(TestData testData, TaskLogger logger) {
		SandboxWorkspaceController.testSandbox(getSandboxExecutable(), getSandboxSettings(testData.getTemplate()), Bootstrap.getTrustCertsDir(), logger);
	}

	@Editable(name="Specify a Sandbox Template to Test Against")
	public static class TestData implements Serializable {

		@Serial
		private static final long serialVersionUID = 1L;

		private String template;

		@Editable(description="""
			Specify a Docker sandbox template image to test against. The image must have \
			shell, Git, Git LFS, Python 3, and tmux installed. See the \
			<a href='${docsite}/tutorials/workspace/build-sandbox-template' target='_blank'>template setup guide</a>.""")
		@OmitName
		@NotEmpty
		public String getTemplate() {
			return template;
		}

		public void setTemplate(String template) {
			this.template = template;
		}

	}

	@Override
	public <T> Future<T> submitTask(@Nullable String pinnedServerAddress, @Nullable Long pinnedAgentId,
			ClusterTask<T> task, TaskLogger logger) {
		if (pinnedAgentId != null)
			throw new ExplicitException("Workspace previously provisioned on an agent cannot be reprovisioned via server sandbox");
		logger.log("Pending resource allocation...");
		return getResourceService().submitServerTask(pinnedServerAddress, getName(), getConcurrencyNumber(), 1, task);
	}

	@Override
	public WorkspaceRuntime provision(WorkspaceContext context, TaskLogger logger) {
		var data = getWorkspaceData(context);
		var address = getClusterService().getLocalServerAddress();
		ServerProvisionerUtils.persistServerAddress(context.getWorkspaceId(), address);
		logger.log("Provisioning sandbox workspace on server '" + address + "'...");
		var host = getClusterService().getServerHost(address);
		try {
			if (InetAddress.getByName(host).isLoopbackAddress())
				host = URI.create(context.getServerUrl()).getHost();
		} catch (UnknownHostException e) {
			throw new RuntimeException(e);
		}
		var controller = new SandboxWorkspaceController(getSandboxExecutable(), data, logger);
		var provisioned = controller.provisionWorkspace(Bootstrap.getTrustCertsDir(), host);
		return new WorkspaceRuntime() {
			@Override
			public GitExecutionResult executeGitCommand(String[] args) {
				return controller.executeGit(args);
			}

			@Override
			public FileData readFile(String path) {
				return controller.readFile(path);
			}

			@Override
			public Shell doOpenShell(String shellId, Terminal terminal) {
				return new CommandlineShell(terminal,
						controller.openShell(shellId, context.getSpec().getShell().getFacility().getExecutable()),
						() -> controller.closeShell(shellId));
			}

			@Override
			public void await() {
				controller.awaitRunner(context::isDeleted);
			}

			@Override
			public Map<Integer, Integer> getPortMappings() {
				return provisioned.getPortMappings();
			}

			@Override
			public String getPortHost() {
				return provisioned.getPortHost();
			}

			@Override
			protected String getTailscaleIp() {
				return provisioned.getTailscaleIp();
			}
		};
	}

	@Override
	public void deleteWorkspace(Long projectId, Long workspaceNumber, String pinnedServerAddress, Long pinnedAgentId) {
		if (pinnedServerAddress != null) {
			getClusterService().runOnServer(pinnedServerAddress, () -> {
				SandboxWorkspaceController.deleteSandbox(getSandboxExecutable(), SandboxWorkspaceController.getSandboxName(projectId, workspaceNumber),
						new TaskLogger() {
							@Override
							public void log(String message, @Nullable String sessionId) {
								LoggerFactory.getLogger(ServerSandboxProvisioner.class).info(message);
							}
						});
				return null;
			});
		}
	}
}
