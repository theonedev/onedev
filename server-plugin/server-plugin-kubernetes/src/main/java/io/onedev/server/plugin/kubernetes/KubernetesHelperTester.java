package io.onedev.server.plugin.kubernetes;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import org.apache.commons.lang.SystemUtils;

import io.onedev.commons.utils.FileUtils;
import io.onedev.commons.utils.command.Commandline;
import io.onedev.commons.utils.command.LineConsumer;
import io.onedev.k8shelper.KubernetesHelper;
import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.SettingManager;
import io.onedev.server.model.support.JobContext;
import io.onedev.server.model.support.JobExecutor;
import io.onedev.server.web.editable.annotation.Editable;

@Editable(order=400)
public class KubernetesHelperTester extends JobExecutor {

	private static final long serialVersionUID = 1L;

	@Override
	public void execute(String jobId, JobContext context) {
		context.notifyJobRunning();
		
		SettingManager settingManager = OneDev.getInstance(SettingManager.class);
		String serverUrl = settingManager.getSystemSetting().getServerUrl();
		
		File workspace = FileUtils.createTempDir("k8s-workspace");
		try {
			KubernetesHelper.init(serverUrl, jobId, workspace);
			
			Future<?> sidecar = OneDev.getInstance(ExecutorService.class).submit(new Runnable() {

				@Override
				public void run() {
					KubernetesHelper.sidecar(serverUrl, jobId, workspace);
				}
				
			});

			try {
				Commandline cmd;
				if (SystemUtils.IS_OS_WINDOWS) {
					File scriptFile = new File(workspace, "onedev-job-commands.bat");
					try {
						FileUtils.writeLines(scriptFile, context.getCommands(), "\r\n");
					} catch (IOException e) {
						throw new RuntimeException(e);
					}
					cmd = new Commandline("cmd");
					cmd.addArgs("/c", scriptFile.getAbsolutePath());
				} else {
					File scriptFile = new File(workspace, "onedev-job-commands.sh");
					try {
						FileUtils.writeLines(scriptFile, context.getCommands(), "\n");
					} catch (IOException e) {
						throw new RuntimeException(e);
					}
					cmd = new Commandline("sh");
					cmd.addArgs(scriptFile.getAbsolutePath());
				}
				cmd.workingDir(workspace);
				cmd.environments(context.getEnvVars());
				cmd.execute(new LineConsumer() {
	
					@Override
					public void consume(String line) {
						context.getLogger().info(line);
					}
					
				}, new LineConsumer() {
	
					@Override
					public void consume(String line) {
						context.getLogger().error(line);
					}
					
				}).checkReturnCode();
			} finally {
				try {
					new File(workspace, KubernetesHelper.JOB_FINISH_FILE).createNewFile();
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			}

			try {
				sidecar.get();
			} catch (InterruptedException e) {
				sidecar.cancel(true);
			} catch (ExecutionException e) {
				throw new RuntimeException(e);
			}
		} finally {
			FileUtils.deleteDir(workspace);
		}
	}
	
	@Override
	public void checkCaches() {
	}

	@Override
	public void cleanDir(File dir) {
		FileUtils.cleanDir(dir);
	}

}