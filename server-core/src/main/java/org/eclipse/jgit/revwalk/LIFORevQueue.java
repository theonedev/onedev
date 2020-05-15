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

/**
 * A queue of commits in LIFO order.
 */
public class LIFORevQueue extends BlockRevQueue {
	private Block head;

	/**
	 * Create an empty LIFO queue.
	 */
	public LIFORevQueue() {
		super(false);
	}

	LIFORevQueue(Generator s) throws MissingObjectException,
			IncorrectObjectTypeException, IOException {
		super(s);
	}

	/** {@inheritDoc} */
	@Override
	public void add(RevCommit c) {
		Block b = head;
		if (b == null || !b.canUnpop()) {
			b = free.newBlock();
			b.resetToEnd();
			b.next = head;
			head = b;
		}
		b.unpop(c);
	}

	/** {@inheritDoc} */
	@Override
	public RevCommit next() {
		final Block b = head;
		if (b == null)
			return null;

		final RevCommit c = b.pop();
		if (b.isEmpty()) {
			head = b.next;
			free.freeBlock(b);
		}
		return c;
	}

	/** {@inheritDoc} */
	@Override
	public void clear() {
		head = null;
		free.clear();
	}

	@Override
	boolean everbodyHasFlag(int f) {
		for (Block b = head; b != null; b = b.next) {
			for (int i = b.headIndex; i < b.tailIndex; i++)
				if ((b.commits[i].flags & f) == 0)
					return false;
		}
		return true;
	}

	@Override
	boolean anybodyHasFlag(int f) {
		for (Block b = head; b != null; b = b.next) {
			for (int i = b.headIndex; i < b.tailIndex; i++)
				if ((b.commits[i].flags & f) != 0)
					return true;
		}
		return false;
	}

	/** {@inheritDoc} */
	@Override
	public String toString() {
		final StringBuilder s = new StringBuilder();
		for (Block q = head; q != null; q = q.next) {
			for (int i = q.headIndex; i < q.tailIndex; i++)
				describe(s, q.commits[i]);
		}
		return s.toString();
	}
}
