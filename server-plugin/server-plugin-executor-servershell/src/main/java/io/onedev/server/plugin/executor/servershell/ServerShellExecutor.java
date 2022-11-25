package io.onedev.server.plugin.executor.servershell;

import static io.onedev.agent.ShellExecutorUtils.testCommands;
import static io.onedev.k8shelper.KubernetesHelper.cloneRepository;
import static io.onedev.k8shelper.KubernetesHelper.installGitCert;
import static io.onedev.k8shelper.KubernetesHelper.replacePlaceholders;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.validation.constraints.Size;

import org.apache.commons.lang.SystemUtils;

import com.hazelcast.cluster.Member;

import io.onedev.agent.ExecutorUtils;
import io.onedev.agent.job.FailedException;
import io.onedev.commons.bootstrap.Bootstrap;
import io.onedev.commons.loader.AppLoader;
import io.onedev.commons.utils.ExplicitException;
import io.onedev.commons.utils.FileUtils;
import io.onedev.commons.utils.TaskLogger;
import io.onedev.commons.utils.command.Commandline;
import io.onedev.commons.utils.command.ExecutionResult;
import io.onedev.k8shelper.BuildImageFacade;
import io.onedev.k8shelper.CacheAllocationRequest;
import io.onedev.k8shelper.CacheInstance;
import io.onedev.k8shelper.CheckoutFacade;
import io.onedev.k8shelper.CloneInfo;
import io.onedev.k8shelper.CommandFacade;
import io.onedev.k8shelper.CompositeFacade;
import io.onedev.k8shelper.JobCache;
import io.onedev.k8shelper.LeafFacade;
import io.onedev.k8shelper.LeafHandler;
import io.onedev.k8shelper.OsExecution;
import io.onedev.k8shelper.OsInfo;
import io.onedev.k8shelper.RunContainerFacade;
import io.onedev.k8shelper.ServerSideFacade;
import io.onedev.server.OneDev;
import io.onedev.server.cluster.ClusterManager;
import io.onedev.server.git.config.GitConfig;
import io.onedev.server.job.AgentInfo;
import io.onedev.server.job.JobContext;
import io.onedev.server.job.JobManager;
import io.onedev.server.model.support.administration.jobexecutor.JobExecutor;
import io.onedev.server.plugin.executor.servershell.ServerShellExecutor.TestData;
import io.onedev.server.search.entity.agent.AgentQuery;
import io.onedev.server.terminal.CommandlineShell;
import io.onedev.server.terminal.Shell;
import io.onedev.server.terminal.Terminal;
import io.onedev.server.util.validation.annotation.Code;
import io.onedev.server.web.editable.annotation.Editable;
import io.onedev.server.web.editable.annotation.Horizontal;
import io.onedev.server.web.editable.annotation.OmitName;
import io.onedev.server.web.util.Testable;

@Editable(order=ServerShellExecutor.ORDER, name="Server Shell Executor", description=""
		+ "This executor runs build jobs with OneDev server's shell facility.<br>"
		+ "<b class='text-danger'>WARNING</b>: Jobs running with this executor has same permission "
		+ "as OneDev server process. Make sure it can only be used by trusted jobs via job "
		+ "authorization setting")
@Horizontal
public class ServerShellExecutor extends JobExecutor implements Testable<TestData> {

	private static final long serialVersionUID = 1L;
	
	static final int ORDER = 400;
	
	private static final Object cacheHomeCreationLock = new Object();
	
	private transient volatile LeafFacade runningStep;
	
	private transient volatile File buildDir;
	
	private File getCacheHome(JobExecutor jobExecutor) {
		File file = new File(Bootstrap.getSiteDir(), "cache/" + jobExecutor.getName());
		if (!file.exists()) synchronized (cacheHomeCreationLock) {
			FileUtils.createDir(file);
		}
		return file;
	}
	
	@Override
	public AgentQuery getAgentRequirement() {
		return null;
	}

	private ClusterManager getClusterManager() {
		return OneDev.getInstance(ClusterManager.class);
	}
	
	private JobManager getJobManager() {
		return OneDev.getInstance(JobManager.class);
	}
	
	@Override
	public void execute(JobContext jobContext, TaskLogger jobLogger, AgentInfo agentInfo) {
		if (OneDev.getK8sService() != null) {
			throw new ExplicitException(""
					+ "OneDev running inside kubernetes cluster does not support server shell executor. "
					+ "Please use kubernetes executor instead");
		} else if (Bootstrap.isInDocker()) {
			throw new ExplicitException("Server shell executor is only supported when OneDev is installed "
					+ "directly on bare metal/virtual machine");
		}
		
		buildDir = FileUtils.createTempDir("onedev-build");
		File workspaceDir = new File(buildDir, "workspace");
		try {
			Member server = getClusterManager().getHazelcastInstance().getCluster().getLocalMember();
			jobLogger.log(String.format("Executing job (executor: %s, server: %s)...", getName(), 
					server.getAddress().getHost() + ":" + server.getAddress().getPort()));
			
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
			
			File userDir = new File(buildDir, "user");
			FileUtils.createDir(userDir);
			
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
						
						if (facade instanceof CommandFacade) {
							CommandFacade commandFacade = (CommandFacade) facade;
							OsExecution execution = commandFacade.getExecution(osInfo);
							if (execution.getImage() != null) {
								throw new ExplicitException("This step can only be executed by server docker executor, "
										+ "remote docker executor, or kubernetes executor");
							}
							
							commandFacade.generatePauseCommand(buildDir);
							
							File jobScriptFile = new File(buildDir, "job-commands" + commandFacade.getScriptExtension());
							try {
								FileUtils.writeLines(
										jobScriptFile, 
										new ArrayList<>(replacePlaceholders(execution.getCommands(), buildDir)), 
										commandFacade.getEndOfLine());
							} catch (IOException e) {
								throw new RuntimeException(e);
							}
							
							Commandline interpreter = commandFacade.getScriptInterpreter();
							Map<String, String> environments = new HashMap<>();
							environments.put("GIT_HOME", userDir.getAbsolutePath());
							environments.put("ONEDEV_WORKSPACE", workspaceDir.getAbsolutePath());
							interpreter.workingDir(workspaceDir).environments(environments);
							interpreter.addArgs(jobScriptFile.getAbsolutePath());
							
							ExecutionResult result = interpreter.execute(ExecutorUtils.newInfoLogger(jobLogger), ExecutorUtils.newWarningLogger(jobLogger));
							if (result.getReturnCode() != 0) {
								jobLogger.error("Step \"" + stepNames + "\" is failed: Command exited with code " + result.getReturnCode());
								return false;
							}
						} else if (facade instanceof RunContainerFacade || facade instanceof BuildImageFacade) {
							throw new ExplicitException("This step can only be executed by server docker executor, "
									+ "remote docker executor, or kubernetes executor");
						} else if (facade instanceof CheckoutFacade) {
							try {
								CheckoutFacade checkoutFacade = (CheckoutFacade) facade;
								jobLogger.log("Checking out code...");
								Commandline git = new Commandline(AppLoader.getInstance(GitConfig.class).getExecutable());	
								
								checkoutFacade.setupWorkingDir(git, workspaceDir);
								
								Map<String, String> environments = new HashMap<>();
								environments.put("HOME", userDir.getAbsolutePath());
								git.environments(environments);

								CloneInfo cloneInfo = checkoutFacade.getCloneInfo();
								
								cloneInfo.writeAuthData(userDir, git, ExecutorUtils.newInfoLogger(jobLogger), ExecutorUtils.newWarningLogger(jobLogger));
								
								List<String> trustCertContent = getTrustCertContent();
								if (!trustCertContent.isEmpty()) {
									installGitCert(new File(userDir, "trust-cert.pem"), trustCertContent, 
											git, ExecutorUtils.newInfoLogger(jobLogger), ExecutorUtils.newWarningLogger(jobLogger));
								}

								int cloneDepth = checkoutFacade.getCloneDepth();
								
								cloneRepository(git, jobContext.getProjectGitDir(), cloneInfo.getCloneUrl(), jobContext.getRefName(), 
										jobContext.getCommitId().name(), checkoutFacade.isWithLfs(), checkoutFacade.isWithSubmodules(),
										cloneDepth, ExecutorUtils.newInfoLogger(jobLogger), ExecutorUtils.newWarningLogger(jobLogger));
							} catch (Exception e) {
								jobLogger.error("Step \"" + stepNames + "\" is failed: " + getErrorMessage(e));
								return false;
							}
						} else {
							ServerSideFacade serverSideFacade = (ServerSideFacade) facade;
							try {
								serverSideFacade.execute(buildDir, new ServerSideFacade.Runner() {
									
									@Override
									public Map<String, byte[]> run(File inputDir, Map<String, String> placeholderValues) {
										return getJobManager().runServerStep(jobContext, position, inputDir, 
												placeholderValues, jobLogger);
									}
									
								});
							} catch (Exception e) {
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
			// Fix https://code.onedev.io/onedev/server/~issues/597
			if (SystemUtils.IS_OS_WINDOWS && workspaceDir.exists())
				FileUtils.deleteDir(workspaceDir);
			synchronized (buildDir) {
				FileUtils.deleteDir(buildDir);
			}
		}				
	}

	@Override
	public void resume(JobContext jobContext) {
		if (buildDir != null) synchronized (buildDir) {
			if (buildDir.exists())
				FileUtils.touchFile(new File(buildDir, "continue"));
		}
	}

	@Override
	public void test(TestData testData, TaskLogger jobLogger) {
		Commandline git = new Commandline(AppLoader.getInstance(GitConfig.class).getExecutable());
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

	@Override
	public Shell openShell(JobContext jobContext, Terminal terminal) {
		if (buildDir != null) {
			Commandline shell;
			if (runningStep instanceof CommandFacade) {
				CommandFacade commandStep = (CommandFacade) runningStep;
				shell = new Commandline(commandStep.getShell(SystemUtils.IS_OS_WINDOWS, null)[0]);
			} else if (SystemUtils.IS_OS_WINDOWS) {
				shell = new Commandline("cmd");
			} else {
				shell = new Commandline("sh");
			}
			shell.workingDir(new File(buildDir, "workspace"));
			return new CommandlineShell(terminal, shell);
		} else {
			throw new ExplicitException("Shell not ready");
		}
	}

}