package io.onedev.server.plugin.provisioner.serverdocker;

import static io.onedev.agent.AgentUtils.callWithRegistryLogins;
import static io.onedev.agent.AgentUtils.changeOwner;
import static io.onedev.agent.AgentUtils.getOsIds;
import static io.onedev.agent.AgentUtils.newDockerKiller;
import static io.onedev.agent.workspace.WorkspaceUtils.awaitContainerReady;
import static io.onedev.agent.workspace.WorkspaceUtils.getPublishedPorts;
import static io.onedev.agent.workspace.WorkspaceUtils.upload;
import static io.onedev.k8shelper.RegistryLoginFacade.merge;
import static io.onedev.k8shelper.WorkspaceHelper.CONTAINER_READY_FILE;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

import java.io.File;
import java.io.Serializable;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;

import org.apache.commons.lang3.SystemUtils;
import org.jspecify.annotations.Nullable;

import io.onedev.agent.AgentUtils;
import io.onedev.agent.workspace.FileData;
import io.onedev.agent.workspace.GitExecutionResult;
import io.onedev.agent.workspace.WorkspaceUtils;
import io.onedev.commons.bootstrap.Bootstrap;
import io.onedev.commons.codeassist.InputSuggestion;
import io.onedev.commons.utils.ExplicitException;
import io.onedev.commons.utils.FileUtils;
import io.onedev.commons.utils.StringUtils;
import io.onedev.commons.utils.TaskLogger;
import io.onedev.commons.utils.command.Commandline;
import io.onedev.commons.utils.match.WildcardUtils;
import io.onedev.k8shelper.CacheProvisioner;
import io.onedev.k8shelper.ConfigFileProvisioner;
import io.onedev.k8shelper.RegistryLoginFacade;
import io.onedev.k8shelper.WorkspaceHelper;
import io.onedev.server.OneDev;
import io.onedev.server.annotation.Editable;
import io.onedev.server.annotation.OmitName;
import io.onedev.server.annotation.Patterns;
import io.onedev.server.annotation.ReservedOptions;
import io.onedev.server.cache.ServerWorkspaceCacheProvisioner;
import io.onedev.server.cluster.ClusterTask;
import io.onedev.server.model.Project;
import io.onedev.server.model.support.administration.DockerAware;
import io.onedev.server.model.support.administration.workspaceprovisioner.RegistryLogin;
import io.onedev.server.model.support.administration.workspaceprovisioner.WorkspaceProvisioner;
import io.onedev.server.model.support.workspace.spec.ConfigFile;
import io.onedev.server.model.support.workspace.spec.EnvVar;
import io.onedev.server.model.support.workspace.spec.UserData;
import io.onedev.server.service.SettingService;
import io.onedev.server.terminal.CommandlineShell;
import io.onedev.server.terminal.Shell;
import io.onedev.server.terminal.Terminal;
import io.onedev.server.userdata.ServerUserDataProvisioner;
import io.onedev.server.web.util.SuggestionUtils;
import io.onedev.server.web.util.Testable;
import io.onedev.server.workspace.ServerProvisionerUtils;
import io.onedev.server.workspace.WorkspaceContext;
import io.onedev.server.workspace.WorkspaceRuntime;

@Editable(order=ServerDockerProvisioner.ORDER, name="Server Docker Provisioner", description = """
			This provisioner creates workspaces inside Docker containers on OneDev server""")
public class ServerDockerProvisioner extends WorkspaceProvisioner implements DockerAware, Testable<ServerDockerProvisioner.TestData> {

	private static final long serialVersionUID = 1L;
	
	public static final int ORDER = 50;

	private static final String WORKSPACE_PATH = "/onedev-workspace";

	private List<RegistryLogin> registryLogins = new ArrayList<>();

	private String runOptions;

	private boolean alwaysPullImage = true;

	private String dockerExecutable;

	private boolean mountDockerSock;

	private String dockerSockPath;

	private String cpuLimit;

	private String memoryLimit;

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

	@Editable(order=400, description="Specify registry logins if necessary. For built-in registry, " +
			"use <code>@server_url@</code> for registry url, <code>@job_token@</code> for user name, and " +
			"access token for password")
	@Valid
	public List<RegistryLogin> getRegistryLogins() {
		return registryLogins;
	}

	public void setRegistryLogins(List<RegistryLogin> registryLogins) {
		this.registryLogins = registryLogins;
	}

	@Editable(order=40, group="Resource Settings", placeholder = "No limit", description = "" +
			"Optionally specify cpu limit for workspace containers. This will be " +
			"used as option <a href='https://docs.docker.com/config/containers/resource_constraints/#cpu' target='_blank'>--cpus</a> " +
			"of the container")
	public String getCpuLimit() {
		return cpuLimit;
	}

	public void setCpuLimit(String cpuLimit) {
		this.cpuLimit = cpuLimit;
	}

	@Editable(order=50, group="Resource Settings", placeholder = "No limit", description = "" +
			"Optionally specify memory limit for workspace containers. This will be " +
			"used as option <a href='https://docs.docker.com/config/containers/resource_constraints/#memory' target='_blank'>--memory</a> " +
			"of the container")
	public String getMemoryLimit() {
		return memoryLimit;
	}

	public void setMemoryLimit(String memoryLimit) {
		this.memoryLimit = memoryLimit;
	}

	@Editable(order=30, group="Security Settings", description="Whether or not to mount docker sock into workspace container to "
			+ "support docker operations in workspace<br>"
			+ "<b class='text-danger'>WARNING</b>: Malicious workspaces can take control of whole OneDev "
			+ "by operating the mounted docker sock. Make sure this provisioner can only be used by "
			+ "trusted workspaces if this option is enabled")
	public boolean isMountDockerSock() {
		return mountDockerSock;
	}

	public void setMountDockerSock(boolean mountDockerSock) {
		this.mountDockerSock = mountDockerSock;
	}

	@Editable(order=510, group="More Settings", placeholder="Default", description="Optionally specify docker sock to use. "
			+ "Defaults to <i>/var/run/docker.sock</i>")
	public String getDockerSockPath() {
		return dockerSockPath;
	}

	public void setDockerSockPath(String dockerSockPath) {
		this.dockerSockPath = dockerSockPath;
	}

	@Editable(order=600, group="Security Settings", description = "Whether or not to always pull image when "
			+ "running container. This option should be enabled to avoid images being replaced by "
			+ "malicious operations")
	public boolean isAlwaysPullImage() {
		return alwaysPullImage;
	}

	public void setAlwaysPullImage(boolean alwaysPullImage) {
		this.alwaysPullImage = alwaysPullImage;
	}

	@Editable(order=10000, placeholder="Any project", description="Optionally specify projects applicable for this provisioner. " +
			"Use '**', '*' or '?' for <a href='https://docs.onedev.io/appendix/path-wildcard' target='_blank'>path wildcard match</a>. " +
			"Multiple projects should be separated by space")
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

	@Editable(order=50050, group="More Settings", description="Optionally specify docker options to run container. " +
			"Multiple options should be separated by space, and single option containing spaces should be quoted")
	@ReservedOptions({"-w", "(--workdir)=.*", "-d", "--detach", "-a", "--attach", "-t", "--tty",
			"-i", "--interactive", "--rm", "--restart", "(--name)=.*"})
	public String getRunOptions() {
		return runOptions;
	}

	public void setRunOptions(String runOptions) {
		this.runOptions = runOptions;
	}

	@Editable(order=50100, group="More Settings", placeholder="Use default", description=""
			+ "Optionally specify docker executable, for instance <i>/usr/local/bin/docker</i>. "
			+ "Leave empty to use docker executable in PATH")
	public String getDockerExecutable() {
		return dockerExecutable;
	}

	public void setDockerExecutable(String dockerExecutable) {
		this.dockerExecutable = dockerExecutable;
	}

	private Commandline newDocker() {
		return AgentUtils.newDocker(getDockerExecutable(), getDockerSockPath());
	}

	private String getHostPath(String path) {
		if (Bootstrap.isInDocker()) 
			return AgentUtils.getHostPath(newDocker(), path);
		else 
			return path;
	}

	private List<RegistryLoginFacade> getRegistryLoginFacades(String token) {
		return getRegistryLogins().stream()
				.map(it -> it.getFacade(token))
				.collect(toList());
	}

	private void checkApplicable() {
		if (OneDev.getK8sService() != null) {
			throw new ExplicitException("OneDev running inside kubernetes cluster does not support workspaces yet");
		}
	}

	@Override
	public void test(TestData testData, TaskLogger jobLogger) {
		checkApplicable();
		var docker = newDocker();
		AgentUtils.testDocker(docker, getRegistryLoginFacades(testData.getDockerImage()),
				testData.getDockerImage(), getCpuLimit(), getMemoryLimit(), getRunOptions(),
				this::getHostPath, jobLogger);
	}

	@Editable(name="Specify a Docker Image to Test Against")
	public static class TestData implements Serializable {

		private static final long serialVersionUID = 1L;

		private String dockerImage;

		@Editable
		@OmitName
		@NotEmpty
		public String getDockerImage() {
			return dockerImage;
		}

		public void setDockerImage(String dockerImage) {
			this.dockerImage = dockerImage;
		}

	}

	@Override
	public WorkspaceRuntime provision(WorkspaceContext context, TaskLogger workspaceLogger) {		
		checkApplicable();

		if (!context.getSpec().isRunInContainer()) 
			throw new ExplicitException("This workspace can only be provisioned by shell provisioner");

		var serverAddress = getClusterService().getLocalServerAddress();
		workspaceLogger.log("Provisioning workspace on server '" + serverAddress + "'...");
		ServerProvisionerUtils.persistServerAddress(context.getWorkspaceId(), serverAddress);		

		var workspaceDir = ServerProvisionerUtils.getWorkspaceDir(context);
		FileUtils.createDir(workspaceDir);

		var osIds = getOsIds(workspaceLogger);
		if (SystemUtils.IS_OS_LINUX && !osIds.equals("0:0")) {
			workspaceLogger.log("Changing owner of workspace directory to host user...");		
			changeOwner(newDocker(), osIds, workspaceDir, osIds, workspaceLogger);
		}

		var provisionerRegistryLogins = getRegistryLoginFacades(context.getToken());
		var registryLogins = context.getSpec().getRegistryLogins().stream()
				.map(it -> new RegistryLoginFacade(it.getRegistryUrl(), it.getUserName(), it.getPassword()))
				.collect(toList());
		var allRegistryLogins = merge(registryLogins, provisionerRegistryLogins);

		var infoLogger = AgentUtils.newInfoLogger(workspaceLogger);
		var warningLogger = AgentUtils.newWarningLogger(workspaceLogger);

		ServerProvisionerUtils.setupRepository(context, WORKSPACE_PATH, workspaceLogger);

		var envVars = WorkspaceHelper.buildEnvVars(
				context.getSpec().getEnvVars().stream()
						.collect(toMap(EnvVar::getName, it -> it.isSecret() ? it.getSecretValue() : it.getValue())),
				OneDev.getInstance(SettingService.class).getSystemSetting().getServerUrl(),
				context.getToken(), WORKSPACE_PATH + "/work");
		
		var cacheProvisioners = new ArrayList<CacheProvisioner>();
		var cacheConfigIndex = 1;
		for (var cacheConfig : context.getSpec().getCacheConfigs()) {
			var cacheProvisioner = new ServerWorkspaceCacheProvisioner(cacheConfig.getFacade(), cacheConfigIndex++, context);
			cacheProvisioner.download(workspaceDir, workspaceLogger);
			cacheProvisioners.add(cacheProvisioner);
		}

		var userDataFacades = context.getSpec().getUserDatas().stream()
				.map(UserData::getFacade)
				.collect(Collectors.toList());
		var userDataProvisioner = new ServerUserDataProvisioner(context.getUserId(), userDataFacades);
		userDataProvisioner.download(workspaceDir, workspaceLogger);

		var configFileFacades = context.getSpec().getConfigFiles().stream()
				.map(ConfigFile::getFacade)
				.collect(Collectors.toList());
		
		var configFileProvisioner = new ConfigFileProvisioner(configFileFacades);
		configFileProvisioner.provision(workspaceDir, workspaceLogger);

		var containerWorkspacePath = WORKSPACE_PATH;

		var setupScriptConfig = context.getSetupScriptConfig();
		var entrypointArgs = WorkspaceHelper.buildEntrypointArgs(setupScriptConfig, true);
		if (setupScriptConfig != null)
			WorkspaceHelper.writeSetupScript(workspaceDir, setupScriptConfig);

		var containerReadyFile = new File(workspaceDir, CONTAINER_READY_FILE);
		if (containerReadyFile.exists())
			FileUtils.deleteFile(containerReadyFile);

		var docker = newDocker();
		var runAs = context.getSpec().getRunAs();

		if (SystemUtils.IS_OS_LINUX && !runAs.equals("0:0")) {
			workspaceLogger.log("Changing owner of workspace directory to container user...");
			changeOwner(docker, runAs, workspaceDir, osIds, workspaceLogger);
		}
		
		try {
			var containerName = "workspace-" + getName() + "-" + context.getProjectId() + "-" + context.getWorkspaceNumber();
			var userDataInitEntrypointArgs = userDataProvisioner.getInitEntrypointArgs(WORKSPACE_PATH);
			if (userDataInitEntrypointArgs != null) {
				workspaceLogger.log("Initializing new user data...");

				callWithRegistryLogins(docker, allRegistryLogins, () -> {
					var initContainerName = containerName + "-user-data-init";
					WorkspaceUtils.deleteContainerIfExist(docker, initContainerName, workspaceLogger);

					WorkspaceUtils.setCommonDockerRunOptions(docker, initContainerName, runAs, isAlwaysPullImage(),
							getCpuLimit(), getMemoryLimit());
					docker.processKiller(newDockerKiller(newDocker(), initContainerName, workspaceLogger));

					docker.addArgs("-v", getHostPath(workspaceDir.getAbsolutePath()) + ":" + WORKSPACE_PATH);

					docker.addArgs("--entrypoint", "sh", context.getSpec().getImage(), "-c", userDataInitEntrypointArgs);
					docker.execute(infoLogger, warningLogger).checkReturnCode();

					return null;
				});
			}		

			var future = OneDev.getInstance(ExecutorService.class).submit(() -> {
				workspaceLogger.log("Starting docker container...");

				callWithRegistryLogins(docker, allRegistryLogins, () -> {
					WorkspaceUtils.deleteContainerIfExist(docker, containerName, workspaceLogger);

					WorkspaceUtils.setCommonDockerRunOptions(docker, containerName, runAs, 
							isAlwaysPullImage() && userDataInitEntrypointArgs == null,
							getCpuLimit(), getMemoryLimit());
					docker.processKiller(newDockerKiller(newDocker(), containerName, workspaceLogger));

					if (!context.getSpec().getContainerPorts().isEmpty()) {
						for (int containerPort : context.getSpec().getContainerPorts())
							docker.addArgs("--expose", String.valueOf(containerPort));
						docker.addArgs("-P");
					}
					docker.addArgs("-v", getHostPath(workspaceDir.getAbsolutePath()) + ":" + containerWorkspacePath);
					
					for (var cacheProvisioner : cacheProvisioners) 
						cacheProvisioner.mountVolumes(docker, workspaceDir, path -> getHostPath(path));

					userDataProvisioner.mountVolumes(docker, workspaceDir, path -> getHostPath(path));

					configFileProvisioner.mountVolumes(docker, workspaceDir, path -> getHostPath(path));

					if (isMountDockerSock()) {
						if (getDockerSockPath() != null)
							docker.addArgs("-v", getDockerSockPath() + ":/var/run/docker.sock");
						else
							docker.addArgs("-v", "/var/run/docker.sock:/var/run/docker.sock");
					}
					if (getRunOptions() != null)
						docker.addArgs(StringUtils.parseQuoteTokens(getRunOptions()));

					for (var entry : envVars.entrySet())
						docker.addArgs("-e", entry.getKey() + "=" + entry.getValue());

					docker.addArgs("--entrypoint", "sh", context.getSpec().getImage(), "-c", entrypointArgs);

					var executeResult = docker.execute(infoLogger, warningLogger);
					if (executeResult.getReturnCode() != 0)
						throw new ExplicitException("Docker container exited with code " + executeResult.getReturnCode());
					return null;
				});
			});

			try {
				awaitContainerReady(future, containerReadyFile);

				Map<Integer, Integer> portMappings;
				if (!context.getSpec().getContainerPorts().isEmpty())
					portMappings = getPublishedPorts(newDocker(), containerName, context.getSpec().getContainerPorts());
				else
					portMappings = new HashMap<>();

				var portHost = getPortHost();
				
				var containerWorkDirPath = containerWorkspacePath + "/work";
				return new WorkspaceRuntime() {

					@Override
					public GitExecutionResult executeGitCommand(String[] gitArgs) {
						return WorkspaceUtils.executeGit(getDockerExecutable(), getDockerSockPath(),
								containerName, containerWorkDirPath, gitArgs);
					}

					@Override
					@Nullable
					public FileData readFileData(String path) {
						return WorkspaceUtils.readFileData(workspaceDir, path);
					}

					@Override
					public Shell doOpenShell(Terminal terminal) {
						var docker = newDocker();
						var shellExecutable = context.getSpec().getShell().getFacility().getExecutable();
						docker.addArgs("exec", "-it", "--detach-keys=ctrl-z,z", 
								"-w", containerWorkDirPath, containerName,
								"tmux", "new-session", shellExecutable);
						return new CommandlineShell(terminal, docker);
					}

					@Override
					public void await() {
						try {
							future.get();
						} catch (InterruptedException e) {
							future.cancel(true);
							throw new RuntimeException(e);
						} catch (ExecutionException e) {
							throw new RuntimeException(e);
						} finally {								
							if (SystemUtils.IS_OS_LINUX && !osIds.equals("0:0")) 
								changeOwner(newDocker(), osIds, workspaceDir, osIds, workspaceLogger);

							upload(workspaceDir, userDataProvisioner, cacheProvisioners, workspaceLogger);
						}
					}

					@Override
					public Map<Integer, Integer> getPortMappings() {
						return portMappings;
					}

					@Override
					public String getPortHost() {
						return portHost;
					}

				};
			} catch (Throwable t) {
				future.cancel(true);
				throw t;
			}
		} catch (Throwable t) {
			if (SystemUtils.IS_OS_LINUX && !osIds.equals("0:0")) 
				changeOwner(newDocker(), osIds, workspaceDir, osIds, workspaceLogger);
			throw t;
		}
	}

	private String getPortHost() {
		var serverHost = getClusterService().getServerHost(getClusterService().getLocalServerAddress());
		try {
			if (InetAddress.getByName(serverHost).isLoopbackAddress()) {
				try {
					serverHost = new URL(getSettingService().getSystemSetting().getServerUrl()).getHost();
				} catch (MalformedURLException e) {
					throw new RuntimeException(e);
				}
			}
		} catch (UnknownHostException e) {
			throw new RuntimeException(e);
		}
		return serverHost;
	}

	protected int getConcurrencyNumber() {
		return concurrency != null ? concurrency : 0;
	}

	@Override
	public List<RegistryLoginFacade> getRegistryLogins(String token) {
		return getRegistryLoginFacades(token);
	}

	@Override
	public <T> Future<T> submitTask(@Nullable String pinnedServerAddress, @Nullable Long pinnedAgentId,
						 ClusterTask<T> task, TaskLogger logger) {
		if (pinnedAgentId != null) {
			throw new ExplicitException("""
				This workspace is provisioned on agent previously, \
				and cannot be reprovisioned via server docker""");
		}
		logger.log("Pending resource allocation...");
		return getResourceService().submitServerTask(
				pinnedServerAddress, getName(), getConcurrencyNumber(), 1, task);
	}

	@Override
	public void deleteWorkspace(Long projectId, Long workspaceNumber, @Nullable String pinnedServer, @Nullable Long pinnedAgentId) {
		ServerProvisionerUtils.deleteWorkspace(projectId, workspaceNumber, pinnedServer);
	}

	@Override
	public boolean isApplicable(Project project) {
		return getApplicableProjects() == null || WildcardUtils.matchPath(getApplicableProjects(), project.getPath());
	}

}
