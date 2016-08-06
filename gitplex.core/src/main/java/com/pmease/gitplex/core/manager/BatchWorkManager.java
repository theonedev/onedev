package com.pmease.gitplex.core.manager;

import com.pmease.commons.util.concurrent.Prioritized;
import com.pmease.gitplex.core.manager.support.BatchWorker;

public interface BatchWorkManager {

	void submit(BatchWorker worker, Prioritized work);

}
