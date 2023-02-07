package io.onedev.server.plugin.executor.serverdocker;

import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.hazelcast.cluster.Member;
import io.onedev.agent.DockerExecutorUtils;
import io.onedev.agent.ExecutorUtils;
import io.onedev.agent.job.FailedException;
import io.onedev.commons.bootstrap.Bootstrap;
import io.onedev.commons.loader.AppLoader;
import io.onedev.commons.utils.*;
import io.onedev.commons.utils.command.Commandline;
import io.onedev.commons.utils.command.ExecutionResult;
import io.onedev.commons.utils.command.LineConsumer;
import io.onedev.k8shelper.*;
import io.onedev.server.OneDev;
import io.onedev.server.buildspec.Service;
import io.onedev.server.cluster.ClusterManager;
import io.onedev.server.cluster.ClusterRunnable;
import io.onedev.server.git.location.GitLocation;
import io.onedev.server.job.JobContext;
import io.onedev.server.job.JobManager;
import io.onedev.server.job.JobRunnable;
import io.onedev.server.job.ResourceAllocator;
import io.onedev.server.model.support.RegistryLogin;
import io.onedev.server.model.support.administration.jobexecutor.JobExecutor;
import io.onedev.server.plugin.executor.serverdocker.ServerDockerExecutor.TestData;
import io.onedev.server.terminal.CommandlineShell;
import io.onedev.server.terminal.Shell;
import io.onedev.server.terminal.Terminal;
import io.onedev.server.util.EditContext;
import io.onedev.server.util.validation.Validatable;
import io.onedev.server.util.validation.annotation.ClassValidating;
import io.onedev.server.web.editable.annotation.*;
import io.onedev.server.web.util.Testable;
import org.apache.commons.lang3.SystemUtils;

import javax.annotation.Nullable;
import javax.validation.ConstraintValidatorContext;
import javax.validation.constraints.NotEmpty;
import java.io.File;
import java.io.Serializable;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

import static io.onedev.agent.DockerExecutorUtils.*;
import static io.onedev.k8shelper.KubernetesHelper.*;

@Editable(order=ServerDockerExecutor.ORDER, name="Server Docker Executor", 
		description="This executor runs build jobs as docker containers on OneDev server")
@ClassValidating
@Horizontal
public class ServerDockerExecutor extends JobExecutor implements Testable<TestData>, Validatable {

	private static final long serialVersionUID = 1L;
	
	static final int ORDER = 50;

	private static final Object cacheHomeCreationLock = new Object();
	
	private List<RegistryLogin> registryLogins = new ArrayList<>();
	
	private String runOptions;
	
	private String dockerExecutable;
	
	private boolean mountDockerSock;
	
	private String dockerSockPath;
	
	private String cpuLimit;
	
	private String memoryLimit;
	
	private String concurrency;
	
	private transient volatile File hostBuildHome;
	
	private transient volatile LeafFacade runningStep;
	
	private transient volatile String containerName;
	
	private static transient volatile String hostInstallPath;
	
	@Editable(order=400, description="Specify login information for docker registries if necessary")
	public List<RegistryLogin> getRegistryLogins() {
		return registryLogins;
	}

	public void setRegistryLogins(List<RegistryLogin> registryLogins) {
		this.registryLogins = registryLogins;
	}

	@Editable(order=450, placeholder = "Number of server cpu", description = "" +
			"Specify max number of jobs/services this executor can run concurrently")
	@Numeric
	public String getConcurrency() {
		return concurrency;
	}

	public void setConcurrency(String concurrency) {
		this.concurrency = concurrency;
	}

	@Editable(order=500, group="More Settings", description="Whether or not to mount docker sock into job container to "
			+ "support docker operations in job commands, for instance to build docker image.<br>"
			+ "<b class='text-danger'>WARNING</b>: Malicious jobs can take control of whole OneDev "
			+ "by operating the mounted docker sock. You should configure job authorization "
			+ "to make sure the executor can only be used by trusted jobs if this option is enabled")
	public boolean isMountDockerSock() {
		return mountDockerSock;
	}

	public void setMountDockerSock(boolean mountDockerSock) {
		this.mountDockerSock = mountDockerSock;
	}
	
	@SuppressWarnings("unused")
	private static boolean isMountDockerSockEnabled() {
		return (Boolean)EditContext.get().getInputValue("mountDockerSock");
	}

	@Editable(order=510, group="More Settings", placeholder="Default", description="Optionally specify docker sock path to mount from. "
			+ "Defaults to <i>/var/run/docker.sock</i> on Linux, and <i>//./pipe/docker_engine</i> on Windows")
	@ShowCondition("isMountDockerSockEnabled")
	public String getDockerSockPath() {
		return dockerSockPath;
	}

	public void setDockerSockPath(String dockerSockPath) {
		this.dockerSockPath = dockerSockPath;
	}

	@Editable(order=50010, group="More Settings", placeholder = "No limit", description = "" +
			"Optionally specify cpu limit of jobs/services using this executor. This will be " +
			"used as option <a href='https://docs.docker.com/config/containers/resource_constraints/#cpu' target='_blank'>--cpus</a> " +
			"of relevant containers")
	public String getCpuLimit() {
		return cpuLimit;
	}

	public void setCpuLimit(String cpuLimit) {
		this.cpuLimit = cpuLimit;
	}

	@Editable(order=50020, group="More Settings", placeholder = "No limit", description = "" +
			"Optionally specify memory limit of jobs/services using this executor. This will be " +
			"used as option <a href='https://docs.docker.com/config/containers/resource_constraints/#memory' target='_blank'>--memory</a> " +
			"of relevant containers")
	public String getMemoryLimit() {
		return memoryLimit;
	}

	public void setMemoryLimit(String memoryLimit) {
		this.memoryLimit = memoryLimit;
	}

	@Editable(order=50050, group="More Settings", description="Optionally specify options to run container")
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
		if (getDockerExecutable() != null)
			return new Commandline(getDockerExecutable());
		else if (SystemUtils.IS_OS_MAC_OSX && new File("/usr/local/bin/docker").exists())
			return new Commandline("/usr/local/bin/docker");
		else
			return new Commandline("docker");
	}
	
	private File getCacheHome(JobExecutor jobExecutor) {
		File file = new File(Bootstrap.getSiteDir(), "cache/" + jobExecutor.getName());
		if (!file.exists()) synchronized (cacheHomeCreationLock) {
			FileUtils.createDir(file);
		}
		return file;
	}

	private ClusterManager getClusterManager() {
		return OneDev.getInstance(ClusterManager.class);
	}
	
	private JobManager getJobManager() {
		return OneDev.getInstance(JobManager.class);
	}
	
	private ResourceAllocator getResourceAllocator() {
		return OneDev.getInstance(ResourceAllocator.class);
	}

	private int getConcurrencyNumber() {
		if (getConcurrency() != null)
			return Integer.parseInt(getConcurrency());
		else 
			return 0;
	}
	
	@Override
	public void execute(JobContext jobContext, TaskLogger jobLogger) {
		ClusterRunnable runnable = () -> {
			getJobManager().runJobLocal(jobContext, new JobRunnable() {

				private static final long serialVersionUID = 1L;

				@Override
				public void run(TaskLogger jobLogger) {
					notifyJobRunning(jobContext.getBuildId(), null);
					
					if (OneDev.getK8sService() != null) {
						throw new ExplicitException(""
								+ "OneDev running inside kubernetes cluster does not support server docker executor. "
								+ "Please use kubernetes executor instead");
					}

					hostBuildHome = FileUtils.createTempDir("onedev-build");
					try {
						String network = getName() + "-" + jobContext.getProjectId() + "-"
								+ jobContext.getBuildNumber() + "-" + jobContext.getRetried();

						Member member = getClusterManager().getHazelcastInstance().getCluster().getLocalMember();
						jobLogger.log(String.format("Executing job (executor: %s, server: %s, network: %s)...", getName(),
								member.getAddress().getHost() + ":" + member.getAddress().getPort(), network));

						File hostCacheHome = getCacheHome(jobContext.getJobExecutor());

						jobLogger.log("Setting up job cache...");
						JobCache cache = new JobCache(hostCacheHome) {

							@Override
							protected Map<CacheInstance, String> allocate(CacheAllocationRequest request) {
								return getJobManager().allocateCaches(jobContext, request);
							}

							@Override
							protected void delete(File cacheDir) {
								deleteDir(cacheDir, newDocker(), Bootstrap.isInDocker());
							}

						};
						cache.init(false);

						login(jobLogger);

						createNetwork(newDocker(), network, jobLogger);
						try {
							OsInfo osInfo = OneDev.getInstance(OsInfo.class);

							for (Service jobService : jobContext.getServices()) {
								jobLogger.log("Starting service (name: " + jobService.getName() + ", image: " + jobService.getImage() + ")...");
								startService(newDocker(), network, jobService.toMap(), osInfo, getCpuLimit(), getMemoryLimit(), jobLogger);
							}

							File hostWorkspace = new File(hostBuildHome, "workspace");
							FileUtils.createDir(hostWorkspace);

							AtomicReference<File> hostAuthInfoDir = new AtomicReference<>(null);
							try {
								cache.installSymbolinks(hostWorkspace);

								jobLogger.log("Copying job dependencies...");
								getJobManager().copyDependencies(jobContext, hostWorkspace);

								String containerBuildHome;
								String containerWorkspace;
								String containerTrustCerts;
								if (SystemUtils.IS_OS_WINDOWS) {
									containerBuildHome = "C:\\onedev-build";
									containerWorkspace = "C:\\onedev-build\\workspace";
									containerTrustCerts = "C:\\onedev-build\\trust-certs.pem";
								} else {
									containerBuildHome = "/onedev-build";
									containerWorkspace = "/onedev-build/workspace";
									containerTrustCerts = "/onedev-build/trust-certs.pem";
								}

								getJobManager().reportJobWorkspace(jobContext, containerWorkspace);
								CompositeFacade entryFacade = new CompositeFacade(jobContext.getActions());
								boolean successful = entryFacade.execute(new LeafHandler() {

									private int runStepContainer(String image, @Nullable String entrypoint,
																 List<String> arguments, Map<String, String> environments,
																 @Nullable String workingDir, Map<String, String> volumeMounts,
																 List<Integer> position, boolean useTTY) {
										// Uninstall symbol links as docker can not process it well
										cache.uninstallSymbolinks(hostWorkspace);
										containerName = network + "-step-" + stringifyStepPosition(position);
										try {
											Commandline docker = newDocker();
											docker.addArgs("run", "--name=" + containerName, "--network=" + network);
											if (getCpuLimit() != null)
												docker.addArgs("--cpus", getCpuLimit());
											if (getMemoryLimit() != null)
												docker.addArgs("--memory", getMemoryLimit());
											if (getRunOptions() != null)
												docker.addArgs(StringUtils.parseQuoteTokens(getRunOptions()));

											docker.addArgs("-v", getHostPath(hostBuildHome.getAbsolutePath()) + ":" + containerBuildHome);

											for (Map.Entry<String, String> entry : volumeMounts.entrySet()) {
												if (entry.getKey().contains(".."))
													throw new ExplicitException("Volume mount source path should not contain '..'");
												String hostPath = getHostPath(new File(hostWorkspace, entry.getKey()).getAbsolutePath());
												docker.addArgs("-v", hostPath + ":" + entry.getValue());
											}

											if (entrypoint != null) {
												docker.addArgs("-w", containerWorkspace);
											} else if (workingDir != null) {
												if (workingDir.contains(".."))
													throw new ExplicitException("Container working dir should not contain '..'");
												docker.addArgs("-w", workingDir);
											}

											for (Map.Entry<CacheInstance, String> entry : cache.getAllocations().entrySet()) {
												String hostCachePath = entry.getKey().getDirectory(hostCacheHome).getAbsolutePath();
												String containerCachePath = PathUtils.resolve(containerWorkspace, entry.getValue());
												docker.addArgs("-v", getHostPath(hostCachePath) + ":" + containerCachePath);
											}

											if (isMountDockerSock()) {
												if (getDockerSockPath() != null) {
													if (SystemUtils.IS_OS_WINDOWS)
														docker.addArgs("-v", getDockerSockPath() + "://./pipe/docker_engine");
													else
														docker.addArgs("-v", getDockerSockPath() + ":/var/run/docker.sock");
												} else {
													if (SystemUtils.IS_OS_WINDOWS)
														docker.addArgs("-v", "//./pipe/docker_engine://./pipe/docker_engine");
													else
														docker.addArgs("-v", "/var/run/docker.sock:/var/run/docker.sock");
												}
											}

											if (hostAuthInfoDir.get() != null) {
												String hostPath = getHostPath(hostAuthInfoDir.get().getAbsolutePath());
												if (SystemUtils.IS_OS_WINDOWS) {
													docker.addArgs("-v", hostPath + ":C:\\Users\\ContainerAdministrator\\auth-info");
													docker.addArgs("-v", hostPath + ":C:\\Users\\ContainerUser\\auth-info");
												} else {
													docker.addArgs("-v", hostPath + ":/root/auth-info");
												}
											}

											for (Map.Entry<String, String> entry : environments.entrySet())
												docker.addArgs("-e", entry.getKey() + "=" + entry.getValue());

											docker.addArgs("-e", "ONEDEV_WORKSPACE=" + containerWorkspace);

											if (useTTY)
												docker.addArgs("-t");

											if (entrypoint != null)
												docker.addArgs("--entrypoint=" + entrypoint);

											if (isUseProcessIsolation(newDocker(), image, osInfo, jobLogger))
												docker.addArgs("--isolation=process");

											docker.addArgs(image);
											docker.addArgs(arguments.toArray(new String[arguments.size()]));
											docker.processKiller(newDockerKiller(newDocker(), containerName, jobLogger));
											ExecutionResult result = docker.execute(ExecutorUtils.newInfoLogger(jobLogger),
													ExecutorUtils.newWarningLogger(jobLogger), null);
											return result.getReturnCode();
										} finally {
											containerName = null;
											cache.installSymbolinks(hostWorkspace);
										}
									}

									@Override
									public boolean execute(LeafFacade facade, List<Integer> position) {
										runningStep = facade;
										try {
											String stepNames = entryFacade.getNamesAsString(position);
											jobLogger.notice("Running step \"" + stepNames + "\"...");

											if (facade instanceof CommandFacade) {
												CommandFacade commandFacade = (CommandFacade) facade;

												OsExecution execution = commandFacade.getExecution(osInfo);
												if (execution.getImage() == null) {
													throw new ExplicitException("This step can only be executed by server shell "
															+ "executor or remote shell executor");
												}

												Commandline entrypoint = DockerExecutorUtils.getEntrypoint(
														hostBuildHome, commandFacade, osInfo, hostAuthInfoDir.get() != null);

												int exitCode = runStepContainer(execution.getImage(), entrypoint.executable(),
														entrypoint.arguments(), new HashMap<>(), null, new HashMap<>(),
														position, commandFacade.isUseTTY());

												if (exitCode != 0) {
													jobLogger.error("Step \"" + stepNames + "\" is failed: Command exited with code " + exitCode);
													return false;
												}
											} else if (facade instanceof BuildImageFacade) {
												DockerExecutorUtils.buildImage(newDocker(), (BuildImageFacade) facade,
														hostBuildHome, jobLogger);
											} else if (facade instanceof RunContainerFacade) {
												RunContainerFacade rubContainerFacade = (RunContainerFacade) facade;
												OsContainer container = rubContainerFacade.getContainer(osInfo);
												List<String> arguments = new ArrayList<>();
												if (container.getArgs() != null)
													arguments.addAll(Arrays.asList(StringUtils.parseQuoteTokens(container.getArgs())));
												int exitCode = runStepContainer(container.getImage(), null, arguments, container.getEnvMap(),
														container.getWorkingDir(), container.getVolumeMounts(), position, rubContainerFacade.isUseTTY());
												if (exitCode != 0) {
													jobLogger.error("Step \"" + stepNames + "\" is failed: Container exited with code " + exitCode);
													return false;
												}
											} else if (facade instanceof CheckoutFacade) {
												try {
													CheckoutFacade checkoutFacade = (CheckoutFacade) facade;
													jobLogger.log("Checking out code...");
													
													Commandline git = new Commandline(AppLoader.getInstance(GitLocation.class).getExecutable());
													
													if (hostAuthInfoDir.get() == null)
														hostAuthInfoDir.set(FileUtils.createTempDir());
													git.environments().put("HOME", hostAuthInfoDir.get().getAbsolutePath());

													checkoutFacade.setupWorkingDir(git, hostWorkspace);
													if (!Bootstrap.isInDocker()) {
														checkoutFacade.setupSafeDirectory(git, containerWorkspace,
																newInfoLogger(jobLogger), newErrorLogger(jobLogger));
													}

													File trustCertsFile = new File(hostBuildHome, "trust-certs.pem");
													installGitCert(git, Bootstrap.getTrustCertsDir(),
															trustCertsFile, containerTrustCerts,
															ExecutorUtils.newInfoLogger(jobLogger), 
															ExecutorUtils.newWarningLogger(jobLogger));
													
													CloneInfo cloneInfo = checkoutFacade.getCloneInfo();
													cloneInfo.writeAuthData(hostAuthInfoDir.get(), git, true, 
															ExecutorUtils.newInfoLogger(jobLogger), 
															ExecutorUtils.newWarningLogger(jobLogger));

													if (trustCertsFile.exists())
														git.addArgs("-c", "http.sslCAInfo=" + trustCertsFile.getAbsolutePath());

													int cloneDepth = checkoutFacade.getCloneDepth();
			
													cloneRepository(git, jobContext.getProjectGitDir(), cloneInfo.getCloneUrl(),
															jobContext.getRefName(), jobContext.getCommitId().name(),
															checkoutFacade.isWithLfs(), checkoutFacade.isWithSubmodules(),
															cloneDepth, ExecutorUtils.newInfoLogger(jobLogger), ExecutorUtils.newWarningLogger(jobLogger));
												} catch (Exception e) {
													jobLogger.error("Step \"" + stepNames + "\" is failed: " + getErrorMessage(e));
													return false;
												}
											} else {
												ServerSideFacade serverSideFacade = (ServerSideFacade) facade;
												try {
													serverSideFacade.execute(hostBuildHome, new ServerSideFacade.Runner() {

														@Override
														public Map<String, byte[]> run(File inputDir, Map<String, String> placeholderValues) {
															return getJobManager().runServerStep(jobContext, position, inputDir,
																	placeholderValues, false, jobLogger);
														}

													});
												} catch (Exception e) {
													if (ExceptionUtils.find(e, InterruptedException.class) == null)
														jobLogger.error("Step \"" + stepNames + "\" is failed: " + getErrorMessage(e));
													return false;
												}
											}
											jobLogger.success("Step \"" + stepNames + "\" is successful");
											return true;
										} finally {
											runningStep = null;
										}
									}

									@Override
									public void skip(LeafFacade facade, List<Integer> position) {
										jobLogger.notice("Step \"" + entryFacade.getNamesAsString(position) + "\" is skipped");
									}

								}, new ArrayList<>());

								if (!successful)
									throw new FailedException();
							} finally {
								cache.uninstallSymbolinks(hostWorkspace);
								// Fix https://code.onedev.io/onedev/server/~issues/597
								if (SystemUtils.IS_OS_WINDOWS)
									FileUtils.deleteDir(hostWorkspace);
								if (hostAuthInfoDir.get() != null)
									FileUtils.deleteDir(hostAuthInfoDir.get());
							}
						} finally {
							deleteNetwork(newDocker(), network, jobLogger);
						}
					} finally {
						synchronized (hostBuildHome) {
							deleteDir(hostBuildHome, newDocker(), Bootstrap.isInDocker());
						}
					}
				}

				@Override
				public void resume(JobContext jobContext) {
					if (hostBuildHome != null) synchronized (hostBuildHome) {
						if (hostBuildHome.exists())
							FileUtils.touchFile(new File(hostBuildHome, "continue"));
					}
				}

				@Override
				public Shell openShell(JobContext jobContext, Terminal terminal) {
					String containerNameCopy = containerName;
					if (containerNameCopy != null) {
						Commandline docker = newDocker();
						docker.addArgs("exec", "-it", containerNameCopy);
						if (runningStep instanceof CommandFacade) {
							CommandFacade commandStep = (CommandFacade) runningStep;
							docker.addArgs(commandStep.getShell(SystemUtils.IS_OS_WINDOWS, null));
						} else if (SystemUtils.IS_OS_WINDOWS) {
							docker.addArgs("cmd");
						} else {
							docker.addArgs("sh");
						}
						return new CommandlineShell(terminal, docker);
					} else if (hostBuildHome != null) {
						Commandline shell;
						if (SystemUtils.IS_OS_WINDOWS)
							shell = new Commandline("cmd");
						else
							shell = new Commandline("sh");
						shell.workingDir(new File(hostBuildHome, "workspace"));
						return new CommandlineShell(terminal, shell);
					} else {
						throw new ExplicitException("Shell not ready");
					}
				}

			});
		};
		jobLogger.log("Pending resource allocation...");
		getResourceAllocator().runServerJob(getName(), getConcurrencyNumber(),
				jobContext.getServices().size() + 1, runnable);
	}

	private void login(TaskLogger jobLogger) {
		for (RegistryLogin login: getRegistryLogins()) 
			DockerExecutorUtils.login(newDocker(), login.getRegistryUrl(), login.getUserName(), login.getPassword(), jobLogger);
	}
	
	private boolean hasOptions(String[] arguments, String... options) {
		for (String argument: arguments) {
			for (String option: options) {
				if (option.startsWith("--")) {
					if (argument.startsWith(option + "=") || argument.equals(option))
						return true;
				} else if (option.startsWith("-")) {
					if (argument.startsWith(option))
						return true;
				} else {
					throw new ExplicitException("Invalid option: " + option);
				}
			}
		}
		return false;
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
		if (getRunOptions() != null) {
			String[] arguments = StringUtils.parseQuoteTokens(getRunOptions());
			String reservedOptions[] = new String[] {"-w", "--workdir", "-d", "--detach", "-a", "--attach", "-t", "--tty", 
					"-i", "--interactive", "--rm", "--restart", "--name"}; 
			if (hasOptions(arguments, reservedOptions)) {
				StringBuilder errorMessage = new StringBuilder("Can not use options: "
						+ Joiner.on(", ").join(reservedOptions));
				context.buildConstraintViolationWithTemplate(errorMessage.toString())
						.addPropertyNode("runOptions").addConstraintViolation();
				isValid = false;
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
				hostInstallPath = DockerExecutorUtils.getHostPath(newDocker(), installPath);
			else 
				hostInstallPath = installPath;
		}
		return hostInstallPath + path.substring(installPath.length());
	}
	
	@Override
	public void test(TestData testData, TaskLogger jobLogger) {
		login(jobLogger);
		
		File workspaceDir = null;
		File cacheDir = null;

		Commandline docker = newDocker();
		try {
			workspaceDir = FileUtils.createTempDir("workspace");
			cacheDir = new File(getCacheHome(ServerDockerExecutor.this), UUID.randomUUID().toString());
			FileUtils.createDir(cacheDir);
			
			jobLogger.log("Testing specified docker image...");
			docker.clearArgs();
			docker.addArgs("run", "--rm");
			if (getCpuLimit() != null)
				docker.addArgs("--cpus", getCpuLimit());
			if (getMemoryLimit() != null)
				docker.addArgs("--memory", getMemoryLimit());
			if (getRunOptions() != null)
				docker.addArgs(StringUtils.parseQuoteTokens(getRunOptions()));
			String containerWorkspacePath;
			String containerCachePath;
			if (SystemUtils.IS_OS_WINDOWS) {
				containerWorkspacePath = "C:\\onedev-build\\workspace";
				containerCachePath = "C:\\onedev-build\\cache";
			} else {
				containerWorkspacePath = "/onedev-build/workspace";
				containerCachePath = "/onedev-build/cache";
			}
			docker.addArgs("-v", getHostPath(workspaceDir.getAbsolutePath()) + ":" + containerWorkspacePath);
			docker.addArgs("-v", getHostPath(cacheDir.getAbsolutePath()) + ":" + containerCachePath);
			
			docker.addArgs("-w", containerWorkspacePath);
			docker.addArgs(testData.getDockerImage());
			
			if (SystemUtils.IS_OS_WINDOWS) 
				docker.addArgs("cmd", "/c", "echo hello from container");
			else 
				docker.addArgs("sh", "-c", "echo hello from container");
			
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
			if (workspaceDir != null)
				FileUtils.deleteDir(workspaceDir);
			if (cacheDir != null)
				FileUtils.deleteDir(cacheDir);
		}
		
		if (!SystemUtils.IS_OS_WINDOWS) {
			jobLogger.log("Checking busybox availability...");
			docker = newDocker();
			docker.addArgs("run", "--rm", "busybox", "sh", "-c", "echo hello from busybox");			
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
		}
		
		Commandline git = new Commandline(AppLoader.getInstance(GitLocation.class).getExecutable());
		KubernetesHelper.testGitLfsAvailability(git, jobLogger);
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