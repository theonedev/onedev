package io.onedev.server.manager;

import io.onedev.commons.utils.concurrent.Prioritized;
import io.onedev.server.util.BatchWorker;

public interface BatchWorkManager {

	void submit(BatchWorker worker, Prioritized work);

}
