package io.onedev.server.util.work;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import io.onedev.server.util.concurrent.PrioritizedCallable;
import io.onedev.server.util.concurrent.PrioritizedRunnable;

/**
 * This executor is intended to run resource intensive long running tasks
 * 
 * @author robin
 *
 */
public interface WorkExecutor {
	
	void execute(PrioritizedRunnable command);

	<T> Future<T> submit(PrioritizedCallable<T> task);

	<T> Future<T> submit(PrioritizedRunnable task, T result);

	Future<?> submit(PrioritizedRunnable task);

	<T> List<Future<T>> invokeAll(Collection<? extends PrioritizedCallable<T>> tasks)
			throws InterruptedException;

	<T> List<Future<T>> invokeAll(Collection<? extends PrioritizedCallable<T>> tasks, 
			long timeout, TimeUnit unit) throws InterruptedException;

	<T> T invokeAny(Collection<? extends PrioritizedCallable<T>> tasks)
			throws InterruptedException, ExecutionException;

	<T> T invokeAny(Collection<? extends PrioritizedCallable<T>> tasks,
			long timeout, TimeUnit unit) throws InterruptedException,
			ExecutionException, TimeoutException;
	
}
