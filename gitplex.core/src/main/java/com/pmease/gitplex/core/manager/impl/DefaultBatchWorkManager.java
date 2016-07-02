package com.pmease.gitplex.core.manager.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.pmease.commons.util.concurrent.Prioritized;
import com.pmease.commons.util.concurrent.PrioritizedRunnable;
import com.pmease.gitplex.core.listener.LifecycleListener;
import com.pmease.gitplex.core.manager.BatchWorkManager;
import com.pmease.gitplex.core.manager.WorkExecutor;
import com.pmease.gitplex.core.manager.support.BatchWorker;

@Singleton
public class DefaultBatchWorkManager implements BatchWorkManager, LifecycleListener, Runnable {

	private static final Logger logger = LoggerFactory.getLogger(DefaultBatchWorkManager.class);
	
	private final WorkExecutor workExecutor;
	
	private final Map<BatchWorker, Works> works = new HashMap<>();
	
	private Thread thread;
	
	@Inject
	public DefaultBatchWorkManager(WorkExecutor workExecutor) {
		this.workExecutor = workExecutor;
	}
	
	private Works getWorks(BatchWorker worker) {
		Works worksOfWorker = works.get(worker);
		if (worksOfWorker == null) {
			worksOfWorker = new Works();
			works.put(worker, worksOfWorker);
		}
		return worksOfWorker;
	}

	@Override
	public void systemStarting() {
	}

	@Override
	public void systemStarted() {
		thread = new Thread(this);
		thread.start();
	}

	@Override
	public synchronized void systemStopping() {
		thread = null;
		notify();
	}

	@Override
	public void systemStopped() {
	}

	@Override
	public synchronized void run() {
		while (thread != null) {
			for (Map.Entry<BatchWorker, Works> entry: works.entrySet()) {
				BatchWorker worker = entry.getKey();
				Works works = entry.getValue();
				if (works.working.isEmpty()) {
					works.queued.drainTo(works.working, worker.getMaxBatchSize());
					if (!works.working.isEmpty()) {
						double priority = works.working.stream().collect(Collectors.averagingInt(Prioritized::getPriority));
						workExecutor.execute(new PrioritizedRunnable((int)priority) {
							
							@Override
							public void run() {
								try {
									worker.doWork(works.working);
								} catch (Exception e) {
									logger.error("Error doing works", e);
								} finally {
									synchronized(DefaultBatchWorkManager.this) {
										works.working.clear();
										DefaultBatchWorkManager.this.notify();
									}
								}
							}
							
						});
					}
				}
			}
			try {
				wait();
			} catch (InterruptedException e) {
			}
		}
	}

	@Override
	public synchronized void submit(BatchWorker worker, Prioritized work) {
		getWorks(worker).queued.offer(work);
		notify();
	}

	@Override
	public synchronized void remove(BatchWorker worker) {
		works.remove(worker);
	}
	
	private static class Works {
		BlockingQueue<Prioritized> queued = new PriorityBlockingQueue<>();
		
		Collection<Prioritized> working = new ArrayList<>();
	}

}
