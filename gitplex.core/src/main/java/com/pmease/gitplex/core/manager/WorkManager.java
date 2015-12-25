package com.pmease.gitplex.core.manager;

import java.util.concurrent.Callable;
import java.util.concurrent.Future;

public interface WorkManager {
	void execute(Runnable command);
	
	<T> Future<T> submit(Callable<T> task);
	
	Future<?> submit(Runnable task);
}
