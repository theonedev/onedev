/*
 * Copyright (C) 2008, Shawn O. Pearce <spearce@spearce.org>
 * and other copyright owners as documented in the project's IP log.
 *
 * This program and the accompanying materials are made available
 * under the terms of the Eclipse Distribution License v1.0 which
 * accompanies this distribution, is reproduced below, and is
 * available at http://www.eclipse.org/org/documents/edl-v10.php
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or
 * without modification, are permitted provided that the following
 * conditions are met:
 *
 * - Redistributions of source code must retain the above copyright
 *   notice, this list of conditions and the following disclaimer.
 *
 * - Redistributions in binary form must reproduce the above
 *   copyright notice, this list of conditions and the following
 *   disclaimer in the documentation and/or other materials provided
 *   with the distribution.
 *
 * - Neither the name of the Eclipse Foundation, Inc. nor the
 *   names of its contributors may be used to endorse or promote
 *   products derived from this software without specific prior
 *   written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND
 * CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
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
