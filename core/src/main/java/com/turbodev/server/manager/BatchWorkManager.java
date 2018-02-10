package com.turbodev.server.manager;

import com.turbodev.utils.concurrent.Prioritized;
import com.turbodev.server.util.BatchWorker;

public interface BatchWorkManager {

	void submit(BatchWorker worker, Prioritized work);

}
