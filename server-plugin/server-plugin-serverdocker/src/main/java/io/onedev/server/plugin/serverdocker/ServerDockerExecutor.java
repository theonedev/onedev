package io.onedev.server.plugin.serverdocker;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.function.Consumer;

import javax.validation.ConstraintValidatorContext;

import org.apache.commons.codec.Charsets;
import org.apache.commons.lang3.SystemUtils;
import org.hibernate.validator.constraints.NotEmpty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Joiner;

import io.onedev.commons.launcher.bootstrap.Bootstrap;
import io.onedev.commons.utils.FileUtils;
import io.onedev.commons.utils.PathUtils;
import io.onedev.commons.utils.StringUtils;
import io.onedev.commons.utils.command.Commandline;
import io.onedev.commons.utils.command.LineConsumer;
import io.onedev.commons.utils.command.ProcessKiller;
import io.onedev.commons.utils.concurrent.CapacityRunner;
import io.onedev.k8shelper.CacheInstance;
import io.onedev.k8shelper.KubernetesHelper;
import io.onedev.server.OneDev;
import io.onedev.server.ci.job.JobContext;
import io.onedev.server.ci.job.JobManager;
import io.onedev.server.model.support.JobExecutor;
import io.onedev.server.plugin.serverdocker.ServerDockerExecutor.TestData;
import io.onedev.server.util.JobLogger;
import io.onedev.server.util.OneContext;
import io.onedev.server.util.validation.Validatable;
import io.onedev.server.util.validation.annotation.ClassValidating;
import io.onedev.server.web.editable.annotation.Editable;
import io.onedev.server.web.editable.annotation.NameOfEmptyValue;
import io.onedev.server.web.editable.annotation.OmitName;
import io.onedev.server.web.editable.annotation.Password;
import io.onedev.server.web.util.Testable;

@Editable(order=100, description="This executor interpretates job environments as docker images, "
		+ "and will execute CI jobs inside docker containers created on OneDev server. Please "
		+ "note that On non-Windows platforms, OneDev needs to run busybox image to do some "
		+ "cleanups as root. This image has to be pre-pulled on server if your server is not "
		+ "allowed to connect to official docker registry")
@ClassValidating
public class ServerDockerExecutor extends JobExecutor implements Testable<TestData>, Validatable {

	private static final long serialVersionUID = 1L;
	
	private static final Logger logger = LoggerFactory.getLogger(ServerDockerExecutor.class);

	private String dockerExecutable;
	
	private String runOptions;
	
	private int capacity = Runtime.getRuntime().availableProcessors();
	
	private List<RegistryLogin> registryLogins = new ArrayList<>();
	
	private transient CapacityRunner capacityRunner;

	@Editable(order=100, description="Optionally specify docker executable, for instance <i>/usr/local/bin/docker</i>. "
			+ "Leave empty to use docker executable in PATH")
	@NameOfEmptyValue("Use default")
	public String getDockerExecutable() {
		return dockerExecutable;
	}

	public void setDockerExecutable(String dockerExecutable) {
		this.dockerExecutable = dockerExecutable;
	}

	@Editable(order=20100, group="More Settings", description="Optionally specify options to run container. For instance, you may use <tt>-m 2g</tt> "
			+ "to limit memory of created container to be 2 giga bytes")
	public String getRunOptions() {
		return runOptions;
	}

	public void setRunOptions(String runOptions) {
		this.runOptions = runOptions;
	}

	public static boolean isRegistryAuthenticationRequired() {
		return (boolean) OneContext.get().getEditContext().getInputValue("authenticateToRegistry");
	}

	@Editable(order=20200, group="More Settings", description="Specify max number of concurrent jobs being executed. Each job execution "
			+ "will launch a separate docker container. Defaults to number of processors in the system")
	public int getCapacity() {
		return capacity;
	}

	public void setCapacity(int capacity) {
		this.capacity = capacity;
	}

	@Editable(order=20300, group="More Settings", description="Specify login information for docker registries if necessary")
	public List<RegistryLogin> getRegistryLogins() {
		return registryLogins;
	}

	public void setRegistryLogins(List<RegistryLogin> registryLogins) {
		this.registryLogins = registryLogins;
	}

	private Commandline getDocker() {
		if (getDockerExecutable() != null)
			return new Commandline(getDockerExecutable());
		else
			return new Commandline("docker");
	}
	
	@SuppressWarnings("unchecked")
	private String getImageOS(JobLogger logger, String image) {
		logger.log("Checking image OS...");
		Commandline docker = getDocker();
		docker.addArgs("inspect", image);
		
		StringBuilder builder = new StringBuilder();
		docker.execute(new LineConsumer(Charsets.UTF_8.name()) {

			@Override
			public void consume(String line) {
				ServerDockerExecutor.logger.debug(line);
				builder.append(line).append("\n");
			}
			
		}, newJobLogger(logger)).checkReturnCode();

		Map<String, Object> map;
		try {
			map = (Map<String, Object>) new ObjectMapper()
					.readValue(builder.toString(), List.class).iterator().next();
			return (String) map.get("Os");
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	private synchronized CapacityRunner getCapacityRunner() {
		if (capacityRunner == null)
			capacityRunner = new CapacityRunner(capacity);
		return capacityRunner;
	}
	
	private File getCacheHome() {
		return new File(System.getProperty("user.home"), "onedev-cache"); 
	}

	@Override
	public void execute(String jobToken, JobContext jobContext) {
		File hostCIHome = FileUtils.createTempDir("onedev-ci");
		try {
			JobLogger logger = jobContext.getLogger();
			
			getCapacityRunner().call(new Callable<Void>() {
	
				@Override
				public Void call() {
					jobContext.notifyJobRunning();
					
					JobManager jobManager = OneDev.getInstance(JobManager.class);		
					File hostCacheHome = getCacheHome();
					FileUtils.createDir(hostCacheHome);
					
					logger.log("Allocating job caches...") ;
					Map<CacheInstance, Date> cacheInstances = KubernetesHelper.getCacheInstances(hostCacheHome);
					Map<CacheInstance, String> cacheAllocations = jobManager.allocateJobCaches(jobToken, new Date(), cacheInstances);
					KubernetesHelper.preprocess(hostCacheHome, cacheAllocations, new Consumer<File>() {
	
						@Override
						public void accept(File directory) {
							cleanDirAsRoot(directory);
						}
						
					});
						
					login(logger);
					
					logger.log("Pulling image...") ;
					Commandline docker = getDocker();
					docker.addArgs("pull", jobContext.getEnvironment());
					docker.execute(newDebugLogger(), newJobLogger(logger)).checkReturnCode();
					
					docker.clearArgs();
					String dockerInstance = UUID.randomUUID().toString();
					docker.addArgs("run", "--rm", "--name", dockerInstance);
					for (Map.Entry<String, String> entry: jobContext.getEnvVars().entrySet())
						docker.addArgs("--env", entry.getKey() + "=" + entry.getValue());
					if (getRunOptions() != null)
						docker.addArgs(StringUtils.parseQuoteTokens(getRunOptions()));
					
					String imageOS = getImageOS(logger, jobContext.getEnvironment());
					logger.log("Detected image OS: " + imageOS);
	
					boolean isWindows = imageOS.equals("windows");
					
					File workspaceCache = null;
					for (Map.Entry<CacheInstance, String> entry: cacheAllocations.entrySet()) {
						if (PathUtils.isCurrent(entry.getValue())) {
							workspaceCache = entry.getKey().getDirectory(hostCacheHome);
							break;
						}
					}
					
					File hostWorkspace;
					if (workspaceCache != null) {
						hostWorkspace = workspaceCache;
					} else { 
						hostWorkspace = new File(hostCIHome, "workspace");
						FileUtils.createDir(hostWorkspace);
					}
					
					if (jobContext.isRetrieveSource()) {
						logger.log("Retrieving source code...");
						jobContext.retrieveSource(hostWorkspace);
					}
					
					logger.log("Retrieving job dependencies...");
					try {
						FileUtils.copyDirectory(jobContext.getServerWorkspace(), hostWorkspace);
					} catch (IOException e) {
						throw new RuntimeException(e);
					}

					String containerCIHome;
					String containerWorkspace;
					String[] containerCommand;
					if (isWindows) {
						containerCIHome = "C:\\onedev-ci";
						containerWorkspace = "C:\\onedev-ci\\workspace";
						containerCommand = new String[] {"cmd", "/c", "C:\\onedev-ci\\job-commands.bat"};						

						File scriptFile = new File(hostCIHome, "job-commands.bat");
						try {
							FileUtils.writeLines(scriptFile, jobContext.getCommands(), "\r\n");
						} catch (IOException e) {
							throw new RuntimeException(e);
						}
					} else {
						containerCIHome = "/onedev-ci";
						containerWorkspace = "/onedev-ci/workspace";
						containerCommand = new String[] {"sh", "/onedev-ci/job-commands.sh"};
						
						File scriptFile = new File(hostCIHome, "job-commands.sh");
						try {
							FileUtils.writeLines(scriptFile, jobContext.getCommands(), "\n");
						} catch (IOException e) {
							throw new RuntimeException(e);
						}
					}
					
					docker.addArgs("-v", hostCIHome.getAbsolutePath() + ":" + containerCIHome);
					if (workspaceCache != null)
						docker.addArgs("-v", workspaceCache.getAbsolutePath() + ":" + containerWorkspace);
					for (Map.Entry<CacheInstance, String> entry: cacheAllocations.entrySet()) {
						if (!PathUtils.isCurrent(entry.getValue())) {
							String hostCachePath = entry.getKey().getDirectory(hostCacheHome).getAbsolutePath();
							String containerCachePath = PathUtils.resolve(containerWorkspace, entry.getValue());
							docker.addArgs("-v", hostCachePath + ":" + containerCachePath);
						}
					}
					docker.addArgs("-w", containerWorkspace, jobContext.getEnvironment());
					docker.addArgs(containerCommand);
					
					logger.log("Running container to execute job...");
					
					try {
						docker.execute(newJobLogger(logger), newJobLogger(logger), null, new ProcessKiller() {
	
							@Override
							public void kill(Process process) {
								logger.log("Stopping container...");
								Commandline cmd = getDocker();
								cmd.addArgs("stop", dockerInstance);
								cmd.execute(newDebugLogger(), newJobLogger(logger)).checkReturnCode();
							}
							
						}).checkReturnCode();
					} finally {
						logger.log("Sending job outcomes...");
						
						int baseLen = hostWorkspace.getAbsolutePath().length()+1;
						for (File file: jobContext.getCollectFiles().listFiles(hostWorkspace)) {
							try {
								FileUtils.copyFile(file, new File(jobContext.getServerWorkspace(), file.getAbsolutePath().substring(baseLen)));
							} catch (IOException e) {
								throw new RuntimeException(e);
							}
						}
					}
					logger.log("Reporting job caches...");
					
					jobManager.reportJobCaches(jobToken, KubernetesHelper.getCacheInstances(hostCacheHome).keySet());
					
					return null;
				}
				
			});
		} finally {
			cleanDirAsRoot(hostCIHome);
			FileUtils.deleteDir(hostCIHome);
		}
	}

	private LineConsumer newJobLogger(JobLogger logger) {
		return new LineConsumer(Charsets.UTF_8.name()) {

			@Override
			public void consume(String line) {
				logger.log(line);
			}
			
		};
	}

	private LineConsumer newDebugLogger() {
		return new LineConsumer(Charsets.UTF_8.name()) {

			@Override
			public void consume(String line) {
				logger.debug(line);
			}
			
		};
	}
	
	private void login(JobLogger logger) {
		for (RegistryLogin login: getRegistryLogins()) {
			if (login.getRegistryUrl() != null)
				logger.log(String.format("Login to docker registry '%s'...", login.getRegistryUrl()));
			else
				logger.log("Login to official docker registry...");
			Commandline cmd = getDocker();
			cmd.addArgs("login", "-u", login.getUserName(), "--password-stdin");
			if (login.getRegistryUrl() != null)
				cmd.addArgs(login.getRegistryUrl());
			ByteArrayInputStream input;
			try {
				input = new ByteArrayInputStream(login.getPassword().getBytes(Charsets.UTF_8.name()));
			} catch (UnsupportedEncodingException e) {
				throw new RuntimeException(e);
			}
			cmd.execute(newDebugLogger(), newJobLogger(logger), input).checkReturnCode();
		}
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
					throw new RuntimeException("Invalid option: " + option);
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
			String invalidOptions[] = new String[] {"-w", "--workdir", "-d", "--detach", "-a", "--attach", "-t", "--tty", 
					"-i", "--interactive", "--rm", "--restart", "--name"}; 
			if (hasOptions(arguments, invalidOptions)) {
				StringBuilder errorMessage = new StringBuilder("Can not use options: "
						+ Joiner.on(", ").join(invalidOptions));
				context.buildConstraintViolationWithTemplate(errorMessage.toString())
						.addPropertyNode("runOptions").addConstraintViolation();
				isValid = false;
			} 
		}
		if (!isValid)
			context.disableDefaultConstraintViolation();
		return isValid;
	}
	
	@Override
	public void test(TestData testData, JobLogger logger) {
		login(logger);
		
		logger.log("Pulling image...");
		
		Commandline cmd = getDocker();
		cmd.addArgs("pull", testData.getDockerImage());
		cmd.execute(newDebugLogger(), newJobLogger(logger)).checkReturnCode();
		
		boolean windows = getImageOS(logger, testData.getDockerImage()).equals("windows");
		
		logger.log("Running container...");
		File workspaceDir = null;
		File cacheDir = null;
		try {
			workspaceDir = Bootstrap.createTempDir("workspace");
			cacheDir = new File(getCacheHome(), UUID.randomUUID().toString());
			FileUtils.createDir(cacheDir);
			
			cmd.clearArgs();
			cmd.addArgs("run", "--rm");
			if (getRunOptions() != null)
				cmd.addArgs(StringUtils.parseQuoteTokens(getRunOptions()));
			String containerWorkspacePath;
			String containerCachePath;
			if (windows) {
				containerWorkspacePath = "C:\\onedev-ci\\workspace";
				containerCachePath = "C:\\onedev-ci\\cache";
			} else {
				containerWorkspacePath = "/onedev-ci/workspace";
				containerCachePath = "/onedev-ci/cache";
			}
			cmd.addArgs("-v", workspaceDir.getAbsolutePath() + ":" + containerWorkspacePath);
			cmd.addArgs("-v", cacheDir.getAbsolutePath() + ":" + containerCachePath);
			
			cmd.addArgs("-w", containerWorkspacePath);
			cmd.addArgs(testData.getDockerImage());
			
			if (windows) 
				cmd.addArgs("cmd", "/c", "echo hello from container");
			else 
				cmd.addArgs("sh", "-c", "echo hello from container");
			
			cmd.execute(newJobLogger(logger), newJobLogger(logger)).checkReturnCode();
		} finally {
			if (workspaceDir != null)
				FileUtils.deleteDir(workspaceDir);
			if (cacheDir != null)
				FileUtils.deleteDir(cacheDir);
		}
		
		if (!SystemUtils.IS_OS_WINDOWS) {
			logger.log("Checking busybox...");
			cmd = getDocker();
			cmd.addArgs("run", "--rm", "busybox", "sh", "-c", "echo hello from busybox");			
			cmd.execute(newJobLogger(logger), newJobLogger(logger)).checkReturnCode();
		}
	}
	
	public void cleanDirAsRoot(File dir) {
		if (SystemUtils.IS_OS_WINDOWS) {
			FileUtils.cleanDir(dir);
		} else {
			Commandline cmd = getDocker();
			String containerPath = "/dir-to-clean";
			cmd.addArgs("run", "-v", dir.getAbsolutePath() + ":" + containerPath, "--rm", 
					"busybox", "sh", "-c", "rm -rf " + containerPath + "/*");			
			cmd.execute(new LineConsumer() {

				@Override
				public void consume(String line) {
					logger.info(line);
				}
				
			}, new LineConsumer() {

				@Override
				public void consume(String line) {
					logger.error(line);
				}
				
			}).checkReturnCode();
		}
	}
	
	@Editable
	public static class RegistryLogin implements Serializable {
		
		private static final long serialVersionUID = 1L;

		private String registryUrl;
		
		private String userName;
		
		private String password;

		@Editable(order=100, description="Specify registry url. Leave empty for official registry")
		@NameOfEmptyValue("Default Registry")
		public String getRegistryUrl() {
			return registryUrl;
		}

		public void setRegistryUrl(String registryUrl) {
			this.registryUrl = registryUrl;
		}

		@Editable(order=200)
		@NotEmpty
		public String getUserName() {
			return userName;
		}

		public void setUserName(String userName) {
			this.userName = userName;
		}

		@Editable(order=300)
		@NotEmpty
		@Password
		public String getPassword() {
			return password;
		}

		public void setPassword(String password) {
			this.password = password;
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