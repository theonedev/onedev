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
import io.onedev.server.job.JobContext;
import io.onedev.server.job.JobManager;
import io.onedev.server.job.JobRunnable;
import io.onedev.server.job.ResourceAllocator;
import io.onedev.server.model.support.administration.jobexecutor.JobExecutor;
import io.onedev.server.plugin.executor.servershell.ServerShellExecutor.TestData;
import io.onedev.server.terminal.CommandlineShell;
import io.onedev.server.terminal.Shell;
import io.onedev.server.terminal.Terminal;
import io.onedev.server.util.DateUtils;
import io.onedev.server.web.util.Testable;
import org.apache.commons.lang.SystemUtils;

import javax.validation.constraints.Size;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
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
	
	private static final Object cacheHomeCreationLock = new Object();
	
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
						String serverAddress = getClusterManager().getLocalServerAddress();
						jobLogger.log(String.format("Executing job (executor: %s, server: %s)...", 
								getName(), serverAddress));

						jobLogger.log(String.format("Executing job with executor '%s'...", getName()));

						if (!jobContext.getServices().isEmpty()) {
							throw new ExplicitException("This job requires services, which can only be supported "
									+ "by docker aware executors");
						}

						File cacheHomeDir = getCacheHome(jobContext.getJobExecutor());

						jobLogger.log("Setting up job cache...") ;
						JobCache cache = new JobCache(cacheHomeDir) {

							@Override
							protected Map<CacheInstance, String> allocate(CacheAllocationRequest request) {
								return getJobManager().allocateCaches(jobContext, request);
							}

							@Override
							protected void delete(File cacheDir) {
								FileUtils.cleanDir(cacheDir);
							}

						};
						cache.init(true);
						FileUtils.createDir(workspaceDir);

						cache.installSymbolinks(workspaceDir);

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

										File jobScriptFile = new File(buildHome, "job-commands" + commandFacade.getScriptExtension());
										try {
											FileUtils.writeLines(
													jobScriptFile,
													new ArrayList<>(replacePlaceholders(execution.getCommands(), buildHome)),
													commandFacade.getEndOfLine());
										} catch (IOException e) {
											throw new RuntimeException(e);
										}

										Commandline interpreter = commandFacade.getScriptInterpreter();
										Map<String, String> environments = new HashMap<>();
										environments.put("GIT_HOME", userHome.getAbsolutePath());
										environments.put("ONEDEV_WORKSPACE", workspaceDir.getAbsolutePath());
										interpreter.workingDir(workspaceDir).environments(environments);
										interpreter.addArgs(jobScriptFile.getAbsolutePath());

										ExecutionResult result = interpreter.execute(newInfoLogger(jobLogger), newWarningLogger(jobLogger));
										if (result.getReturnCode() != 0) {
											long duration = System.currentTimeMillis() - time;
											jobLogger.error("Step \"" + stepNames + "\" is failed (" + DateUtils.formatDuration(duration) + "): Command exited with code " + result.getReturnCode());
											return false;
										}
									} else if (facade instanceof RunContainerFacade) {
										throw new ExplicitException("This step can only be executed by server docker executor, "
												+ "remote docker executor, or kubernetes executor");
									} else if (facade instanceof BuildImageFacade) {
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
									} else {
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

						if (!successful)
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

		private List<String> commands = new ArrayList<>();

		@Editable
		@OmitName
		@Code(language=Code.SHELL)
		@Size(min=1, message="May not be empty")
		public List<String> getCommands() {
			return commands;
		}

		public void setCommands(List<String> commands) {
			this.commands = commands;
		}
		
	}

}