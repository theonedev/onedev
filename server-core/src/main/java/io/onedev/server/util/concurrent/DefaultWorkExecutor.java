package io.onedev.server.util.concurrent;

import io.onedev.server.entitymanager.SettingManager;
import io.onedev.server.event.Listen;
import io.onedev.server.model.support.administration.SystemSetting;
import io.onedev.server.security.SecurityUtils;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.*;
import java.util.concurrent.*;

import static java.util.Collections.sort;
import static java.util.Comparator.comparingInt;

@Singleton
public class DefaultWorkExecutor implements WorkExecutor {

	private final SettingManager settingManager;
	
	private final ExecutorService executorService;
	
	private final Map<String, Collection<PrioritizedCallable<?>>> runnings = new HashMap<>();
	
	private final Map<String, Collection<WorkFuture<?>>> waitings = new HashMap<>();
	
	@Inject
	public DefaultWorkExecutor(ExecutorService executorService, SettingManager settingManager) {
		this.executorService = executorService;
		this.settingManager = settingManager;
	}
	
	@Listen
	public void on(SystemSetting event) {
			
	}
	
	private int getConcurrency() {
		return settingManager.getPerformanceSetting().getCpuIntensiveTaskConcurrency();
	}

	private synchronized void check() {
		if (getConcurrency() > runnings.size()) {
			Map<String, Integer> averagePriorities = new HashMap<>();
			for (Map.Entry<String, Collection<WorkFuture<?>>> entry: waitings.entrySet()) {
				int totalPriorities = 0;
				for (WorkFuture<?> future: entry.getValue()) 
					totalPriorities += future.callable.getPriority();
				averagePriorities.put(entry.getKey(), totalPriorities/entry.getValue().size());
			}
			List<String> groupIds = new ArrayList<>(waitings.keySet());
			sort(groupIds, comparingInt(averagePriorities::get));
			for (String groupId: groupIds) {
				Collection<PrioritizedCallable<?>> runningsOfGroup = new ArrayList<>();
				for (WorkFuture<?> future: waitings.remove(groupId)) {
					future.runningFuture = call(future.groupId, future.callable);
					notifyAll();
					runningsOfGroup.add(future.callable);
				}
				runnings.put(groupId, runningsOfGroup);
				if (runnings.size() == getConcurrency())
					break;
			}
		}
	}
	
	private synchronized <T> Future<T> call(String groupId, PrioritizedCallable<T> callable) {
		return executorService.submit(() -> {
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
			var waitingsOfGroup = waitings.computeIfAbsent(groupId, k -> new ArrayList<>());
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
					if (waitingsOfGroup != null) {
						var cancelled = waitingsOfGroup.remove(this);
						if (waitingsOfGroup.isEmpty())
							waitings.remove(groupId);
						return cancelled;
					} else {
						return false;
					}
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
			public Void call() {
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
