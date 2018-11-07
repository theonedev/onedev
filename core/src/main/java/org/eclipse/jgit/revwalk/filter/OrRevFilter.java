/*
 * Copyright (C) 2007, Robin Rosenberg <robin.rosenberg@dewire.com>
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
import java.util.Collection;

import org.eclipse.jgit.errors.IncorrectObjectTypeException;
import org.eclipse.jgit.errors.MissingObjectException;
import org.eclipse.jgit.internal.JGitText;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;

/**
 * Includes a commit if any subfilters include the same commit.
 * <p>
 * Classic shortcut behavior is used, so evaluation of the
 * {@link org.eclipse.jgit.revwalk.filter.RevFilter#include(RevWalk, RevCommit)}
 * method stops as soon as a true result is obtained. Applications can improve
 * filtering performance by placing faster filters that are more likely to
 * accept a result earlier in the list.
 */
public abstract class OrRevFilter extends RevFilter {
	/**
	 * Create a filter with two filters, one of which must match.
	 *
	 * @param a
	 *            first filter to test.
	 * @param b
	 *            second filter to test.
	 * @return a filter that must match at least one input filter.
	 */
	public static RevFilter create(RevFilter a, RevFilter b) {
		if (a == ALL || b == ALL)
			return ALL;
		return new Binary(a, b);
	}

	/**
	 * Create a filter around many filters, one of which must match.
	 *
	 * @param list
	 *            list of filters to match against. Must contain at least 2
	 *            filters.
	 * @return a filter that must match at least one input filter.
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
	 * Create a filter around many filters, one of which must match.
	 *
	 * @param list
	 *            list of filters to match against. Must contain at least 2
	 *            filters.
	 * @return a filter that must match at least one input filter.
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

	private static class Binary extends OrRevFilter {
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
			return a.include(walker, c) || b.include(walker, c);
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
			return "(" + a.toString() + " OR " + b.toString() + ")";
		}
	}

	private static class List extends OrRevFilter {
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
				if (f.include(walker, c))
					return true;
			}
			return false;
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

		@Override
		public String toString() {
			final StringBuilder r = new StringBuilder();
			r.append("("); //$NON-NLS-1$
			for (int i = 0; i < subfilters.length; i++) {
				if (i > 0)
					r.append(" OR "); //$NON-NLS-1$
				r.append(subfilters[i].toString());
			}
			r.append(")"); //$NON-NLS-1$
			return r.toString();
		}
	}
}
