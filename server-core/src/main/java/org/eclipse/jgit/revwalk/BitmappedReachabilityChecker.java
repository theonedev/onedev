/*
 * Copyright (C) 2019, Google LLC and others
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
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import org.eclipse.jgit.errors.IncorrectObjectTypeException;
import org.eclipse.jgit.errors.MissingObjectException;
import org.eclipse.jgit.lib.BitmapIndex;
import org.eclipse.jgit.lib.BitmapIndex.Bitmap;
import org.eclipse.jgit.lib.BitmapIndex.BitmapBuilder;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.revwalk.filter.RevFilter;

/**
 * Checks the reachability using bitmaps.
 */
class BitmappedReachabilityChecker implements ReachabilityChecker {

	private final RevWalk walk;

	/**
	 * @param walk
	 *            walk on the repository to get or create the bitmaps for the
	 *            commits. It must have bitmaps.
	 * @throws AssertionError
	 *             runtime exception if walk is over a repository without
	 *             bitmaps
	 * @throws IOException
	 *             if the index or the object reader cannot be opened.
	 */
	public BitmappedReachabilityChecker(RevWalk walk)
			throws IOException {
		this.walk = walk;
		if (walk.getObjectReader().getBitmapIndex() == null) {
			throw new AssertionError(
					"Trying to use bitmapped reachability check " //$NON-NLS-1$
							+ "on a repository without bitmaps"); //$NON-NLS-1$
		}
	}

	/**
	 * Check all targets are reachable from the starters.
	 * <p>
	 * In this implementation, it is recommended to put the most popular
	 * starters (e.g. refs/heads tips) at the beginning.
	 */
	@Override
	public Optional<RevCommit> areAllReachable(Collection<RevCommit> targets,
			Stream<RevCommit> starters) throws MissingObjectException,
			IncorrectObjectTypeException, IOException {

		List<RevCommit> remainingTargets = new ArrayList<>(targets);

		walk.reset();
		walk.sort(RevSort.TOPO);

		// Filter emits only commits that are unreachable from previously
		// visited commits. Internally it keeps a bitmap of everything
		// reachable so far, which we use to discard reachable targets.
		BitmapIndex repoBitmaps = walk.getObjectReader().getBitmapIndex();
		ReachedFilter reachedFilter = new ReachedFilter(repoBitmaps);
		walk.setRevFilter(reachedFilter);

		Iterator<RevCommit> startersIter = starters.iterator();
		while (startersIter.hasNext()) {
			walk.markStart(startersIter.next());
			while (walk.next() != null) {
				remainingTargets.removeIf(reachedFilter::isReachable);

				if (remainingTargets.isEmpty()) {
					return Optional.empty();
				}
			}
			walk.reset();
		}

		return Optional.of(remainingTargets.get(0));
	}

	/**
	 * This filter emits commits that were not bitmap-reachable from anything
	 * visited before. Or in other words, commits that add something (themselves
	 * or their bitmap) to the "reached" bitmap.
	 *
	 * Current progress can be queried via {@link #isReachable(RevCommit)}.
	 */
	private static class ReachedFilter extends RevFilter {

		private final BitmapIndex repoBitmaps;
		private final BitmapBuilder reached;

		/**
		 * Create a filter that emits only previously unreachable commits.
		 *
		 * @param repoBitmaps
		 *            bitmap index of the repo
		 */
		public ReachedFilter(BitmapIndex repoBitmaps) {
			this.repoBitmaps = repoBitmaps;
			this.reached = repoBitmaps.newBitmapBuilder();
		}

		/** {@inheritDoc} */
		@Override
		public final boolean include(RevWalk walker, RevCommit cmit) {
			Bitmap commitBitmap;

			if (reached.contains(cmit)) {
				// already seen or included
				dontFollow(cmit);
				return false;
			}

			if ((commitBitmap = repoBitmaps.getBitmap(cmit)) != null) {
				reached.or(commitBitmap);
				// Emit the commit because there are new contents in the bitmap
				// but don't follow parents (they are already in the bitmap)
				dontFollow(cmit);
				return true;
			}

			// No bitmaps, keep going
			reached.addObject(cmit, Constants.OBJ_COMMIT);
			return true;
		}

		private static final void dontFollow(RevCommit cmit) {
			for (RevCommit p : cmit.getParents()) {
				p.add(RevFlag.SEEN);
			}
		}

		/** {@inheritDoc} */
		@Override
		public final RevFilter clone() {
			throw new UnsupportedOperationException();
		}

		/** {@inheritDoc} */
		@Override
		public final boolean requiresCommitBody() {
			return false;
		}

		boolean isReachable(RevCommit commit) {
			return reached.contains(commit);
		}
	}
}
