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

class BoundaryGenerator extends Generator {
	static final int UNINTERESTING = RevWalk.UNINTERESTING;

	Generator g;

	BoundaryGenerator(RevWalk w, Generator s) {
		g = new InitialGenerator(w, s);
	}

	@Override
	int outputType() {
		return g.outputType() | HAS_UNINTERESTING;
	}

	@Override
	void shareFreeList(BlockRevQueue q) {
		g.shareFreeList(q);
	}

	@Override
	RevCommit next() throws MissingObjectException,
			IncorrectObjectTypeException, IOException {
		return g.next();
	}

	private class InitialGenerator extends Generator {
		private static final int PARSED = RevWalk.PARSED;

		private static final int DUPLICATE = RevWalk.TEMP_MARK;

		private final RevWalk walk;

		private final FIFORevQueue held;

		private final Generator source;

		InitialGenerator(RevWalk w, Generator s) {
			walk = w;
			held = new FIFORevQueue();
			source = s;
			source.shareFreeList(held);
		}

		@Override
		int outputType() {
			return source.outputType();
		}

		@Override
		void shareFreeList(BlockRevQueue q) {
			q.shareFreeList(held);
		}

		@Override
		RevCommit next() throws MissingObjectException,
				IncorrectObjectTypeException, IOException {
			RevCommit c = source.next();
			if (c != null) {
				for (RevCommit p : c.parents)
					if ((p.flags & UNINTERESTING) != 0)
						held.add(p);
				return c;
			}

			final FIFORevQueue boundary = new FIFORevQueue();
			boundary.shareFreeList(held);
			for (;;) {
				c = held.next();
				if (c == null)
					break;
				if ((c.flags & DUPLICATE) != 0)
					continue;
				if ((c.flags & PARSED) == 0)
					c.parseHeaders(walk);
				c.flags |= DUPLICATE;
				boundary.add(c);
			}
			boundary.removeFlag(DUPLICATE);
			g = boundary;
			return boundary.next();
		}
	}
}
