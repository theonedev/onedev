package com.pmease.gitplex.core.manager;

import java.util.concurrent.Future;

import com.pmease.commons.util.concurrent.PrioritizedCallable;
import com.pmease.commons.util.concurrent.PrioritizedRunnable;

public interface WorkManager {
	
	<T> Future<T> submit(PrioritizedCallable<T> task);
	
	Future<?> submit(PrioritizedRunnable task);
	
	<T> Future<T> submit(PrioritizedRunnable task, T result);

	void execute(PrioritizedRunnable command);
	
	boolean remove(Runnable task);
	
}
