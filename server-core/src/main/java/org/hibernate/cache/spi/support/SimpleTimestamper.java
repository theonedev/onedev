/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.cache.spi.support;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Generates increasing identifiers (in a single VM only). Not valid across multiple VMs.  Identifiers are not
 * necessarily strictly increasing, but usually are.
 * <p/>
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
		// Fix issue #711 - After update to v7.x CPU is loaded on 100%
		/*
		while ( true ) {
			long base = System.currentTimeMillis() << BIN_DIGITS;
			long maxValue = base + ONE_MS - 1;

			for ( long current = VALUE.get(), update = Math.max( base, current + 1 ); update < maxValue;
					current = VALUE.get(), update = Math.max( base, current + 1 ) ) {
				if ( VALUE.compareAndSet( current, update ) ) {
					return update;
				}
			}
		}
		*/
		return VALUE.incrementAndGet();
	}

	public static int timeOut() {
		return (int) TimeUnit.SECONDS.toMillis( 60 ) * ONE_MS;
	}

	private SimpleTimestamper() {
	}
}
