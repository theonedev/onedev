package com.gitplex.server.manager;

import com.gitplex.server.util.BatchWorker;
import com.gitplex.utils.concurrent.Prioritized;

public interface BatchWorkManager {

	void submit(BatchWorker worker, Prioritized work);

}
