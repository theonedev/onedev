package io.onedev.server.util.work;

import io.onedev.server.util.concurrent.Prioritized;

public interface BatchWorkManager {

	void submit(BatchWorker worker, Prioritized work);

}
