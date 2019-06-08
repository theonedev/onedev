package io.onedev.server.model.support.jobexecutor;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Callable;

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
import io.onedev.commons.utils.LockUtils;
import io.onedev.commons.utils.StringUtils;
import io.onedev.commons.utils.command.Commandline;
import io.onedev.commons.utils.command.LineConsumer;
import io.onedev.commons.utils.command.ProcessKiller;
import io.onedev.commons.utils.concurrent.ConstrainedRunner;
import io.onedev.server.ci.job.cache.CacheAllocation;
import io.onedev.server.ci.job.cache.CacheCallable;
import io.onedev.server.ci.job.cache.CacheRunner;
import io.onedev.server.ci.job.cache.JobCache;
import io.onedev.server.model.support.jobexecutor.ServerDockerExecutor.TestData;
import io.onedev.server.util.OneContext;
import io.onedev.server.util.patternset.PatternSet;
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
	
	private transient ConstrainedRunner constrainedRunner;

	@Editable(order=20000, group="More Settings", description="Optionally specify docker executable, for instance <i>/usr/local/bin/docker</i>. "
			+ "Leave empty to use docker executable in PATH")
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

	private Commandline getDockerCmd() {
		if (getDockerExecutable() != null)
			return new Commandline(getDockerExecutable());
		else
			return new Commandline("docker");
	}
	
	@SuppressWarnings("unchecked")
	private String getImageOS(Logger logger, String image) {
		logger.info("Checking image OS...");
		Commandline cmd = getDockerCmd();
		cmd.addArgs("inspect", image);
		
		StringBuilder output = new StringBuilder();
		cmd.execute(new LineConsumer(Charsets.UTF_8.name()) {

			@Override
			public void consume(String line) {
				logger.debug(line);
				output.append(line).append("\n");
			}
			
		}, newErrorLogger(logger)).checkReturnCode();

		Map<String, Object> map;
		try {
			map = (Map<String, Object>) new ObjectMapper()
					.readValue(output.toString(), List.class).iterator().next();
			return (String) map.get("Os");
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	private synchronized ConstrainedRunner getConstrainedRunner() {
		if (constrainedRunner == null)
			constrainedRunner = new ConstrainedRunner(capacity);
		return constrainedRunner;
	}
	
	@Override
	public boolean hasCapacity() {
		return getConstrainedRunner().hasCapacity();
	}
	
	private File getCacheHome() {
		return new File(Bootstrap.getCacheDir(), getName());
	}

	@Override
	public void execute(String environment, File workspace, Map<String, String> envVars, 
			List<String> commands, SourceSnapshot snapshot, Collection<JobCache> caches, 
			PatternSet collectFiles, Logger logger) {
		getConstrainedRunner().call(new Callable<Void>() {

			@Override
			public Void call() {
				return new CacheRunner(getCacheHome(), caches).call(new CacheCallable<Void>() {

					@Override
					public Void call(Collection<CacheAllocation> allocations) {
						login(logger);
						
						logger.info("Pulling image...") ;
						Commandline cmd = getDockerCmd();
						cmd.addArgs("pull", environment);
						cmd.execute(newInfoLogger(logger), newErrorLogger(logger)).checkReturnCode();
						
						cmd.clearArgs();
						String jobInstance = UUID.randomUUID().toString();
						cmd.addArgs("run", "--rm", "--name", jobInstance);
						for (Map.Entry<String, String> entry: envVars.entrySet())
							cmd.addArgs("--env", entry.getKey() + "=" + entry.getValue());
						if (getRunOptions() != null)
							cmd.addArgs(StringUtils.parseQuoteTokens(getRunOptions()));
						
						String imageOS = getImageOS(logger, environment);
						logger.info("Detected image OS: " + imageOS);

						boolean windows = imageOS.equals("windows");
						
						String dockerWorkspacePath;
						if (windows)
							dockerWorkspacePath = "C:\\" + WORKSPACE;
						else 
							dockerWorkspacePath = "/" + WORKSPACE;
						
						File workspaceCache = null;
						for (CacheAllocation allocation: allocations) {
							if (allocation.isWorkspace()) {
								workspaceCache = allocation.getInstance();
								break;
							}
						}
						
						File effectiveWorkspace = workspaceCache != null? workspaceCache: workspace;
						
						if (snapshot != null) {
							logger.info("Cloning source code...");
							snapshot.checkout(effectiveWorkspace);
						}
						
						if (workspaceCache != null) {
							try {
								FileUtils.copyDirectory(workspace, workspaceCache);
							} catch (IOException e) {
								throw new RuntimeException(e);
							}
						}
						
						cmd.addArgs("-v", effectiveWorkspace.getAbsolutePath() + ":" + dockerWorkspacePath);
						for (CacheAllocation allocation: allocations) {
							if (!allocation.isWorkspace())
								cmd.addArgs("-v", allocation.getInstance().getAbsolutePath() + ":" + allocation.resolvePath(dockerWorkspacePath));
						}
						cmd.addArgs("-w", dockerWorkspacePath);
						
						if (windows) {
							File scriptFile = new File(effectiveWorkspace, "onedev-job-commands.bat");
							try {
								FileUtils.writeLines(scriptFile, commands, "\r\n");
							} catch (IOException e) {
								throw new RuntimeException(e);
							}
							cmd.addArgs(environment);
							cmd.addArgs("cmd", "/c", dockerWorkspacePath + "\\onedev-job-commands.bat");
						} else {
							File scriptFile = new File(effectiveWorkspace, "onedev-job-commands.sh");
							try {
								FileUtils.writeLines(scriptFile, commands, "\n");
							} catch (IOException e) {
								throw new RuntimeException(e);
							}
							cmd.addArgs(environment);
							cmd.addArgs("sh", dockerWorkspacePath + "/onedev-job-commands.sh");
						}
						
						logger.info("Running container to execute job...");
						
						try {
							cmd.execute(newInfoLogger(logger), newErrorLogger(logger), null, new ProcessKiller() {
	
								@Override
								public void kill(Process process) {
									logger.info("Stopping container...");
									Commandline cmd = getDockerCmd();
									cmd.addArgs("stop", jobInstance);
									cmd.execute(newInfoLogger(logger), newErrorLogger(logger));
								}
								
							}, logger).checkReturnCode();
							
							return null;
						} finally {
							if (workspaceCache != null) {
								int baseLen = workspaceCache.getAbsolutePath().length()+1;
								for (File file: collectFiles.listFiles(workspaceCache)) {
									try {
										FileUtils.copyFile(file, new File(workspace, file.getAbsolutePath().substring(baseLen)));
									} catch (IOException e) {
										throw new RuntimeException(e);
									}
								}
							}
						}
					}
					
				}, logger);
			}
			
		});
	}

	private LineConsumer newInfoLogger(Logger logger) {
		return new LineConsumer(Charsets.UTF_8.name()) {

			@Override
			public void consume(String line) {
				logger.info(line);
			}
			
		};
	}

	private LineConsumer newErrorLogger(Logger logger) {
		return new LineConsumer(Charsets.UTF_8.name()) {

			@Override
			public void consume(String line) {
				logger.error(line);
			}
			
		};
	}
	
	private void login(Logger logger) {
		for (RegistryLogin login: getRegistryLogins()) {
			if (login.getRegistryUrl() != null)
				logger.info("Login to docker registry '{}'...", login.getRegistryUrl());
			else
				logger.info("Login to official docker registry...");
			Commandline cmd = getDockerCmd();
			cmd.addArgs("login", "-u", login.getUserName(), "--password-stdin");
			if (login.getRegistryUrl() != null)
				cmd.addArgs(login.getRegistryUrl());
			ByteArrayInputStream input;
			try {
				input = new ByteArrayInputStream(login.getPassword().getBytes(Charsets.UTF_8.name()));
			} catch (UnsupportedEncodingException e) {
				throw new RuntimeException(e);
			}
			cmd.execute(newInfoLogger(logger), newErrorLogger(logger), input).checkReturnCode();
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
	public void checkCaches() {
		File cacheHome = getCacheHome();
		if (cacheHome.exists()) {
			for (File keyDir: cacheHome.listFiles()) {
				for (File cacheInstance: keyDir.listFiles()) {
					if (System.currentTimeMillis() - cacheInstance.lastModified() > getCacheTTL() * 24L * 3600L * 1000L) {
						File lockFile = new File(cacheInstance, JobCache.LOCK_FILE);
						try {
							if (lockFile.createNewFile()) {
								LockUtils.call(keyDir.getAbsolutePath(), new Callable<Void>() {

									@Override
									public Void call() throws Exception {
										cleanDir(cacheInstance);
										FileUtils.deleteDir(cacheInstance);
										return null;
									}
									
								});
							}
						} catch (IOException e) {
							logger.error("Error removing cache '" + cacheInstance.getAbsolutePath() + "'", e);
						}
					}
				}
			}
		}
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
	public void test(TestData testData) {
		logger.info("Testing local docker executor...");
		
		login(logger);
		
		logger.info("Pulling image...");
		
		Commandline cmd = getDockerCmd();
		cmd.addArgs("pull", testData.getDockerImage());
		cmd.execute(newInfoLogger(logger), newErrorLogger(logger)).checkReturnCode();
		
		boolean windows = getImageOS(logger, testData.getDockerImage()).equals("windows");
		
		logger.info("Running container...");
		File cacheHome = getCacheHome();
		boolean cacheHomeExists = cacheHome.exists();
		File workspaceDir = null;
		File cacheDir = null;
		try {
			workspaceDir = Bootstrap.createTempDir("workspace");
			cacheDir = new File(cacheHome, UUID.randomUUID().toString());
			FileUtils.createDir(cacheDir);
			
			cmd.clearArgs();
			cmd.addArgs("run", "--rm");
			if (getRunOptions() != null)
				cmd.addArgs(StringUtils.parseQuoteTokens(getRunOptions()));
			String containerWorkspacePath;
			String containerCachePath = "$onedev-cache-test$";
			if (windows) {
				containerWorkspacePath = "C:\\" + WORKSPACE;
				containerCachePath = "C:\\" + containerCachePath;
			} else {
				containerWorkspacePath = "/" + WORKSPACE;
				containerCachePath = "/" + containerCachePath;
			}
			cmd.addArgs("-v", workspaceDir.getAbsolutePath() + ":" + containerWorkspacePath);
			cmd.addArgs("-v", cacheDir.getAbsolutePath() + ":" + containerCachePath);
			
			cmd.addArgs("-w", containerWorkspacePath);
			cmd.addArgs(testData.getDockerImage());
			
			if (windows) 
				cmd.addArgs("cmd", "/c", "echo hello from container");
			else 
				cmd.addArgs("sh", "-c", "echo hello from container");
			
			cmd.execute(newInfoLogger(logger), newErrorLogger(logger)).checkReturnCode();
		} finally {
			if (workspaceDir != null)
				FileUtils.deleteDir(workspaceDir);
			if (cacheDir != null)
				FileUtils.deleteDir(cacheDir);
			if (!cacheHomeExists)
				FileUtils.deleteDir(cacheHome);
		}
		
		if (!SystemUtils.IS_OS_WINDOWS) {
			logger.info("Checking busybox...");
			cmd = getDockerCmd();
			cmd.addArgs("run", "--rm", "busybox", "sh", "-c", "echo hello from busybox");			
			cmd.execute(newInfoLogger(logger), newErrorLogger(logger)).checkReturnCode();
		}
	}
	
	@Override
	public void cleanDir(File dir) {
		if (SystemUtils.IS_OS_WINDOWS) {
			FileUtils.cleanDir(dir);
		} else {
			Commandline cmd = getDockerCmd();
			String containerPath = "/onedev_dir_to_clean";
			cmd.addArgs("run", "-v", dir.getAbsolutePath() + ":" + containerPath, "--rm", 
					"busybox", "sh", "-c", "rm -rf " + containerPath + "/*");			
			cmd.execute(newInfoLogger(logger), newErrorLogger(logger)).checkReturnCode();
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