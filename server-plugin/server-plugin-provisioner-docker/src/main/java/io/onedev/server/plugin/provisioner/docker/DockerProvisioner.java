package io.onedev.server.plugin.provisioner.docker;

import static io.onedev.agent.AgentUtils.callWithRegistryLogins;
import static io.onedev.agent.AgentUtils.changeOwner;
import static io.onedev.agent.AgentUtils.getOsIds;
import static io.onedev.agent.AgentUtils.newDockerKiller;
import static io.onedev.k8shelper.RegistryLoginFacade.merge;
import static java.util.stream.Collectors.toList;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;

import net.bytebuddy.implementation.bytecode.Throw;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.SystemUtils;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;

import io.onedev.agent.AgentUtils;
import io.onedev.commons.bootstrap.Bootstrap;
import io.onedev.commons.codeassist.InputSuggestion;
import io.onedev.commons.utils.ExplicitException;
import io.onedev.commons.utils.FileUtils;
import io.onedev.commons.utils.StringUtils;
import io.onedev.commons.utils.TaskLogger;
import io.onedev.commons.utils.command.Commandline;
import io.onedev.commons.utils.command.LineConsumer;
import io.onedev.commons.utils.match.WildcardUtils;
import io.onedev.k8shelper.KubernetesHelper;
import io.onedev.k8shelper.RegistryLoginFacade;
import io.onedev.server.OneDev;
import io.onedev.server.annotation.Editable;
import io.onedev.server.annotation.OmitName;
import io.onedev.server.annotation.Patterns;
import io.onedev.server.annotation.ReservedOptions;
import io.onedev.server.cache.WorkspaceCacheProvisioner;
import io.onedev.server.model.Project;
import io.onedev.server.model.support.administration.DockerAware;
import io.onedev.server.model.support.administration.workspaceprovisioner.RegistryLogin;
import io.onedev.server.model.support.administration.workspaceprovisioner.WorkspaceProvisioner;
import io.onedev.server.terminal.CommandlineShell;
import io.onedev.server.terminal.Shell;
import io.onedev.server.terminal.Terminal;
import io.onedev.server.web.util.SuggestionUtils;
import io.onedev.server.web.util.Testable;
import io.onedev.server.workspace.GitExecutionResult;
import io.onedev.server.workspace.WorkspaceContext;
import io.onedev.server.workspace.WorkspaceRuntime;

@Editable(order=DockerProvisioner.ORDER, name="Docker Provisioner", description = """
			This provisioner creates workspaces inside Docker containers on the OneDev server for 
			security and isolation purpose. It is currently not supported when OneDev server is 
			installed inside Kubernetes cluster""")
public class DockerProvisioner extends WorkspaceProvisioner implements DockerAware, Testable<DockerProvisioner.TestData> {

	private static final long serialVersionUID = 1L;

	private static final Logger logger = LoggerFactory.getLogger(DockerProvisioner.class);
	
	public static final int ORDER = 100;

	private static final String CONTAINER_WORKSPACE_PATH = "/onedev-workspace";

	private List<RegistryLogin> registryLogins = new ArrayList<>();

	private String runOptions;

	private boolean alwaysPullImage = true;

	private String dockerExecutable;

	private boolean mountDockerSock;

	private String dockerSockPath;

	private String cpuLimit;

	private String memoryLimit;

	private static volatile String hostInstallPath;

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
		var docker = new Commandline(AgentUtils.getDockerExecutable(getDockerExecutable()));
		AgentUtils.useDockerSock(docker, getDockerSockPath());
		return docker;
	}

	private String getHostPath(String path) {
		String installPath = Bootstrap.installDir.getAbsolutePath();
		Preconditions.checkState(path.startsWith(installPath + "/") || path.startsWith(installPath + "\\"));
		if (hostInstallPath == null) {
			if (Bootstrap.isInDocker())
				hostInstallPath = AgentUtils.getHostPath(newDocker(), installPath);
			else
				hostInstallPath = installPath;
		}
		return hostInstallPath + path.substring(installPath.length());
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
		callWithRegistryLogins(docker, getRegistryLoginFacades(testData.getDockerImage()), () -> {
			jobLogger.log("Testing specified docker image...");
			docker.args("run", "--rm");
			if (getCpuLimit() != null)
				docker.addArgs("--cpus", getCpuLimit());
			if (getMemoryLimit() != null)
				docker.args("--memory", getMemoryLimit());
			if (getRunOptions() != null)
				docker.args(StringUtils.parseQuoteTokens(getRunOptions()));
			docker.args(testData.getDockerImage(), "sh", "-c", "echo hello from container");
			docker.execute(new LineConsumer() {
				@Override
				public void consume(String line) {
					jobLogger.log(line);
				}
			}, new LineConsumer() {
				@Override
				public void consume(String line) {
					jobLogger.log(line);
				}
			}).checkReturnCode();
			return null;
		});
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

	private void deleteContainerIfExist(Commandline docker, String containerName, TaskLogger logger) {
		docker.args("rm", "-f", containerName);
		docker.execute(AgentUtils.newInfoLogger(logger), new LineConsumer() {
			@Override
			public void consume(String line) {
				if (!line.equals(containerName) && !line.contains("No such container")) 
					logger.warning(line);
			}
		});
	}

	private Map<Integer, Integer> getPublishedPorts(String containerName, Collection<Integer> containerPorts) {
		var docker = newDocker();
		docker.args("port", containerName);

		var stdoutStream = new ByteArrayOutputStream();
		var stderrStream = new ByteArrayOutputStream();
		var result = docker.execute(stdoutStream, stderrStream);
		if (result.getReturnCode() != 0)
			throw new ExplicitException(new String(stderrStream.toByteArray(), StandardCharsets.UTF_8).trim());

		var output = new String(stdoutStream.toByteArray(), StandardCharsets.UTF_8).trim();
		var portMappings = new HashMap<Integer, Integer>();
		for (var line : output.split("\\R")) {
			var fields = line.split("\\s+->\\s+", 2);
			if (fields.length == 2) {
				var slashIndex = fields[0].indexOf('/');
				var colonIndex = fields[1].lastIndexOf(':');
				try {
					if (slashIndex != -1 && colonIndex != -1 && fields[0].substring(slashIndex + 1).equals("tcp")) {
						var containerPort = Integer.parseInt(fields[0].substring(0, slashIndex));
						if (containerPorts.contains(containerPort) && !portMappings.containsKey(containerPort))
							portMappings.put(containerPort, Integer.parseInt(fields[1].substring(colonIndex + 1)));
					}
				} catch (NumberFormatException ignored) {
				}
			}
		}
		for (int containerPort : containerPorts) {
			if (!portMappings.containsKey(containerPort))
				throw new ExplicitException("Unable to determine host port mapped to container port " + containerPort);
		}
		return portMappings;
	}

	private void setCommonDockerRunOptions(Commandline docker, String containerName, String runAs) {
		docker.args("run", "--rm", "--name=" + containerName);
		if (isAlwaysPullImage())
			docker.addArgs("--pull=always");

		docker.addArgs("--user", runAs);

		if (getCpuLimit() != null)
			docker.addArgs("--cpus", getCpuLimit());
		if (getMemoryLimit() != null)
			docker.addArgs("--memory", getMemoryLimit());
	}

	@Override
	public WorkspaceRuntime provision(WorkspaceContext context, TaskLogger logger) {		
		checkApplicable();

		if (!context.getSpec().isRunInContainer()) 
			throw new ExplicitException("This workspace can only be provisioned by shell provisioner");

		var workspaceDir = getWorkspaceDir(context);
		var workDir = new File(workspaceDir, "work");
		FileUtils.createDir(workDir);

		var osIds = getOsIds(logger);
		if (SystemUtils.IS_OS_LINUX && !osIds.equals("0:0")) {
			logger.log("Changing owner of workspace directory to host user...");		
			changeOwner(newDocker(), osIds, workspaceDir, osIds, logger);
		}

		var provisionerRegistryLogins = getRegistryLoginFacades(context.getToken());
		var registryLogins = context.getSpec().getRegistryLogins().stream()
				.map(it -> new RegistryLoginFacade(it.getRegistryUrl(), it.getUserName(), it.getPassword()))
				.collect(toList());
		var allRegistryLogins = merge(registryLogins, provisionerRegistryLogins);

		var infoLogger = AgentUtils.newInfoLogger(logger);
		var warningLogger = AgentUtils.newWarningLogger(logger);

		var envVars = setupRepository(context, CONTAINER_WORKSPACE_PATH, logger);

		envVars.put("TERM", "xterm-256color");
		envVars.put("LANG", "C.UTF-8");
		envVars.put("ONEDEV_WORKDIR", CONTAINER_WORKSPACE_PATH + "/work");

		logger.log("Setting up cache...");

		var cacheProvisioner = new WorkspaceCacheProvisioner(workspaceDir, context, logger);
		cacheProvisioner.setupCaches();

		logger.log("Setting up user data...");

		var userDataProvisioner = new UserDataProvisioner(context, workspaceDir, logger);
		var userDataPathMap = userDataProvisioner.setup();
		var userDataCheckDate = new Date();

		logger.log("Setting up config files...");

		var configFileProvisioner = new ConfigFileProvisioner(workspaceDir, context);
		var configFilePathMap = configFileProvisioner.setup();

		var containerWorkspacePath = CONTAINER_WORKSPACE_PATH;

		var entrypointArgs = new StringBuilder(KubernetesHelper.GIT_TRUST_ALL_DIRS);
		var shell = context.getSpec().getShell();
		var setupCommands = shell.getSetupCommands();
		if (setupCommands != null) {
			var scriptFile = new File(workspaceDir, "setup" + shell.getFacility().getScriptExtension());
			try {
				FileUtils.writeStringToFile(
						scriptFile,
						shell.getFacility().normalizeCommands(setupCommands),
						StandardCharsets.UTF_8);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}

			var containerScriptPath = containerWorkspacePath + "/" + scriptFile.getName();
			entrypointArgs
					.append(" && cd /onedev-workspace/work && echo Running setup commands... && ")
					.append(shell.getFacility().getExecutable())
					.append(" ")
					.append(StringUtils.join(shell.getFacility().getScriptOptions(), " "))
					.append(" ")
					.append(containerScriptPath)
					.append(" && touch /onedev-workspace/successful && chmod +r /onedev-workspace/successful || exit 1");
		} else {
			entrypointArgs.append(" && touch /onedev-workspace/successful && chmod +r /onedev-workspace/successful");
		}

		entrypointArgs.append(" && tail -f /dev/null");

		var successfulFile = new File(workspaceDir, "successful");
		if (successfulFile.exists())
			FileUtils.deleteFile(successfulFile);

		var emptyUserDataMounts = new HashMap<String, File>();
		for (var userData: context.getSpec().getUserDatas()) {
			for (var path: userData.getPaths()) {
				var pathDir = Preconditions.checkNotNull(userDataPathMap.get(path));
				if (!pathDir.exists() || pathDir.list().length == 0) {
					FileUtils.createDir(pathDir);
					emptyUserDataMounts.put(path, pathDir);
				}
			}
		}

		var docker = newDocker();
		var runAs = context.getSpec().getRunAs();

		if (SystemUtils.IS_OS_LINUX && !runAs.equals("0:0")) {
			logger.log("Changing owner of workspace directory to container user...");
			changeOwner(docker, runAs, workspaceDir, osIds, logger);
		}

		if (!emptyUserDataMounts.isEmpty()) {
			logger.log("Initializing user data...");

			callWithRegistryLogins(docker, allRegistryLogins, () -> {
				var containerName = "workspace-" + getName() + "-" + context.getProjectId()
						+ "-" + context.getWorkspaceNumber() + "-user-data-init";
				deleteContainerIfExist(docker, containerName, logger);

				setCommonDockerRunOptions(docker, containerName, runAs);
				docker.processKiller(newDockerKiller(newDocker(), containerName, logger));

				var initEntrypointArgs = new StringBuilder("set -e");
				for (var entry : emptyUserDataMounts.entrySet()) {
					var containerPath = entry.getKey();
					var mountedDirPath = "/onedev-user-data/" + entry.getValue().getName();
					docker.addArgs("-v", getHostPath(entry.getValue().getAbsolutePath()) + ":" + mountedDirPath);
					initEntrypointArgs.append(" ; ");
					initEntrypointArgs.append("if [ -f '").append(containerPath).append("' ]; then ")
							.append("cp '").append(containerPath).append("' '")
							.append(mountedDirPath).append("/").append(UserDataProvisioner.MOUNT_FILE).append("'")
							.append("; elif [ -d '").append(containerPath).append("' ]; then ")
							.append("cp -a '").append(containerPath).append("/.' '").append(mountedDirPath).append("/'")
							.append("; fi");
				}

				docker.addArgs("--entrypoint", "sh", context.getSpec().getImage(), "-c", initEntrypointArgs.toString());
				docker.execute(infoLogger, warningLogger).checkReturnCode();

				return null;
			});
		}

		var containerName = "workspace-" + getName() + "-" + context.getProjectId() + "-" + context.getWorkspaceNumber();
		var future = OneDev.getInstance(ExecutorService.class).submit(() -> {
			logger.log("Starting docker container...");

			callWithRegistryLogins(docker, allRegistryLogins, () -> {
				deleteContainerIfExist(docker, containerName, logger);

				setCommonDockerRunOptions(docker, containerName, runAs);
				docker.processKiller(newDockerKiller(newDocker(), containerName, logger));

				if (!context.getSpec().getContainerPorts().isEmpty()) {
					for (int containerPort : context.getSpec().getContainerPorts())
						docker.addArgs("--expose", String.valueOf(containerPort));
					docker.addArgs("-P");
				}

				docker.addArgs("-v", getHostPath(workspaceDir.getAbsolutePath()) + ":" + containerWorkspacePath);

				for (var allocation: cacheProvisioner.getAllocations()) {
					for (var entry: allocation.getPathMap().entrySet()) {
						if (FilenameUtils.getPrefixLength(entry.getKey()) > 0)
							docker.addArgs("-v", getHostPath(entry.getValue().getAbsolutePath()) + ":" + entry.getKey());
					}
				}

				for (var entry: userDataPathMap.entrySet()) {
					File mountFrom = entry.getValue();
					var file = new File(entry.getValue(), UserDataProvisioner.MOUNT_FILE);
					if (file.exists())
						mountFrom = file;
					docker.addArgs("-v", getHostPath(mountFrom.getAbsolutePath()) + "/:" + entry.getKey());
				}

				for (var entry: configFilePathMap.entrySet())
					docker.addArgs("-v", getHostPath(entry.getValue().getAbsolutePath()) + ":" + entry.getKey());

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

				docker.addArgs("--entrypoint", "sh", context.getSpec().getImage(), "-c", entrypointArgs.toString());

				var executeResult = docker.execute(infoLogger, warningLogger);
				if (executeResult.getReturnCode() != 0)
					throw new ExplicitException("Docker container exited with code " + executeResult.getReturnCode());
				return null;
			});
		});

		while (true) {
			if (future.isDone()) {
				try {
					future.get();
				} catch (InterruptedException|ExecutionException e) {
					throw new RuntimeException(e);
				}
				throw new ExplicitException("Docker container stopped unexpectedly");
			} else if (successfulFile.exists()) {
				break;
			}
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				future.cancel(true);
				throw new RuntimeException(e);
			}
		}

		Map<Integer, Integer> portMappings;
		if (!context.getSpec().getContainerPorts().isEmpty())
			portMappings = getPublishedPorts(containerName, context.getSpec().getContainerPorts());
		else
			portMappings = new HashMap<>();

		var containerWorkDirPath = containerWorkspacePath + "/work";
		return new WorkspaceRuntime() {

			@Override
			public GitExecutionResult executeGitCommand(String[] gitArgs) {
				var docker = newDocker();
				docker.addArgs("exec", "-w", containerWorkDirPath, containerName, "git");
				docker.addArgs(gitArgs);

				var stdoutStream = new ByteArrayOutputStream();
				var stderrStream = new ByteArrayOutputStream();
				var returnCode = docker.execute(stdoutStream, stderrStream).getReturnCode();
				return new GitExecutionResult(stdoutStream.toByteArray(), stderrStream.toByteArray(), returnCode);
			}

			@Override
			public Shell doOpenShell(Terminal terminal) {
				var docker = newDocker();
				docker.addArgs("exec", "-it", "--detach-keys=ctrl-z,z", "-w", containerWorkDirPath, containerName, 						
						"tmux", "new-session", context.getSpec().getShell().getFacility().getExecutable());
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
					if (SystemUtils.IS_OS_LINUX && !osIds.equals("0:0")) {
						changeOwner(newDocker(), osIds, workspaceDir, osIds, new TaskLogger() {
							@Override
							public void log(String message, @Nullable String sessionId) {
								DockerProvisioner.logger.info(message);
							}

						});
					}
					userDataProvisioner.store(userDataCheckDate, userDataPathMap);
					cacheProvisioner.uploadCaches();
				}
			}

			@Override
			public Map<Integer, Integer> getPortMappings() {
				return portMappings;
			}

		};
	}

	@Override
	public List<RegistryLoginFacade> getRegistryLogins(String token) {
		return getRegistryLoginFacades(token);
	}

	@Override
	public boolean isApplicable(Project project) {
		return getApplicableProjects() == null || WildcardUtils.matchPath(getApplicableProjects(), project.getPath());
	}

}
