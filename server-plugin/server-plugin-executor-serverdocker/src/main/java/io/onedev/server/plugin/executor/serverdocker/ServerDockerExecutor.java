package io.onedev.server.plugin.executor.serverdocker;

import static io.onedev.k8shelper.KubernetesHelper.*;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

import javax.inject.Provider;
import javax.validation.ConstraintValidatorContext;

import org.apache.commons.lang3.SystemUtils;
import org.hibernate.validator.constraints.NotEmpty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;
import com.google.common.base.Throwables;

import io.onedev.agent.DockerUtils;
import io.onedev.commons.loader.AppLoader;
import io.onedev.commons.bootstrap.Bootstrap;
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
import io.onedev.k8shelper.LeafExecutable;
import io.onedev.k8shelper.LeafHandler;
import io.onedev.k8shelper.ServerExecutable;
import io.onedev.k8shelper.SshCloneInfo;
import io.onedev.server.OneDev;
import io.onedev.server.buildspec.Service;
import io.onedev.server.buildspec.job.JobContext;
import io.onedev.server.buildspec.job.JobManager;
import io.onedev.server.git.config.GitConfig;
import io.onedev.server.job.resource.ResourceManager;
import io.onedev.server.model.support.RegistryLogin;
import io.onedev.server.model.support.administration.jobexecutor.JobExecutor;
import io.onedev.server.plugin.executor.serverdocker.ServerDockerExecutor.TestData;
import io.onedev.server.util.ExceptionUtils;
import io.onedev.server.util.PKCS12CertExtractor;
import io.onedev.server.util.ServerConfig;
import io.onedev.server.util.patternset.PatternSet;
import io.onedev.server.util.validation.Validatable;
import io.onedev.server.util.validation.annotation.ClassValidating;
import io.onedev.server.web.editable.annotation.Editable;
import io.onedev.server.web.editable.annotation.Horizontal;
import io.onedev.server.web.editable.annotation.NameOfEmptyValue;
import io.onedev.server.web.editable.annotation.OmitName;
import io.onedev.server.web.util.Testable;

@Editable(order=200, name="Server Docker Executor", description="This executor runs build jobs as docker containers on OneDev server")
@ClassValidating
@Horizontal
public class ServerDockerExecutor extends JobExecutor implements Testable<TestData>, Validatable {

	private static final long serialVersionUID = 1L;
	
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
	
	private LineConsumer newInfoLogger(TaskLogger jobLogger) {
		return new LineConsumer(StandardCharsets.UTF_8.name()) {

			private final String sessionId = UUID.randomUUID().toString();
			
			@Override
			public void consume(String line) {
				jobLogger.log(line, sessionId);
			}
			
		};
	}
	
	private LineConsumer newErrorLogger(TaskLogger jobLogger) {
		return new LineConsumer(StandardCharsets.UTF_8.name()) {

			@Override
			public void consume(String line) {
				jobLogger.warning(line);
			}
			
		};
	}
	
	protected List<String> getTrustCertContent() {
		List<String> trustCertContent = new ArrayList<>();
		ServerConfig serverConfig = OneDev.getInstance(ServerConfig.class); 
		File keystoreFile = serverConfig.getKeystoreFile();
		if (keystoreFile != null) {
			String password = serverConfig.getKeystorePassword();
			for (Map.Entry<String, String> entry: new PKCS12CertExtractor(keystoreFile, password).extact().entrySet()) 
				trustCertContent.addAll(Splitter.on('\n').trimResults().splitToList(entry.getValue()));
		}
		if (serverConfig.getTrustCertsDir() != null) {
			for (File file: serverConfig.getTrustCertsDir().listFiles()) {
				if (file.isFile()) {
					try {
						trustCertContent.addAll(FileUtils.readLines(file, UTF_8));
					} catch (IOException e) {
						throw new RuntimeException(e);
					}
				}
			}
		}
		return trustCertContent;
	}
	
	@Override
	public void execute(String jobToken, JobContext jobContext) {
		File hostBuildHome = FileUtils.createTempDir("onedev-build");
		try {
			TaskLogger jobLogger = jobContext.getLogger();
			OneDev.getInstance(ResourceManager.class).run(new Runnable() {

				@Override
				public void run() {
					String network = getName() + "-" + jobContext.getProjectName() + "-" 
							+ jobContext.getBuildNumber() + "-" + jobContext.getRetried();

					jobLogger.log(String.format("Executing job (executor: %s, network: %s)...", getName(), network));
					jobContext.notifyJobRunning(null);
					
					JobManager jobManager = OneDev.getInstance(JobManager.class);		
					File hostCacheHome = getCacheHome();
					
					jobLogger.log("Allocating job caches...") ;
					Map<CacheInstance, Date> cacheInstances = getCacheInstances(hostCacheHome);
					Map<CacheInstance, String> cacheAllocations = jobManager.allocateJobCaches(jobToken, new Date(), cacheInstances);
					preprocess(hostCacheHome, cacheAllocations, new Consumer<File>() {
	
						@Override
						public void accept(File directory) {
							DockerUtils.cleanDirAsRoot(directory, newDocker(), Bootstrap.isInDocker());
						}
						
					});
						
					login(jobLogger);
					
					DockerUtils.createNetwork(newDocker(), network, jobLogger);
					try {
						for (Service jobService: jobContext.getServices()) {
							jobLogger.log("Starting service (name: " + jobService.getName() + ", image: " + jobService.getImage() + ")...");
							DockerUtils.startService(newDocker(), network, jobService.toMap(), jobLogger);
						}
						
						AtomicReference<File> workspaceCache = new AtomicReference<>(null);
						for (Map.Entry<CacheInstance, String> entry: cacheAllocations.entrySet()) {
							if (PathUtils.isCurrent(entry.getValue())) {
								workspaceCache.set(entry.getKey().getDirectory(hostCacheHome));
								break;
							}
						}
						
						File hostWorkspace;
						if (workspaceCache.get() != null) {
							hostWorkspace = workspaceCache.get();
						} else { 
							hostWorkspace = new File(hostBuildHome, "workspace");
							FileUtils.createDir(hostWorkspace);
						}
						
						AtomicReference<File> hostAuthInfoHome = new AtomicReference<>(null);
						try {						
							jobLogger.log("Copying job dependencies...");
							jobContext.copyDependencies(hostWorkspace);
	
							String containerBuildHome;
							String containerWorkspace;
							String containerEntryPoint;
							if (SystemUtils.IS_OS_WINDOWS) {
								containerBuildHome = "C:\\onedev-build";
								containerWorkspace = "C:\\onedev-build\\workspace";
								containerEntryPoint = "cmd";
							} else {
								containerBuildHome = "/onedev-build";
								containerWorkspace = "/onedev-build/workspace";
								containerEntryPoint = "sh";
							}
							
							jobContext.reportJobWorkspace(containerWorkspace);
							CompositeExecutable entryExecutable = new CompositeExecutable(jobContext.getActions());
							
							List<String> errorMessages = new ArrayList<>();
							
							entryExecutable.execute(new LeafHandler() {

								@Override
								public boolean execute(LeafExecutable executable, List<Integer> position) {
									String stepNames = entryExecutable.getNamesAsString(position);
									jobLogger.log("Running step \"" + stepNames + "\"...");
									
									if (executable instanceof CommandExecutable) {
										CommandExecutable commandExecutable = (CommandExecutable) executable;
										String[] containerCommand;
										if (SystemUtils.IS_OS_WINDOWS) {
											if (hostAuthInfoHome.get() != null)
												containerCommand = new String[] {"/c", "xcopy /Y /S /K /Q /H /R C:\\Users\\%USERNAME%\\auth-info\\* C:\\Users\\%USERNAME% > nul && C:\\onedev-build\\job-commands.bat"};						
											else
												containerCommand = new String[] {"/c", "C:\\onedev-build\\job-commands.bat"};						
											File scriptFile = new File(hostBuildHome, "job-commands.bat");
											try {
												FileUtils.writeLines(
														scriptFile, 
														new ArrayList<>(replacePlaceholders(commandExecutable.getCommands(), hostBuildHome)), 
														"\r\n");
											} catch (IOException e) {
												throw new RuntimeException(e);
											}
										} else {
											if (hostAuthInfoHome.get() != null)
												containerCommand = new String[] {"-c", "cp -r -f -p /root/auth-info/. /root && sh /onedev-build/job-commands.sh"};
											else
												containerCommand = new String[] {"/onedev-build/job-commands.sh"};
											File scriptFile = new File(hostBuildHome, "job-commands.sh");
											try {
												FileUtils.writeLines(
														scriptFile, 
														new ArrayList<>(replacePlaceholders(commandExecutable.getCommands(), hostBuildHome)), 
														"\n");
											} catch (IOException e) {
												throw new RuntimeException(e);
											}
										}
										
										String containerName = network + "-step-" + stringifyPosition(position);
										Commandline docker = newDocker();
										docker.clearArgs();
										docker.addArgs("run", "--name=" + containerName, "--network=" + network);
										if (getRunOptions() != null)
											docker.addArgs(StringUtils.parseQuoteTokens(getRunOptions()));
										
										docker.addArgs("-v", getOuterPath(hostBuildHome.getAbsolutePath()) + ":" + containerBuildHome);
										if (workspaceCache.get() != null)
											docker.addArgs("-v", getOuterPath(workspaceCache.get().getAbsolutePath()) + ":" + containerWorkspace);
										for (Map.Entry<CacheInstance, String> entry: cacheAllocations.entrySet()) {
											if (!PathUtils.isCurrent(entry.getValue())) {
												String hostCachePath = entry.getKey().getDirectory(hostCacheHome).getAbsolutePath();
												String containerCachePath = PathUtils.resolve(containerWorkspace, entry.getValue());
												docker.addArgs("-v", getOuterPath(hostCachePath) + ":" + containerCachePath);
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

										if (commandExecutable.isUseTTY())
											docker.addArgs("-t");
										docker.addArgs("-w", containerWorkspace, "--entrypoint=" + containerEntryPoint);
										
										docker.addArgs(commandExecutable.getImage());
										docker.addArgs(containerCommand);
										
										ProcessKiller killer = DockerUtils.newDockerKiller(newDocker(), containerName, jobLogger);
										ExecutionResult result = docker.execute(newInfoLogger(jobLogger), newErrorLogger(jobLogger), null, killer);
										if (result.getReturnCode() != 0) {
											errorMessages.add("Step \"" + stepNames + "\": Command failed with exit code " + result.getReturnCode());
											return false;
										} else {
											return true;
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
											
											List<String> trustCertContent = getTrustCertContent();
											if (!trustCertContent.isEmpty()) {
												installGitCert(new File(hostAuthInfoHome.get(), "trust-cert.pem"), trustCertContent, 
														git, newInfoLogger(jobLogger), newErrorLogger(jobLogger));
											}
	
											int cloneDepth = checkoutExecutable.getCloneDepth();
											
											cloneRepository(git, jobContext.getProjectGitDir().getAbsolutePath(), 
													jobContext.getCommitId().name(), cloneDepth, 
													newInfoLogger(jobLogger), newErrorLogger(jobLogger));
											
											addOriginRemote(git, cloneInfo.getCloneUrl(), newInfoLogger(jobLogger), newErrorLogger(jobLogger));
											
											if (SystemUtils.IS_OS_WINDOWS || !(cloneInfo instanceof SshCloneInfo)) {
												updateSubmodulesIfNecessary(git, cloneDepth, newInfoLogger(jobLogger), newErrorLogger(jobLogger));
											} else if (new File(hostWorkspace, ".gitmodules").exists()) {
												/*
												 * We need to update submodules within a helper image in order to use our own .ssh folder. 
												 * Specifying HOME env to change ~/.ssh folder does not have effect on Linux 
												 */
												Provider<Commandline> dockerProvider = new Provider<Commandline>() {

													@Override
													public Commandline get() {
														return newDocker();
													}
													
												};
												
												String hostAuthInfoHomeOuterPath = getOuterPath(hostAuthInfoHome.get().getAbsolutePath());
												String workspaceOuterPath = getOuterPath(hostWorkspace.getAbsolutePath());
												DockerUtils.dockerUpdateSubmodules(dockerProvider, network, hostAuthInfoHomeOuterPath, 
														workspaceOuterPath, cloneDepth, jobLogger);
											}
											
											return true;
										} catch (Exception e) {
											errorMessages.add("Step \"" + stepNames + "\" is failed: " + getErrorMessage(e));
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
											
											return true;
										} catch (Exception e) {
											errorMessages.add("Step \"" + stepNames + "\" is failed: " + getErrorMessage(e));
											return false;
										} finally {
											FileUtils.deleteDir(filesDir);
										}
									}
								}

								@Override
								public void skip(LeafExecutable executable, List<Integer> position) {
									jobLogger.log("Skipping step \"" + entryExecutable.getNamesAsString(position) + "\"...");
								}
								
							}, new ArrayList<>());

							if (!errorMessages.isEmpty())
								throw new ExplicitException(errorMessages.iterator().next());
							
							jobLogger.log("Reporting job caches...");
							
							jobManager.reportJobCaches(jobToken, getCacheInstances(hostCacheHome).keySet());
						} finally {
							if (hostAuthInfoHome.get() != null)
								FileUtils.deleteDir(hostAuthInfoHome.get());
						}
					} finally {
						DockerUtils.deleteNetwork(newDocker(), network, jobLogger);
					}					
				}
				
			}, jobContext.getResourceRequirements(), jobLogger);
		} finally {
			DockerUtils.cleanDirAsRoot(hostBuildHome, newDocker(), Bootstrap.isInDocker());
			FileUtils.deleteDir(hostBuildHome);
		}
	}

	private void login(TaskLogger jobLogger) {
		for (RegistryLogin login: getRegistryLogins()) 
			DockerUtils.login(newDocker(), login.getRegistryUrl(), login.getUserName(), login.getPassword(), jobLogger);
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
	
	private String getErrorMessage(Exception exception) {
		String errorMessage = ExceptionUtils.getExpectedError(exception);
		if (errorMessage == null) 
			errorMessage = Throwables.getStackTraceAsString(exception);
		return errorMessage;
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
	
	private String getOuterPath(String hostPath) {
		String hostInstallPath = Bootstrap.installDir.getAbsolutePath();
		Preconditions.checkState(hostPath.startsWith(hostInstallPath + "/")
				|| hostPath.startsWith(hostInstallPath + "\\"));
		if (outerInstallPath == null) {
			if (Bootstrap.isInDocker()) {
				AtomicReference<String> installDirRef = new AtomicReference<>(null);
				Commandline docker = newDocker();
				String inspectFormat = String.format(
						"{{range .Mounts}} {{if eq .Destination \"%s\"}} {{.Source}} {{end}} {{end}}", 
						hostInstallPath);
				docker.addArgs("inspect", "-f", inspectFormat, System.getenv("HOSTNAME"));						
				docker.execute(new LineConsumer() {
		
					@Override
					public void consume(String line) {
						installDirRef.set(line.trim());
					}
					
				}, new LineConsumer() {
		
					@Override
					public void consume(String line) {
						logger.error(line);
					}
					
				}).checkReturnCode();
				
				outerInstallPath = Preconditions.checkNotNull(installDirRef.get());
			} else {
				outerInstallPath = hostInstallPath;
			}
		}
		return outerInstallPath + hostPath.substring(hostInstallPath.length());
	}
	
	@Override
	public void test(TestData testData, TaskLogger jobLogger) {
		login(jobLogger);
		
		jobLogger.log("Running container...");
		File workspaceDir = null;
		File cacheDir = null;

		Commandline docker = newDocker();
		try {
			workspaceDir = FileUtils.createTempDir("workspace");
			cacheDir = new File(getCacheHome(), UUID.randomUUID().toString());
			FileUtils.createDir(cacheDir);
			
			jobLogger.log("Test running specified docker image...");
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
			jobLogger.log("Test running busybox...");
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