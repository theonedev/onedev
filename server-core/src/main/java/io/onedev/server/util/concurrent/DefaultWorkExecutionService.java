package io.onedev.server.util.concurrent;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.onedev.server.service.SettingService;
import io.onedev.server.security.SecurityUtils;

@Singleton
public class DefaultWorkExecutionService implements WorkExecutionService {

	@Inject
	private SettingService settingService;

	@Inject
	private ExecutorService executorService;
	
	private final Map<Key, Collection<Callable<?>>> runnings = new HashMap<>();
	
	private final Map<Key, Collection<WorkFuture<?>>> waitings = new TreeMap<>();

	private int getConcurrency() {
		return settingService.getPerformanceSetting().getCpuIntensiveTaskConcurrency();
	}

	private synchronized void check() {
		if (getConcurrency() > runnings.size()) {			
			for (var it = waitings.entrySet().iterator(); it.hasNext();) {
				var entry = it.next();
				it.remove();

				var keyRunnings = new ArrayList<Callable<?>>();
				for (var future: entry.getValue()) {
					future.runningFuture = call(future.priority, future.groupId, future.callable);
					notifyAll();
					keyRunnings.add(future.callable);
				}

				runnings.put(entry.getKey(), keyRunnings);
				if (runnings.size() == getConcurrency())
					break;
			}
		}
	}
	
	private synchronized <T> Future<T> call(int priority, String groupId, Callable<T> callable) {
		return executorService.submit(() -> {
			try {
				return callable.call();
			} finally {
				synchronized (DefaultWorkExecutionService.this) {
					var key = new Key(priority, groupId);
					var keyRunnings = runnings.get(key);
					keyRunnings.remove(callable);
					if (keyRunnings.isEmpty()) {
						runnings.remove(key);
						check();
					}
				}
			}
		});
	}
	
	@Override
	public synchronized <T> Future<T> submit(int priority, String groupId, Callable<T> callable) {
		callable = SecurityUtils.inheritSubject(callable);
		var key = new Key(priority, groupId);
		Collection<Callable<?>> keyRunnings = runnings.get(key);
		if (keyRunnings != null) {
			keyRunnings.add(callable);
			return call(priority, groupId, callable);
		} else {
			WorkFuture<T> future = new WorkFuture<T>(priority, groupId, callable);
			var keyWaitings = waitings.computeIfAbsent(key, k -> new ArrayList<>());
			keyWaitings.add(future);
			check();
			return future;
		}
	}

	private class WorkFuture<T> implements Future<T> {

		private final int priority;
		
		private final String groupId;
		
		private final Callable<T> callable;
		
		private Future<?> runningFuture;
		
		public WorkFuture(int priority, String groupId, Callable<T> callable) {
			this.priority = priority;
			this.groupId = groupId;
			this.callable = callable;
		}
		
		@Override
		public boolean cancel(boolean mayInterruptIfRunning) {
			synchronized (DefaultWorkExecutionService.this) {
				if (runningFuture != null) {
					return runningFuture.cancel(mayInterruptIfRunning);
				} else {
					var key = new Key(priority, groupId);
					var keyWaitings = waitings.get(key);
					if (keyWaitings != null) {
						var cancelled = keyWaitings.remove(this);
						if (keyWaitings.isEmpty())
							waitings.remove(key);
						return cancelled;
					} else {
						return false;
					}
				}
			}
		}

		@Override
		public boolean isCancelled() {
			synchronized (DefaultWorkExecutionService.this) {
				if (runningFuture != null) {
					return runningFuture.isCancelled();
				} else {
					var keyWaitings = waitings.get(new Key(priority, groupId));
					if (keyWaitings != null)
						return !keyWaitings.contains(this);
					else 
						return true;
				}
			}
		}

		@Override
		public boolean isDone() {
			synchronized (DefaultWorkExecutionService.this) {
				if (runningFuture != null) 
					return runningFuture.isDone();
				else
					return false;
			}
		}

		@SuppressWarnings("unchecked")
		@Override
		public T get() throws InterruptedException, ExecutionException {
			synchronized (DefaultWorkExecutionService.this) {
				while (runningFuture == null) 
					DefaultWorkExecutionService.this.wait();
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
			synchronized (DefaultWorkExecutionService.this) {
				while (runningFuture == null) 
					DefaultWorkExecutionService.this.wait(getRemainingTime(timeoutTime));
			}			
			return (T) runningFuture.get(getRemainingTime(timeoutTime), TimeUnit.MILLISECONDS);
		}
		
	}

	@Override
	public <T> Future<T> submit(int priority, Callable<T> callable) {
		return submit(priority, UUID.randomUUID().toString(), callable);
	}

	@Override
	public Future<?> submit(int priority, String groupId, Runnable runnable) {
		return submit(priority, groupId, new Callable<Void>() {

			@Override
			public Void call() {
				runnable.run();
				return null;
			}
			
		});
	}

	@Override
	public Future<?> submit(int priority, Runnable runnable) {
		return submit(priority, UUID.randomUUID().toString(), runnable);
	}

	public static class Key implements Comparable<Key> {
		
		private final int priority;
		
		private final String group;
		
		public Key(int priority, String group) {
			this.priority = priority;
			this.group = group;
		}
		
		public int getPriority() {
			return priority;
		}
		
		public String getGroup() {
			return group;
		}
		
		@Override
		public boolean equals(Object other) {
			if (!(other instanceof Key)) 
				return false;
			if (this == other)
				return true;
			Key otherKey = (Key) other;
			return priority == otherKey.priority && group.equals(otherKey.group);
		}
	
		@Override
		public int hashCode() {
			return Objects.hash(priority, group);
		}

		@Override
		public int compareTo(Key o) {
			var result = Integer.compare(priority, o.priority);
			if (result != 0)
				return result;
			else
				return group.compareTo(o.group);
		}		
	}	
	
}
