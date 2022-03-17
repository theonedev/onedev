/*
 * Copyright (C) 2020, Google LLC. and others
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

/** Sorts commits in topological order without intermixing lines of history. */
class TopoNonIntermixSortGenerator extends Generator {
	private static final int TOPO_QUEUED = RevWalk.TOPO_QUEUED;

	private final FIFORevQueue pending;

	private final int outputType;

	/**
	 * Create a new sorter and completely spin the generator.
	 * <p>
	 * When the constructor completes the supplied generator will have no
	 * commits remaining, as all of the commits will be held inside of this
	 * generator's internal buffer.
	 *
	 * @param s
	 *            generator to pull all commits out of, and into this buffer.
	 * @throws MissingObjectException
	 * @throws IncorrectObjectTypeException
	 * @throws IOException
	 */
	TopoNonIntermixSortGenerator(Generator s) throws MissingObjectException,
			IncorrectObjectTypeException, IOException {
		super(s.firstParent);
		pending = new FIFORevQueue(firstParent);
		outputType = s.outputType() | SORT_TOPO;
		s.shareFreeList(pending);
		for (;;) {
			final RevCommit c = s.next();
			if (c == null) {
				break;
			}
			if ((c.flags & TOPO_QUEUED) == 0) {
				for (RevCommit p : c.parents) {
					p.inDegree++;

					if (firstParent) {
						break;
					}
				}
			}
			c.flags |= TOPO_QUEUED;
			pending.add(c);
		}
	}

	@Override
	int outputType() {
		return outputType;
	}

	@Override
	void shareFreeList(BlockRevQueue q) {
		q.shareFreeList(pending);
	}

	@Override
	RevCommit next() throws MissingObjectException,
			IncorrectObjectTypeException, IOException {
		for (;;) {
			final RevCommit c = pending.next();
			if (c == null) {
				return null;
			}

			if (c.inDegree > 0) {
				// At least one of our children is missing. We delay
				// production until all of our children are output.
				//
				continue;
			}

			if ((c.flags & TOPO_QUEUED) == 0) {
				// c is a parent that already produced or a parent that
				// was never in the priority queue and should never produce.
				//
				continue;
			}

			for (RevCommit p : c.parents) {
				if (--p.inDegree == 0 && (p.flags & TOPO_QUEUED) != 0) {
					// The parent has no unproduced interesting children. unpop
					// the parent so it goes right behind this child. This means
					// that this parent commit may appear in "pending" more than
					// once, but this is safe since upon the second and
					// subsequent iterations with this commit, it will no longer
					// have TOPO_QUEUED set, and thus will be skipped.
					//
					pending.unpop(p);
				}
				if (firstParent) {
					break;
				}
			}

			c.flags &= ~TOPO_QUEUED;
			return c;
		}
	}
}
