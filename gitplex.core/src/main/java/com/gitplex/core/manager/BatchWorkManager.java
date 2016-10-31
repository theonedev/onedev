package com.gitplex.core.manager;

import com.gitplex.core.manager.support.BatchWorker;
import com.gitplex.commons.util.concurrent.Prioritized;

public interface BatchWorkManager {

	void submit(BatchWorker worker, Prioritized work);

}
