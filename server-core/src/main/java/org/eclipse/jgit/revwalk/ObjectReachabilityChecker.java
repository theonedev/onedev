/*
 * Copyright (C) 2020, Google LLC and others
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

/**
 * Checks if all objects are reachable from certain starting points.
 *
 * This is an expensive check that browses commits, trees, blobs and tags. For
 * reachability just between commits see {@link ReachabilityChecker}
 * implementations.
 *
 * @since 5.8
 */
public interface ObjectReachabilityChecker {

	/**
	 * Checks that all targets are reachable from the starters.
	 *
	 * @implSpec Missing or invalid objects are reported as illegal state.
	 *           Caller should have found them while translating ObjectIds into
	 *           RevObjects. They can only happen here if the caller is mixing
	 *           revwalks.
	 *
	 * @param targets
	 *            objects to check for reachability from the starters
	 * @param starters
	 *            objects known to be reachable to the caller
	 * @return Optional a single unreachable target if there are any (there
	 *         could be more). Empty optional means all targets are reachable.
	 * @throws IOException
	 *             Cannot access underlying storage
	 */
	Optional<RevObject> areAllReachable(Collection<RevObject> targets,
			Stream<RevObject> starters) throws IOException;

}