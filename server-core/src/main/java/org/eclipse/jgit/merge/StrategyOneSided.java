/*
 * Copyright (C) 2008-2009, Google Inc.
 * Copyright (C) 2009, Robin Rosenberg <robin.rosenberg@dewire.com> and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0 which is available at
 * https://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package org.eclipse.jgit.merge;

import java.io.IOException;

import org.eclipse.jgit.lib.Config;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectInserter;
import org.eclipse.jgit.lib.Repository;

/**
 * Trivial merge strategy to make the resulting tree exactly match an input.
 * <p>
 * This strategy can be used to cauterize an entire side branch of history, by
 * setting the output tree to one of the inputs, and ignoring any of the paths
 * of the other inputs.
 */
public class StrategyOneSided extends MergeStrategy {
	private final String strategyName;

	private final int treeIndex;

	/**
	 * Create a new merge strategy to select a specific input tree.
	 *
	 * @param name
	 *            name of this strategy.
	 * @param index
	 *            the position of the input tree to accept as the result.
	 */
	protected StrategyOneSided(String name, int index) {
		strategyName = name;
		treeIndex = index;
	}

	/** {@inheritDoc} */
	@Override
	public String getName() {
		return strategyName;
	}

	/** {@inheritDoc} */
	@Override
	public Merger newMerger(Repository db) {
		return new OneSide(db, treeIndex);
	}

	/** {@inheritDoc} */
	@Override
	public Merger newMerger(Repository db, boolean inCore) {
		return new OneSide(db, treeIndex);
	}

	/** {@inheritDoc} */
	@Override
	public Merger newMerger(ObjectInserter inserter, Config config) {
		return new OneSide(inserter, treeIndex);
	}

	static class OneSide extends Merger {
		private final int treeIndex;

		protected OneSide(Repository local, int index) {
			super(local);
			treeIndex = index;
		}

		protected OneSide(ObjectInserter inserter, int index) {
			super(inserter);
			treeIndex = index;
		}

		@Override
		protected boolean mergeImpl() throws IOException {
			return treeIndex < sourceTrees.length;
		}

		@Override
		public ObjectId getResultTreeId() {
			return sourceTrees[treeIndex];
		}

		@Override
		public ObjectId getBaseCommitId() {
			return null;
		}
	}
}
