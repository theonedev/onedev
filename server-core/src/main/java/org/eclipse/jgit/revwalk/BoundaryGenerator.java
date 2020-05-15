/*
 * Copyright (C) 2009, Google Inc.
 * Copyright (C) 2008, Shawn O. Pearce <spearce@spearce.org> and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0 which is available at
 * https://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package org.eclipse.jgit.revwalk;

import java.io.IOException;

import org.eclipse.jgit.errors.IncorrectObjectTypeException;
import org.eclipse.jgit.errors.MissingObjectException;

class BoundaryGenerator extends Generator {
	static final int UNINTERESTING = RevWalk.UNINTERESTING;

	Generator g;

	BoundaryGenerator(RevWalk w, Generator s) {
		super(s.firstParent);
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
			super(s.firstParent);
			walk = w;
			held = new FIFORevQueue(firstParent);
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
				for (int i = 0; i < c.parents.length; i++) {
					if (firstParent && i > 0) {
						break;
					}
					RevCommit p = c.parents[i];
					if ((p.flags & UNINTERESTING) != 0) {
						held.add(p);
					}
				}
				return c;
			}

			final FIFORevQueue boundary = new FIFORevQueue(firstParent);
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
