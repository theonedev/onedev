package com.gitplex.server.core.manager;

import com.gitplex.commons.util.concurrent.Prioritized;
import com.gitplex.server.core.manager.support.BatchWorker;

public interface BatchWorkManager {

	void submit(BatchWorker worker, Prioritized work);

}
