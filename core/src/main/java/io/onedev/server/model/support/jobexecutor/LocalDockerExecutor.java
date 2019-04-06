package io.onedev.server.model.support.jobexecutor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import io.onedev.server.ci.job.JobCallback;
import io.onedev.server.ci.job.JobResult;
import io.onedev.server.web.editable.annotation.Editable;

@Editable(order=100)
public class LocalDockerExecutor extends JobExecutor {

	private static final long serialVersionUID = 1L;

	private static final int MAX_JOBS = 5;
	
	private static final Map<String, Thread> threads = new HashMap<>();
	
	@Override
	public String run(String environment, List<String> commands, JobCallback callback) {
		synchronized (threads) {
			if (threads.size() < MAX_JOBS) {
				String jobInstance = UUID.randomUUID().toString();
				Thread thread = new Thread(new Runnable() {

					@Override
					public void run() {
						JobResult result = JobResult.SUCCESSFUL;
						try {
							callback.getJobLogger().info("hello world");
							callback.getJobLogger().error("may i help u");
							callback.getJobLogger().debug("just do it");
							callback.getJobLogger().trace("I am a grace");
							callback.getJobLogger().warn("wow\nyou are clever");
							callback.getJobLogger().warn(""
									+ "wow2\n"
									+ "  you are clever\n"
									+ "  don't you?");
						} finally {
							synchronized (threads) {
								threads.remove(jobInstance);
							}
							callback.jobFinished(result, null);
						}
					}
					
				});
				threads.put(jobInstance, thread);
				thread.start();
				return jobInstance;
			} else {
				return null;
			}
		}
	}

	@Override
	public boolean isRunning(String jobInstance) {
		synchronized (threads) {
			return threads.containsKey(jobInstance);
		}
	}

	@Override
	public void stop(String jobInstance) {
		synchronized (threads) {
			Thread thread = threads.get(jobInstance);
			if (thread != null)
				thread.interrupt();
		}
	}
	
}