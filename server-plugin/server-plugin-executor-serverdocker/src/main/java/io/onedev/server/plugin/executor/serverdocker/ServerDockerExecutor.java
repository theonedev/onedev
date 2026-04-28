package io.onedev.server.plugin.executor.serverdocker;

import static io.onedev.agent.AgentUtils.buildImage;
import static io.onedev.agent.AgentUtils.callWithRegistryLogins;
import static io.onedev.agent.AgentUtils.changeOwner;
import static io.onedev.agent.AgentUtils.createNetwork;
import static io.onedev.agent.AgentUtils.deleteNetwork;
import static io.onedev.agent.AgentUtils.getEntrypointArgs;
import static io.onedev.agent.AgentUtils.getOsIds;
import static io.onedev.agent.AgentUtils.newDockerKiller;
import static io.onedev.agent.AgentUtils.pruneBuilderCache;
import static io.onedev.agent.AgentUtils.runImagetools;
import static io.onedev.agent.AgentUtils.startService;
import static io.onedev.k8shelper.KubernetesHelper.cloneRepository;
import static io.onedev.k8shelper.KubernetesHelper.initRepository;
import static io.onedev.k8shelper.KubernetesHelper.setupGitCerts;
import static io.onedev.k8shelper.KubernetesHelper.stringifyStepPosition;
import static io.onedev.k8shelper.RegistryLoginFacade.merge;
import static java.util.stream.Collectors.toList;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import javax.validation.ConstraintValidatorContext;
import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.SystemUtils;
import org.jspecify.annotations.Nullable;

import com.google.common.base.Preconditions;

import io.onedev.agent.AgentUtils;
import io.onedev.commons.bootstrap.Bootstrap;
import io.onedev.commons.bootstrap.SecretMasker;
import io.onedev.commons.utils.ExplicitException;
import io.onedev.commons.utils.FileUtils;
import io.onedev.commons.utils.StringUtils;
import io.onedev.commons.utils.TaskLogger;
import io.onedev.commons.utils.command.Commandline;
import io.onedev.commons.utils.command.LineConsumer;
import io.onedev.k8shelper.BuildImageFacade;
import io.onedev.k8shelper.CheckoutFacade;
import io.onedev.k8shelper.CloneInfo;
import io.onedev.k8shelper.CommandFacade;
import io.onedev.k8shelper.CompositeFacade;
import io.onedev.k8shelper.KubernetesHelper;
import io.onedev.k8shelper.LeafFacade;
import io.onedev.k8shelper.LeafHandler;
import io.onedev.k8shelper.PruneBuilderCacheFacade;
import io.onedev.k8shelper.RegistryLoginFacade;
import io.onedev.k8shelper.RunContainerFacade;
import io.onedev.k8shelper.RunImagetoolsFacade;
import io.onedev.k8shelper.ServerSideFacade;
import io.onedev.k8shelper.ServerStepResult;
import io.onedev.k8shelper.SetupCacheFacade;
import io.onedev.server.OneDev;
import io.onedev.server.annotation.ClassValidating;
import io.onedev.server.annotation.Editable;
import io.onedev.server.annotation.Numeric;
import io.onedev.server.annotation.OmitName;
import io.onedev.server.annotation.ReservedOptions;
import io.onedev.server.cache.ServerJobCacheProvisioner;
import io.onedev.server.cluster.ClusterService;
import io.onedev.server.cluster.ClusterTask;
import io.onedev.server.git.CommandUtils;
import io.onedev.server.job.JobContext;
import io.onedev.server.job.JobRunnable;
import io.onedev.server.job.JobService;
import io.onedev.server.job.JobTerminal;
import io.onedev.server.job.match.JobMatch;
import io.onedev.server.job.match.JobMatchContext;
import io.onedev.server.model.support.administration.DockerAware;
import io.onedev.server.model.support.administration.jobexecutor.JobExecutor;
import io.onedev.server.model.support.administration.jobexecutor.RegistryLogin;
import io.onedev.server.plugin.executor.serverdocker.ServerDockerExecutor.TestData;
import io.onedev.server.service.ResourceService;
import io.onedev.server.terminal.CommandlineShell;
import io.onedev.server.terminal.Shell;
import io.onedev.server.validation.Validatable;
import io.onedev.server.web.util.Testable;

@Editable(order=ServerDockerExecutor.ORDER, name="Server Docker Executor", 
		description="This executor runs build jobs as docker containers on OneDev server")
@ClassValidating
public class ServerDockerExecutor extends JobExecutor implements DockerAware, Testable<TestData>, Validatable {

	private static final long serialVersionUID = 1L;
	
	static final int ORDER = 50;
	
	private List<RegistryLogin> registryLogins = new ArrayList<>();
	
	private String runOptions;
	
	private boolean alwaysPullImage = true;
	
	private String dockerExecutable;
	
	private boolean mountDockerSock;
	
	private String dockerSockPath;
	
	private String dockerBuilder = "onedev";
	
	private String networkOptions;
	
	private String cpuLimit;
	
	private String memoryLimit;
	
	private String concurrency;
	
	private transient volatile File hostBuildDir;
	
	private transient volatile LeafFacade runningStep;
	
	private transient volatile String containerName;
	
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

	@Editable(order=450, placeholder = "Number of CPU Cores", description = "" +
			"Specify max number of jobs/services this executor can run concurrently. " +
			"Leave empty to set as CPU cores")
	@Numeric
	public String getConcurrency() {
		return concurrency;
	}

	public void setConcurrency(String concurrency) {
		this.concurrency = concurrency;
	}

	@Editable(order=10000, name="Applicable Jobs", placeholder="Any job",
			description="Optionally specify applicable jobs of this executor")
	@io.onedev.server.annotation.JobMatch(withProjectCriteria = true, withJobCriteria = true)
	@Nullable
	public String getJobMatch() {
		return jobMatch;
	}

	public void setJobMatch(String jobMatch) {
		this.jobMatch = jobMatch;
	}

	@Override
	public boolean isApplicable(JobMatchContext context) {
		if (jobMatch != null)
			return JobMatch.parse(jobMatch, true, true).matches(context);
		else
			return true;
	}

	@Editable(order=510, group="More Settings", placeholder="Default", description="Optionally specify docker sock to use. "
			+ "Defaults to <i>/var/run/docker.sock</i>")
	public String getDockerSockPath() {
		return dockerSockPath;
	}

	public void setDockerSockPath(String dockerSockPath) {
		this.dockerSockPath = dockerSockPath;
	}

	@Editable(order=515, group="More Settings", name="Buildx Builder", description = "Specify dockerx builder used to " +
			"build docker image. OneDev will create the builder automatically if it does not exist. Check " +
			"<a href='https://docs.onedev.io/tutorials/cicd/insecure-docker-registry' target='_blank'>this tutorial</a> " +
			"on how to customize the builder for instance to allow publishing to insecure registries")
	@NotEmpty
	public String getDockerBuilder() {
		return dockerBuilder;
	}

	public void setDockerBuilder(String dockerBuilder) {
		this.dockerBuilder = dockerBuilder;
	}
	
	@Editable(order=600, group="Security Settings", description = "Whether or not to always pull image when " +
			"run container or build images. This option should be enabled to avoid images being replaced by " +
			"malicious jobs running on same machine")
	public boolean isAlwaysPullImage() {
		return alwaysPullImage;
	}

	public void setAlwaysPullImage(boolean alwaysPullImage) {
		this.alwaysPullImage = alwaysPullImage;
	}
	
	@Editable(order=520, group="Security Settings", description="Whether or not to mount docker sock into job container to "
			+ "support docker operations in job commands<br>"
			+ "<b class='text-danger'>WARNING</b>: Malicious jobs can take control of whole OneDev "
			+ "by operating the mounted docker sock. Make sure this executor can only be used by "
			+ "trusted jobs if this option is enabled")
	public boolean isMountDockerSock() {
		return mountDockerSock;
	}

	public void setMountDockerSock(boolean mountDockerSock) {
		this.mountDockerSock = mountDockerSock;
	}

	@Editable(order=40, group="Resource Settings", placeholder = "No limit", description = "" +
			"Optionally specify cpu limit of each job/service using this executor. This will be " +
			"used as option <a href='https://docs.docker.com/config/containers/resource_constraints/#cpu' target='_blank'>--cpus</a> " +
			"of relevant containers")
	public String getCpuLimit() {
		return cpuLimit;
	}

	public void setCpuLimit(String cpuLimit) {
		this.cpuLimit = cpuLimit;
	}

	@Editable(order=50, group="Resource Settings", placeholder = "No limit", description = "" +
			"Optionally specify memory limit of each job/service using this executor. This will be " +
			"used as option <a href='https://docs.docker.com/config/containers/resource_constraints/#memory' target='_blank'>--memory</a> " +
			"of relevant containers")
	public String getMemoryLimit() {
		return memoryLimit;
	}

	public void setMemoryLimit(String memoryLimit) {
		this.memoryLimit = memoryLimit;
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

	@Editable(order=50075, group="More Settings", description = "Optionally specify docker options to create network. " +
			"Multiple options should be separated by space, and single option containing spaces should be quoted")
	@ReservedOptions({"-d", "(--driver)=.*"})
	public String getNetworkOptions() {
		return networkOptions;
	}

	public void setNetworkOptions(String networkOptions) {
		this.networkOptions = networkOptions;
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

	private ClusterService getClusterService() {
		return OneDev.getInstance(ClusterService.class);
	}
		
	private JobService getJobService() {
		return OneDev.getInstance(JobService.class);
	}
	
	private ResourceService getResourceService() {
		return OneDev.getInstance(ResourceService.class);
	}

	private int getConcurrencyNumber() {
		if (getConcurrency() != null)
			return Integer.parseInt(getConcurrency());
		else 
			return 0;
	}
	
	private void checkApplicable() {
		if (OneDev.getK8sService() != null) {
			throw new ExplicitException(""
					+ "OneDev running inside kubernetes cluster does not support server docker executor. "
					+ "Please use kubernetes executor instead");
		}
	}
	
	@Override
	public boolean execute(JobContext jobContext, TaskLogger jobLogger) {
		ClusterTask<Boolean> runnable = () -> getJobService().runJob(jobContext, new JobRunnable() {

			private static final long serialVersionUID = 1L;

			@Override
			public boolean run(TaskLogger jobLogger) {
				notifyJobRunning(jobContext.getBuildId(), null);
				checkApplicable();

				hostBuildDir = new File(Bootstrap.getTempDir(),
						"onedev-build-" + jobContext.getProjectId() + "-" + jobContext.getBuildNumber() + "-"	+ jobContext.getSubmitSequence());
				FileUtils.createDir(hostBuildDir);
				SecretMasker.push(jobContext.getSecretMasker());
				try {
					String network = "build-"+ getName() + "-" + jobContext.getProjectId() + "-"
							+ jobContext.getBuildNumber() + "-" + jobContext.getSubmitSequence();

					String localServer = getClusterService().getLocalServerAddress();
					jobLogger.log(String.format("Executing job (executor: %s, server: %s, network: %s)...", 
							getName(), localServer, network));

					createNetwork(newDocker(), network, getNetworkOptions(), jobLogger);
					try {
						var jobToken = jobContext.getJobToken();
						for (var jobService : jobContext.getServices()) {
							var docker = newDocker();
							var registryLogins = merge(jobService.getRegistryLogins(), getRegistryLogins(jobToken));
							callWithRegistryLogins(docker, registryLogins, () -> {
								startService(docker, network, jobService, getCpuLimit(), getMemoryLimit(), jobLogger);
								return null;
							});
						}

						File hostWorkDir = new File(hostBuildDir, "work");
						FileUtils.createDir(hostWorkDir);

						var cacheProvisioner = new ServerJobCacheProvisioner(hostBuildDir, jobContext, jobLogger);
						
						try {
							jobLogger.log("Copying job dependencies...");
							getJobService().copyDependencies(jobContext, hostWorkDir);

							var containerBuildDirPath = "/onedev-build";
							var containerWorkDirPath = "/onedev-build/work";
							var containerTrustCertsFilePath = "/onedev-build/trust-certs.pem";

							var osIds = getOsIds(jobLogger);
							getJobService().reportJobWorkDir(jobContext, containerWorkDirPath);
							CompositeFacade entryFacade = new CompositeFacade(jobContext.getActions());
							var successful = entryFacade.execute(new LeafHandler() {

								private int runStepContainer(Commandline docker, String image, String runAs, 
															 @Nullable String entrypoint, List<String> arguments, 
															 Map<String, String> environments, @Nullable String workingDir, 
															 Map<String, String> volumeMounts, List<Integer> position, boolean useTTY) {
									containerName = network + "-step-" + stringifyStepPosition(position);
									try {
										docker.args("run", "--name=" + containerName, "--network=" + network);
										if (isAlwaysPullImage())
											docker.addArgs("--pull=always");
										
										docker.addArgs("--user", runAs);
										
										if (getCpuLimit() != null)
											docker.addArgs("--cpus", getCpuLimit());
										if (getMemoryLimit() != null)
											docker.addArgs("--memory", getMemoryLimit());
										if (getRunOptions() != null)
											docker.addArgs(StringUtils.parseQuoteTokens(getRunOptions()));

										docker.addArgs("-v", getHostPath(hostBuildDir.getAbsolutePath()) + ":" + containerBuildDirPath);

										for (Map.Entry<String, String> entry : volumeMounts.entrySet()) {
											if (entry.getKey().contains(".."))
												throw new ExplicitException("Volume mount source path should not contain '..'");
											String hostPath = getHostPath(new File(hostWorkDir, entry.getKey()).getAbsolutePath());
											docker.addArgs("-v", hostPath + ":" + entry.getValue());
										}

										if (entrypoint != null) 
											docker.addArgs("-w", containerWorkDirPath);
										else if (workingDir != null) 
											docker.addArgs("-w", workingDir);

										for (var allocation: cacheProvisioner.getAllocations()) {
											for (var entry: allocation.getPathMap().entrySet()) {
												if (FilenameUtils.getPrefixLength(entry.getKey()) > 0)
													docker.addArgs("-v", getHostPath(entry.getValue().getAbsolutePath()) + ":" + entry.getKey());
											}
										}

										if (isMountDockerSock()) {
											if (getDockerSockPath() != null) 
												docker.addArgs("-v", getDockerSockPath() + ":/var/run/docker.sock");
											else 
												docker.addArgs("-v", "/var/run/docker.sock:/var/run/docker.sock");
										}

										for (Map.Entry<String, String> entry : environments.entrySet())
											docker.addArgs("-e", entry.getKey() + "=" + entry.getValue());

										docker.addArgs("-e", "ONEDEV_WORKDIR=" + containerWorkDirPath);

										if (useTTY)
											docker.addArgs("-t");

										if (entrypoint != null)
											docker.addArgs("--entrypoint=" + entrypoint);
										
										docker.addArgs(image);
										docker.addArgs(arguments.toArray(new String[0]));
										docker.processKiller(newDockerKiller(newDocker(), containerName, jobLogger));
																				
										var result = docker.execute(AgentUtils.newInfoLogger(jobLogger),
												AgentUtils.newWarningLogger(jobLogger), null);
										return result.getReturnCode();													
									} finally {
										containerName = null;
									}
								}
								
								@Override
								public boolean execute(LeafFacade facade, List<Integer> position) {
									return AgentUtils.runStep(entryFacade, position, jobLogger, () -> {
										runningStep = facade;
										try {
											return doExecute(facade, position);
										} finally {
											runningStep = null;
										}
									});
								}
								
								private boolean doExecute(LeafFacade facade, List<Integer> position) {
									if (facade instanceof CommandFacade) {
										CommandFacade commandFacade = ((CommandFacade) facade).replacePlaceholders(hostBuildDir);

										if (commandFacade.getImage() == null) {
											throw new ExplicitException("This step can only be executed by server shell "
													+ "executor or remote shell executor");
										}
										var entrypointArgs = getEntrypointArgs(hostBuildDir, commandFacade, position);

										var docker = newDocker();
										var registryLogins = merge(commandFacade.getRegistryLogins(), getRegistryLogins(jobToken));

										var runAs = commandFacade.getRunAs();
										if (SystemUtils.IS_OS_LINUX && !runAs.equals("0:0")) {
											jobLogger.log("Changing owner of build directory to container user...");
											changeOwner(docker, runAs, hostBuildDir, osIds, jobLogger);
										}										
										try {
											int exitCode = callWithRegistryLogins(docker, registryLogins, () -> {
												return runStepContainer(docker, commandFacade.getImage(), runAs,
														"sh", entrypointArgs, commandFacade.getEnvMap(), 
														null, new HashMap<>(), position, commandFacade.isUseTTY());
											});

											if (exitCode != 0) {
												jobLogger.error("Command exited with code " + exitCode);
												return false;
											}
										} finally {
											if (SystemUtils.IS_OS_LINUX && !osIds.equals("0:0")) {
												jobLogger.log("Changing owner of build directory to host user...");
												changeOwner(newDocker(), osIds, hostBuildDir, osIds, jobLogger);
											}
										} 
									} else if (facade instanceof BuildImageFacade) {
										var buildImageFacade = (BuildImageFacade) facade;
										var docker = newDocker();
										var registryLogins = merge(buildImageFacade.getRegistryLogins(), getRegistryLogins(jobToken));
										callWithRegistryLogins(docker, registryLogins, () -> {
											buildImage(docker, getDockerBuilder(), buildImageFacade, hostBuildDir, 
													isAlwaysPullImage(), jobLogger);
											return null;
										});
									} else if (facade instanceof RunImagetoolsFacade) {
										var runImagetoolsFacade = (RunImagetoolsFacade) facade;
										var docker = newDocker();
										var registryLogins = merge(runImagetoolsFacade.getRegistryLogins(), getRegistryLogins(jobToken));
										callWithRegistryLogins(docker, registryLogins, () -> {
											runImagetools(docker, runImagetoolsFacade, hostBuildDir, jobLogger);
											return null;
										});
									} else if (facade instanceof PruneBuilderCacheFacade) {
										var pruneBuilderCacheFacade = (PruneBuilderCacheFacade) facade;
										var docker = newDocker();
										callWithRegistryLogins(docker, new ArrayList<>(), () -> {
											pruneBuilderCache(docker, getDockerBuilder(), pruneBuilderCacheFacade, 
													hostBuildDir, jobLogger);
											return null;
										});
									} else if (facade instanceof RunContainerFacade) {
										RunContainerFacade runContainerFacade = ((RunContainerFacade) facade).replacePlaceholders(hostBuildDir);
										List<String> arguments = new ArrayList<>();
										if (runContainerFacade.getArgs() != null)
											arguments.addAll(Arrays.asList(StringUtils.parseQuoteTokens(runContainerFacade.getArgs())));
										
										var docker = newDocker();
										var registryLogins = merge(runContainerFacade.getRegistryLogins(), getRegistryLogins(jobToken));

										var runAs = runContainerFacade.getRunAs() != null ? runContainerFacade.getRunAs() : "0:0";
										if (SystemUtils.IS_OS_LINUX && !runAs.equals("0:0")) {
											jobLogger.log("Changing owner of build directory to container user...");
											changeOwner(docker, runAs, hostBuildDir, osIds, jobLogger);
										}
										try {
											int exitCode = callWithRegistryLogins(docker, registryLogins, () -> {
												return runStepContainer(docker, runContainerFacade.getImage(), runAs, null,
														arguments, runContainerFacade.getEnvMap(), runContainerFacade.getWorkingDir(), runContainerFacade.getVolumeMounts(),
														position, runContainerFacade.isUseTTY());
											});
											if (exitCode != 0) {
												jobLogger.error("Container exited with code " + exitCode);
												return false;
											}
										} finally {
											if (SystemUtils.IS_OS_LINUX && !osIds.equals("0:0")) {
												jobLogger.log("Changing owner of build directory to host user...");
												changeOwner(newDocker(), osIds, hostBuildDir, osIds, jobLogger);
											}
										}
									} else if (facade instanceof CheckoutFacade) {
										CheckoutFacade checkoutFacade = (CheckoutFacade) facade;
										jobLogger.log("Checking out code...");

										var infoLogger = AgentUtils.newInfoLogger(jobLogger);
										var warningLogger = AgentUtils.newWarningLogger(jobLogger);

										Commandline git = CommandUtils.newGit();
										checkoutFacade.setupWorkingDir(git, hostWorkDir);

										initRepository(git, infoLogger, warningLogger);

										git.clearArgs();
										setupGitCerts(git, Bootstrap.getTrustCertsDir(),
												new File(hostBuildDir, "trust-certs.pem"), 
												containerTrustCertsFilePath, infoLogger, warningLogger);

										CloneInfo cloneInfo = checkoutFacade.getCloneInfo();
										cloneInfo.setupGitAuth(git, hostBuildDir, containerBuildDirPath,
												infoLogger, warningLogger);

										int cloneDepth = checkoutFacade.getCloneDepth();

										cloneRepository(git, jobContext.getProjectGitDir(), cloneInfo.getCloneUrl(),
												jobContext.getRefName(), jobContext.getCommitId().name(),
												checkoutFacade.isWithLfs(), checkoutFacade.isWithSubmodules(),
												cloneDepth, infoLogger, warningLogger);
									} else if (facade instanceof SetupCacheFacade) {
										SetupCacheFacade setupCacheFacade = (SetupCacheFacade) facade;
										cacheProvisioner.setupCache(setupCacheFacade.getCacheConfig());
									} else if (facade instanceof ServerSideFacade) {
										ServerSideFacade serverSideFacade = (ServerSideFacade) facade;
										return serverSideFacade.execute(hostBuildDir, new ServerSideFacade.Runner() {

											@Override
											public ServerStepResult run(File inputDir, Map<String, String> placeholderValues) {
												return getJobService().runServerStep(jobContext, position, inputDir,
														placeholderValues, false, jobLogger);
											}

										});
									} else {
										throw new ExplicitException("Unexpected step type: " + facade.getClass());
									}
									return true;
								}

								@Override
								public void skip(LeafFacade facade, List<Integer> position) {
									jobLogger.notice("Step \"" + entryFacade.getPathAsString(position) + "\" is skipped");
								}

							}, new ArrayList<>());

							if (successful) 
								cacheProvisioner.uploadCaches();
							
							return successful;
						} finally {
							// Fix https://code.onedev.io/onedev/server/~issues/597
							if (SystemUtils.IS_OS_WINDOWS) {
								FileUtils.deleteDir(hostWorkDir);
							}
						}
					} finally {
						deleteNetwork(newDocker(), network, jobLogger);
					}
				} finally {
					SecretMasker.pop();
					synchronized (hostBuildDir) {
						FileUtils.deleteDir(hostBuildDir);
					}
				}
			}

			@Override
			public void resume(JobContext jobContext) {
				if (hostBuildDir != null) synchronized (hostBuildDir) {
					if (hostBuildDir.exists())
						FileUtils.touchFile(new File(hostBuildDir, "continue"));
				}
			}

			@Override
			public Shell openShell(JobContext jobContext, JobTerminal terminal) {
				String containerNameCopy = containerName;
				if (containerNameCopy != null) {
					Commandline docker = newDocker();
					docker.addArgs("exec", "-it", containerNameCopy);
					if (runningStep instanceof CommandFacade) {
						CommandFacade commandStep = (CommandFacade) runningStep;
						docker.addArgs(commandStep.getExecutable());
					} else {
						docker.addArgs("sh");
					}
					return new CommandlineShell(terminal, docker);
				} else if (hostBuildDir != null) {
					Commandline shell;
					if (SystemUtils.IS_OS_WINDOWS)
						shell = new Commandline("cmd");
					else
						shell = new Commandline("sh");
					shell.workingDir(new File(hostBuildDir, "work"));
					return new CommandlineShell(terminal, shell);
				} else {
					throw new ExplicitException("Shell not ready");
				}
			}

		});
		jobLogger.log("Pending resource allocation...");
		return getResourceService().runServerJob(getName(), getConcurrencyNumber(), 
				jobContext.getServices().size() + 1, runnable);
	}
	
	@Override
	public boolean isValid(ConstraintValidatorContext context) {
		boolean isValid = true;
		Set<String> registryUrls = new HashSet<>();
		for (RegistryLogin login: getRegistryLogins()) {
			if (!registryUrls.add(login.getRegistryUrl())) {
				isValid = false;
				String message;
				if (login.getRegistryUrl() != null)
					message = "Duplicate login entry for registry '" + login.getRegistryUrl() + "'";
				else
					message = "Duplicate login entry for official registry";
				context.buildConstraintViolationWithTemplate(message)
						.addPropertyNode("registryLogins").addConstraintViolation();
				break;
			}
		}
		if (!isValid)
			context.disableDefaultConstraintViolation();
		return isValid;
	}
	
	private String getHostPath(String path) {
		String installPath = Bootstrap.installDir.getAbsolutePath();
		Preconditions.checkState(path.startsWith(installPath + "/")
				|| path.startsWith(installPath + "\\"));
		if (hostInstallPath == null) {
			if (Bootstrap.isInDocker()) 
				hostInstallPath = AgentUtils.getHostPath(newDocker(), installPath);
			else 
				hostInstallPath = installPath;
		}
		return hostInstallPath + path.substring(installPath.length());
	}
	
	@Override
	public List<RegistryLoginFacade> getRegistryLogins(String jobToken) {
		return getRegistryLogins().stream().map(it->it.getFacade(jobToken)).collect(toList());		
	}
	
	@Override
	public void test(TestData testData, TaskLogger jobLogger) {
		checkApplicable();
		var docker = newDocker();
		callWithRegistryLogins(docker, getRegistryLogins(UUID.randomUUID().toString()), () -> {
			File workDir = null;
			try {
				workDir = FileUtils.createTempDir("work");
				jobLogger.log("Testing specified docker image...");
				docker.args("run", "--rm");
				if (getCpuLimit() != null)
					docker.addArgs("--cpus", getCpuLimit());
				if (getMemoryLimit() != null)
					docker.addArgs("--memory", getMemoryLimit());
				if (getRunOptions() != null)
					docker.addArgs(StringUtils.parseQuoteTokens(getRunOptions()));

				var containerWorkspacePath = "/onedev-build/work";
				docker.addArgs("-v", getHostPath(workDir.getAbsolutePath()) + ":" + containerWorkspacePath);

				docker.addArgs("-w", containerWorkspacePath);
				docker.addArgs(testData.getDockerImage(), "sh", "-c", "echo hello from container");

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
			} finally {
				if (workDir != null)
					FileUtils.deleteDir(workDir);
			}

			jobLogger.log("Checking busybox availability...");
			docker.args("run", "--rm", "busybox", "sh", "-c", "echo hello from busybox");
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

			Commandline git = CommandUtils.newGit();
			KubernetesHelper.testGitLfsAvailability(git, jobLogger);

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

}