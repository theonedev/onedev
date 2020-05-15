/*
 * Copyright (C) 2009, Google Inc. and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0 which is available at
 * https://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package org.eclipse.jgit.merge;

import org.eclipse.jgit.lib.Repository;

/**
 * A merge strategy to merge 2 trees, using a common base ancestor tree.
 */
public abstract class ThreeWayMergeStrategy extends MergeStrategy {
	/** {@inheritDoc} */
	@Override
	public abstract ThreeWayMerger newMerger(Repository db);

	/** {@inheritDoc} */
	@Override
	public abstract ThreeWayMerger newMerger(Repository db, boolean inCore);
}
