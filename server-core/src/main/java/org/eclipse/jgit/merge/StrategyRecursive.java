/*
 * Copyright (C) 2012, Research In Motion Limited and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0 which is available at
 * https://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package org.eclipse.jgit.merge;

import org.eclipse.jgit.lib.Config;
import org.eclipse.jgit.lib.ObjectInserter;
import org.eclipse.jgit.lib.Repository;

/**
 * A three-way merge strategy performing a content-merge if necessary
 *
 * @since 3.0
 */
public class StrategyRecursive extends StrategyResolve {

	/** {@inheritDoc} */
	@Override
	public ThreeWayMerger newMerger(Repository db) {
		return new RecursiveMerger(db, false);
	}

	/** {@inheritDoc} */
	@Override
	public ThreeWayMerger newMerger(Repository db, boolean inCore) {
		return new RecursiveMerger(db, inCore);
	}

	/** {@inheritDoc} */
	@Override
	public ThreeWayMerger newMerger(ObjectInserter inserter, Config config) {
		return new RecursiveMerger(inserter, config);
	}

	/** {@inheritDoc} */
	@Override
	public String getName() {
		return "recursive"; //$NON-NLS-1$
	}
}
