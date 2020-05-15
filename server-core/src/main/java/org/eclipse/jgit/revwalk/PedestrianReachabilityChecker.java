/*
 * Copyright (C) 2019, Google LLC. and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0 which is available at
 * https://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */
package org.eclipse.jgit.revwalk;

import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import java.util.Optional;
import java.util.stream.Stream;

import org.eclipse.jgit.errors.IncorrectObjectTypeException;
import org.eclipse.jgit.errors.MissingObjectException;

/**
 * Checks the reachability walking the graph from the starters towards the
 * target.
 */
class PedestrianReachabilityChecker implements ReachabilityChecker {

	private final boolean topoSort;

	private final RevWalk walk;

	/**
	 * New instance of the reachability checker using a existing walk.
	 *
	 * @param topoSort
	 *            walk commits in topological order
	 * @param walk
	 *            RevWalk instance to reuse. Caller retains ownership.
	 */
	public PedestrianReachabilityChecker(boolean topoSort,
			RevWalk walk) {
		this.topoSort = topoSort;
		this.walk = walk;
	}

	@Override
	public Optional<RevCommit> areAllReachable(Collection<RevCommit> targets,
			Stream<RevCommit> starters)
					throws MissingObjectException, IncorrectObjectTypeException,
					IOException {
		walk.reset();
		if (topoSort) {
			walk.sort(RevSort.TOPO);
		}

		for (RevCommit target: targets) {
			walk.markStart(target);
		}

		Iterator<RevCommit> iterator = starters.iterator();
		while (iterator.hasNext()) {
			walk.markUninteresting(iterator.next());
		}

		return Optional.ofNullable(walk.next());
	}
}
