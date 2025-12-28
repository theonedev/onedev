package io.onedev.server.service.impl;

import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;
import java.util.function.Consumer;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.jspecify.annotations.Nullable;
import org.quartz.ScheduleBuilder;
import org.quartz.SimpleScheduleBuilder;

import io.onedev.server.event.Listen;
import io.onedev.server.event.system.SystemStarted;
import io.onedev.server.event.system.SystemStopping;
import io.onedev.server.service.ManagedFutureService;
import io.onedev.server.taskschedule.SchedulableTask;
import io.onedev.server.taskschedule.TaskScheduler;

@Singleton
public class DefaultManagedFutureService implements ManagedFutureService, SchedulableTask {

	private final Map<String, ManagedFuture<?>> futures = new ConcurrentHashMap<>();

	private final TaskScheduler taskScheduler;

	private volatile String taskId;

	@Inject
	public DefaultManagedFutureService(TaskScheduler taskScheduler) {
		this.taskScheduler = taskScheduler;
	}

	@Listen
	public void on(SystemStarted event) {
		taskId = taskScheduler.schedule(this);
	}

	@Listen
	public void on(SystemStopping event) {
		if (taskId != null)
			taskScheduler.unschedule(taskId);
	}

	@Override
	public void execute() {
		var now = new Date();
		for (var it = futures.entrySet().iterator(); it.hasNext();) {
			var entry = it.next();
			var managed = entry.getValue();
			if (managed.timeout.before(now)) {
				if (managed.onTimeout != null) {
					try {
						managed.invokeCallback();
					} catch (Exception e) {
						// Ignore callback exceptions during cleanup
					}
				}
				managed.future.cancel(true);
				it.remove();
			}
		}
	}

	@Override
	public ScheduleBuilder<?> getScheduleBuilder() {
		return SimpleScheduleBuilder.repeatMinutelyForever();
	}

	@Override
	public <T> void addFuture(String futureId, Future<T> future, Date timeout, @Nullable Consumer<Future<T>> onTimeout) {
		futures.put(futureId, new ManagedFuture<>(future, timeout, onTimeout));
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> Future<T> removeFuture(String futureId) {
		var managed = futures.remove(futureId);
		if (managed != null)
			return (Future<T>) managed.future;
		else
			return null;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> Future<T> getFuture(String futureId) {
		var managed = futures.get(futureId);
		if (managed != null)
			return (Future<T>) managed.future;
		else
			return null;
	}

	private static class ManagedFuture<T> {

		final Future<T> future;

		final Date timeout;

		final Consumer<Future<T>> onTimeout;

		ManagedFuture(Future<T> future, Date timeout, Consumer<Future<T>> onTimeout) {
			this.future = future;
			this.timeout = timeout;
			this.onTimeout = onTimeout;
		}

		void invokeCallback() {
			if (onTimeout != null)
				onTimeout.accept(future);
		}

	}

}

