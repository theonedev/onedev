/*
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
 * Produces commits for RevWalk to return to applications.
 * <p>
 * Implementations of this basic class provide the real work behind RevWalk.
 * Conceptually a Generator is an iterator or a queue, it returns commits until
 * there are no more relevant. Generators may be piped/stacked together to
 * create a more complex set of operations.
 *
 * @see PendingGenerator
 * @see StartGenerator
 */
abstract class Generator {
	/** Commits are sorted by commit date and time, descending. */
	static final int SORT_COMMIT_TIME_DESC = 1 << 0;

	/** Output may have {@link RevWalk#REWRITE} marked on it. */
	static final int HAS_REWRITE = 1 << 1;

	/** Output needs {@link RewriteGenerator}. */
	static final int NEEDS_REWRITE = 1 << 2;

	/** Topological ordering is enforced (all children before parents). */
	static final int SORT_TOPO = 1 << 3;

	/** Output may have {@link RevWalk#UNINTERESTING} marked on it. */
	static final int HAS_UNINTERESTING = 1 << 4;

	protected final boolean firstParent;

	protected Generator(boolean firstParent) {
		this.firstParent = firstParent;
	}

	/**
	 * Connect the supplied queue to this generator's own free list (if any).
	 *
	 * @param q
	 *            another FIFO queue that wants to share our queue's free list.
	 */
	void shareFreeList(BlockRevQueue q) {
		// Do nothing by default.
	}

	/**
	 * Obtain flags describing the output behavior of this generator.
	 *
	 * @return one or more of the constants declared in this class, describing
	 *         how this generator produces its results.
	 */
	abstract int outputType();

	/**
	 * Return the next commit to the application, or the next generator.
	 *
	 * @return next available commit; null if no more are to be returned.
	 * @throws MissingObjectException
	 * @throws IncorrectObjectTypeException
	 * @throws IOException
	 */
	abstract RevCommit next() throws MissingObjectException,
			IncorrectObjectTypeException, IOException;
}
