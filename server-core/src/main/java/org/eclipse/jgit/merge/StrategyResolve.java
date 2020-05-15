/*
 * Copyright (C) 2010, Christian Halstrick <christian.halstrick@sap.com>,
 * Copyright (C) 2010, Matthias Sohn <matthias.sohn@sap.com> and others
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
 */
public class StrategyResolve extends ThreeWayMergeStrategy {

	/** {@inheritDoc} */
	@Override
	public ThreeWayMerger newMerger(Repository db) {
		return new ResolveMerger(db, false);
	}

	/** {@inheritDoc} */
	@Override
	public ThreeWayMerger newMerger(Repository db, boolean inCore) {
		return new ResolveMerger(db, inCore);
	}

	/** {@inheritDoc} */
	@Override
	public ThreeWayMerger newMerger(ObjectInserter inserter, Config config) {
		return new ResolveMerger(inserter, config);
	}

	/** {@inheritDoc} */
	@Override
	public String getName() {
		return "resolve"; //$NON-NLS-1$
	}
}
