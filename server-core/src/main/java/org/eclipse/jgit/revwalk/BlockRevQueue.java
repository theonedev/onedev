/*
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

abstract class BlockRevQueue extends AbstractRevQueue {
	protected BlockFreeList free;

	/**
	 * Create an empty revision queue.
	 *
	 * @param firstParent
	 *            whether only first-parent links should be followed when
	 *            walking
	 */
	protected BlockRevQueue(boolean firstParent) {
		super(firstParent);
		free = new BlockFreeList();
	}

	BlockRevQueue(Generator s) throws MissingObjectException,
			IncorrectObjectTypeException, IOException {
		super(s.firstParent);
		free = new BlockFreeList();
		outputType = s.outputType();
		s.shareFreeList(this);
		for (;;) {
			final RevCommit c = s.next();
			if (c == null)
				break;
			add(c);
		}
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * Reconfigure this queue to share the same free list as another.
	 * <p>
	 * Multiple revision queues can be connected to the same free list, making
	 * it less expensive for applications to shuttle commits between them. This
	 * method arranges for the receiver to take from / return to the same free
	 * list as the supplied queue.
	 * <p>
	 * Free lists are not thread-safe. Applications must ensure that all queues
	 * sharing the same free list are doing so from only a single thread.
	 */
	@Override
	public void shareFreeList(BlockRevQueue q) {
		free = q.free;
	}

	static final class BlockFreeList {
		private Block next;

		Block newBlock() {
			Block b = next;
			if (b == null)
				return new Block();
			next = b.next;
			b.clear();
			return b;
		}

		void freeBlock(Block b) {
			b.next = next;
			next = b;
		}

		void clear() {
			next = null;
		}
	}

	static final class Block {
		static final int BLOCK_SIZE = 256;

		/** Next block in our chain of blocks; null if we are the last. */
		Block next;

		/** Our table of queued commits. */
		final RevCommit[] commits = new RevCommit[BLOCK_SIZE];

		/** Next valid entry in {@link #commits}. */
		int headIndex;

		/** Next free entry in {@link #commits} for addition at. */
		int tailIndex;

		boolean isFull() {
			return tailIndex == BLOCK_SIZE;
		}

		boolean isEmpty() {
			return headIndex == tailIndex;
		}

		boolean canUnpop() {
			return headIndex > 0;
		}

		void add(RevCommit c) {
			commits[tailIndex++] = c;
		}

		void unpop(RevCommit c) {
			commits[--headIndex] = c;
		}

		RevCommit pop() {
			return commits[headIndex++];
		}

		RevCommit peek() {
			return commits[headIndex];
		}

		void clear() {
			next = null;
			headIndex = 0;
			tailIndex = 0;
		}

		void resetToMiddle() {
			headIndex = tailIndex = BLOCK_SIZE / 2;
		}

		void resetToEnd() {
			headIndex = tailIndex = BLOCK_SIZE;
		}
	}
}
