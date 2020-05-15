/*
 * Copyright (C) 2008, Shawn O. Pearce <spearce@spearce.org> and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0 which is available at
 * https://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package org.eclipse.jgit.revwalk.filter;

import java.io.IOException;
import java.util.Arrays;

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
		set.addAll(Arrays.asList(a));
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
		set.addAll(Arrays.asList(a));
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
