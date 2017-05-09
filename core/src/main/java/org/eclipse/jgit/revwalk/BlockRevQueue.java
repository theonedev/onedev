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

import java.io.IOException;

import org.eclipse.jgit.errors.IncorrectObjectTypeException;
import org.eclipse.jgit.errors.MissingObjectException;

abstract class BlockRevQueue extends AbstractRevQueue {
	protected BlockFreeList free;

	/** Create an empty revision queue. */
	protected BlockRevQueue() {
		free = new BlockFreeList();
	}

	BlockRevQueue(final Generator s) throws MissingObjectException,
			IncorrectObjectTypeException, IOException {
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
	 * Reconfigure this queue to share the same free list as another.
	 * <p>
	 * Multiple revision queues can be connected to the same free list, making
	 * it less expensive for applications to shuttle commits between them. This
	 * method arranges for the receiver to take from / return to the same free
	 * list as the supplied queue.
	 * <p>
	 * Free lists are not thread-safe. Applications must ensure that all queues
	 * sharing the same free list are doing so from only a single thread.
	 *
	 * @param q
	 *            the other queue we will steal entries from.
	 */
	@Override
	public void shareFreeList(final BlockRevQueue q) {
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

		void freeBlock(final Block b) {
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

		void add(final RevCommit c) {
			commits[tailIndex++] = c;
		}

		void unpop(final RevCommit c) {
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
