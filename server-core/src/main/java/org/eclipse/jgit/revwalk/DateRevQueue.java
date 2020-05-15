/*
 * Copyright (C) 2008, Shawn O. Pearce <spearce@spearce.org>,
 * Copyright (C) 2013, Gustaf Lundh <gustaf.lundh@sonymobile.com> and others
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
 * A queue of commits sorted by commit time order.
 */
public class DateRevQueue extends AbstractRevQueue {
	private static final int REBUILD_INDEX_COUNT = 1000;

	private Entry head;

	private Entry free;

	private int inQueue;

	private int sinceLastIndex;

	private Entry[] index;

	private int first;

	private int last = -1;

	/** Create an empty date queue. */
	public DateRevQueue() {
		super(false);
	}

	DateRevQueue(boolean firstParent) {
		super(firstParent);
	}

	DateRevQueue(Generator s) throws MissingObjectException,
			IncorrectObjectTypeException, IOException {
		super(s.firstParent);
		for (;;) {
			final RevCommit c = s.next();
			if (c == null)
				break;
			add(c);
		}
	}

	/** {@inheritDoc} */
	@Override
	public void add(RevCommit c) {
		sinceLastIndex++;
		if (++inQueue > REBUILD_INDEX_COUNT
				&& sinceLastIndex > REBUILD_INDEX_COUNT)
			buildIndex();

		Entry q = head;
		final long when = c.commitTime;

		if (first <= last && index[first].commit.commitTime > when) {
			int low = first, high = last;
			while (low <= high) {
				int mid = (low + high) >>> 1;
				int t = index[mid].commit.commitTime;
				if (t < when)
					high = mid - 1;
				else if (t > when)
					low = mid + 1;
				else {
					low = mid - 1;
					break;
				}
			}
			low = Math.min(low, high);
			while (low > first && when == index[low].commit.commitTime)
				--low;
			q = index[low];
		}

		final Entry n = newEntry(c);
		if (q == null || (q == head && when > q.commit.commitTime)) {
			n.next = q;
			head = n;
		} else {
			Entry p = q.next;
			while (p != null && p.commit.commitTime > when) {
				q = p;
				p = q.next;
			}
			n.next = q.next;
			q.next = n;
		}
	}

	/** {@inheritDoc} */
	@Override
	public RevCommit next() {
		final Entry q = head;
		if (q == null)
			return null;

		if (index != null && q == index[first])
			index[first++] = null;
		inQueue--;

		head = q.next;
		freeEntry(q);
		return q.commit;
	}

	private void buildIndex() {
		sinceLastIndex = 0;
		first = 0;
		index = new Entry[inQueue / 100 + 1];
		int qi = 0, ii = 0;
		for (Entry q = head; q != null; q = q.next) {
			if (++qi % 100 == 0)
				index[ii++] = q;
		}
		last = ii - 1;
	}

	/**
	 * Peek at the next commit, without removing it.
	 *
	 * @return the next available commit; null if there are no commits left.
	 */
	public RevCommit peek() {
		return head != null ? head.commit : null;
	}

	/** {@inheritDoc} */
	@Override
	public void clear() {
		head = null;
		free = null;
		index = null;
		inQueue = 0;
		sinceLastIndex = 0;
		last = -1;
	}

	@Override
	boolean everbodyHasFlag(int f) {
		for (Entry q = head; q != null; q = q.next) {
			if ((q.commit.flags & f) == 0)
				return false;
		}
		return true;
	}

	@Override
	boolean anybodyHasFlag(int f) {
		for (Entry q = head; q != null; q = q.next) {
			if ((q.commit.flags & f) != 0)
				return true;
		}
		return false;
	}

	@Override
	int outputType() {
		return outputType | SORT_COMMIT_TIME_DESC;
	}

	/** {@inheritDoc} */
	@Override
	public String toString() {
		final StringBuilder s = new StringBuilder();
		for (Entry q = head; q != null; q = q.next)
			describe(s, q.commit);
		return s.toString();
	}

	private Entry newEntry(RevCommit c) {
		Entry r = free;
		if (r == null)
			r = new Entry();
		else
			free = r.next;
		r.commit = c;
		return r;
	}

	private void freeEntry(Entry e) {
		e.next = free;
		free = e;
	}

	static class Entry {
		Entry next;

		RevCommit commit;
	}
}
