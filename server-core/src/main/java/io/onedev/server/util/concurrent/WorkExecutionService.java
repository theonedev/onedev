package io.onedev.server.util.concurrent;

import java.util.concurrent.Callable;
import java.util.concurrent.Future;

/**
 * Work executor executes submitted works with concurrency limit. Only concurrent number of 
 * different groups is limited, works within same group are not subject to concurrency check. 
 * This is so as some work (git lfs push over ssh for instance) may need to wait for 
 * completion of other works. Putting them in same group can prevent deadlocks
 */
public interface WorkExecutionService {

	<T> Future<T> submit(int priority, String groupId, Callable<T> callable); 
	
	<T> Future<T> submit(int priority, Callable<T> callable); 
	
	Future<?> submit(int priority, String groupId, Runnable runnable); 
	
	Future<?> submit(int priority, Runnable runnable); 
}
