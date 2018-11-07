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

package org.eclipse.jgit.revwalk.filter;

import java.io.IOException;

import org.eclipse.jgit.errors.IncorrectObjectTypeException;
import org.eclipse.jgit.errors.MissingObjectException;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevFlag;
import org.eclipse.jgit.revwalk.RevFlagSet;
import org.eclipse.jgit.revwalk.RevWalk;

/**
 * Matches only commits with some/all RevFlags already set.
 */
public abstract class RevFlagFilter extends RevFilter {
	/**
	 * Create a new filter that tests for a single flag.
	 *
	 * @param a
	 *            the flag to test.
	 * @return filter that selects only commits with flag <code>a</code>.
	 */
	public static RevFilter has(RevFlag a) {
		final RevFlagSet s = new RevFlagSet();
		s.add(a);
		return new HasAll(s);
	}

	/**
	 * Create a new filter that tests all flags in a set.
	 *
	 * @param a
	 *            set of flags to test.
	 * @return filter that selects only commits with all flags in <code>a</code>.
	 */
	public static RevFilter hasAll(RevFlag... a) {
		final RevFlagSet set = new RevFlagSet();
		for (RevFlag flag : a)
			set.add(flag);
		return new HasAll(set);
	}

	/**
	 * Create a new filter that tests all flags in a set.
	 *
	 * @param a
	 *            set of flags to test.
	 * @return filter that selects only commits with all flags in <code>a</code>.
	 */
	public static RevFilter hasAll(RevFlagSet a) {
		return new HasAll(new RevFlagSet(a));
	}

	/**
	 * Create a new filter that tests for any flag in a set.
	 *
	 * @param a
	 *            set of flags to test.
	 * @return filter that selects only commits with any flag in <code>a</code>.
	 */
	public static RevFilter hasAny(RevFlag... a) {
		final RevFlagSet set = new RevFlagSet();
		for (RevFlag flag : a)
			set.add(flag);
		return new HasAny(set);
	}

	/**
	 * Create a new filter that tests for any flag in a set.
	 *
	 * @param a
	 *            set of flags to test.
	 * @return filter that selects only commits with any flag in <code>a</code>.
	 */
	public static RevFilter hasAny(RevFlagSet a) {
		return new HasAny(new RevFlagSet(a));
	}

	final RevFlagSet flags;

	RevFlagFilter(RevFlagSet m) {
		flags = m;
	}

	/** {@inheritDoc} */
	@Override
	public RevFilter clone() {
		return this;
	}

	/** {@inheritDoc} */
	@Override
	public String toString() {
		return super.toString() + flags;
	}

	private static class HasAll extends RevFlagFilter {
		HasAll(RevFlagSet m) {
			super(m);
		}

		@Override
		public boolean include(RevWalk walker, RevCommit c)
				throws MissingObjectException, IncorrectObjectTypeException,
				IOException {
			return c.hasAll(flags);
		}

		@Override
		public boolean requiresCommitBody() {
			return false;
		}
	}

	private static class HasAny extends RevFlagFilter {
		HasAny(RevFlagSet m) {
			super(m);
		}

		@Override
		public boolean include(RevWalk walker, RevCommit c)
				throws MissingObjectException, IncorrectObjectTypeException,
				IOException {
			return c.hasAny(flags);
		}

		@Override
		public boolean requiresCommitBody() {
			return false;
		}
	}
}
