package io.onedev.server.plugin.executor.serverdocker;

import static io.onedev.agent.DockerExecutorUtils.cleanDirAsRoot;
import static io.onedev.agent.DockerExecutorUtils.createNetwork;
import static io.onedev.agent.DockerExecutorUtils.deleteNetwork;
import static io.onedev.agent.DockerExecutorUtils.newDockerKiller;
import static io.onedev.agent.DockerExecutorUtils.newErrorLogger;
import static io.onedev.agent.DockerExecutorUtils.newInfoLogger;
import static io.onedev.agent.DockerExecutorUtils.startService;
import static io.onedev.k8shelper.KubernetesHelper.checkCacheAllocations;
import static io.onedev.k8shelper.KubernetesHelper.cloneRepository;
import static io.onedev.k8shelper.KubernetesHelper.getCacheInstances;
import static io.onedev.k8shelper.KubernetesHelper.installGitCert;
import static io.onedev.k8shelper.KubernetesHelper.readPlaceholderValues;
import static io.onedev.k8shelper.KubernetesHelper.replacePlaceholders;
import static io.onedev.k8shelper.KubernetesHelper.stringifyPosition;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

import javax.annotation.Nullable;
import javax.validation.ConstraintValidatorContext;

import org.apache.commons.lang3.SystemUtils;
import org.hibernate.validator.constraints.NotEmpty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;

import io.onedev.agent.DockerExecutorUtils;
import io.onedev.agent.job.FailedException;
import io.onedev.commons.bootstrap.Bootstrap;
import io.onedev.commons.loader.AppLoader;
import io.onedev.commons.utils.ExplicitException;
import io.onedev.commons.utils.FileUtils;
import io.onedev.commons.utils.PathUtils;
import io.onedev.commons.utils.StringUtils;
import io.onedev.commons.utils.TaskLogger;
import io.onedev.commons.utils.command.Commandline;
import io.onedev.commons.utils.command.ExecutionResult;
import io.onedev.commons.utils.command.LineConsumer;
import io.onedev.commons.utils.command.ProcessKiller;
import io.onedev.k8shelper.CacheInstance;
import io.onedev.k8shelper.CheckoutExecutable;
import io.onedev.k8shelper.CloneInfo;
import io.onedev.k8shelper.CommandExecutable;
import io.onedev.k8shelper.CompositeExecutable;
import io.onedev.k8shelper.ContainerExecutable;
import io.onedev.k8shelper.KubernetesHelper;
import io.onedev.k8shelper.LeafExecutable;
import io.onedev.k8shelper.LeafHandler;
import io.onedev.k8shelper.ServerExecutable;
import io.onedev.server.OneDev;
import io.onedev.server.buildspec.Service;
import io.onedev.server.buildspec.job.JobContext;
import io.onedev.server.buildspec.job.JobManager;
import io.onedev.server.git.config.GitConfig;
import io.onedev.server.job.resource.ResourceManager;
import io.onedev.server.model.support.RegistryLogin;
import io.onedev.server.model.support.administration.jobexecutor.JobExecutor;
import io.onedev.server.plugin.executor.serverdocker.ServerDockerExecutor.TestData;
import io.onedev.server.util.patternset.PatternSet;
import io.onedev.server.util.validation.Validatable;
import io.onedev.server.util.validation.annotation.ClassValidating;
import io.onedev.server.web.editable.annotation.Editable;
import io.onedev.server.web.editable.annotation.Horizontal;
import io.onedev.server.web.editable.annotation.NameOfEmptyValue;
import io.onedev.server.web.editable.annotation.OmitName;
import io.onedev.server.web.util.Testable;

@Editable(order=ServerDockerExecutor.ORDER, name="Server Docker Executor", description="This executor runs build jobs as docker containers on OneDev server")
@ClassValidating
@Horizontal
public class ServerDockerExecutor extends JobExecutor implements Testable<TestData>, Validatable {

	private static final long serialVersionUID = 1L;
	
	static final int ORDER=200;
	
	private static final Logger logger = LoggerFactory.getLogger(ServerDockerExecutor.class);

	private static final Object cacheHomeCreationLock = new Object();
	
	private List<RegistryLogin> registryLogins = new ArrayList<>();
	
	private String runOptions;
	
	private String dockerExecutable;
	
	private transient volatile String outerInstallPath;

	@Editable(order=400, description="Specify login information for docker registries if necessary")
	public List<RegistryLogin> getRegistryLogins() {
		return registryLogins;
	}

	public void setRegistryLogins(List<RegistryLogin> registryLogins) {
		this.registryLogins = registryLogins;
	}

	@Editable(order=50050, group="More Settings", description="Optionally specify options to run container. For instance, you may use <tt>-m 2g</tt> "
			+ "to limit memory of created container to be 2 giga bytes")
	public String getRunOptions() {
		return runOptions;
	}

	public void setRunOptions(String runOptions) {
		this.runOptions = runOptions;
	}

	@Editable(order=50100, group="More Settings", description="Optionally specify docker executable, for instance <i>/usr/local/bin/docker</i>. "
			+ "Leave empty to use docker executable in PATH")
	@NameOfEmptyValue("Use default")
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
	
	private File getCacheHome() {
		File file = new File(Bootstrap.getSiteDir(), "cache");
		if (!file.exists()) synchronized (cacheHomeCreationLock) {
			FileUtils.createDir(file);
		}
		return file;
	}
	
	@Override
	public void execute(String jobToken, JobContext jobContext) {
		File hostBuildHome = FileUtils.createTempDir("onedev-build");
		try {
			TaskLogger jobLogger = jobContext.getLogger();
			OneDev.getInstance(ResourceManager.class).run(new Runnable() {

				@Override
				public void run() {
					String network = getName() + "-" + jobContext.getProjectId() + "-" 
							+ jobContext.getBuildNumber() + "-" + jobContext.getRetried();

					jobLogger.log(String.format("Executing job (executor: %s, network: %s)...", getName(), network));
					jobContext.notifyJobRunning(null);
					
					JobManager jobManager = OneDev.getInstance(JobManager.class);		
					File hostCacheHome = getCacheHome();
					
					jobLogger.log("Allocating job caches...") ;
					Map<CacheInstance, Date> cacheInstances = getCacheInstances(hostCacheHome);
					Map<CacheInstance, String> cacheAllocations = jobManager.allocateJobCaches(jobToken, new Date(), cacheInstances);
					checkCacheAllocations(hostCacheHome, cacheAllocations, new Consumer<File>() {
	
						@Override
						public void accept(File directory) {
							cleanDirAsRoot(directory, newDocker(), Bootstrap.isInDocker());
						}
						
					});
						
					login(jobLogger);
					
					createNetwork(newDocker(), network, jobLogger);
					try {
						for (Service jobService: jobContext.getServices()) {
							jobLogger.log("Starting service (name: " + jobService.getName() + ", image: " + jobService.getImage() + ")...");
							startService(newDocker(), network, jobService.toMap(), jobLogger);
						}
						
						File hostWorkspace = new File(hostBuildHome, "workspace");
						FileUtils.createDir(hostWorkspace);
						
						AtomicReference<File> hostAuthInfoHome = new AtomicReference<>(null);
						try {						
							jobLogger.log("Copying job dependencies...");
							jobContext.copyDependencies(hostWorkspace);
	
							String containerBuildHome;
							String containerWorkspace;
							if (SystemUtils.IS_OS_WINDOWS) {
								containerBuildHome = "C:\\onedev-build";
								containerWorkspace = "C:\\onedev-build\\workspace";
							} else {
								containerBuildHome = "/onedev-build";
								containerWorkspace = "/onedev-build/workspace";
							}
							
							jobContext.reportJobWorkspace(containerWorkspace);
							CompositeExecutable entryExecutable = new CompositeExecutable(jobContext.getActions());
							
							boolean successful = entryExecutable.execute(new LeafHandler() {

								private int runStepContainer(String image, @Nullable String entrypoint, 
										List<String> arguments, Map<String, String> environments, 
										@Nullable String workingDir, List<Integer> position, boolean useTTY) {
									String containerName = network + "-step-" + stringifyPosition(position);
									Commandline docker = newDocker();
									docker.addArgs("run", "--name=" + containerName, "--network=" + network);
									if (getRunOptions() != null)
										docker.addArgs(StringUtils.parseQuoteTokens(getRunOptions()));
									
									docker.addArgs("-v", getOuterPath(hostBuildHome.getAbsolutePath()) + ":" + containerBuildHome);
									if (workingDir != null) {
										docker.addArgs("-v", getOuterPath(hostWorkspace.getAbsolutePath()) + ":" + workingDir);
										docker.addArgs("-w", workingDir);
									} else {
										docker.addArgs("-w", containerWorkspace);
									}
									for (Map.Entry<CacheInstance, String> entry: cacheAllocations.entrySet()) {
										if (!PathUtils.isCurrent(entry.getValue())) {
											String each = entry.getKey().getDirectory(hostCacheHome).getAbsolutePath();
											String containerCachePath = PathUtils.resolve(containerWorkspace, entry.getValue());
											docker.addArgs("-v", getOuterPath(each) + ":" + containerCachePath);
										} else {
											throw new ExplicitException("Invalid cache path: " + entry.getValue());
										}
									}
									
									if (SystemUtils.IS_OS_LINUX) 
										docker.addArgs("-v", "/var/run/docker.sock:/var/run/docker.sock");
									
									if (hostAuthInfoHome.get() != null) {
										String outerPath = getOuterPath(hostAuthInfoHome.get().getAbsolutePath());
										if (SystemUtils.IS_OS_WINDOWS) {
											docker.addArgs("-v",  outerPath + ":C:\\Users\\ContainerAdministrator\\auth-info");
											docker.addArgs("-v",  outerPath + ":C:\\Users\\ContainerUser\\auth-info");
										} else { 
											docker.addArgs("-v", outerPath + ":/root/auth-info");
										}
									}
									
									for (Map.Entry<String, String> entry: environments.entrySet()) 
										docker.addArgs("-e", entry.getKey() + "=" + entry.getValue());

									if (useTTY)
										docker.addArgs("-t");
									
									if (entrypoint != null)
										docker.addArgs("--entrypoint=" + entrypoint);
									
									docker.addArgs(image);
									docker.addArgs(arguments.toArray(new String[arguments.size()]));
									
									ProcessKiller killer = newDockerKiller(newDocker(), containerName, jobLogger);
									return docker.execute(newInfoLogger(jobLogger), newErrorLogger(jobLogger), null, killer).getReturnCode();
								}
								
								@Override
								public boolean execute(LeafExecutable executable, List<Integer> position) {
									String stepNames = entryExecutable.getNamesAsString(position);
									jobLogger.notice("Running step \"" + stepNames + "\"...");
									
									if (executable instanceof CommandExecutable) {
										CommandExecutable commandExecutable = (CommandExecutable) executable;

										if (commandExecutable.getImage() == null) {
											throw new ExplicitException("This step can only be executed by server shell "
													+ "executor or remote shell executor");
										}
										
										Commandline entrypoint = DockerExecutorUtils.getEntrypoint(
												hostBuildHome, commandExecutable, hostAuthInfoHome.get() != null);
										
										int exitCode = runStepContainer(commandExecutable.getImage(), entrypoint.executable(), 
												entrypoint.arguments(), new HashMap<>(), null, position, commandExecutable.isUseTTY());
										
										if (exitCode != 0) {
											jobLogger.error("Step \"" + stepNames + "\" is failed: Command exited with code " + exitCode);
											return false;
										}
									} else if (executable instanceof ContainerExecutable) {
										ContainerExecutable containerExecutable = (ContainerExecutable) executable;

										List<String> arguments = new ArrayList<>();
										if (containerExecutable.getArgs() != null)
											arguments.addAll(Arrays.asList(StringUtils.parseQuoteTokens(containerExecutable.getArgs())));
										int exitCode = runStepContainer(containerExecutable.getImage(), null, arguments, 
												containerExecutable.getEnvMap(), containerExecutable.getWorkingDir(), 
												position, containerExecutable.isUseTTY());
										if (exitCode != 0) {
											jobLogger.error("Step \"" + stepNames + "\" is failed: Container exited with code " + exitCode);
											return false;
										} 
									} else if (executable instanceof CheckoutExecutable) {
										try {
											CheckoutExecutable checkoutExecutable = (CheckoutExecutable) executable;
											jobLogger.log("Checking out code...");
											if (hostAuthInfoHome.get() == null)
												hostAuthInfoHome.set(FileUtils.createTempDir());
											Commandline git = new Commandline(AppLoader.getInstance(GitConfig.class).getExecutable());	
											git.workingDir(hostWorkspace).environments().put("HOME", hostAuthInfoHome.get().getAbsolutePath());
	
											CloneInfo cloneInfo = checkoutExecutable.getCloneInfo();
											
											cloneInfo.writeAuthData(hostAuthInfoHome.get(), git, newInfoLogger(jobLogger), newErrorLogger(jobLogger));
											try {
												List<String> trustCertContent = getTrustCertContent();
												if (!trustCertContent.isEmpty()) {
													installGitCert(new File(hostAuthInfoHome.get(), "trust-cert.pem"), trustCertContent, 
															git, newInfoLogger(jobLogger), newErrorLogger(jobLogger));
												}
		
												int cloneDepth = checkoutExecutable.getCloneDepth();
												
												cloneRepository(git, jobContext.getProjectGitDir().getAbsolutePath(), 
														cloneInfo.getCloneUrl(), jobContext.getCommitId().name(), 
														checkoutExecutable.isWithLfs(), checkoutExecutable.isWithSubmodules(),
														cloneDepth, newInfoLogger(jobLogger), newErrorLogger(jobLogger));
											} finally {
												git.clearArgs();
												git.addArgs("config", "--global", "--unset", "core.sshCommand");
												ExecutionResult result = git.execute(newInfoLogger(jobLogger), newErrorLogger(jobLogger));
												if (result.getReturnCode() != 5 && result.getReturnCode() != 0)
													result.checkReturnCode();
											}
										} catch (Exception e) {
											jobLogger.error("Step \"" + stepNames + "\" is failed: " + getErrorMessage(e));
											return false;
										}
									} else {
										ServerExecutable serverExecutable = (ServerExecutable) executable;
										
										File filesDir = FileUtils.createTempDir();
										try {
											Collection<String> placeholders = serverExecutable.getPlaceholders();
											Map<String, String> placeholderValues = readPlaceholderValues(hostBuildHome, placeholders);
											PatternSet filePatterns = new PatternSet(
													new HashSet<>(replacePlaceholders(serverExecutable.getIncludeFiles(), placeholderValues)), 
													new HashSet<>(replacePlaceholders(serverExecutable.getExcludeFiles(), placeholderValues)));

											int baseLen = hostWorkspace.getAbsolutePath().length()+1;
											for (File file: filePatterns.listFiles(hostWorkspace)) {
												try {
													FileUtils.copyFile(file, new File(filesDir, file.getAbsolutePath().substring(baseLen)));
												} catch (IOException e) {
													throw new RuntimeException(e);
												}
											}

											Map<String, byte[]> outputFiles = jobContext.runServerStep(position, filesDir, placeholderValues, jobLogger);
											
											if (outputFiles != null) {
												for (Map.Entry<String, byte[]> entry: outputFiles.entrySet()) {
													FileUtils.writeByteArrayToFile(
															new File(hostBuildHome, entry.getKey()), 
															entry.getValue());
												}
											}
										} catch (Exception e) {
											jobLogger.error("Step \"" + stepNames + "\" is failed: " + getErrorMessage(e));
											return false;
										} finally {
											FileUtils.deleteDir(filesDir);
										}
									}
									jobLogger.success("Step \"" + stepNames + "\" is successful");
									return true;
								}

								@Override
								public void skip(LeafExecutable executable, List<Integer> position) {
									jobLogger.notice("Step \"" + entryExecutable.getNamesAsString(position) + "\" is skipped");
								}
								
							}, new ArrayList<>());

							if (!successful)
								throw new FailedException();
						} finally {
							if (hostAuthInfoHome.get() != null)
								FileUtils.deleteDir(hostAuthInfoHome.get());
						}
					} finally {
						deleteNetwork(newDocker(), network, jobLogger);
					}					
				}
				
			}, jobContext.getResourceRequirements(), jobLogger);
		} finally {
			cleanDirAsRoot(hostBuildHome, newDocker(), Bootstrap.isInDocker());
			FileUtils.deleteDir(hostBuildHome);
		}
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
	
	private String getHostMountPath(String dockerNameOrId, String containerMountPath) {
		AtomicReference<String> hostMountPath = new AtomicReference<>(null);
		Commandline docker = newDocker();
		String inspectFormat = String.format(
				"{{range .Mounts}} {{if eq .Destination \"%s\"}} {{.Source}} {{end}} {{end}}", 
				containerMountPath);
		docker.addArgs("container", "inspect", "-f", inspectFormat, dockerNameOrId);						
		
		AtomicBoolean noSuchContainer = new AtomicBoolean(false);
		ExecutionResult result = docker.execute(new LineConsumer() {
	
			@Override
			public void consume(String line) {
				hostMountPath.set(line.trim());
			}
			
		}, new LineConsumer() {
	
			@Override
			public void consume(String line) {
				if (line.contains("Error: No such container:"))
					noSuchContainer.set(true);
				else
					logger.error(line);
			}
			
		});
		
		if (noSuchContainer.get()) {
			return null;
		} else {
			result.checkReturnCode();
			return hostMountPath.get();
		}
	}
	
	private String getOuterPath(String hostPath) {
		String hostInstallPath = Bootstrap.installDir.getAbsolutePath();
		Preconditions.checkState(hostPath.startsWith(hostInstallPath + "/")
				|| hostPath.startsWith(hostInstallPath + "\\"));
		if (outerInstallPath == null) {
			if (Bootstrap.isInDocker()) {
				outerInstallPath = getHostMountPath(System.getenv("HOSTNAME"), hostInstallPath);
				if (outerInstallPath == null)
					outerInstallPath = getHostMountPath("onedev", hostInstallPath);
				if (outerInstallPath == null)
					throw new RuntimeException("Unable to get container information");
			} else {
				outerInstallPath = hostInstallPath;
			}
		}
		return outerInstallPath + hostPath.substring(hostInstallPath.length());
	}
	
	@Override
	public void test(TestData testData, TaskLogger jobLogger) {
		OneDev.getInstance(ResourceManager.class).run(new Runnable() {

			@Override
			public void run() {
				login(jobLogger);
				
				File workspaceDir = null;
				File cacheDir = null;

				Commandline docker = newDocker();
				try {
					workspaceDir = FileUtils.createTempDir("workspace");
					cacheDir = new File(getCacheHome(), UUID.randomUUID().toString());
					FileUtils.createDir(cacheDir);
					
					jobLogger.log("Testing specified docker image...");
					docker.clearArgs();
					docker.addArgs("run", "--rm");
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
					docker.addArgs("-v", getOuterPath(workspaceDir.getAbsolutePath()) + ":" + containerWorkspacePath);
					docker.addArgs("-v", getOuterPath(cacheDir.getAbsolutePath()) + ":" + containerCachePath);
					
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
				
				Commandline git = new Commandline(AppLoader.getInstance(GitConfig.class).getExecutable());
				KubernetesHelper.testGitLfsAvailability(git, jobLogger);
			}
			
		}, new HashMap<>(), jobLogger);
		
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