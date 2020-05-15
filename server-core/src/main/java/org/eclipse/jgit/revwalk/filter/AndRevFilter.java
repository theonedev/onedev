/*
 * Copyright (C) 2007, Robin Rosenberg <robin.rosenberg@dewire.com>
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
import java.util.Collection;

import org.eclipse.jgit.errors.IncorrectObjectTypeException;
import org.eclipse.jgit.errors.MissingObjectException;
import org.eclipse.jgit.internal.JGitText;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;

/**
 * Includes a commit only if all subfilters include the same commit.
 * <p>
 * Classic shortcut behavior is used, so evaluation of the
 * {@link org.eclipse.jgit.revwalk.filter.RevFilter#include(RevWalk, RevCommit)}
 * method stops as soon as a false result is obtained. Applications can improve
 * filtering performance by placing faster filters that are more likely to
 * reject a result earlier in the list.
 */
public abstract class AndRevFilter extends RevFilter {
	/**
	 * Create a filter with two filters, both of which must match.
	 *
	 * @param a
	 *            first filter to test.
	 * @param b
	 *            second filter to test.
	 * @return a filter that must match both input filters.
	 */
	public static RevFilter create(RevFilter a, RevFilter b) {
		if (a == ALL)
			return b;
		if (b == ALL)
			return a;
		return new Binary(a, b);
	}

	/**
	 * Create a filter around many filters, all of which must match.
	 *
	 * @param list
	 *            list of filters to match against. Must contain at least 2
	 *            filters.
	 * @return a filter that must match all input filters.
	 */
	public static RevFilter create(RevFilter[] list) {
		if (list.length == 2)
			return create(list[0], list[1]);
		if (list.length < 2)
			throw new IllegalArgumentException(JGitText.get().atLeastTwoFiltersNeeded);
		final RevFilter[] subfilters = new RevFilter[list.length];
		System.arraycopy(list, 0, subfilters, 0, list.length);
		return new List(subfilters);
	}

	/**
	 * Create a filter around many filters, all of which must match.
	 *
	 * @param list
	 *            list of filters to match against. Must contain at least 2
	 *            filters.
	 * @return a filter that must match all input filters.
	 */
	public static RevFilter create(Collection<RevFilter> list) {
		if (list.size() < 2)
			throw new IllegalArgumentException(JGitText.get().atLeastTwoFiltersNeeded);
		final RevFilter[] subfilters = new RevFilter[list.size()];
		list.toArray(subfilters);
		if (subfilters.length == 2)
			return create(subfilters[0], subfilters[1]);
		return new List(subfilters);
	}

	private static class Binary extends AndRevFilter {
		private final RevFilter a;

		private final RevFilter b;

		private final boolean requiresCommitBody;

		Binary(RevFilter one, RevFilter two) {
			a = one;
			b = two;
			requiresCommitBody = a.requiresCommitBody()
					|| b.requiresCommitBody();
		}

		@Override
		public boolean include(RevWalk walker, RevCommit c)
				throws MissingObjectException, IncorrectObjectTypeException,
				IOException {
			return a.include(walker, c) && b.include(walker, c);
		}

		@Override
		public boolean requiresCommitBody() {
			return requiresCommitBody;
		}

		@Override
		public RevFilter clone() {
			return new Binary(a.clone(), b.clone());
		}

		@SuppressWarnings("nls")
		@Override
		public String toString() {
			return "(" + a.toString() + " AND " + b.toString() + ")"; //$NON-NLS-1$
		}
	}

	private static class List extends AndRevFilter {
		private final RevFilter[] subfilters;

		private final boolean requiresCommitBody;

		List(RevFilter[] list) {
			subfilters = list;

			boolean rcb = false;
			for (RevFilter filter : subfilters)
				rcb |= filter.requiresCommitBody();
			requiresCommitBody = rcb;
		}

		@Override
		public boolean include(RevWalk walker, RevCommit c)
				throws MissingObjectException, IncorrectObjectTypeException,
				IOException {
			for (RevFilter f : subfilters) {
				if (!f.include(walker, c))
					return false;
			}
			return true;
		}

		@Override
		public boolean requiresCommitBody() {
			return requiresCommitBody;
		}

		@Override
		public RevFilter clone() {
			final RevFilter[] s = new RevFilter[subfilters.length];
			for (int i = 0; i < s.length; i++)
				s[i] = subfilters[i].clone();
			return new List(s);
		}

		@SuppressWarnings("nls")
		@Override
		public String toString() {
			final StringBuilder r = new StringBuilder();
			r.append("(");
			for (int i = 0; i < subfilters.length; i++) {
				if (i > 0)
					r.append(" AND ");
				r.append(subfilters[i].toString());
			}
			r.append(")");
			return r.toString();
		}
	}
}
