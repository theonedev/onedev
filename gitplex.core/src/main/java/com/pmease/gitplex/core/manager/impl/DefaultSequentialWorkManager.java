package com.pmease.gitplex.core.manager.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import javax.inject.Singleton;

import com.pmease.gitplex.core.manager.SequentialWorkManager;

@Singleton
public class DefaultSequentialWorkManager implements SequentialWorkManager {

	private final Map<String, ExecutorService> sequentialExecutors = new HashMap<>();
	
	@Override
	public <T> Future<T> submit(String key, Callable<T> task) {
		return getRepositoryExecutor(key).submit(task);
	}

	@Override
	public Future<?> submit(String key, Runnable task) {
		return getRepositoryExecutor(key).submit(task);
	}

	@Override
	public void execute(String key, Runnable command) {
		getRepositoryExecutor(key).execute(command);
	}
	
	private synchronized ExecutorService getRepositoryExecutor(String key) {
		ExecutorService executor = sequentialExecutors.get(key);
		if (executor == null) {
			executor = Executors.newSingleThreadExecutor();
			sequentialExecutors.put(key, executor);
		}
		return executor;
	}

}
