package org.hibernate.cache.spi.support;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import org.junit.Test;

public class SimpleTimestamperTest {

	@Test
	public void keepsGeneratingCacheTimestampsWhenClockFallsBehind() throws Exception {
		// #711: the upstream clock-based loop spins when its counter is ahead of the clock.
		// Isolate the static counter so this test cannot affect another cache user.
		try (var loader = new URLClassLoader(new java.net.URL[] {
				SimpleTimestamper.class.getProtectionDomain().getCodeSource().getLocation()
		}, ClassLoader.getPlatformClassLoader())) {
			var timestamper = loader.loadClass(SimpleTimestamper.class.getName());
			var field = timestamper.getDeclaredField("VALUE");
			field.setAccessible(true);
			var counter = (AtomicLong) field.get(null);
			long future = (System.currentTimeMillis() + TimeUnit.DAYS.toMillis(1)) * SimpleTimestamper.ONE_MS;
			counter.set(future);
			var executor = Executors.newSingleThreadExecutor();
			try {
				var next = executor.submit(() -> (Long) timestamper.getMethod("next").invoke(null));
				assertEquals(future + 1, (long) next.get(5, TimeUnit.SECONDS));
			} finally {
				// Also releases the upstream spin loop if this regression is reintroduced.
				counter.set(0);
				executor.shutdownNow();
				assertTrue(executor.awaitTermination(5, TimeUnit.SECONDS));
			}
		}
	}

	@Test
	public void concurrentCacheUsersReceiveDistinctIncreasingTimestamps() throws Exception {
		var executor = Executors.newFixedThreadPool(4);
		try {
			var tasks = new ArrayList<Callable<long[]>>();
			for (int i = 0; i < 4; i++) {
				tasks.add(() -> {
					long[] timestamps = new long[10000];
					for (int j = 0; j < timestamps.length; j++) {
						timestamps[j] = SimpleTimestamper.next();
						if (j != 0)
							assertTrue(timestamps[j] > timestamps[j - 1]);
					}
					return timestamps;
				});
			}
			var distinct = new HashSet<Long>();
			for (var result : executor.invokeAll(tasks)) {
				for (long timestamp : result.get())
					assertTrue("Duplicate cache timestamp", distinct.add(timestamp));
			}
			assertEquals(40000, distinct.size());
		} finally {
			executor.shutdownNow();
		}
	}
}
