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
import java.util.List;

import org.eclipse.jgit.diff.DiffConfig;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffEntry.ChangeType;
import org.eclipse.jgit.diff.RenameDetector;
import org.eclipse.jgit.errors.CorruptObjectException;
import org.eclipse.jgit.errors.IncorrectObjectTypeException;
import org.eclipse.jgit.errors.MissingObjectException;
import org.eclipse.jgit.errors.StopWalkException;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.revwalk.filter.RevFilter;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.eclipse.jgit.treewalk.filter.TreeFilter;

/**
 * Filter applying a {@link org.eclipse.jgit.treewalk.filter.TreeFilter} against
 * changed paths in each commit.
 * <p>
 * Each commit is differenced concurrently against all of its parents to look
 * for tree entries that are interesting to the
 * {@link org.eclipse.jgit.treewalk.filter.TreeFilter}.
 *
 * @since 3.5
 */
public class TreeRevFilter extends RevFilter {
	private static final int PARSED = RevWalk.PARSED;

	private static final int UNINTERESTING = RevWalk.UNINTERESTING;

	private final int rewriteFlag;
	private final TreeWalk pathFilter;

	/**
	 * Create a {@link org.eclipse.jgit.revwalk.filter.RevFilter} from a
	 * {@link org.eclipse.jgit.treewalk.filter.TreeFilter}.
	 *
	 * @param walker
	 *            walker used for reading trees.
	 * @param t
	 *            filter to compare against any changed paths in each commit. If
	 *            a {@link org.eclipse.jgit.revwalk.FollowFilter}, will be
	 *            replaced with a new filter following new paths after a rename.
	 * @since 3.5
	 */
	public TreeRevFilter(RevWalk walker, TreeFilter t) {
		this(walker, t, 0);
	}


	/**
	 * Create a filter for the first phase of a parent-rewriting limited revision
	 * walk.
	 * <p>
	 * This filter is ANDed to evaluate before all other filters and ties the
	 * configured {@link TreeFilter} into the revision walking process.
	 * <p>
	 * If no interesting tree entries are found the commit is colored with
	 * {@code rewriteFlag}, allowing a later pass implemented by
	 * {@link RewriteGenerator} to remove those colored commits from the DAG.
	 *
	 * @see RewriteGenerator
	 *
	 * @param walker
	 *            walker used for reading trees.
	 * @param t
	 *            filter to compare against any changed paths in each commit. If a
	 *            {@link FollowFilter}, will be replaced with a new filter
	 *            following new paths after a rename.
	 * @param rewriteFlag
	 *            flag to color commits to be removed from the simplified DAT.
	 */
	TreeRevFilter(RevWalk walker, TreeFilter t, int rewriteFlag) {
		pathFilter = new TreeWalk(walker.reader);
		pathFilter.setFilter(t);
		pathFilter.setRecursive(t.shouldBeRecursive());
		this.rewriteFlag = rewriteFlag;
	}

	/** {@inheritDoc} */
	@Override
	public RevFilter clone() {
		throw new UnsupportedOperationException();
	}

	/** {@inheritDoc} */
	@Override
	public boolean include(RevWalk walker, RevCommit c)
			throws StopWalkException, MissingObjectException,
			IncorrectObjectTypeException, IOException {
		// Reset the tree filter to scan this commit and parents.
		//
		RevCommit[] pList = c.parents;
		int nParents = pList.length;
		TreeWalk tw = pathFilter;
		ObjectId[] trees = new ObjectId[nParents + 1];
		for (int i = 0; i < nParents; i++) {
			RevCommit p = c.parents[i];
			if ((p.flags & PARSED) == 0) {
				p.parseHeaders(walker);
			}
			trees[i] = p.getTree();
		}
		trees[nParents] = c.getTree();
		tw.reset(trees);

		if (nParents == 1) {
			// We have exactly one parent. This is a very common case.
			//
			int chgs = 0, adds = 0;
			while (tw.next()) {
				chgs++;
				if (tw.getRawMode(0) == 0 && tw.getRawMode(1) != 0) {
					adds++;
				} else {
					break; // no point in looking at this further.
				}
			}

			if (chgs == 0) {
				// No changes, so our tree is effectively the same as
				// our parent tree. We pass the buck to our parent.
				//
				c.flags |= rewriteFlag;
				return false;
			}

			// We have interesting items, but neither of the special
			// cases denoted above.
			//
			if (adds > 0 && tw.getFilter() instanceof FollowFilter) {
				// One of the paths we care about was added in this
				// commit. We need to update our filter to its older
				// name, if we can discover it. Find out what that is.
				//
				updateFollowFilter(trees, ((FollowFilter) tw.getFilter()).cfg);
			}
			return true;
		} else if (nParents == 0) {
			// We have no parents to compare against. Consider us to be
			// REWRITE only if we have no paths matching our filter.
			//
			if (tw.next()) {
				return true;
			}
			c.flags |= rewriteFlag;
			return false;
		}

		// We are a merge commit. We can only be REWRITE if we are same
		// to _all_ parents. We may also be able to eliminate a parent if
		// it does not contribute changes to us. Such a parent may be an
		// uninteresting side branch.
		//
		int[] chgs = new int[nParents];
		int[] adds = new int[nParents];
		while (tw.next()) {
			int myMode = tw.getRawMode(nParents);
			for (int i = 0; i < nParents; i++) {
				int pMode = tw.getRawMode(i);
				if (myMode == pMode && tw.idEqual(i, nParents)) {
					continue;
				}
				chgs[i]++;
				if (pMode == 0 && myMode != 0) {
					adds[i]++;
				}
			}
		}

		boolean same = false;
		boolean diff = false;
		for (int i = 0; i < nParents; i++) {
			if (chgs[i] == 0) {
				// No changes, so our tree is effectively the same as
				// this parent tree. We pass the buck to only this one
				// parent commit.
				//

				RevCommit p = pList[i];
				if ((p.flags & UNINTERESTING) != 0) {
					// This parent was marked as not interesting by the
					// application. We should look for another parent
					// that is interesting.
					//
					same = true;
					continue;
				}

				c.flags |= rewriteFlag;
				c.parents = new RevCommit[] { p };
				return false;
			}

			// Comment out below as this assumption is incorrect
			/*
			if (chgs[i] == adds[i]) {
				// All of the differences from this parent were because we
				// added files that they did not have. This parent is our
				// "empty tree root" and thus their history is not relevant.
				// Cut our grandparents to be an empty list.
				//
				pList[i].parents = RevCommit.NO_PARENTS;
			}
			*/
			
			// We have an interesting difference relative to this parent.
			//
			diff = true;
		}

		if (diff && !same) {
			// We did not abort above, so we are different in at least one
			// way from all of our parents. We have to take the blame for
			// that difference.
			//
			return true;
		}

		// We are the same as all of our parents. We must keep them
		// as they are and allow those parents to flow into pending
		// for further scanning.
		//
		c.flags |= rewriteFlag;
		return false;
	}

	/** {@inheritDoc} */
	@Override
	public boolean requiresCommitBody() {
		return false;
	}

	private void updateFollowFilter(ObjectId[] trees, DiffConfig cfg)
			throws MissingObjectException, IncorrectObjectTypeException,
			CorruptObjectException, IOException {
		TreeWalk tw = pathFilter;
		FollowFilter oldFilter = (FollowFilter) tw.getFilter();
		tw.setFilter(TreeFilter.ANY_DIFF);
		tw.reset(trees);

		List<DiffEntry> files = DiffEntry.scan(tw);
		RenameDetector rd = new RenameDetector(tw.getObjectReader(), cfg);
		rd.addAll(files);
		files = rd.compute();

		TreeFilter newFilter = oldFilter;
		for (DiffEntry ent : files) {
			if (isRename(ent) && ent.getNewPath().equals(oldFilter.getPath())) {
				newFilter = FollowFilter.create(ent.getOldPath(), cfg);
				RenameCallback callback = oldFilter.getRenameCallback();
				if (callback != null) {
					callback.renamed(ent);
					// forward the callback to the new follow filter
					((FollowFilter) newFilter).setRenameCallback(callback);
				}
				break;
			}
		}
		tw.setFilter(newFilter);
	}

	private static boolean isRename(DiffEntry ent) {
		return ent.getChangeType() == ChangeType.RENAME
				|| ent.getChangeType() == ChangeType.COPY;
	}
}
