/*
 * Copyright (C) 2008-2009, Google Inc. and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0 which is available at
 * https://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package org.eclipse.jgit.merge;

import java.io.IOException;

import org.eclipse.jgit.dircache.DirCache;
import org.eclipse.jgit.dircache.DirCacheBuilder;
import org.eclipse.jgit.dircache.DirCacheEntry;
import org.eclipse.jgit.errors.UnmergedPathException;
import org.eclipse.jgit.lib.Config;
import org.eclipse.jgit.lib.FileMode;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectInserter;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.treewalk.AbstractTreeIterator;
import org.eclipse.jgit.treewalk.NameConflictTreeWalk;

/**
 * Merges two commits together in-memory, ignoring any working directory.
 * <p>
 * The strategy chooses a path from one of the two input trees if the path is
 * unchanged in the other relative to their common merge base tree. This is a
 * trivial 3-way merge (at the file path level only).
 * <p>
 * Modifications of the same file path (content and/or file mode) by both input
 * trees will cause a merge conflict, as this strategy does not attempt to merge
 * file contents.
 */
public class StrategySimpleTwoWayInCore extends ThreeWayMergeStrategy {
	/**
	 * Create a new instance of the strategy.
	 */
	protected StrategySimpleTwoWayInCore() {
		//
	}

	/** {@inheritDoc} */
	@Override
	public String getName() {
		return "simple-two-way-in-core"; //$NON-NLS-1$
	}

	/** {@inheritDoc} */
	@Override
	public ThreeWayMerger newMerger(Repository db) {
		return new InCoreMerger(db);
	}

	/** {@inheritDoc} */
	@Override
	public ThreeWayMerger newMerger(Repository db, boolean inCore) {
		// This class is always inCore, so ignore the parameter
		return newMerger(db);
	}

	/** {@inheritDoc} */
	@Override
	public ThreeWayMerger newMerger(ObjectInserter inserter, Config config) {
		return new InCoreMerger(inserter);
	}

	private static class InCoreMerger extends ThreeWayMerger {
		private static final int T_BASE = 0;

		private static final int T_OURS = 1;

		private static final int T_THEIRS = 2;

		private final NameConflictTreeWalk tw;

		private final DirCache cache;

		private DirCacheBuilder builder;

		private ObjectId resultTree;

		InCoreMerger(Repository local) {
			super(local);
			tw = new NameConflictTreeWalk(local, reader);
			cache = DirCache.newInCore();
		}

		InCoreMerger(ObjectInserter inserter) {
			super(inserter);
			tw = new NameConflictTreeWalk(null, reader);
			cache = DirCache.newInCore();
		}

		@Override
		protected boolean mergeImpl() throws IOException {
			tw.addTree(mergeBase());
			tw.addTree(sourceTrees[0]);
			tw.addTree(sourceTrees[1]);

			boolean hasConflict = false;
			builder = cache.builder();
			while (tw.next()) {
				final int modeO = tw.getRawMode(T_OURS);
				final int modeT = tw.getRawMode(T_THEIRS);
				if (modeO == modeT && tw.idEqual(T_OURS, T_THEIRS)) {
					add(T_OURS, DirCacheEntry.STAGE_0);
					continue;
				}

				final int modeB = tw.getRawMode(T_BASE);
				if (modeB == modeO && tw.idEqual(T_BASE, T_OURS))
					add(T_THEIRS, DirCacheEntry.STAGE_0);
				else if (modeB == modeT && tw.idEqual(T_BASE, T_THEIRS))
					add(T_OURS, DirCacheEntry.STAGE_0);
				else {
					if (nonTree(modeB)) {
						add(T_BASE, DirCacheEntry.STAGE_1);
						hasConflict = true;
					}
					if (nonTree(modeO)) {
						add(T_OURS, DirCacheEntry.STAGE_2);
						hasConflict = true;
					}
					if (nonTree(modeT)) {
						add(T_THEIRS, DirCacheEntry.STAGE_3);
						hasConflict = true;
					}
					if (tw.isSubtree())
						tw.enterSubtree();
				}
			}
			builder.finish();
			builder = null;

			if (hasConflict)
				return false;
			try {
				ObjectInserter odi = getObjectInserter();
				resultTree = cache.writeTree(odi);
				odi.flush();
				return true;
			} catch (UnmergedPathException upe) {
				resultTree = null;
				return false;
			}
		}

		private static boolean nonTree(int mode) {
			return mode != 0 && !FileMode.TREE.equals(mode);
		}

		private void add(int tree, int stage) throws IOException {
			final AbstractTreeIterator i = getTree(tree);
			if (i != null) {
				if (FileMode.TREE.equals(tw.getRawMode(tree))) {
					builder.addTree(tw.getRawPath(), stage, reader, tw
							.getObjectId(tree));
				} else {
					final DirCacheEntry e;

					e = new DirCacheEntry(tw.getRawPath(), stage);
					e.setObjectIdFromRaw(i.idBuffer(), i.idOffset());
					e.setFileMode(tw.getFileMode(tree));
					builder.add(e);
				}
			}
		}

		private AbstractTreeIterator getTree(int tree) {
			return tw.getTree(tree, AbstractTreeIterator.class);
		}

		@Override
		public ObjectId getResultTreeId() {
			return resultTree;
		}
	}

}
