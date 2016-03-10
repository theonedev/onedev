package com.pmease.gitplex.core.manager.impl;

import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;

import javax.inject.Singleton;

import com.pmease.commons.util.concurrent.PrioritizedCallable;
import com.pmease.commons.util.concurrent.PrioritizedExecutor;
import com.pmease.commons.util.concurrent.PrioritizedRunnable;
import com.pmease.gitplex.core.listener.LifecycleListener;
import com.pmease.gitplex.core.manager.WorkManager;

@Singleton
public class DefaultWorkManager implements WorkManager, LifecycleListener {

	private final ThreadPoolExecutor executor = new PrioritizedExecutor(Runtime.getRuntime().availableProcessors());

	@Override
	public <T> Future<T> submit(PrioritizedCallable<T> task) {
		return executor.submit(task);
	}

	@Override
	public Future<?> submit(PrioritizedRunnable task) {
		return executor.submit(task);
	}

	@Override
	public <T> Future<T> submit(PrioritizedRunnable task, T result) {
		return executor.submit(task, result);
	}

	@Override
	public void execute(PrioritizedRunnable command) {
		executor.execute(command);
	}

	@Override
	public boolean remove(Runnable task) {
		return executor.remove(task);
	}

	@Override
	public void systemStarting() {
	}

	@Override
	public void systemStarted() {
	}

	@Override
	public void systemStopping() {
		executor.shutdown();
	}

	@Override
	public void systemStopped() {
	}

}
