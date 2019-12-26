package io.onedev.server.buildspec.job;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeoutException;

import javax.annotation.Nullable;

public class JobExecution {

	private final Future<?> future;
	
	private final long timeout;
	
	private volatile long beginTime;
	
	private volatile Long cancellerId;
	
	public JobExecution(Future<?> future, long timeout) {
		this.future = future;
		beginTime = System.currentTimeMillis();
		this.timeout = timeout;
	}

	public boolean cancel(@Nullable Long cancellerId) {
		if (!future.isDone()) {
			this.cancellerId = cancellerId;
			return future.cancel(true);
		} else {
			return false;
		}
	}

	public boolean isTimedout() {
		return System.currentTimeMillis() - beginTime > timeout;
	}
	
	public void updateBeginTime() {
		beginTime = System.currentTimeMillis();
	}
	
	public boolean isDone() {
		return future.isDone();
	}

	public void check() throws InterruptedException, TimeoutException, ExecutionException {
		if (isTimedout())
			throw new TimeoutException();
		else if (cancellerId != null)
			throw new CancellerAwareCancellationException(cancellerId);
		else
			future.get();
	}

}
