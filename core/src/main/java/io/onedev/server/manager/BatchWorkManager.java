package io.onedev.server.manager;

import io.onedev.server.util.BatchWorker;
import io.onedev.utils.concurrent.Prioritized;

public interface BatchWorkManager {

	void submit(BatchWorker worker, Prioritized work);

}
