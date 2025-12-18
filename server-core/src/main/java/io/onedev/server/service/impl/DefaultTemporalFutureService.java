package io.onedev.server.service.impl;

import java.util.Date;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.commons.lang3.tuple.Pair;
import org.quartz.ScheduleBuilder;
import org.quartz.SimpleScheduleBuilder;

import io.onedev.server.event.Listen;
import io.onedev.server.event.system.SystemStarted;
import io.onedev.server.event.system.SystemStopping;
import io.onedev.server.service.TemporalFutureService;
import io.onedev.server.taskschedule.SchedulableTask;
import io.onedev.server.taskschedule.TaskScheduler;

@Singleton
public class DefaultTemporalFutureService implements TemporalFutureService, SchedulableTask {

	private static final int CLEANUP_TIMEOUT = 1000*60; // in milliseconds

	private final Map<String, Pair<CompletableFuture<?>, Date>> futureAndDates = new ConcurrentHashMap<>();

	@Inject
	private TaskScheduler taskScheduler;
	
	private volatile String cleanupTaskId;

	@Listen
	public void on(SystemStarted event) {
		cleanupTaskId = taskScheduler.schedule(this);
	}
	
	@Listen
	public void on(SystemStopping event) {
		if (cleanupTaskId != null)
			taskScheduler.unschedule(cleanupTaskId);
	}

	@Override
	public void execute() {
		var expireDate = new Date(System.currentTimeMillis() - CLEANUP_TIMEOUT);
		for (var it = futureAndDates.entrySet().iterator(); it.hasNext();) {
			var futureAndDate = it.next().getValue();
			if (futureAndDate.getRight().before(expireDate)) {
				futureAndDate.getLeft().cancel(true);
				it.remove();
			}
		}
	}

	@Override
	public ScheduleBuilder<?> getScheduleBuilder() {
		return SimpleScheduleBuilder.repeatMinutelyForever();
	}

	@Override
	public <T> void addFuture(String futureId, CompletableFuture<T> future) {
		futureAndDates.put(futureId, Pair.of(future, new Date()));
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> CompletableFuture<T> removeFuture(String futureId) {
		var futureAndDate = futureAndDates.remove(futureId);
		if (futureAndDate != null)
			return (CompletableFuture<T>) futureAndDate.getLeft();
		else
			return null;
	}
	
}