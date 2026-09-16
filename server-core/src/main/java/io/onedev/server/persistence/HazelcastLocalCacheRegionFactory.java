package io.onedev.server.persistence;

import java.util.concurrent.TimeUnit;

public class HazelcastLocalCacheRegionFactory extends com.hazelcast.hibernate.HazelcastLocalCacheRegionFactory {

	@Override
	public long getTimeout() {
		// Hazelcast timestamps use milliseconds, not Hibernate SimpleTimestamper's scaled units.
		return TimeUnit.SECONDS.toMillis(60);
	}

}
