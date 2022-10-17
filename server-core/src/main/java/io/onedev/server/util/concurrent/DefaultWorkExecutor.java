package io.onedev.server.util.concurrent;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.onedev.server.ServerConfig;
import io.onedev.server.security.SecurityUtils;

@Singleton
public class DefaultWorkExecutor implements WorkExecutor {

	private final ExecutorService executorService;
	
	private final Map<String, Collection<PrioritizedCallable<?>>> runnings = new HashMap<>();
	
	private final Map<String, Collection<WorkFuture<?>>> waitings = new HashMap<>();

	private final int concurrency;
	
	@Inject
	public DefaultWorkExecutor(ExecutorService executorService, ServerConfig serverConfig) {
		this.executorService = executorService;
		concurrency = serverConfig.getServerCpu() / 1000;
	}

	private synchronized void check() {
		if (concurrency > runnings.size()) {
			Map<String, Integer> averagePriorities = new HashMap<>();
			for (Map.Entry<String, Collection<WorkFuture<?>>> entry: waitings.entrySet()) {
				int totalPriorities = 0;
				for (WorkFuture<?> future: entry.getValue()) 
					totalPriorities += future.callable.getPriority();
				averagePriorities.put(entry.getKey(), totalPriorities/entry.getValue().size());
			}
			List<String> groupIds = new ArrayList<>(waitings.keySet());
			Collections.sort(groupIds, new Comparator<String>() {

				@Override
				public int compare(String o1, String o2) {
					return averagePriorities.get(o1) - averagePriorities.get(o2);
				}
				
			});
			for (String groupId: groupIds) {
				Collection<PrioritizedCallable<?>> runningsOfGroup = new ArrayList<>();
				for (WorkFuture<?> future: waitings.remove(groupId)) {
					future.runningFuture = call(future.groupId, future.callable);
					notifyAll();
					runningsOfGroup.add(future.callable);
				}
				runnings.put(groupId, runningsOfGroup);
				if (runnings.size() == concurrency)
					break;
			}
		}
	}
	
	private synchronized <T> Future<T> call(String groupId, PrioritizedCallable<T> callable) {
		return executorService.submit(new Callable<T>() {

			@Override
			public T call() throws Exception {
				try {
					return callable.call();
				} finally {
					synchronized (DefaultWorkExecutor.this) {
						Collection<PrioritizedCallable<?>> runningsOfGroup = runnings.get(groupId);
						runningsOfGroup.remove(callable);
						if (runningsOfGroup.isEmpty()) {
							runnings.remove(groupId);
							check();
						}
					}
				}
			}
			
		});
	}
	
	@Override
	public synchronized <T> Future<T> submit(String groupId, PrioritizedCallable<T> callable) {
		callable = SecurityUtils.inheritSubject(callable);
		Collection<PrioritizedCallable<?>> runningsOfGroup = runnings.get(groupId);
		if (runningsOfGroup != null) {
			runningsOfGroup.add(callable);
			return call(groupId, callable);
		} else {
			WorkFuture<T> future = new WorkFuture<T>(groupId, callable);
			Collection<WorkFuture<?>> waitingsOfGroup = waitings.get(groupId);
			if (waitingsOfGroup == null) {
				waitingsOfGroup = new ArrayList<>();
				waitings.put(groupId, waitingsOfGroup);
			}
			waitingsOfGroup.add(future);
			check();
			return future;
		}
	}

	private class WorkFuture<T> implements Future<T> {

		private final String groupId;
		
		private final PrioritizedCallable<T> callable;
		
		private Future<?> runningFuture;
		
		public WorkFuture(String groupId, PrioritizedCallable<T> callable) {
			this.groupId = groupId;
			this.callable = callable;
		}
		
		@Override
		public boolean cancel(boolean mayInterruptIfRunning) {
			synchronized (DefaultWorkExecutor.this) {
				if (runningFuture != null) {
					return runningFuture.cancel(mayInterruptIfRunning);
				} else {
					Collection<WorkFuture<?>> waitingsOfGroup = waitings.get(groupId);
					if (waitingsOfGroup != null)
						return waitingsOfGroup.remove(this);
					else 
						return false;
				}
			}
		}

		@Override
		public boolean isCancelled() {
			synchronized (DefaultWorkExecutor.this) {
				if (runningFuture != null) {
					return runningFuture.isCancelled();
				} else {
					Collection<WorkFuture<?>> waitingsOfGroup = waitings.get(groupId);
					if (waitingsOfGroup != null)
						return !waitingsOfGroup.contains(this);
					else 
						return true;
				}
			}
		}

		@Override
		public boolean isDone() {
			synchronized (DefaultWorkExecutor.this) {
				if (runningFuture != null) 
					return runningFuture.isDone();
				else
					return false;
			}
		}

		@SuppressWarnings("unchecked")
		@Override
		public T get() throws InterruptedException, ExecutionException {
			synchronized (DefaultWorkExecutor.this) {
				while (runningFuture == null) 
					DefaultWorkExecutor.this.wait();
			}
			return (T) runningFuture.get();
		}

		private long getRemainingTime(long timeoutTime) throws TimeoutException {
			long remainingTime = timeoutTime - System.currentTimeMillis();
			if (remainingTime > 0)
				return remainingTime;
			else
				throw new TimeoutException();
		}
		
		@SuppressWarnings("unchecked")
		@Override
		public T get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
			long timeoutTime = System.currentTimeMillis() + unit.toMillis(timeout);
			synchronized (DefaultWorkExecutor.this) {
				while (runningFuture == null) 
					DefaultWorkExecutor.this.wait(getRemainingTime(timeoutTime));
			}			
			return (T) runningFuture.get(getRemainingTime(timeoutTime), TimeUnit.MILLISECONDS);
		}
		
	}

	@Override
	public <T> Future<T> submit(PrioritizedCallable<T> callable) {
		return submit(UUID.randomUUID().toString(), callable);
	}

	@Override
	public Future<?> submit(String groupId, PrioritizedRunnable runnable) {
		return submit(groupId, new PrioritizedCallable<Void>(runnable.getPriority()) {

			@Override
			public Void call() throws Exception {
				runnable.run();
				return null;
			}
			
		});
	}

	@Override
	public Future<?> submit(PrioritizedRunnable runnable) {
		return submit(UUID.randomUUID().toString(), runnable);
	}
	
}
