package io.onedev.server.job;

import javax.annotation.Nullable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeoutException;

public class JobExecution {

	private final Future<Boolean> future;

	private final long timeout;
	
	private volatile long beginTime;
	
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
	
	public boolean isCancelled() {
		return future.isCancelled();
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

	public boolean check() throws InterruptedException, TimeoutException, ExecutionException {
		if (isTimedout())
			throw new TimeoutException();
		else if (cancellerId != null)
			throw new CancellationException(cancellerId);
		else
			return future.get();
	}

}
