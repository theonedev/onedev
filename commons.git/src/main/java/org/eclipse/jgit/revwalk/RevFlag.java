/*
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

import java.text.MessageFormat;

import org.eclipse.jgit.internal.JGitText;

/**
 * Application level mark bit for {@link RevObject}s.
 * <p>
 * To create a flag use {@link RevWalk#newFlag(String)}.
 */
public class RevFlag {
	/**
	 * Uninteresting by {@link RevWalk#markUninteresting(RevCommit)}.
	 * <p>
	 * We flag commits as uninteresting if the caller does not want commits
	 * reachable from a commit to {@link RevWalk#markUninteresting(RevCommit)}.
	 * This flag is always carried into the commit's parents and is a key part
	 * of the "rev-list B --not A" feature; A is marked UNINTERESTING.
	 * <p>
	 * This is a static flag. Its RevWalk is not available.
	 */
	public static final RevFlag UNINTERESTING = new StaticRevFlag(
			"UNINTERESTING", RevWalk.UNINTERESTING); //$NON-NLS-1$

	/**
	 * Set on RevCommit instances added to {@link RevWalk#pending} queue.
	 * <p>
	 * We use this flag to avoid adding the same commit instance twice to our
	 * queue, especially if we reached it by more than one path.
	 * <p>
	 * This is a static flag. Its RevWalk is not available.
	 *
	 * @since 3.0
	 */
	public static final RevFlag SEEN = new StaticRevFlag("SEEN", RevWalk.SEEN); //$NON-NLS-1$

	final RevWalk walker;

	final String name;

	final int mask;

	RevFlag(final RevWalk w, final String n, final int m) {
		walker = w;
		name = n;
		mask = m;
	}

	/**
	 * Get the revision walk instance this flag was created from.
	 *
	 * @return the walker this flag was allocated out of, and belongs to.
	 */
	public RevWalk getRevWalk() {
		return walker;
	}

	public String toString() {
		return name;
	}

	static class StaticRevFlag extends RevFlag {
		StaticRevFlag(final String n, final int m) {
			super(null, n, m);
		}

		@Override
		public RevWalk getRevWalk() {
			throw new UnsupportedOperationException(MessageFormat.format(
					JGitText.get().isAStaticFlagAndHasNorevWalkInstance, toString()));
		}
	}
}
