package io.onedev.server.util.concurrent;

public interface BatchWorkManager {

	void submit(BatchWorker worker, Prioritized work);
	
}
