package io.onedev.server.ci.job;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeoutException;

import javax.annotation.Nullable;

public class JobExecution {

	private final Future<Boolean> future;
	
	private final long beginTime;
	
	private final long timeout;
	
	private volatile Long cancellerId;
	
	public JobExecution(Future<Boolean> future, long timeout) {
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
	
	public boolean isDone() {
		return future.isDone();
	}

	public boolean get() throws InterruptedException, TimeoutException, ExecutionException {
		if (isTimedout())
			throw new TimeoutException();
		else if (cancellerId != null)
			throw new CancellerAwareCancellationException(cancellerId);
		else
			return future.get();
	}

}
