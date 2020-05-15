/*
 * Copyright (C) 2011-2012, Robin Stocker <robin@nibor.org> and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0 which is available at
 * https://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package org.eclipse.jgit.revwalk;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.jgit.errors.IncorrectObjectTypeException;
import org.eclipse.jgit.errors.MissingObjectException;
import org.eclipse.jgit.internal.JGitText;
import org.eclipse.jgit.lib.NullProgressMonitor;
import org.eclipse.jgit.lib.ProgressMonitor;
import org.eclipse.jgit.lib.Ref;

/**
 * Utility methods for {@link org.eclipse.jgit.revwalk.RevWalk}.
 */
public final class RevWalkUtils {

	private RevWalkUtils() {
		// Utility class
	}

	/**
	 * Count the number of commits that are reachable from <code>start</code>
	 * until a commit that is reachable from <code>end</code> is encountered. In
	 * other words, count the number of commits that are in <code>start</code>,
	 * but not in <code>end</code>.
	 * <p>
	 * Note that this method calls
	 * {@link org.eclipse.jgit.revwalk.RevWalk#reset()} at the beginning. Also
	 * note that the existing rev filter on the walk is left as-is, so be sure
	 * to set the right rev filter before calling this method.
	 *
	 * @param walk
	 *            the rev walk to use
	 * @param start
	 *            the commit to start counting from
	 * @param end
	 *            the commit where counting should end, or null if counting
	 *            should be done until there are no more commits
	 * @return the number of commits
	 * @throws org.eclipse.jgit.errors.MissingObjectException
	 * @throws org.eclipse.jgit.errors.IncorrectObjectTypeException
	 * @throws java.io.IOException
	 */
	public static int count(final RevWalk walk, final RevCommit start,
			final RevCommit end) throws MissingObjectException,
			IncorrectObjectTypeException, IOException {
		return find(walk, start, end).size();
	}

	/**
	 * Find commits that are reachable from <code>start</code> until a commit
	 * that is reachable from <code>end</code> is encountered. In other words,
	 * Find of commits that are in <code>start</code>, but not in
	 * <code>end</code>.
	 * <p>
	 * Note that this method calls
	 * {@link org.eclipse.jgit.revwalk.RevWalk#reset()} at the beginning. Also
	 * note that the existing rev filter on the walk is left as-is, so be sure
	 * to set the right rev filter before calling this method.
	 *
	 * @param walk
	 *            the rev walk to use
	 * @param start
	 *            the commit to start counting from
	 * @param end
	 *            the commit where counting should end, or null if counting
	 *            should be done until there are no more commits
	 * @return the commits found
	 * @throws org.eclipse.jgit.errors.MissingObjectException
	 * @throws org.eclipse.jgit.errors.IncorrectObjectTypeException
	 * @throws java.io.IOException
	 */
	public static List<RevCommit> find(final RevWalk walk,
			final RevCommit start, final RevCommit end)
			throws MissingObjectException, IncorrectObjectTypeException,
			IOException {
		walk.reset();
		walk.markStart(start);
		if (end != null)
			walk.markUninteresting(end);

		List<RevCommit> commits = new ArrayList<>();
		for (RevCommit c : walk)
			commits.add(c);
		return commits;
	}

	/**
	 * Find the list of branches a given commit is reachable from when following
	 * parents.
	 * <p>
	 * Note that this method calls
	 * {@link org.eclipse.jgit.revwalk.RevWalk#reset()} at the beginning.
	 * <p>
	 * In order to improve performance this method assumes clock skew among
	 * committers is never larger than 24 hours.
	 *
	 * @param commit
	 *            the commit we are looking at
	 * @param revWalk
	 *            The RevWalk to be used.
	 * @param refs
	 *            the set of branches we want to see reachability from
	 * @return the list of branches a given commit is reachable from
	 * @throws org.eclipse.jgit.errors.MissingObjectException
	 * @throws org.eclipse.jgit.errors.IncorrectObjectTypeException
	 * @throws java.io.IOException
	 */
	public static List<Ref> findBranchesReachableFrom(RevCommit commit,
			RevWalk revWalk, Collection<Ref> refs)
			throws MissingObjectException, IncorrectObjectTypeException,
			IOException {
		return findBranchesReachableFrom(commit, revWalk, refs,
				NullProgressMonitor.INSTANCE);
	}

	/**
	 * Find the list of branches a given commit is reachable from when following
	 * parents.
	 * <p>
	 * Note that this method calls
	 * {@link org.eclipse.jgit.revwalk.RevWalk#reset()} at the beginning.
	 * <p>
	 * In order to improve performance this method assumes clock skew among
	 * committers is never larger than 24 hours.
	 *
	 * @param commit
	 *            the commit we are looking at
	 * @param revWalk
	 *            The RevWalk to be used.
	 * @param refs
	 *            the set of branches we want to see reachability from
	 * @param monitor
	 *            the callback for progress and cancellation
	 * @return the list of branches a given commit is reachable from
	 * @throws org.eclipse.jgit.errors.MissingObjectException
	 * @throws org.eclipse.jgit.errors.IncorrectObjectTypeException
	 * @throws java.io.IOException
	 * @since 5.4
	 */
	public static List<Ref> findBranchesReachableFrom(RevCommit commit,
			RevWalk revWalk, Collection<Ref> refs, ProgressMonitor monitor)
			throws MissingObjectException, IncorrectObjectTypeException,
			IOException {

		// Make sure commit is from the same RevWalk
		commit = revWalk.parseCommit(commit.getId());
		revWalk.reset();
		List<Ref> result = new ArrayList<>();
		monitor.beginTask(JGitText.get().searchForReachableBranches,
				refs.size());
		final int SKEW = 24*3600; // one day clock skew

		for (Ref ref : refs) {
			if (monitor.isCancelled())
				return result;
			monitor.update(1);
			RevObject maybehead = revWalk.parseAny(ref.getObjectId());
			if (!(maybehead instanceof RevCommit))
				continue;
			RevCommit headCommit = (RevCommit) maybehead;

			// if commit is in the ref branch, then the tip of ref should be
			// newer than the commit we are looking for. Allow for a large
			// clock skew.
			if (headCommit.getCommitTime() + SKEW < commit.getCommitTime())
				continue;

			if (revWalk.isMergedInto(commit, headCommit))
				result.add(ref);
		}
		monitor.endTask();
		return result;
	}

}
