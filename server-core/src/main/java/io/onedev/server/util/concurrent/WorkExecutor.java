package io.onedev.server.util.concurrent;

import java.util.concurrent.Future;

/**
 * Work executor executes submitted works with concurrency limit. Only concurrent number of 
 * different groups is limited, works within same group are not subject to concurrency check. 
 * This is so as some work (git lfs push over ssh for instance) may need to wait for 
 * completion of other works. Putting them in same group can prevent deadlocks
 */
public interface WorkExecutor {

	<T> Future<T> submit(String groupId, PrioritizedCallable<T> callable); 
	
	<T> Future<T> submit(PrioritizedCallable<T> callable); 
	
	Future<?> submit(String groupId, PrioritizedRunnable runnable); 
	
	Future<?> submit(PrioritizedRunnable runnable); 
}
