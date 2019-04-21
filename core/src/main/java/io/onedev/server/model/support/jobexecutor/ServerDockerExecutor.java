package io.onedev.server.model.support.jobexecutor;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Callable;

import javax.validation.ConstraintValidatorContext;

import org.apache.commons.codec.Charsets;
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
import io.onedev.server.ci.job.cache.CacheRunnable;
import io.onedev.server.ci.job.cache.CacheRunner;
import io.onedev.server.ci.job.cache.JobCache;
import io.onedev.server.model.support.jobexecutor.ServerDockerExecutor.TestData;
import io.onedev.server.util.OneContext;
import io.onedev.server.util.patternset.PatternSet;
import io.onedev.server.util.validation.Validatable;
import io.onedev.server.util.validation.annotation.ClassValidating;
import io.onedev.server.web.editable.annotation.Editable;
import io.onedev.server.web.editable.annotation.OmitName;
import io.onedev.server.web.editable.annotation.Password;
import io.onedev.server.web.editable.annotation.ShowCondition;
import io.onedev.server.web.util.Testable;

@Editable(order=100, description="This executor interpretates job environments as docker images, "
		+ "and will execute CI jobs inside docker containers created on OneDev server")
@ClassValidating
public class ServerDockerExecutor extends JobExecutor implements Testable<TestData>, Validatable {

	private static final long serialVersionUID = 1L;
	
	private static final Logger logger = LoggerFactory.getLogger(ServerDockerExecutor.class);

	private String dockerExecutable;
	
	private String dockerRegistry;
	
	private boolean authenticateToRegistry;
	
	private String userName;
	
	private String password;
	
	private String runOptions;
	
	private int capacity = Runtime.getRuntime().availableProcessors();

	private transient ConstrainedRunner constrainedRunner;

	@Editable(order=1000, description="Optionally specify docker executable, for instance <i>/usr/local/bin/docker</i>. "
			+ "Leave empty to use docker executable in PATH")
	public String getDockerExecutable() {
		return dockerExecutable;
	}

	public void setDockerExecutable(String dockerExecutable) {
		this.dockerExecutable = dockerExecutable;
	}

	@Editable(order=1100, description="Optionally specify a docker registry to use. Leave empty to use the official registry")
	public String getDockerRegistry() {
		return dockerRegistry;
	}

	public void setDockerRegistry(String dockerRegistry) {
		this.dockerRegistry = dockerRegistry;
	}

	@Editable(order=1150)
	public boolean isAuthenticateToRegistry() {
		return authenticateToRegistry;
	}

	public void setAuthenticateToRegistry(boolean authenticateToRegistry) {
		this.authenticateToRegistry = authenticateToRegistry;
	}

	@Editable(order=1200, description="Specify user name to access docker registry")
	@NotEmpty
	@ShowCondition("isRegistryAuthenticationRequired")
	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	@Editable(order=1300, description="Specify password to access docker registry")
	@Password
	@NotEmpty
	@ShowCondition("isRegistryAuthenticationRequired")
	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}
	
	@Editable(order=20000, group="More Settings", description="Optionally specify options to run container. For instance, you may use <tt>-m 2g</tt> "
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

	@Editable(order=21000, group="More Settings", description="Specify max number of concurrent jobs being executed. Each job execution "
			+ "will launch a separate docker container. Defaults to number of processors in the system")
	public int getCapacity() {
		return capacity;
	}

	public void setCapacity(int capacity) {
		this.capacity = capacity;
	}
	
	private Commandline getDockerCmd() {
		if (getDockerExecutable() != null)
			return new Commandline(getDockerExecutable());
		else
			return new Commandline("docker");
	}
	
	private String getPullImage(String image) {
		if (getDockerRegistry() != null) {
			if (image.contains("/"))
				return getDockerRegistry() + "/" + image;
			else
				return getDockerRegistry() + "/library/" + image;
		} else {
			return image;
		}
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
		getConstrainedRunner().run(new Runnable() {

			@Override
			public void run() {
				new CacheRunner(getCacheHome(), caches).run(new CacheRunnable() {

					@Override
					public void run(Collection<CacheAllocation> allocations) {
						login(logger);
						
						logger.info("Pulling image...") ;
						Commandline cmd = getDockerCmd();
						cmd.addArgs("pull", getPullImage(environment));
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
								try {
									FileUtils.copyDirectory(workspace, workspaceCache);
								} catch (IOException e) {
									throw new RuntimeException(e);
								}
								break;
							}
						}
						
						File effectiveWorkspace = workspaceCache != null? workspaceCache: workspace;
						
						if (snapshot != null) {
							logger.info("Cloning source code...");
							snapshot.checkout(effectiveWorkspace);
						}
									
						cmd.addArgs("-v", effectiveWorkspace.getAbsolutePath() + ":" + dockerWorkspacePath);
						for (CacheAllocation allocation: allocations) {
							if (!allocation.isWorkspace())
								cmd.addArgs("-v", allocation.getInstance().getAbsolutePath() + ":" + allocation.getPath());
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
							cmd.addArgs("sh", "-c", dockerWorkspacePath + "/onedev-job-commands.sh");
						}
						
						logger.info("Running container to execute job...");
						cmd.execute(newInfoLogger(logger), newErrorLogger(logger), null, new ProcessKiller() {

							@Override
							public void kill(Process process) {
								logger.info("Stopping container...");
								Commandline cmd = getDockerCmd();
								cmd.addArgs("stop", jobInstance);
								cmd.execute(newInfoLogger(logger), newErrorLogger(logger));
							}
							
						}).checkReturnCode();		
						
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
		if (isAuthenticateToRegistry()) {
			logger.info("Login to docker registry...");
			Commandline cmd = getDockerCmd();
			cmd.addArgs("login", "-u", getUserName(), "--password-stdin");
			if (getDockerRegistry() != null)
				cmd.addArgs(getDockerRegistry());
			ByteArrayInputStream input;
			try {
				input = new ByteArrayInputStream(getPassword().getBytes(Charsets.UTF_8.name()));
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
								/*
								 * Remove other files first to avoid locking for too long time
								 */
								for (File each: cacheInstance.listFiles()) {
									if (!each.getName().equals(JobCache.LOCK_FILE)) {
										if (each.isFile())
											FileUtils.deleteFile(each);
										else
											FileUtils.deleteDir(each);
									}
								}
								LockUtils.call(keyDir.getAbsolutePath(), new Callable<Void>() {

									@Override
									public Void call() throws Exception {
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
		if (getRunOptions() != null) {
			String[] arguments = StringUtils.parseQuoteTokens(getRunOptions());
			String invalidOptions[] = new String[] {"-w", "--workdir", "-d", "--detach", "-a", "--attach", "-t", "--tty", 
					"-i", "--interactive", "--rm", "--restart", "--name"}; 
			if (hasOptions(arguments, invalidOptions)) {
				context.disableDefaultConstraintViolation();
				StringBuilder errorMessage = new StringBuilder("Can not use options: "
						+ Joiner.on(", ").join(invalidOptions));
				context.buildConstraintViolationWithTemplate(errorMessage.toString())
						.addPropertyNode("runOptions").addConstraintViolation();
				return false;
			} 
		}
		return true;
	}
	
	@Override
	public void test(TestData testData) {
		logger.info("Testing local docker executor...");
		
		login(logger);
		
		logger.info("Pulling image...");
		
		Commandline cmd = getDockerCmd();
		cmd.addArgs("pull", getPullImage(testData.getDockerImage()));
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
			String dockerWorkspacePath;
			String dockerCachePath = "$onedev-cache-test$";
			if (windows) {
				dockerWorkspacePath = "C:\\" + WORKSPACE;
				dockerCachePath = "C:\\" + dockerCachePath;
			} else {
				dockerWorkspacePath = "/" + WORKSPACE;
				dockerCachePath = "/" + dockerCachePath;
			}
			cmd.addArgs("-v", workspaceDir.getAbsolutePath() + ":" + dockerWorkspacePath);
			cmd.addArgs("-v", cacheDir.getAbsolutePath() + ":" + dockerCachePath);
			
			cmd.addArgs("-w", dockerWorkspacePath);
			cmd.addArgs(testData.getDockerImage());
			
			if (windows) 
				cmd.addArgs("cmd", "/c", "echo this is a test");
			else 
				cmd.addArgs("sh", "-c", "echo this is a test");
			
			cmd.execute(newInfoLogger(logger), newErrorLogger(logger)).checkReturnCode();
		} finally {
			if (workspaceDir != null)
				FileUtils.deleteDir(workspaceDir);
			if (cacheDir != null)
				FileUtils.deleteDir(cacheDir);
			if (!cacheHomeExists)
				FileUtils.deleteDir(cacheHome);
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