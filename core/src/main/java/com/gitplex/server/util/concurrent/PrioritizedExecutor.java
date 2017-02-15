package com.gitplex.server.util.concurrent;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.RunnableFuture;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import com.google.common.base.Preconditions;

public class PrioritizedExecutor extends ThreadPoolExecutor {

	public PrioritizedExecutor(int poolSize) {
		super(poolSize, poolSize, 0L, TimeUnit.MILLISECONDS, new PriorityBlockingQueue<Runnable>());
	}

	@Override
	protected <T> RunnableFuture<T> newTaskFor(Callable<T> callable) {
		return new PrioritizedFutureTask<T>((PrioritizedCallable<T>)callable);
	}

	@Override
	protected <T> RunnableFuture<T> newTaskFor(Runnable runnable, T value) {
		return new PrioritizedFutureTask<T>((PrioritizedRunnable)runnable, value);
	}

	@Override
	public void execute(Runnable command) {
		Preconditions.checkArgument(command instanceof PriorityAware);
		super.execute(command);
	}

	@Override
	public Future<?> submit(Runnable task) {
		Preconditions.checkArgument(task instanceof PriorityAware);
		return super.submit(task);
	}

	@Override
	public <T> Future<T> submit(Runnable task, T result) {
		Preconditions.checkArgument(task instanceof PriorityAware);
		return super.submit(task, result);
	}

	@Override
	public <T> Future<T> submit(Callable<T> task) {
		Preconditions.checkArgument(task instanceof PriorityAware);
		return super.submit(task);
	}

	@Override
	public <T> T invokeAny(Collection<? extends Callable<T>> tasks) 
			throws InterruptedException, ExecutionException {
		for (Callable<T> task: tasks) 
			Preconditions.checkArgument(task instanceof PriorityAware);
		return super.invokeAny(tasks);
	}

	@Override
	public <T> T invokeAny(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit)
			throws InterruptedException, ExecutionException, TimeoutException {
		for (Callable<T> task: tasks) 
			Preconditions.checkArgument(task instanceof PriorityAware);
		return super.invokeAny(tasks, timeout, unit);
	}

	@Override
	public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks) 
			throws InterruptedException {
		for (Callable<T> task: tasks) 
			Preconditions.checkArgument(task instanceof PriorityAware);
		return super.invokeAll(tasks);
	}

	@Override
	public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit)
			throws InterruptedException {
		for (Callable<T> task: tasks) 
			Preconditions.checkArgument(task instanceof PriorityAware);
		return super.invokeAll(tasks, timeout, unit);
	}

}
