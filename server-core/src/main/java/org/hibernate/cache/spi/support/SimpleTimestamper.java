/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.cache.spi.support;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Generates increasing identifiers (in a single VM only). Not valid across multiple VMs.  Identifiers are not
 * necessarily strictly increasing, but usually are.
 * <p>
 * Core while loop implemented by Alex Snaps - EHCache project - under ASL 2.0
 *
 * @author Hibernate team
 * @author Alex Snaps
 */
public final class SimpleTimestamper {
	private static final int BIN_DIGITS = 12;
	private static final AtomicLong VALUE = new AtomicLong();

	public static final short ONE_MS = 1 << BIN_DIGITS;

	public static long next() {
        // Preserve wall-clock units and ordering within this JVM without waiting
        // for the wall clock to catch up if it moves backwards (issue #711).
        long now = System.currentTimeMillis() << BIN_DIGITS;
        return VALUE.updateAndGet(previous -> Math.max(now, previous + 1));
    }

	public static int timeOut() {
		return (int) TimeUnit.SECONDS.toMillis( 60 ) * ONE_MS;
	}

	private SimpleTimestamper() {
	}
}
