package io.onedev.server.plugin.executor.servershell;

import static io.onedev.agent.AgentUtils.newInfoLogger;
import static io.onedev.agent.AgentUtils.newWarningLogger;
import static io.onedev.k8shelper.KubernetesHelper.cloneRepository;
import static io.onedev.k8shelper.KubernetesHelper.initRepository;
import static io.onedev.k8shelper.KubernetesHelper.replacePlaceholders;
import static io.onedev.k8shelper.KubernetesHelper.setupGitCerts;
import static io.onedev.k8shelper.KubernetesHelper.stringifyStepPosition;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.validation.constraints.NotEmpty;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.SystemUtils;

import io.onedev.agent.AgentUtils;
import io.onedev.commons.bootstrap.Bootstrap;
import io.onedev.commons.bootstrap.SecretMasker;
import io.onedev.commons.utils.ExplicitException;
import io.onedev.commons.utils.FileUtils;
import io.onedev.commons.utils.TaskLogger;
import io.onedev.commons.utils.command.Commandline;
import io.onedev.k8shelper.BuildImageFacade;
import io.onedev.k8shelper.CheckoutFacade;
import io.onedev.k8shelper.CommandFacade;
import io.onedev.k8shelper.CompositeFacade;
import io.onedev.k8shelper.KubernetesHelper;
import io.onedev.k8shelper.LeafFacade;
import io.onedev.k8shelper.LeafHandler;
import io.onedev.k8shelper.PruneBuilderCacheFacade;
import io.onedev.k8shelper.RunContainerFacade;
import io.onedev.k8shelper.RunImagetoolsFacade;
import io.onedev.k8shelper.ServerSideFacade;
import io.onedev.k8shelper.ServerStepResult;
import io.onedev.k8shelper.SetupCacheFacade;
import io.onedev.server.OneDev;
import io.onedev.server.annotation.Code;
import io.onedev.server.annotation.Editable;
import io.onedev.server.annotation.Numeric;
import io.onedev.server.annotation.OmitName;
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
import io.onedev.server.model.support.administration.jobexecutor.JobExecutor;
import io.onedev.server.plugin.executor.servershell.ServerShellExecutor.TestData;
import io.onedev.server.service.ResourceService;
import io.onedev.server.terminal.CommandlineShell;
import io.onedev.server.terminal.Shell;
import io.onedev.server.web.util.Testable;

@Editable(order=ServerShellExecutor.ORDER, name="Server Shell Executor", description="" +
		"This executor runs build jobs with OneDev server's shell facility")
public class ServerShellExecutor extends JobExecutor implements Testable<TestData> {

	private static final long serialVersionUID = 1L;
	
	static final int ORDER = 400;
	
	private String concurrency;

	private transient volatile LeafFacade runningStep;
	
	private transient volatile File buildDir;

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

	@Editable(order=10000, name="Applicable Jobs", description="""
			Specify applicable jobs of this executor. 
			<b class='text-danger'>WARNING</b>: Jobs running with this executor has same privilege as OneDev process. 
			Please make sure that only trusted jobs can use this executor""")
	@io.onedev.server.annotation.JobMatch(withProjectCriteria = true, withJobCriteria = true)
	@NotEmpty
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
			return false;
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
	
	@Override
	public boolean execute(JobContext jobContext, TaskLogger jobLogger) {
		ClusterTask<Boolean> runnable = () -> getJobService().runJob(jobContext, new JobRunnable() {

			@Override
			public boolean run(TaskLogger jobLogger) {
				notifyJobRunning(jobContext.getBuildId(), null);
				
				buildDir = new File(Bootstrap.getTempDir(),
						"onedev-build-" + jobContext.getProjectId() + "-" + jobContext.getBuildNumber() + "-" + jobContext.getSubmitSequence());
				FileUtils.createDir(buildDir);
				File workDir = new File(buildDir, "work");
				SecretMasker.push(jobContext.getSecretMasker());
				try {
					String localServer = getClusterService().getLocalServerAddress();
					jobLogger.log(String.format("Executing job (executor: %s, server: %s)...", 
							getName(), localServer));

					jobLogger.log(String.format("Executing job with executor '%s'...", getName()));

					if (!jobContext.getServices().isEmpty()) {
						throw new ExplicitException("This job requires services, which can only be supported "
								+ "by docker aware executors");
					}
					FileUtils.createDir(workDir);

					var cacheProvisioner = new ServerJobCacheProvisioner(buildDir, jobContext, jobLogger);
				
					jobLogger.log("Copying job dependencies...");
					getJobService().copyDependencies(jobContext, workDir);

					getJobService().reportJobWorkDir(jobContext, workDir.getAbsolutePath());
					CompositeFacade entryFacade = new CompositeFacade(jobContext.getActions());

					var successful = entryFacade.execute(new LeafHandler() {

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
								CommandFacade commandFacade = (CommandFacade) facade;
								if (commandFacade.getImage() != null) {
									throw new ExplicitException("This step can only be executed by server docker executor, "
											+ "remote docker executor, or kubernetes executor");
								}

								commandFacade.generatePauseCommand(buildDir);

								var commandDir = new File(buildDir, "command");
								FileUtils.createDir(commandDir);
								File stepScriptFile = new File(commandDir, "step-" + stringifyStepPosition(position) + commandFacade.getScriptExtension());
								try {
									FileUtils.writeStringToFile(
											stepScriptFile,
											commandFacade.normalizeCommands(replacePlaceholders(commandFacade.getCommands(), buildDir)),
											StandardCharsets.UTF_8);
								} catch (IOException e) {
									throw new RuntimeException(e);
								}

								Commandline cmdline = new Commandline(commandFacade.getExecutable());
								cmdline.addArgs(commandFacade.getScriptOptions());

								Map<String, String> envs = new HashMap<>();
								envs.put("ONEDEV_WORKDIR", workDir.getAbsolutePath());
								envs.putAll(commandFacade.getEnvMap());
								cmdline.workingDir(workDir).envs(envs);
								cmdline.addArgs(stepScriptFile.getAbsolutePath());

								var result = cmdline.execute(newInfoLogger(jobLogger), newWarningLogger(jobLogger));
								if (result.getReturnCode() != 0) {
									jobLogger.error("Command exited with code " + result.getReturnCode());
									return false;
								}
							} else if (facade instanceof BuildImageFacade || facade instanceof RunContainerFacade
									|| facade instanceof RunImagetoolsFacade || facade instanceof PruneBuilderCacheFacade) {
								throw new ExplicitException("This step can only be executed by server docker executor or "
										+ "remote docker executor");
							} else if (facade instanceof CheckoutFacade) {
								CheckoutFacade checkoutFacade = (CheckoutFacade) facade;
								jobLogger.log("Checking out code...");

								var infoLogger = newInfoLogger(jobLogger);
								var warningLogger = newWarningLogger(jobLogger);

								var git = CommandUtils.newGit();
								checkoutFacade.setupWorkingDir(git, workDir);							
								initRepository(git, infoLogger, warningLogger);

								git.clearArgs();
								var trustCertsFile = new File(buildDir, "trust-certs.pem");
								setupGitCerts(git, Bootstrap.getTrustCertsDir(), trustCertsFile,
									trustCertsFile.getAbsolutePath(), infoLogger, warningLogger);

								var cloneInfo = checkoutFacade.getCloneInfo();
								cloneInfo.setupGitAuth(git, buildDir, buildDir.getAbsolutePath(), infoLogger, warningLogger);
					
								cloneRepository(git, jobContext.getProjectGitDir(), cloneInfo.getCloneUrl(),
										jobContext.getRefName(), jobContext.getCommitId().name(),
										checkoutFacade.isWithLfs(), checkoutFacade.isWithSubmodules(),
										checkoutFacade.getCloneDepth(),
										infoLogger, warningLogger);
							} else if (facade instanceof SetupCacheFacade) {
								SetupCacheFacade setupCacheFacade = (SetupCacheFacade) facade;
								for (var path: setupCacheFacade.getCacheConfig().getPaths()) {
									if (FilenameUtils.getPrefixLength(path) > 0)
										throw new ExplicitException("Shell executor does not allow absolute cache path: " + path);
								}
								cacheProvisioner.setupCache(setupCacheFacade.getCacheConfig());
							} else if (facade instanceof ServerSideFacade) {
								ServerSideFacade serverSideFacade = (ServerSideFacade) facade;
								return serverSideFacade.execute(buildDir, new ServerSideFacade.Runner() {

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
					SecretMasker.pop();
					// Fix https://code.onedev.io/onedev/server/~issues/597
					if (SystemUtils.IS_OS_WINDOWS && workDir.exists())
						FileUtils.deleteDir(workDir);
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
			public Shell openShell(JobContext jobContext, JobTerminal terminal) {
				if (buildDir != null) {
					Commandline cmdline;
					if (runningStep instanceof CommandFacade) {
						CommandFacade commandStep = (CommandFacade) runningStep;
						cmdline = new Commandline(commandStep.getExecutable());
					} else if (SystemUtils.IS_OS_WINDOWS) {
						cmdline = new Commandline("cmd");
					} else {
						cmdline = new Commandline("sh");
					}
					cmdline.workingDir(new File(buildDir, "work"));
					return new CommandlineShell(terminal, cmdline);
				} else {
					throw new ExplicitException("Shell not ready");
				}
			}
			
		});
		jobLogger.log("Pending resource allocation...");
		return getResourceService().runServerJob(getName(), getConcurrencyNumber(), 1, runnable);
	}
	
	@Override
	public void test(TestData testData, TaskLogger jobLogger) {
		Commandline git = CommandUtils.newGit();
		AgentUtils.testCommands(testData.getCommands(), jobLogger);
		KubernetesHelper.testGitLfsAvailability(git, jobLogger);
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