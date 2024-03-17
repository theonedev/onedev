package io.onedev.server.plugin.executor.servershell;

import io.onedev.agent.job.FailedException;
import io.onedev.commons.bootstrap.Bootstrap;
import io.onedev.commons.loader.AppLoader;
import io.onedev.commons.utils.ExceptionUtils;
import io.onedev.commons.utils.ExplicitException;
import io.onedev.commons.utils.FileUtils;
import io.onedev.commons.utils.TaskLogger;
import io.onedev.commons.utils.command.Commandline;
import io.onedev.commons.utils.command.ExecutionResult;
import io.onedev.k8shelper.*;
import io.onedev.server.OneDev;
import io.onedev.server.annotation.*;
import io.onedev.server.cluster.ClusterManager;
import io.onedev.server.cluster.ClusterRunnable;
import io.onedev.server.git.location.GitLocation;
import io.onedev.server.job.*;
import io.onedev.server.model.support.administration.jobexecutor.JobExecutor;
import io.onedev.server.plugin.executor.servershell.ServerShellExecutor.TestData;
import io.onedev.server.terminal.CommandlineShell;
import io.onedev.server.terminal.Shell;
import io.onedev.server.terminal.Terminal;
import io.onedev.server.util.DateUtils;
import io.onedev.server.web.util.Testable;
import org.apache.commons.lang.SystemUtils;

import javax.validation.constraints.NotEmpty;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.onedev.agent.ExecutorUtils.newInfoLogger;
import static io.onedev.agent.ExecutorUtils.newWarningLogger;
import static io.onedev.agent.ShellExecutorUtils.testCommands;
import static io.onedev.k8shelper.KubernetesHelper.*;

@Editable(order=ServerShellExecutor.ORDER, name="Server Shell Executor", description="" +
		"This executor runs build jobs with OneDev server's shell facility.<br>" +
		"<b class='text-danger'>WARNING</b>: Jobs running with this executor has same " +
		"permission as OneDev server process. Make sure it can only be used by trusted " +
		"jobs via job requirement setting")
@Horizontal
public class ServerShellExecutor extends JobExecutor implements Testable<TestData> {

	private static final long serialVersionUID = 1L;
	
	static final int ORDER = 400;
	
	private String concurrency;
	
	private transient volatile LeafFacade runningStep;
	
	private transient volatile File buildHome;

	@Editable(order=1000, description = "" +
			"Specify max number of jobs this executor can run concurrently. " +
			"Leave empty to set as CPU cores")
	@Numeric
	public String getConcurrency() {
		return concurrency;
	}

	public void setConcurrency(String concurrency) {
		this.concurrency = concurrency;
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
			getJobManager().runJob(jobContext, new JobRunnable() {

				@Override
				public void run(TaskLogger jobLogger) {
					notifyJobRunning(jobContext.getBuildId(), null);
					
					if (OneDev.getK8sService() != null) {
						throw new ExplicitException(""
								+ "OneDev running inside kubernetes cluster does not support server shell executor. "
								+ "Please use kubernetes executor instead");
					} else if (Bootstrap.isInDocker()) {
						throw new ExplicitException("Server shell executor is only supported when OneDev is installed "
								+ "directly on bare metal/virtual machine");
					}

					buildHome = new File(Bootstrap.getTempDir(),
							"onedev-build-" + jobContext.getProjectId() + "-" + jobContext.getBuildNumber());
					FileUtils.createDir(buildHome);
					File workspaceDir = new File(buildHome, "workspace");
					try {
						String localServer = getClusterManager().getLocalServerAddress();
						jobLogger.log(String.format("Executing job (executor: %s, server: %s)...", 
								getName(), localServer));

						jobLogger.log(String.format("Executing job with executor '%s'...", getName()));

						if (!jobContext.getServices().isEmpty()) {
							throw new ExplicitException("This job requires services, which can only be supported "
									+ "by docker aware executors");
						}
						FileUtils.createDir(workspaceDir);

						var cacheHelper = new ServerCacheHelper(buildHome, jobContext, jobLogger);
						
						jobLogger.log("Copying job dependencies...");
						getJobManager().copyDependencies(jobContext, workspaceDir);

						File userHome = new File(buildHome, "user");
						FileUtils.createDir(userHome);

						getJobManager().reportJobWorkspace(jobContext, workspaceDir.getAbsolutePath());
						CompositeFacade entryFacade = new CompositeFacade(jobContext.getActions());

						OsInfo osInfo = OneDev.getInstance(OsInfo.class);
						boolean successful = entryFacade.execute(new LeafHandler() {

							@Override
							public boolean execute(LeafFacade facade, List<Integer> position) {
								runningStep = facade;
								try {
									String stepNames = entryFacade.getNamesAsString(position);
									jobLogger.notice("Running step \"" + stepNames + "\"...");
									
									long time = System.currentTimeMillis();
									if (facade instanceof CommandFacade) {
										CommandFacade commandFacade = (CommandFacade) facade;
										OsExecution execution = commandFacade.getExecution(osInfo);
										if (execution.getImage() != null) {
											throw new ExplicitException("This step can only be executed by server docker executor, "
													+ "remote docker executor, or kubernetes executor");
										}

										commandFacade.generatePauseCommand(buildHome);

										var commandDir = new File(buildHome, "command");
										FileUtils.createDir(commandDir);
										File stepScriptFile = new File(commandDir, "step-" + stringifyStepPosition(position) + commandFacade.getScriptExtension());
										try {
											FileUtils.writeStringToFile(
													stepScriptFile,
													commandFacade.normalizeCommands(replacePlaceholders(execution.getCommands(), buildHome)),
													StandardCharsets.UTF_8);
										} catch (IOException e) {
											throw new RuntimeException(e);
										}

										Commandline interpreter = commandFacade.getScriptInterpreter();
										Map<String, String> environments = new HashMap<>();
										environments.put("GIT_HOME", userHome.getAbsolutePath());
										environments.put("ONEDEV_WORKSPACE", workspaceDir.getAbsolutePath());
										interpreter.workingDir(workspaceDir).environments(environments);
										interpreter.addArgs(stepScriptFile.getAbsolutePath());

										ExecutionResult result = interpreter.execute(newInfoLogger(jobLogger), newWarningLogger(jobLogger));
										if (result.getReturnCode() != 0) {
											long duration = System.currentTimeMillis() - time;
											jobLogger.error("Step \"" + stepNames + "\" is failed (" + DateUtils.formatDuration(duration) + "): Command exited with code " + result.getReturnCode());
											return false;
										}
									} else if (facade instanceof BuildImageFacade || facade instanceof RunContainerFacade
											|| facade instanceof RunImagetoolsFacade) {
										throw new ExplicitException("This step can only be executed by server docker executor or "
												+ "remote docker executor");
									} else if (facade instanceof CheckoutFacade) {
										try {
											CheckoutFacade checkoutFacade = (CheckoutFacade) facade;
											jobLogger.log("Checking out code...");

											Commandline git = new Commandline(AppLoader.getInstance(GitLocation.class).getExecutable());

											Map<String, String> environments = new HashMap<>();
											environments.put("HOME", userHome.getAbsolutePath());
											git.environments(environments);

											checkoutFacade.setupWorkingDir(git, workspaceDir);

											File trustCertsFile = new File(buildHome, "trust-certs.pem");
											installGitCert(git, Bootstrap.getTrustCertsDir(), trustCertsFile,
													trustCertsFile.getAbsolutePath(),
													newInfoLogger(jobLogger),
													newWarningLogger(jobLogger));

											CloneInfo cloneInfo = checkoutFacade.getCloneInfo();
											cloneInfo.writeAuthData(userHome, git, false,
													newInfoLogger(jobLogger),
													newWarningLogger(jobLogger));

											int cloneDepth = checkoutFacade.getCloneDepth();

											cloneRepository(git, jobContext.getProjectGitDir(), cloneInfo.getCloneUrl(), jobContext.getRefName(),
													jobContext.getCommitId().name(), checkoutFacade.isWithLfs(), checkoutFacade.isWithSubmodules(),
													cloneDepth, newInfoLogger(jobLogger), newWarningLogger(jobLogger));
										} catch (Exception e) {
											long duration = System.currentTimeMillis() - time;
											jobLogger.error("Step \"" + stepNames + "\" is failed (" + DateUtils.formatDuration(duration) + "): " + getErrorMessage(e));
											return false;
										}
									} else if (facade instanceof SetupCacheFacade) {
										SetupCacheFacade setupCacheFacade = (SetupCacheFacade) facade;
										for (var cachePath: setupCacheFacade.getPaths()) {
											if (new File(cachePath).isAbsolute())
												throw new ExplicitException("Shell executor does not allow absolute cache path: " + cachePath);
										}
										cacheHelper.setupCache(setupCacheFacade);
									} else if (facade instanceof ServerSideFacade) {
										ServerSideFacade serverSideFacade = (ServerSideFacade) facade;
										try {
											serverSideFacade.execute(buildHome, new ServerSideFacade.Runner() {

												@Override
												public Map<String, byte[]> run(File inputDir, Map<String, String> placeholderValues) {
													return getJobManager().runServerStep(jobContext, position, inputDir,
															placeholderValues, false, jobLogger);
												}

											});
										} catch (Exception e) {
											if (ExceptionUtils.find(e, InterruptedException.class) == null) {
												long duration = System.currentTimeMillis() - time;
												jobLogger.error("Step \"" + stepNames + "\" is failed: (" + DateUtils.formatDuration(duration) + ") " + getErrorMessage(e));
											}
											return false;
										}
									} else {
										throw new ExplicitException("Unexpected step type: " + facade.getClass());
									}
									long duration = System.currentTimeMillis() - time;
									jobLogger.success("Step \"" + stepNames + "\" is successful (" + DateUtils.formatDuration(duration) + ")");
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

						if (successful)
							cacheHelper.uploadCaches();
						else
							throw new FailedException();
					} finally {
						// Fix https://code.onedev.io/onedev/server/~issues/597
						if (SystemUtils.IS_OS_WINDOWS && workspaceDir.exists())
							FileUtils.deleteDir(workspaceDir);
						synchronized (buildHome) {
							FileUtils.deleteDir(buildHome);
						}
					}
				}

				@Override
				public void resume(JobContext jobContext) {
					if (buildHome != null) synchronized (buildHome) {
						if (buildHome.exists())
							FileUtils.touchFile(new File(buildHome, "continue"));
					}
				}

				@Override
				public Shell openShell(JobContext jobContext, Terminal terminal) {
					if (buildHome != null) {
						Commandline shell;
						if (runningStep instanceof CommandFacade) {
							CommandFacade commandStep = (CommandFacade) runningStep;
							shell = new Commandline(commandStep.getShell(SystemUtils.IS_OS_WINDOWS, null)[0]);
						} else if (SystemUtils.IS_OS_WINDOWS) {
							shell = new Commandline("cmd");
						} else {
							shell = new Commandline("sh");
						}
						shell.workingDir(new File(buildHome, "workspace"));
						return new CommandlineShell(terminal, shell);
					} else {
						throw new ExplicitException("Shell not ready");
					}
				}
				
			});			
		};
		jobLogger.log("Pending resource allocation...");
		getResourceAllocator().runServerJob(getName(), getConcurrencyNumber(), 1, runnable);
	}

	@Override
	public void test(TestData testData, TaskLogger jobLogger) {
		Commandline git = new Commandline(AppLoader.getInstance(GitLocation.class).getExecutable());
		testCommands(git, testData.getCommands(), jobLogger);
	}
	
	@Editable(name="Specify Shell/Batch Commands to Run")
	public static class TestData implements Serializable {

		private static final long serialVersionUID = 1L;

		private String commands;

		@Editable
		@OmitName
		@Code(language=Code.SHELL)
		@NotEmpty
		public String getCommands() {
			return commands;
		}

		public void setCommands(String commands) {
			this.commands = commands;
		}
		
	}

}