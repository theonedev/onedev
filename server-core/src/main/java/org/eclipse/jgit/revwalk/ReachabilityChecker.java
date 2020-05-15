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
import java.util.Optional;
import java.util.stream.Stream;

import org.eclipse.jgit.errors.IncorrectObjectTypeException;
import org.eclipse.jgit.errors.MissingObjectException;

/**
 * Check if a commit is reachable from a collection of starting commits.
 * <p>
 * Note that this checks the reachability of commits (and tags). Trees, blobs or
 * any other object will cause IncorrectObjectTypeException exceptions.
 *
 * @since 5.4
 */
public interface ReachabilityChecker {

	/**
	 * Check if all targets are reachable from the {@code starter} commits.
	 * <p>
	 * Caller should parse the objectIds (preferably with
	 * {@code walk.parseCommit()} and handle missing/incorrect type objects
	 * before calling this method.
	 *
	 * @param targets
	 *            commits to reach.
	 * @param starters
	 *            known starting points.
	 * @return An unreachable target if at least one of the targets is
	 *         unreachable. An empty optional if all targets are reachable from
	 *         the starters.
	 *
	 * @throws MissingObjectException
	 *             if any of the incoming objects doesn't exist in the
	 *             repository.
	 * @throws IncorrectObjectTypeException
	 *             if any of the incoming objects is not a commit or a tag.
	 * @throws IOException
	 *             if any of the underlying indexes or readers can not be
	 *             opened.
	 *
	 * @deprecated see {{@link #areAllReachable(Collection, Stream)}
	 */
	@Deprecated
	default Optional<RevCommit> areAllReachable(Collection<RevCommit> targets,
                       Collection<RevCommit> starters) throws MissingObjectException,
			IncorrectObjectTypeException, IOException {
		return areAllReachable(targets, starters.stream());
	}

	/**
	 * Check if all targets are reachable from the {@code starter} commits.
	 * <p>
	 * Caller should parse the objectIds (preferably with
	 * {@code walk.parseCommit()} and handle missing/incorrect type objects
	 * before calling this method.
	 *
	 * @param targets
	 *            commits to reach.
	 * @param starters
	 *            known starting points.
	 * @return An unreachable target if at least one of the targets is
	 *         unreachable. An empty optional if all targets are reachable from
	 *         the starters.
	 *
	 * @throws MissingObjectException
	 *             if any of the incoming objects doesn't exist in the
	 *             repository.
	 * @throws IncorrectObjectTypeException
	 *             if any of the incoming objects is not a commit or a tag.
	 * @throws IOException
	 *             if any of the underlying indexes or readers can not be
	 *             opened.
	 * @since 5.6
	 */
	Optional<RevCommit> areAllReachable(Collection<RevCommit> targets,
			Stream<RevCommit> starters)
			throws MissingObjectException, IncorrectObjectTypeException,
			IOException;
}
