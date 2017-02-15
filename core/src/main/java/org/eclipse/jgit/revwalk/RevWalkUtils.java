/*
 * Copyright (C) 2011-2012, Robin Stocker <robin@nibor.org>
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
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.jgit.errors.IncorrectObjectTypeException;
import org.eclipse.jgit.errors.MissingObjectException;
import org.eclipse.jgit.lib.Ref;

/**
 * Utility methods for {@link RevWalk}.
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
	 * Note that this method calls {@link RevWalk#reset()} at the beginning.
	 * Also note that the existing rev filter on the walk is left as-is, so be
	 * sure to set the right rev filter before calling this method.
	 *
	 * @param walk
	 *            the rev walk to use
	 * @param start
	 *            the commit to start counting from
	 * @param end
	 *            the commit where counting should end, or null if counting
	 *            should be done until there are no more commits
	 *
	 * @return the number of commits
	 * @throws MissingObjectException
	 * @throws IncorrectObjectTypeException
	 * @throws IOException
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
	 * Note that this method calls {@link RevWalk#reset()} at the beginning.
	 * Also note that the existing rev filter on the walk is left as-is, so be
	 * sure to set the right rev filter before calling this method.
	 *
	 * @param walk
	 *            the rev walk to use
	 * @param start
	 *            the commit to start counting from
	 * @param end
	 *            the commit where counting should end, or null if counting
	 *            should be done until there are no more commits
	 * @return the commits found
	 * @throws MissingObjectException
	 * @throws IncorrectObjectTypeException
	 * @throws IOException
	 */
	public static List<RevCommit> find(final RevWalk walk,
			final RevCommit start, final RevCommit end)
			throws MissingObjectException, IncorrectObjectTypeException,
			IOException {
		walk.reset();
		walk.markStart(start);
		if (end != null)
			walk.markUninteresting(end);

		List<RevCommit> commits = new ArrayList<RevCommit>();
		for (RevCommit c : walk)
			commits.add(c);
		return commits;
	}

	/**
	 * Find the list of branches a given commit is reachable from when following
	 * parent.s
	 * <p>
	 * Note that this method calls {@link RevWalk#reset()} at the beginning.
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
	 * @throws MissingObjectException
	 * @throws IncorrectObjectTypeException
	 * @throws IOException
	 */
	public static List<Ref> findBranchesReachableFrom(RevCommit commit,
			RevWalk revWalk, Collection<Ref> refs)
			throws MissingObjectException, IncorrectObjectTypeException,
			IOException {

		// Make sure commit is from the same RevWalk
		commit = revWalk.parseCommit(commit.getId());
		revWalk.reset();
		List<Ref> result = new ArrayList<Ref>();

		final int SKEW = 24*3600; // one day clock skew

		for (Ref ref : refs) {
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
		return result;
	}

}
