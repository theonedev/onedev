package io.onedev.server.ci.job.cache;

import java.util.Collection;

public interface CacheRunnable {

	void run(Collection<CacheAllocation> allocations);
	
}
