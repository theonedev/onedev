/*
 * Copyright (C) 2023, Tencent.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0 which is available at
 * https://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package org.eclipse.jgit.revwalk;

import java.io.IOException;

import org.eclipse.jgit.errors.IncorrectObjectTypeException;
import org.eclipse.jgit.errors.MissingObjectException;
import org.eclipse.jgit.internal.storage.commitgraph.ChangedPathFilter;
import org.eclipse.jgit.internal.storage.commitgraph.CommitGraph;
import org.eclipse.jgit.lib.AnyObjectId;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.ObjectId;

/**
 * RevCommit parsed from
 * {@link org.eclipse.jgit.internal.storage.commitgraph.CommitGraph}.
 *
 * @since 6.5
 */
class RevCommitCG extends RevCommit {

	private final int graphPosition;

	private int generation = Constants.COMMIT_GENERATION_UNKNOWN;

	/**
	 * Create a new commit reference.
	 *
	 * @param id
	 *            object name for the commit.
	 * @param graphPosition
	 *            the position in the commit-graph of the object.
	 */
	protected RevCommitCG(AnyObjectId id, int graphPosition) {
		super(id);
		this.graphPosition = graphPosition;
	}

	@Override
	void parseCanonical(RevWalk walk, byte[] raw) throws IOException {
		if (walk.isRetainBody()) {
			buffer = raw;
		}
		parseInGraph(walk);
	}

	@Override
	void parseHeaders(RevWalk walk) throws MissingObjectException,
			IncorrectObjectTypeException, IOException {
		if (walk.isRetainBody()) {
			super.parseBody(walk); // This parses header and body
			return;
		}
		parseInGraph(walk);
	}

	private void parseInGraph(RevWalk walk) throws IOException {
		CommitGraph graph = walk.commitGraph();
		CommitGraph.CommitData data = graph.getCommitData(graphPosition);
		if (data == null) {
			// RevCommitCG was created because we got its graphPosition from
			// commit-graph. If now the commit-graph doesn't know about it,
			// something went wrong.
			throw new IllegalStateException();
		}
		if (!walk.shallowCommitsInitialized) {
			walk.initializeShallowCommits(this);
		}

		this.tree = walk.lookupTree(data.getTree());
		this.commitTime = (int) data.getCommitTime();
		this.generation = data.getGeneration();

		if (getParents() == null) {
			int[] pGraphList = data.getParents();
			if (pGraphList.length == 0) {
				this.parents = RevCommit.NO_PARENTS;
			} else {
				RevCommit[] pList = new RevCommit[pGraphList.length];
				for (int i = 0; i < pList.length; i++) {
					int graphPos = pGraphList[i];
					ObjectId objId = graph.getObjectId(graphPos);
					pList[i] = walk.lookupCommit(objId, graphPos);
				}
				this.parents = pList;
			}
		}
		flags |= PARSED;
	}

	@Override
	int getGeneration() {
		return generation;
	}

	/** {@inheritDoc} */
	@Override
	public ChangedPathFilter getChangedPathFilter(RevWalk rw) {
		return rw.commitGraph().getChangedPathFilter(graphPosition);
	}
}
