package io.onedev.server.web.component.taskbutton;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.quartz.ScheduleBuilder;
import org.quartz.SimpleScheduleBuilder;

import io.onedev.server.event.Listen;
import io.onedev.server.event.system.SystemStarted;
import io.onedev.server.event.system.SystemStopping;
import io.onedev.server.taskschedule.SchedulableTask;
import io.onedev.server.taskschedule.TaskScheduler;

@Singleton
public class DefaultTaskFutureService implements SchedulableTask, TaskFutureService {

	private final Map<String, TaskFuture> taskFutures = new ConcurrentHashMap<>();

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
    public Map<String, TaskFuture> getTaskFutures() {
        return taskFutures;
    }

	@Override
	public void execute() {
		for (Iterator<Map.Entry<String, TaskFuture>> it = taskFutures.entrySet().iterator(); it.hasNext();) {
			TaskFuture taskFuture = it.next().getValue();
			if (taskFuture.isTimedout()) {
				taskFuture.cancel(true);
				it.remove();
			}
		}
	}

	@Override
	public ScheduleBuilder<?> getScheduleBuilder() {
		return SimpleScheduleBuilder.repeatMinutelyForever();
	}
	
}