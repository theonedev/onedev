/*
 * Copyright (C) 2009, Google Inc.
 * Copyright (C) 2012, Research In Motion Limited and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0 which is available at
 * https://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package org.eclipse.jgit.merge;

import java.io.IOException;

import org.eclipse.jgit.errors.IncorrectObjectTypeException;
import org.eclipse.jgit.errors.MissingObjectException;
import org.eclipse.jgit.lib.AnyObjectId;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectInserter;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.treewalk.AbstractTreeIterator;
import org.eclipse.jgit.treewalk.EmptyTreeIterator;

/**
 * A merge of 2 trees, using a common base ancestor tree.
 */
public abstract class ThreeWayMerger extends Merger {
	private RevTree baseTree;

	private ObjectId baseCommitId;

	/**
	 * Create a new merge instance for a repository.
	 *
	 * @param local
	 *            the repository this merger will read and write data on.
	 */
	protected ThreeWayMerger(Repository local) {
		super(local);
	}

	/**
	 * Create a new merge instance for a repository.
	 *
	 * @param local
	 *            the repository this merger will read and write data on.
	 * @param inCore
	 *            perform the merge in core with no working folder involved
	 */
	protected ThreeWayMerger(Repository local, boolean inCore) {
		this(local);
	}

	/**
	 * Create a new in-core merge instance from an inserter.
	 *
	 * @param inserter
	 *            the inserter to write objects to.
	 * @since 4.8
	 */
	protected ThreeWayMerger(ObjectInserter inserter) {
		super(inserter);
	}

	/**
	 * Set the common ancestor tree.
	 *
	 * @param id
	 *            common base treeish; null to automatically compute the common
	 *            base from the input commits during
	 *            {@link #merge(AnyObjectId...)}.
	 * @throws org.eclipse.jgit.errors.IncorrectObjectTypeException
	 *             the object is not a treeish.
	 * @throws org.eclipse.jgit.errors.MissingObjectException
	 *             the object does not exist.
	 * @throws java.io.IOException
	 *             the object could not be read.
	 */
	public void setBase(AnyObjectId id) throws MissingObjectException,
			IncorrectObjectTypeException, IOException {
		if (id != null) {
			baseTree = walk.parseTree(id);
		} else {
			baseTree = null;
		}
	}

	/** {@inheritDoc} */
	@Override
	public boolean merge(AnyObjectId... tips) throws IOException {
		if (tips.length != 2)
			return false;
		return super.merge(tips);
	}

	/** {@inheritDoc} */
	@Override
	public ObjectId getBaseCommitId() {
		return baseCommitId;
	}

	/**
	 * Create an iterator to walk the merge base.
	 *
	 * @return an iterator over the caller-specified merge base, or the natural
	 *         merge base of the two input commits.
	 * @throws java.io.IOException
	 */
	protected AbstractTreeIterator mergeBase() throws IOException {
		if (baseTree != null) {
			return openTree(baseTree);
		}
		RevCommit baseCommit = (baseCommitId != null) ? walk
				.parseCommit(baseCommitId) : getBaseCommit(sourceCommits[0],
				sourceCommits[1]);
		if (baseCommit == null) {
			baseCommitId = null;
			return new EmptyTreeIterator();
		}
		baseCommitId = baseCommit.toObjectId();
		return openTree(baseCommit.getTree());
	}
}
