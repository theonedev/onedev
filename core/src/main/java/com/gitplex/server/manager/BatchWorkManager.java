package com.gitplex.server.manager;

import com.gitplex.server.manager.support.BatchWorker;
import com.gitplex.server.util.concurrent.Prioritized;

public interface BatchWorkManager {

	void submit(BatchWorker worker, Prioritized work);

}
