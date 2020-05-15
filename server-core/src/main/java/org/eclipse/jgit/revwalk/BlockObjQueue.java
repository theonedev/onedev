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

class BlockObjQueue {
	private BlockFreeList free;

	private Block head;

	private Block tail;

	/** Create an empty queue. */
	BlockObjQueue() {
		free = new BlockFreeList();
	}

	void add(RevObject c) {
		Block b = tail;
		if (b == null) {
			b = free.newBlock();
			b.add(c);
			head = b;
			tail = b;
			return;
		} else if (b.isFull()) {
			b = free.newBlock();
			tail.next = b;
			tail = b;
		}
		b.add(c);
	}

	RevObject next() {
		final Block b = head;
		if (b == null)
			return null;

		final RevObject c = b.pop();
		if (b.isEmpty()) {
			head = b.next;
			if (head == null)
				tail = null;
			free.freeBlock(b);
		}
		return c;
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
	}

	static final class Block {
		private static final int BLOCK_SIZE = 256;

		/** Next block in our chain of blocks; null if we are the last. */
		Block next;

		/** Our table of queued objects. */
		final RevObject[] objects = new RevObject[BLOCK_SIZE];

		/** Next valid entry in {@link #objects}. */
		int headIndex;

		/** Next free entry in {@link #objects} for addition at. */
		int tailIndex;

		boolean isFull() {
			return tailIndex == BLOCK_SIZE;
		}

		boolean isEmpty() {
			return headIndex == tailIndex;
		}

		void add(RevObject c) {
			objects[tailIndex++] = c;
		}

		RevObject pop() {
			return objects[headIndex++];
		}

		void clear() {
			next = null;
			headIndex = 0;
			tailIndex = 0;
		}
	}
}
