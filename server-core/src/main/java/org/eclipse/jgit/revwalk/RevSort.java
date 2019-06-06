/*
 * Copyright (C) 2008, Marek Zawirski <marek.zawirski@gmail.com>
 * Copyright (C) 2008, Shawn O. Pearce <spearce@spearce.org>
 * and other copyright owners as documented in the project's IP log.
 *
 * This program and the accompanying materials are made available
 * under the terms of the Eclipse Distribution License v1.0 which
 * accompanies this distribution, is reproduced below, and is
 * available at http://www.eclipse.org/org/documents/edl-v10.php
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or
 * without modification, are permitted provided that the following
 * conditions are met:
 *
 * - Redistributions of source code must retain the above copyright
 *   notice, this list of conditions and the following disclaimer.
 *
 * - Redistributions in binary form must reproduce the above
 *   copyright notice, this list of conditions and the following
 *   disclaimer in the documentation and/or other materials provided
 *   with the distribution.
 *
 * - Neither the name of the Eclipse Foundation, Inc. nor the
 *   names of its contributors may be used to endorse or promote
 *   products derived from this software without specific prior
 *   written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND
 * CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
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
