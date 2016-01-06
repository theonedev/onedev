package com.pmease.gitplex.core.manager;

import java.util.concurrent.Future;

import com.pmease.commons.util.concurrent.PrioritizedCallable;
import com.pmease.commons.util.concurrent.PrioritizedRunnable;

public interface SequentialWorkManager {
	
	<T> Future<T> submit(String key, PrioritizedCallable<T> task);

	Future<?> submit(String key, PrioritizedRunnable task);
	
	<T> Future<T> submit(String key, PrioritizedRunnable task, T result);

	void execute(String key, PrioritizedRunnable command);
	
	boolean remove(String key, Runnable task);
	
	void removeExecutor(String key);
}
