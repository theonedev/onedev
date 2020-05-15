/*
 * Copyright (C) 2012, Research In Motion Limited
 * Copyright (C) 2012, Christian Halstrick <christian.halstrick@sap.com> and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0 which is available at
 * https://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

/*
 * Contributors:
 *    George Young - initial API and implementation
 *    Christian Halstrick - initial API and implementation
 */
package org.eclipse.jgit.merge;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import org.eclipse.jgit.dircache.DirCache;
import org.eclipse.jgit.errors.IncorrectObjectTypeException;
import org.eclipse.jgit.errors.NoMergeBaseException;
import org.eclipse.jgit.internal.JGitText;
import org.eclipse.jgit.lib.CommitBuilder;
import org.eclipse.jgit.lib.Config;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectInserter;
import org.eclipse.jgit.lib.PersonIdent;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.filter.RevFilter;
import org.eclipse.jgit.treewalk.AbstractTreeIterator;
import org.eclipse.jgit.treewalk.EmptyTreeIterator;
import org.eclipse.jgit.treewalk.WorkingTreeIterator;

/**
 * A three-way merger performing a content-merge if necessary across multiple
 * bases using recursion
 *
 * This merger extends the resolve merger and does several things differently:
 *
 * - allow more than one merge base, up to a maximum
 *
 * - uses "Lists" instead of Arrays for chained types
 *
 * - recursively merges the merge bases together to compute a usable base
 *
 * @since 3.0
 */
public class RecursiveMerger extends ResolveMerger {
	/**
	 * The maximum number of merge bases. This merge will stop when the number
	 * of merge bases exceeds this value
	 */
	public final int MAX_BASES = 200;

	/**
	 * Normal recursive merge when you want a choice of DirCache placement
	 * inCore
	 *
	 * @param local
	 *            a {@link org.eclipse.jgit.lib.Repository} object.
	 * @param inCore
	 *            a boolean.
	 */
	protected RecursiveMerger(Repository local, boolean inCore) {
		super(local, inCore);
	}

	/**
	 * Normal recursive merge, implies not inCore
	 *
	 * @param local a {@link org.eclipse.jgit.lib.Repository} object.
	 */
	protected RecursiveMerger(Repository local) {
		this(local, false);
	}

	/**
	 * Normal recursive merge, implies inCore.
	 *
	 * @param inserter
	 *            an {@link org.eclipse.jgit.lib.ObjectInserter} object.
	 * @param config
	 *            the repository configuration
	 * @since 4.8
	 */
	protected RecursiveMerger(ObjectInserter inserter, Config config) {
		super(inserter, config);
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * Get a single base commit for two given commits. If the two source commits
	 * have more than one base commit recursively merge the base commits
	 * together until you end up with a single base commit.
	 */
	@Override
	protected RevCommit getBaseCommit(RevCommit a, RevCommit b)
			throws IncorrectObjectTypeException, IOException {
		return getBaseCommit(a, b, 0);
	}

	/**
	 * Get a single base commit for two given commits. If the two source commits
	 * have more than one base commit recursively merge the base commits
	 * together until a virtual common base commit has been found.
	 *
	 * @param a
	 *            the first commit to be merged
	 * @param b
	 *            the second commit to be merged
	 * @param callDepth
	 *            the callDepth when this method is called recursively
	 * @return the merge base of two commits. If a criss-cross merge required a
	 *         synthetic merge base this commit is visible only the merger's
	 *         RevWalk and will not be in the repository.
	 * @throws java.io.IOException
	 * @throws IncorrectObjectTypeException
	 *             one of the input objects is not a commit.
	 * @throws NoMergeBaseException
	 *             too many merge bases are found or the computation of a common
	 *             merge base failed (e.g. because of a conflict).
	 */
	protected RevCommit getBaseCommit(RevCommit a, RevCommit b, int callDepth)
			throws IOException {
		ArrayList<RevCommit> baseCommits = new ArrayList<>();
		walk.reset();
		walk.setRevFilter(RevFilter.MERGE_BASE);
		walk.markStart(a);
		walk.markStart(b);
		RevCommit c;
		while ((c = walk.next()) != null)
			baseCommits.add(c);

		if (baseCommits.isEmpty())
			return null;
		if (baseCommits.size() == 1)
			return baseCommits.get(0);
		if (baseCommits.size() >= MAX_BASES)
			throw new NoMergeBaseException(NoMergeBaseException.MergeBaseFailureReason.TOO_MANY_MERGE_BASES, MessageFormat.format(
					JGitText.get().mergeRecursiveTooManyMergeBasesFor,
					Integer.valueOf(MAX_BASES), a.name(), b.name(),
							Integer.valueOf(baseCommits.size())));

		// We know we have more than one base commit. We have to do merges now
		// to determine a single base commit. We don't want to spoil the current
		// dircache and working tree with the results of this intermediate
		// merges. Therefore set the dircache to a new in-memory dircache and
		// disable that we update the working-tree. We set this back to the
		// original values once a single base commit is created.
		RevCommit currentBase = baseCommits.get(0);
		DirCache oldDircache = dircache;
		boolean oldIncore = inCore;
		WorkingTreeIterator oldWTreeIt = workingTreeIterator;
		workingTreeIterator = null;
		try {
			dircache = DirCache.read(reader, currentBase.getTree());
			inCore = true;

			List<RevCommit> parents = new ArrayList<>();
			parents.add(currentBase);
			for (int commitIdx = 1; commitIdx < baseCommits.size(); commitIdx++) {
				RevCommit nextBase = baseCommits.get(commitIdx);
				if (commitIdx >= MAX_BASES)
					throw new NoMergeBaseException(
							NoMergeBaseException.MergeBaseFailureReason.TOO_MANY_MERGE_BASES,
							MessageFormat.format(
							JGitText.get().mergeRecursiveTooManyMergeBasesFor,
							Integer.valueOf(MAX_BASES), a.name(), b.name(),
									Integer.valueOf(baseCommits.size())));
				parents.add(nextBase);
				RevCommit bc = getBaseCommit(currentBase, nextBase,
						callDepth + 1);
				AbstractTreeIterator bcTree = (bc == null) ? new EmptyTreeIterator()
						: openTree(bc.getTree());
				if (mergeTrees(bcTree, currentBase.getTree(),
						nextBase.getTree(), true))
					currentBase = createCommitForTree(resultTree, parents);
				else
					throw new NoMergeBaseException(
							NoMergeBaseException.MergeBaseFailureReason.CONFLICTS_DURING_MERGE_BASE_CALCULATION,
							MessageFormat.format(
									JGitText.get().mergeRecursiveConflictsWhenMergingCommonAncestors,
									currentBase.getName(), nextBase.getName()));
			}
		} finally {
			inCore = oldIncore;
			dircache = oldDircache;
			workingTreeIterator = oldWTreeIt;
			toBeCheckedOut.clear();
			toBeDeleted.clear();
			modifiedFiles.clear();
			unmergedPaths.clear();
			mergeResults.clear();
			failingPaths.clear();
		}
		return currentBase;
	}

	/**
	 * Create a new commit by explicitly specifying the content tree and the
	 * parents. The commit message is not set and author/committer are set to
	 * the current user.
	 *
	 * @param tree
	 *            the tree this commit should capture
	 * @param parents
	 *            the list of parent commits
	 * @return a new commit visible only within this merger's RevWalk.
	 * @throws IOException
	 */
	private RevCommit createCommitForTree(ObjectId tree, List<RevCommit> parents)
			throws IOException {
		CommitBuilder c = new CommitBuilder();
		c.setTreeId(tree);
		c.setParentIds(parents);
		c.setAuthor(mockAuthor(parents));
		c.setCommitter(c.getAuthor());
		return RevCommit.parse(walk, c.build());
	}

	private static PersonIdent mockAuthor(List<RevCommit> parents) {
		String name = RecursiveMerger.class.getSimpleName();
		int time = 0;
		for (RevCommit p : parents)
			time = Math.max(time, p.getCommitTime());
		return new PersonIdent(
				name, name + "@JGit", //$NON-NLS-1$
				new Date((time + 1) * 1000L),
				TimeZone.getTimeZone("GMT+0000")); //$NON-NLS-1$
	}
}
