package com.pmease.gitplex.core.manager.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;

import javax.inject.Singleton;

import com.pmease.commons.util.concurrent.PrioritizedCallable;
import com.pmease.commons.util.concurrent.PrioritizedExecutor;
import com.pmease.commons.util.concurrent.PrioritizedRunnable;
import com.pmease.gitplex.core.listeners.LifecycleListener;
import com.pmease.gitplex.core.manager.SequentialWorkManager;

@Singleton
public class DefaultSequentialWorkManager implements SequentialWorkManager, LifecycleListener {

	private final Map<String, ThreadPoolExecutor> executors = new HashMap<>();
	
	@Override
	public <T> Future<T> submit(String key, PrioritizedCallable<T> task) {
		return getExecutor(key).submit(task);
	}

	@Override
	public Future<?> submit(String key, PrioritizedRunnable task) {
		return getExecutor(key).submit(task);
	}

	@Override
	public <T> Future<T> submit(String key, PrioritizedRunnable task, T result) {
		return getExecutor(key).submit(task, result);
	}

	@Override
	public void execute(String key, PrioritizedRunnable command) {
		getExecutor(key).execute(command);
	}

	@Override
	public boolean remove(String key, Runnable task) {
		return getExecutor(key).remove(task);
	}
	
	private synchronized ThreadPoolExecutor getExecutor(String key) {
		ThreadPoolExecutor executor = executors.get(key);
		if (executor == null) {
			executor = new PrioritizedExecutor(1);
			executors.put(key, executor);
		}
		return executor;
	}

	@Override
	public void systemStarting() {
	}

	@Override
	public void systemStarted() {
	}

	@Override
	public synchronized void systemStopping() {
		for (ExecutorService executor: executors.values())
			executor.shutdown();
	}

	@Override
	public void systemStopped() {
	}

	@Override
	public synchronized void removeExecutor(String key) {
		ExecutorService executor = executors.get(key);
		if (executor != null) {
			executor.shutdown();
			executors.remove(key);
		}
	}

}
