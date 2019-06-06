package io.onedev.server.util.work;

import io.onedev.commons.utils.concurrent.Prioritized;

public interface BatchWorkManager {

	void submit(BatchWorker worker, Prioritized work);

}
