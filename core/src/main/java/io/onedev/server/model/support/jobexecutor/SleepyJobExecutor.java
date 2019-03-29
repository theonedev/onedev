package io.onedev.server.model.support.jobexecutor;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import io.onedev.commons.launcher.loader.ListenerRegistry;
import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.Build2Manager;
import io.onedev.server.event.build2.BuildFinished;
import io.onedev.server.model.Build2;
import io.onedev.server.persistence.TransactionManager;
import io.onedev.server.web.editable.annotation.Editable;

@Editable
public class SleepyJobExecutor implements JobExecutor {

	private static final long serialVersionUID = 1L;

	private static final int MAX_JOBS = 5;
	
	private static final Map<String, Thread> threads = new HashMap<>();
	
	@Override
	public String run(Build2 build) {
		synchronized (threads) {
			if (threads.size() < MAX_JOBS) {
				Long buildId = build.getId();
				String runInstanceId = UUID.randomUUID().toString();
				Thread thread = new Thread(new Runnable() {

					@Override
					public void run() {
						try {
							Thread.sleep(30000);
						} catch (InterruptedException e) {
							synchronized (threads) {
								threads.remove(runInstanceId);
							}
						} finally {
							OneDev.getInstance(TransactionManager.class).run(new Runnable() {

								@Override
								public void run() {
									Build2 build = OneDev.getInstance(Build2Manager.class).load(buildId);
									build.setStatus(Build2.Status.SUCCESSFUL);
									build.setFinishDate(new Date());
									OneDev.getInstance(ListenerRegistry.class).post(new BuildFinished(build));
								}
								
							});
						}
					}
					
				});
				threads.put(runInstanceId, thread);
				return runInstanceId;
			} else {
				return null;
			}
		}
	}

	@Override
	public boolean isRunning(Build2 build) {
		synchronized (threads) {
			return threads.containsKey(build.getRunInstanceId());
		}
	}

	@Override
	public void stop(Build2 build) {
		synchronized (threads) {
			Thread thread = threads.get(build.getRunInstanceId());
			if (thread != null)
				thread.interrupt();
		}
	}

}
