package io.onedev.server.web.component.taskbutton;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.joda.time.DateTime;

import io.onedev.server.buildspec.job.log.JobLogEntryEx;

class TaskFuture implements Future<TaskResult> {

	private final Future<TaskResult> wrapped;
	
	private final List<JobLogEntryEx> logEntries;
	
	private volatile Date lastActive = new Date();
	
	public TaskFuture(Future<TaskResult> wrapped, List<JobLogEntryEx> logEntries) {
		this.wrapped = wrapped;
		this.logEntries = logEntries;
	}
	
	@Override
	public boolean cancel(boolean mayInterruptIfRunning) {
		return wrapped.cancel(mayInterruptIfRunning);
	}

	@Override
	public boolean isCancelled() {
		return wrapped.isCancelled();
	}

	@Override
	public boolean isDone() {
		return wrapped.isDone();
	}
	
	public boolean isTimedout() {
		return lastActive.before(new DateTime().minusMinutes(1).toDate());
	}

	@Override
	public TaskResult get() throws InterruptedException, ExecutionException {
		return wrapped.get();
	}

	@Override
	public TaskResult get(long timeout, TimeUnit unit)
			throws InterruptedException, ExecutionException, TimeoutException {
		return get(timeout, unit);
	}
	
	public List<JobLogEntryEx> getLogEntries() {
		lastActive = new Date();
		synchronized (logEntries) {
			List<JobLogEntryEx> copy = new ArrayList<>(logEntries);
			logEntries.clear();
			return copy;
		}
	}
	
}