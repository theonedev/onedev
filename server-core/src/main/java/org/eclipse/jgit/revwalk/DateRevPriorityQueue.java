/*
 * Copyright (C) 2023, GerritForge Ltd
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0 which is available at
 * https://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */
package org.eclipse.jgit.revwalk;

import java.util.concurrent.atomic.AtomicInteger;

import org.eclipse.jgit.annotations.Nullable;
import org.eclipse.jgit.errors.IncorrectObjectTypeException;
import org.eclipse.jgit.errors.MissingObjectException;
import org.eclipse.jgit.internal.JGitText;

import java.io.IOException;
import java.util.Comparator;
import java.util.PriorityQueue;

/**
 * A queue of commits sorted by commit time order using a Java PriorityQueue.
 * For the commits with the same commit time insertion order will be preserved.
 */
class DateRevPriorityQueue extends DateRevQueue {
	private PriorityQueue<RevCommitEntry> queue;

	private final AtomicInteger sequence = new AtomicInteger(1);

	/**
	 * Create an empty queue of commits sorted by commit time order.
	 */
	public DateRevPriorityQueue() {
		this(false);
	}

	/**
	 * Create an empty queue of commits sorted by commit time order.
	 *
	 * @param firstParent
	 *            treat first element as a parent
	 */
	DateRevPriorityQueue(boolean firstParent) {
		super(firstParent);
		initPriorityQueue();
	}

	private void initPriorityQueue() {
		sequence.set(1);
		queue = new PriorityQueue<>(Comparator.comparingInt(
						(RevCommitEntry ent) -> ent.getEntry().getCommitTime())
				.reversed()
				.thenComparingInt(RevCommitEntry::getInsertSequenceNumber));
	}

    DateRevPriorityQueue(Generator s) throws MissingObjectException,
			IncorrectObjectTypeException, IOException {
		this(s.firstParent);
		for (;;) {
			final RevCommit c = s.next();
			if (c == null) {
				break;
			}
			add(c);
		}
	}

	@Override
	public void add(RevCommit c) {
		// PriorityQueue does not accept null values. To keep the same behaviour
		// do the same check and throw the same exception before creating entry
		if (c == null) {
			throw new NullPointerException(JGitText.get().nullRevCommit);
		}
		queue.add(new RevCommitEntry(sequence.getAndIncrement(), c));
	}

	@Override
	public RevCommit next() {
		RevCommitEntry entry = queue.poll();
		return entry == null ? null : entry.getEntry();
	}

	/**
	 * Peek at the next commit, without removing it.
	 *
	 * @return the next available commit; null if there are no commits left.
	 */
	@Override
	public @Nullable RevCommit peek() {
		RevCommitEntry entry = queue.peek();
		return entry == null ? null : entry.getEntry();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void clear() {
		sequence.set(1);
		queue.clear();
	}

	@Override
	boolean everbodyHasFlag(int f) {
		return queue.stream().map(RevCommitEntry::getEntry)
				.noneMatch(c -> (c.flags & f) == 0);
	}

	@Override
	boolean anybodyHasFlag(int f) {
		return queue.stream().map(RevCommitEntry::getEntry)
				.anyMatch(c -> (c.flags & f) != 0);
	}

	@Override
	int outputType() {
		return outputType | SORT_COMMIT_TIME_DESC;
	}

	@Override
	public String toString() {
		final StringBuilder s = new StringBuilder();
		for (RevCommitEntry e : queue) {
			describe(s, e.getEntry());
		}
		return s.toString();
	}

	private static class RevCommitEntry {
		private final int insertSequenceNumber;

		private final RevCommit entry;

		public RevCommitEntry(int insertSequenceNumber, RevCommit entry) {
			this.insertSequenceNumber = insertSequenceNumber;
			this.entry = entry;
		}

		public int getInsertSequenceNumber() {
			return insertSequenceNumber;
		}

		public RevCommit getEntry() {
			return entry;
		}
	}
}
