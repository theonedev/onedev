/*
 * Copyright (C) 2009, Google Inc.
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

import java.io.IOException;

import org.eclipse.jgit.errors.IncorrectObjectTypeException;
import org.eclipse.jgit.errors.MissingObjectException;

/**
 * Delays commits to be at least {@link PendingGenerator#OVER_SCAN} late.
 * <p>
 * This helps to "fix up" weird corner cases resulting from clock skew, by
 * slowing down what we produce to the caller we get a better chance to ensure
 * PendingGenerator reached back far enough in the graph to correctly mark
 * commits {@link RevWalk#UNINTERESTING} if necessary.
 * <p>
 * This generator should appear before {@link FixUninterestingGenerator} if the
 * lower level {@link #pending} isn't already fully buffered.
 */
final class DelayRevQueue extends Generator {
	private static final int OVER_SCAN = PendingGenerator.OVER_SCAN;

	private final Generator pending;

	private final FIFORevQueue delay;

	private int size;

	DelayRevQueue(Generator g) {
		pending = g;
		delay = new FIFORevQueue();
	}

	@Override
	int outputType() {
		return pending.outputType();
	}

	@Override
	RevCommit next() throws MissingObjectException,
			IncorrectObjectTypeException, IOException {
		while (size < OVER_SCAN) {
			final RevCommit c = pending.next();
			if (c == null)
				break;
			delay.add(c);
			size++;
		}

		final RevCommit c = delay.next();
		if (c == null)
			return null;
		size--;
		return c;
	}
}
