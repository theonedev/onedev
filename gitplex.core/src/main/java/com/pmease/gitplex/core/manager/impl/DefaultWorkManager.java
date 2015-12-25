package com.pmease.gitplex.core.manager.impl;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import javax.inject.Singleton;

import com.pmease.gitplex.core.manager.WorkManager;

@Singleton
public class DefaultWorkManager implements WorkManager {

	private final ExecutorService executor = 
			Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
	
	@Override
	public void execute(Runnable command) {
		executor.execute(command);
	}

	@Override
	public <T> Future<T> submit(Callable<T> task) {
		return executor.submit(task);
	}

	@Override
	public Future<?> submit(Runnable task) {
		return executor.submit(task);
	}

}
