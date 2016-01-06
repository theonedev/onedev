package com.pmease.gitplex.search;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class CompletedFuture<T> implements Future<T> {

	private final T result;
	
	public CompletedFuture(T result) {
		this.result = result;
	}
	
	@Override
	public boolean cancel(boolean mayInterruptIfRunning) {
		return false;
	}

	@Override
	public boolean isCancelled() {
		return false;
	}

	@Override
	public boolean isDone() {
		return true;
	}

	@Override
	public T get() throws InterruptedException, ExecutionException {
		return result;
	}

	@Override
	public T get(long timeout, TimeUnit unit)
			throws InterruptedException, ExecutionException, TimeoutException {
		return result;
	}
		
}
