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
 * Filters out commits marked {@link RevWalk#UNINTERESTING}.
 * <p>
 * This generator is only in front of another generator that has fully buffered
 * commits, such that we are called only after the {@link PendingGenerator} has
 * exhausted its input queue and given up. It skips over any uninteresting
 * commits that may have leaked out of the PendingGenerator due to clock skew
 * being detected in the commit objects.
 */
final class FixUninterestingGenerator extends Generator {
	private final Generator pending;

	FixUninterestingGenerator(Generator g) {
		super(g.firstParent);
		pending = g;
	}

	@Override
	int outputType() {
		return pending.outputType();
	}

	@Override
	RevCommit next() throws MissingObjectException,
			IncorrectObjectTypeException, IOException {
		for (;;) {
			final RevCommit c = pending.next();
			if (c == null)
				return null;
			if ((c.flags & RevWalk.UNINTERESTING) == 0)
				return c;
		}
	}
}
