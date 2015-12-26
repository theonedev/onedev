package com.pmease.gitplex.core.manager;

import java.util.concurrent.Callable;
import java.util.concurrent.Future;

public interface SequentialWorkManager {
	
	<T> Future<T> submit(String key, Callable<T> task);

	Future<?> submit(String key, Runnable task);
	
	void removeExecutor(String key);
	
	void execute(String key, Runnable command);
}
