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

import org.eclipse.jgit.errors.IncorrectObjectTypeException;
import org.eclipse.jgit.errors.MissingObjectException;
import org.eclipse.jgit.errors.StopWalkException;
import org.eclipse.jgit.internal.JGitText;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;

/**
 * Selects interesting revisions during walking.
 * <p>
 * This is an abstract interface. Applications may implement a subclass, or use
 * one of the predefined implementations already available within this package.
 * Filters may be chained together using <code>AndRevFilter</code> and
 * <code>OrRevFilter</code> to create complex boolean expressions.
 * <p>
 * Applications should install the filter on a RevWalk by
 * {@link org.eclipse.jgit.revwalk.RevWalk#setRevFilter(RevFilter)} prior to
 * starting traversal.
 * <p>
 * Unless specifically noted otherwise a RevFilter implementation is not thread
 * safe and may not be shared by different RevWalk instances at the same time.
 * This restriction allows RevFilter implementations to cache state within their
 * instances during {@link #include(RevWalk, RevCommit)} if it is beneficial to
 * their implementation. Deep clones created by {@link #clone()} may be used to
 * construct a thread-safe copy of an existing filter.
 *
 * <p>
 * <b>Message filters:</b>
 * <ul>
 * <li>Author name/email:
 * {@link org.eclipse.jgit.revwalk.filter.AuthorRevFilter}</li>
 * <li>Committer name/email:
 * {@link org.eclipse.jgit.revwalk.filter.CommitterRevFilter}</li>
 * <li>Message body:
 * {@link org.eclipse.jgit.revwalk.filter.MessageRevFilter}</li>
 * </ul>
 *
 * <p>
 * <b>Merge filters:</b>
 * <ul>
 * <li>Skip all merges: {@link #NO_MERGES}.</li>
 * <li>Skip all non-merges: {@link #ONLY_MERGES}</li>
 * </ul>
 *
 * <p>
 * <b>Boolean modifiers:</b>
 * <ul>
 * <li>AND: {@link org.eclipse.jgit.revwalk.filter.AndRevFilter}</li>
 * <li>OR: {@link org.eclipse.jgit.revwalk.filter.OrRevFilter}</li>
 * <li>NOT: {@link org.eclipse.jgit.revwalk.filter.NotRevFilter}</li>
 * </ul>
 */
public abstract class RevFilter {
	/** Default filter that always returns true (thread safe). */
	public static final RevFilter ALL = new AllFilter();

	private static final class AllFilter extends RevFilter {
		@Override
		public boolean include(RevWalk walker, RevCommit c) {
			return true;
		}

		@Override
		public RevFilter clone() {
			return this;
		}

		@Override
		public boolean requiresCommitBody() {
			return false;
		}

		@Override
		public String toString() {
			return "ALL"; //$NON-NLS-1$
		}
	}

	/** Default filter that always returns false (thread safe). */
	public static final RevFilter NONE = new NoneFilter();

	private static final class NoneFilter extends RevFilter {
		@Override
		public boolean include(RevWalk walker, RevCommit c) {
			return false;
		}

		@Override
		public RevFilter clone() {
			return this;
		}

		@Override
		public boolean requiresCommitBody() {
			return false;
		}

		@Override
		public String toString() {
			return "NONE"; //$NON-NLS-1$
		}
	}

	/**
	 * Filter including only merge commits, excluding all commits with less than
	 * two parents (thread safe).
	 *
	 * @since 4.4
	 */
	public static final RevFilter ONLY_MERGES = new OnlyMergesFilter();

	private static final class OnlyMergesFilter extends RevFilter {

		@Override
		public boolean include(RevWalk walker, RevCommit c) {
			return c.getParentCount() >= 2;
		}

		@Override
		public RevFilter clone() {
			return this;
		}

		@Override
		public boolean requiresCommitBody() {
			return false;
		}

		@Override
		public String toString() {
			return "ONLY_MERGES"; //$NON-NLS-1$
		}
	}

	/** Excludes commits with more than one parent (thread safe). */
	public static final RevFilter NO_MERGES = new NoMergesFilter();

	private static final class NoMergesFilter extends RevFilter {
		@Override
		public boolean include(RevWalk walker, RevCommit c) {
			return c.getParentCount() < 2;
		}

		@Override
		public RevFilter clone() {
			return this;
		}

		@Override
		public boolean requiresCommitBody() {
			return false;
		}

		@Override
		public String toString() {
			return "NO_MERGES"; //$NON-NLS-1$
		}
	}

	/**
	 * Selects only merge bases of the starting points (thread safe).
	 * <p>
	 * This is a special case filter that cannot be combined with any other
	 * filter. Its include method always throws an exception as context
	 * information beyond the arguments is necessary to determine if the
	 * supplied commit is a merge base.
	 */
	public static final RevFilter MERGE_BASE = new MergeBaseFilter();

	private static final class MergeBaseFilter extends RevFilter {
		@Override
		public boolean include(RevWalk walker, RevCommit c) {
			throw new UnsupportedOperationException(JGitText.get().cannotBeCombined);
		}

		@Override
		public RevFilter clone() {
			return this;
		}

		@Override
		public boolean requiresCommitBody() {
			return false;
		}

		@Override
		public String toString() {
			return "MERGE_BASE"; //$NON-NLS-1$
		}
	}

	/**
	 * Create a new filter that does the opposite of this filter.
	 *
	 * @return a new filter that includes commits this filter rejects.
	 */
	public RevFilter negate() {
		return NotRevFilter.create(this);
	}

	/**
	 * Whether the filter needs the commit body to be parsed.
	 *
	 * @return true if the filter needs the commit body to be parsed.
	 */
	public boolean requiresCommitBody() {
		// Assume true to be backward compatible with prior behavior.
		return true;
	}

	/**
	 * Determine if the supplied commit should be included in results.
	 *
	 * @param walker
	 *            the active walker this filter is being invoked from within.
	 * @param cmit
	 *            the commit currently being tested. The commit has been parsed
	 *            and its body is available for inspection only if the filter
	 *            returns true from {@link #requiresCommitBody()}.
	 * @return true to include this commit in the results; false to have this
	 *         commit be omitted entirely from the results.
	 * @throws org.eclipse.jgit.errors.StopWalkException
	 *             the filter knows for certain that no additional commits can
	 *             ever match, and the current commit doesn't match either. The
	 *             walk is halted and no more results are provided.
	 * @throws org.eclipse.jgit.errors.MissingObjectException
	 *             an object the filter needs to consult to determine its answer
	 *             does not exist in the Git repository the walker is operating
	 *             on. Filtering this commit is impossible without the object.
	 * @throws org.eclipse.jgit.errors.IncorrectObjectTypeException
	 *             an object the filter needed to consult was not of the
	 *             expected object type. This usually indicates a corrupt
	 *             repository, as an object link is referencing the wrong type.
	 * @throws java.io.IOException
	 *             a loose object or pack file could not be read to obtain data
	 *             necessary for the filter to make its decision.
	 */
	public abstract boolean include(RevWalk walker, RevCommit cmit)
			throws StopWalkException, MissingObjectException,
			IncorrectObjectTypeException, IOException;

	/**
	 * {@inheritDoc}
	 * <p>
	 * Clone this revision filter, including its parameters.
	 * <p>
	 * This is a deep clone. If this filter embeds objects or other filters it
	 * must also clone those, to ensure the instances do not share mutable data.
	 */
	@Override
	public abstract RevFilter clone();

	/** {@inheritDoc} */
	@Override
	public String toString() {
		String n = getClass().getName();
		int lastDot = n.lastIndexOf('.');
		if (lastDot >= 0) {
			n = n.substring(lastDot + 1);
		}
		return n.replace('$', '.');
	}
}
