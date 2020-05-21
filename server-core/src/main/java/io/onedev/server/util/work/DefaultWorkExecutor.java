package io.onedev.server.util.work;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.RunnableFuture;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.inject.Singleton;

import io.onedev.commons.launcher.loader.Listen;
import io.onedev.server.event.system.SystemStopping;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.util.concurrent.PrioritizedCallable;
import io.onedev.server.util.concurrent.PrioritizedFutureTask;
import io.onedev.server.util.concurrent.PrioritizedRunnable;

@Singleton
public class DefaultWorkExecutor implements WorkExecutor {

	private final ExecutorService delegator = new ThreadPoolExecutor(
			Runtime.getRuntime().availableProcessors(), 
			Runtime.getRuntime().availableProcessors(), 
			0L, TimeUnit.MILLISECONDS, new PriorityBlockingQueue<Runnable>()) {
		
		@Override
		protected <T> RunnableFuture<T> newTaskFor(Callable<T> callable) {
			return new PrioritizedFutureTask<T>((PrioritizedCallable<T>)callable);
		}

		@Override
		protected <T> RunnableFuture<T> newTaskFor(Runnable runnable, T value) {
			return new PrioritizedFutureTask<T>((PrioritizedRunnable)runnable, value);
		}
		
	};
	
	@Override
	public void execute(PrioritizedRunnable command) {
		delegator.execute(SecurityUtils.inheritSubject(command));
	}

	@Override
	public <T> Future<T> submit(PrioritizedCallable<T> task) {
		return delegator.submit(SecurityUtils.inheritSubject(task));
	}

	@Override
	public <T> Future<T> submit(PrioritizedRunnable task, T result) {
		return delegator.submit(SecurityUtils.inheritSubject(task), result);
	}

	@Override
	public Future<?> submit(PrioritizedRunnable task) {
		return delegator.submit(SecurityUtils.inheritSubject(task));
	}

	@Override
	public <T> List<Future<T>> invokeAll(Collection<? extends PrioritizedCallable<T>> tasks)
			throws InterruptedException {
		return delegator.invokeAll(SecurityUtils.inheritSubject(tasks));
	}

	@Override
	public <T> List<Future<T>> invokeAll(
			Collection<? extends PrioritizedCallable<T>> tasks, long timeout, TimeUnit unit)
			throws InterruptedException {
		return delegator.invokeAll(SecurityUtils.inheritSubject(tasks), timeout, unit);
	}

	@Override
	public <T> T invokeAny(Collection<? extends PrioritizedCallable<T>> tasks)
			throws InterruptedException, ExecutionException {
		return delegator.invokeAny(SecurityUtils.inheritSubject(tasks));
	}

	@Override
	public <T> T invokeAny(Collection<? extends PrioritizedCallable<T>> tasks,
			long timeout, TimeUnit unit) throws InterruptedException,
			ExecutionException, TimeoutException {
		return delegator.invokeAny(SecurityUtils.inheritSubject(tasks), timeout, unit);
	}

	@Listen
	public void on(SystemStopping event) {
		delegator.shutdown();
	}

}
