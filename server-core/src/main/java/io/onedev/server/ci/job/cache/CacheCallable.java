package io.onedev.server.ci.job.cache;

import java.util.Collection;

public interface CacheCallable<T> {

	T call(Collection<CacheAllocation> allocations);
	
}
