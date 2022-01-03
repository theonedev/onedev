package io.onedev.server.plugin.executor.servershell;

import static io.onedev.agent.ShellExecutorUtils.resolveCachePath;
import static io.onedev.agent.ShellExecutorUtils.testCommands;
import static io.onedev.k8shelper.KubernetesHelper.checkCacheAllocations;
import static io.onedev.k8shelper.KubernetesHelper.cloneRepository;
import static io.onedev.k8shelper.KubernetesHelper.getCacheInstances;
import static io.onedev.k8shelper.KubernetesHelper.installGitCert;
import static io.onedev.k8shelper.KubernetesHelper.readPlaceholderValues;
import static io.onedev.k8shelper.KubernetesHelper.replacePlaceholders;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import javax.validation.constraints.Size;

import io.onedev.agent.ExecutorUtils;
import io.onedev.agent.job.FailedException;
import io.onedev.commons.bootstrap.Bootstrap;
import io.onedev.commons.loader.AppLoader;
import io.onedev.commons.utils.ExplicitException;
import io.onedev.commons.utils.FileUtils;
import io.onedev.commons.utils.PathUtils;
import io.onedev.commons.utils.TaskLogger;
import io.onedev.commons.utils.command.Commandline;
import io.onedev.commons.utils.command.ExecutionResult;
import io.onedev.k8shelper.CacheInstance;
import io.onedev.k8shelper.CheckoutExecutable;
import io.onedev.k8shelper.CloneInfo;
import io.onedev.k8shelper.CommandExecutable;
import io.onedev.k8shelper.CompositeExecutable;
import io.onedev.k8shelper.ContainerExecutable;
import io.onedev.k8shelper.LeafExecutable;
import io.onedev.k8shelper.LeafHandler;
import io.onedev.k8shelper.OsExecution;
import io.onedev.k8shelper.OsInfo;
import io.onedev.k8shelper.ServerExecutable;
import io.onedev.server.OneDev;
import io.onedev.server.buildspec.job.CacheSpec;
import io.onedev.server.buildspec.job.JobContext;
import io.onedev.server.buildspec.job.JobManager;
import io.onedev.server.git.config.GitConfig;
import io.onedev.server.job.resource.ResourceManager;
import io.onedev.server.model.support.administration.jobexecutor.JobExecutor;
import io.onedev.server.plugin.executor.servershell.ServerShellExecutor.TestData;
import io.onedev.server.util.patternset.PatternSet;
import io.onedev.server.util.validation.annotation.Code;
import io.onedev.server.web.editable.annotation.Editable;
import io.onedev.server.web.editable.annotation.Horizontal;
import io.onedev.server.web.editable.annotation.OmitName;
import io.onedev.server.web.util.Testable;

@Editable(order=ServerShellExecutor.ORDER, name="Server Shell Executor", description="This executor runs build jobs with OneDev server's shell facility")
@Horizontal
public class ServerShellExecutor extends JobExecutor implements Testable<TestData> {

	private static final long serialVersionUID = 1L;
	
	static final int ORDER = 400;
	
	private static final Object cacheHomeCreationLock = new Object();
	
	private File getCacheHome() {
		File file = new File(Bootstrap.getSiteDir(), "cache");
		if (!file.exists()) synchronized (cacheHomeCreationLock) {
			FileUtils.createDir(file);
		}
		return file;
	}
	
	@Override
	public void execute(String jobToken, JobContext jobContext) {
		TaskLogger jobLogger = jobContext.getLogger();
		OneDev.getInstance(ResourceManager.class).run(new Runnable() {

			@Override
			public void run() {
				File buildDir = FileUtils.createTempDir("onedev-build");
				try {
					jobLogger.log(String.format("Executing job with executor '%s'...", getName()));
					jobContext.notifyJobRunning(null);
					
					if (!jobContext.getServices().isEmpty()) {
						throw new ExplicitException("This job requires services, which can only be supported "
								+ "by docker aware executors");
					}
					
					JobManager jobManager = OneDev.getInstance(JobManager.class);		
					File cacheHomeDir = getCacheHome();
					
					for (CacheSpec cacheSpec: jobContext.getCacheSpecs()) {
						if (new File(cacheSpec.getPath()).isAbsolute()) {
							throw new ExplicitException("Shell executor does not support "
									+ "absolute cache path: " + cacheSpec.getPath());
						}
					}
					jobLogger.log("Allocating job caches...") ;
					Map<CacheInstance, Date> cacheInstances = getCacheInstances(cacheHomeDir);
					Map<CacheInstance, String> cacheAllocations = jobManager.allocateJobCaches(jobToken, new Date(), cacheInstances);
					checkCacheAllocations(cacheHomeDir, cacheAllocations, new Consumer<File>() {
	
						@Override
						public void accept(File directory) {
							FileUtils.cleanDir(directory);
						}
						
					});
					
					File workspaceDir = new File(buildDir, "workspace");
					FileUtils.createDir(workspaceDir);
					
					jobLogger.log("Copying job dependencies...");
					jobContext.copyDependencies(workspaceDir);
					
					File userDir = new File(buildDir, "user");
					FileUtils.createDir(userDir);
					
					jobContext.reportJobWorkspace(workspaceDir.getAbsolutePath());
					
					CompositeExecutable entryExecutable = new CompositeExecutable(jobContext.getActions());
					
					OsInfo osInfo = OneDev.getInstance(OsInfo.class);
					boolean successful = entryExecutable.execute(new LeafHandler() {

						@Override
						public boolean execute(LeafExecutable executable, List<Integer> position) {
							String stepNames = entryExecutable.getNamesAsString(position);
							jobLogger.notice("Running step \"" + stepNames + "\"...");
							
							if (executable instanceof CommandExecutable) {
								CommandExecutable commandExecutable = (CommandExecutable) executable;
								OsExecution execution = commandExecutable.getExecution(osInfo);
								if (execution.getImage() != null) {
									throw new ExplicitException("This step can only be executed by server docker executor, "
											+ "remote docker executor, or kubernetes executor");
								}
								
								File jobScriptFile = new File(buildDir, "job-commands" + commandExecutable.getScriptExtension());
								try {
									FileUtils.writeLines(
											jobScriptFile, 
											new ArrayList<>(replacePlaceholders(execution.getCommands(), buildDir)), 
											commandExecutable.getEndOfLine());
								} catch (IOException e) {
									throw new RuntimeException(e);
								}
								
								for (Map.Entry<CacheInstance, String> entry: cacheAllocations.entrySet()) {
									if (!PathUtils.isCurrent(entry.getValue())) {
										File sourceDir = entry.getKey().getDirectory(cacheHomeDir);
										File destDir = resolveCachePath(workspaceDir, entry.getValue());
										if (destDir.exists())
											FileUtils.deleteDir(destDir);
										else
											FileUtils.createDir(destDir.getParentFile());
										try {
											Files.createSymbolicLink(destDir.toPath(), sourceDir.toPath());
										} catch (IOException e) {
											throw new RuntimeException(e);
										}
									} else {
										throw new ExplicitException("Invalid cache path: " + entry.getValue());
									}
								}
								
								Commandline interpreter = commandExecutable.getInterpreter();
								Map<String, String> environments = new HashMap<>();
								environments.put("GIT_HOME", userDir.getAbsolutePath());
								interpreter.workingDir(workspaceDir).environments(environments);
								interpreter.addArgs(jobScriptFile.getAbsolutePath());
								
								ExecutionResult result = interpreter.execute(ExecutorUtils.newInfoLogger(jobLogger), ExecutorUtils.newErrorLogger(jobLogger));
								if (result.getReturnCode() != 0) {
									jobLogger.error("Step \"" + stepNames + "\" is failed: Command exited with code " + result.getReturnCode());
									return false;
								}
							} else if (executable instanceof ContainerExecutable) {
								throw new ExplicitException("This step can only be executed by server docker executor, "
										+ "remote docker executor, or kubernetes executor");
							} else if (executable instanceof CheckoutExecutable) {
								try {
									CheckoutExecutable checkoutExecutable = (CheckoutExecutable) executable;
									jobLogger.log("Checking out code...");
									Commandline git = new Commandline(AppLoader.getInstance(GitConfig.class).getExecutable());	
									git.workingDir(workspaceDir);
									Map<String, String> environments = new HashMap<>();
									environments.put("HOME", userDir.getAbsolutePath());
									git.environments(environments);

									CloneInfo cloneInfo = checkoutExecutable.getCloneInfo();
									
									cloneInfo.writeAuthData(userDir, git, ExecutorUtils.newInfoLogger(jobLogger), ExecutorUtils.newErrorLogger(jobLogger));
									
									List<String> trustCertContent = getTrustCertContent();
									if (!trustCertContent.isEmpty()) {
										installGitCert(new File(userDir, "trust-cert.pem"), trustCertContent, 
												git, ExecutorUtils.newInfoLogger(jobLogger), ExecutorUtils.newErrorLogger(jobLogger));
									}

									int cloneDepth = checkoutExecutable.getCloneDepth();
									
									cloneRepository(git, jobContext.getProjectGitDir().getAbsolutePath(), 
											cloneInfo.getCloneUrl(), jobContext.getCommitId().name(), 
											checkoutExecutable.isWithLfs(), checkoutExecutable.isWithSubmodules(),
											cloneDepth, ExecutorUtils.newInfoLogger(jobLogger), ExecutorUtils.newErrorLogger(jobLogger));
								} catch (Exception e) {
									jobLogger.error("Step \"" + stepNames + "\" is failed: " + getErrorMessage(e));
									return false;
								}
							} else {
								ServerExecutable serverExecutable = (ServerExecutable) executable;
								
								File filesDir = FileUtils.createTempDir();
								try {
									Collection<String> placeholders = serverExecutable.getPlaceholders();
									Map<String, String> placeholderValues = readPlaceholderValues(buildDir, placeholders);
									PatternSet filePatterns = new PatternSet(
											new HashSet<>(replacePlaceholders(serverExecutable.getIncludeFiles(), placeholderValues)), 
											new HashSet<>(replacePlaceholders(serverExecutable.getExcludeFiles(), placeholderValues)));

									int baseLen = workspaceDir.getAbsolutePath().length()+1;
									for (File file: filePatterns.listFiles(workspaceDir)) {
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
													new File(buildDir, entry.getKey()), 
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
					FileUtils.deleteDir(buildDir);
				}
			}
			
		}, jobContext.getResourceRequirements(), jobLogger);
	}

	@Override
	public void test(TestData testData, TaskLogger jobLogger) {
		OneDev.getInstance(ResourceManager.class).run(new Runnable() {

			@Override
			public void run() {
				Commandline git = new Commandline(AppLoader.getInstance(GitConfig.class).getExecutable());
				testCommands(git, testData.getCommands(), jobLogger);
			}
			
		}, new HashMap<>(), jobLogger);
		
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