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

/** A queue of commits in FIFO order. */
public class FIFORevQueue extends BlockRevQueue {
	private Block head;

	private Block tail;

	/** Create an empty FIFO queue. */
	public FIFORevQueue() {
		super();
	}

	FIFORevQueue(final Generator s) throws MissingObjectException,
			IncorrectObjectTypeException, IOException {
		super(s);
	}

	public void add(final RevCommit c) {
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

	/**
	 * Insert the commit pointer at the front of the queue.
	 *
	 * @param c
	 *            the commit to insert into the queue.
	 */
	public void unpop(final RevCommit c) {
		Block b = head;
		if (b == null) {
			b = free.newBlock();
			b.resetToMiddle();
			b.add(c);
			head = b;
			tail = b;
			return;
		} else if (b.canUnpop()) {
			b.unpop(c);
			return;
		}

		b = free.newBlock();
		b.resetToEnd();
		b.unpop(c);
		b.next = head;
		head = b;
	}

	public RevCommit next() {
		final Block b = head;
		if (b == null)
			return null;

		final RevCommit c = b.pop();
		if (b.isEmpty()) {
			head = b.next;
			if (head == null)
				tail = null;
			free.freeBlock(b);
		}
		return c;
	}

	public void clear() {
		head = null;
		tail = null;
		free.clear();
	}

	boolean everbodyHasFlag(final int f) {
		for (Block b = head; b != null; b = b.next) {
			for (int i = b.headIndex; i < b.tailIndex; i++)
				if ((b.commits[i].flags & f) == 0)
					return false;
		}
		return true;
	}

	boolean anybodyHasFlag(final int f) {
		for (Block b = head; b != null; b = b.next) {
			for (int i = b.headIndex; i < b.tailIndex; i++)
				if ((b.commits[i].flags & f) != 0)
					return true;
		}
		return false;
	}

	void removeFlag(final int f) {
		final int not_f = ~f;
		for (Block b = head; b != null; b = b.next) {
			for (int i = b.headIndex; i < b.tailIndex; i++)
				b.commits[i].flags &= not_f;
		}
	}

	public String toString() {
		final StringBuilder s = new StringBuilder();
		for (Block q = head; q != null; q = q.next) {
			for (int i = q.headIndex; i < q.tailIndex; i++)
				describe(s, q.commits[i]);
		}
		return s.toString();
	}
}
