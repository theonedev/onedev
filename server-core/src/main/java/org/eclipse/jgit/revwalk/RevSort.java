/*
 * Copyright (C) 2008, Marek Zawirski <marek.zawirski@gmail.com>
 * Copyright (C) 2008, Shawn O. Pearce <spearce@spearce.org> and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0 which is available at
 * https://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package org.eclipse.jgit.revwalk;

/**
 * Sorting strategies supported by {@link org.eclipse.jgit.revwalk.RevWalk} and
 * {@link org.eclipse.jgit.revwalk.ObjectWalk}.
 */
public enum RevSort {
	/**
	 * No specific sorting is requested.
	 * <p>
	 * Applications should not rely upon the ordering produced by this strategy.
	 * Any ordering in the output is caused by low level implementation details
	 * and may change without notice.
	 */
	NONE,

	/**
	 * Sort by commit time, descending (newest first, oldest last).
	 * <p>
	 * This strategy can be combined with {@link #TOPO}.
	 */
	COMMIT_TIME_DESC,

	/**
	 * Topological sorting (all children before parents).
	 * <p>
	 * This strategy can be combined with {@link #COMMIT_TIME_DESC}.
	 */
	TOPO,

	/**
	 * Flip the output into the reverse ordering.
	 * <p>
	 * This strategy can be combined with the others described by this type as
	 * it is usually performed at the very end.
	 */
	REVERSE,

	/**
	 * Include {@link RevFlag#UNINTERESTING} boundary commits after all others.
	 * In {@link ObjectWalk}, objects associated with such commits (trees,
	 * blobs), and all other objects marked explicitly as UNINTERESTING are also
	 * included.
	 * <p>
	 * A boundary commit is a UNINTERESTING parent of an interesting commit that
	 * was previously output.
	 */
	BOUNDARY;
}
