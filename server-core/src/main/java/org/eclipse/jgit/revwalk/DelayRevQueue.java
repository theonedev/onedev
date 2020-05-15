/*
 * Copyright (C) 2009, Google Inc.
 * Copyright (C) 2008, Shawn O. Pearce <spearce@spearce.org> and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0 which is available at
 * https://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package org.eclipse.jgit.revwalk;

import java.io.IOException;

import org.eclipse.jgit.errors.IncorrectObjectTypeException;
import org.eclipse.jgit.errors.MissingObjectException;

/**
 * Delays commits to be at least {@link PendingGenerator#OVER_SCAN} late.
 * <p>
 * This helps to "fix up" weird corner cases resulting from clock skew, by
 * slowing down what we produce to the caller we get a better chance to ensure
 * PendingGenerator reached back far enough in the graph to correctly mark
 * commits {@link RevWalk#UNINTERESTING} if necessary.
 * <p>
 * This generator should appear before {@link FixUninterestingGenerator} if the
 * lower level {@link #pending} isn't already fully buffered.
 */
final class DelayRevQueue extends Generator {
	private static final int OVER_SCAN = PendingGenerator.OVER_SCAN;

	private final Generator pending;

	private final FIFORevQueue delay;

	private int size;

	DelayRevQueue(Generator g) {
		super(g.firstParent);
		pending = g;
		delay = new FIFORevQueue();
	}

	@Override
	int outputType() {
		return pending.outputType();
	}

	@Override
	RevCommit next() throws MissingObjectException,
			IncorrectObjectTypeException, IOException {
		while (size < OVER_SCAN) {
			final RevCommit c = pending.next();
			if (c == null)
				break;
			delay.add(c);
			size++;
		}

		final RevCommit c = delay.next();
		if (c == null)
			return null;
		size--;
		return c;
	}
}
