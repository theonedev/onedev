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

abstract class AbstractRevQueue extends Generator {
	static final AbstractRevQueue EMPTY_QUEUE = new AlwaysEmptyQueue();

	/** Current output flags set for this generator instance. */
	int outputType;

	AbstractRevQueue(boolean firstParent) {
		super(firstParent);
	}

	/**
	 * Add a commit to the queue.
	 * <p>
	 * This method always adds the commit, even if it is already in the queue or
	 * previously was in the queue but has already been removed. To control
	 * queue admission use {@link #add(RevCommit, RevFlag)}.
	 *
	 * @param c
	 *            commit to add.
	 */
	public abstract void add(RevCommit c);

	/**
	 * Add a commit if it does not have a flag set yet, then set the flag.
	 * <p>
	 * This method permits the application to test if the commit has the given
	 * flag; if it does not already have the flag than the commit is added to
	 * the queue and the flag is set. This later will prevent the commit from
	 * being added twice.
	 *
	 * @param c
	 *            commit to add.
	 * @param queueControl
	 *            flag that controls admission to the queue.
	 */
	public final void add(RevCommit c, RevFlag queueControl) {
		if (!c.has(queueControl)) {
			c.add(queueControl);
			add(c);
		}
	}

	/**
	 * Add a commit's parents if one does not have a flag set yet.
	 * <p>
	 * This method permits the application to test if the commit has the given
	 * flag; if it does not already have the flag than the commit is added to
	 * the queue and the flag is set. This later will prevent the commit from
	 * being added twice.
	 *
	 * @param c
	 *            commit whose parents should be added.
	 * @param queueControl
	 *            flag that controls admission to the queue.
	 */
	public final void addParents(RevCommit c, RevFlag queueControl) {
		final RevCommit[] pList = c.parents;
		if (pList == null) {
			return;
		}
		for (int i = 0; i < pList.length; i++) {
			if (firstParent && i > 0) {
				break;
			}
			add(pList[i], queueControl);
		}
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * Remove the first commit from the queue.
	 */
	@Override
	public abstract RevCommit next();

	/**
	 * Remove all entries from this queue.
	 */
	public abstract void clear();

	abstract boolean everbodyHasFlag(int f);

	abstract boolean anybodyHasFlag(int f);

	@Override
	int outputType() {
		return outputType;
	}

	/**
	 * Describe this queue
	 *
	 * @param s
	 *            a StringBuilder
	 * @param c
	 *            a {@link org.eclipse.jgit.revwalk.RevCommit}
	 */
	protected static void describe(StringBuilder s, RevCommit c) {
		s.append(c.toString());
		s.append('\n');
	}

	private static class AlwaysEmptyQueue extends AbstractRevQueue {
		private AlwaysEmptyQueue() {
			super(false);
		}

		@Override
		public void add(RevCommit c) {
			throw new UnsupportedOperationException();
		}

		@Override
		public RevCommit next() {
			return null;
		}

		@Override
		boolean anybodyHasFlag(int f) {
			return false;
		}

		@Override
		boolean everbodyHasFlag(int f) {
			return true;
		}

		@Override
		public void clear() {
			// Nothing to clear, we have no state.
		}

	}
}
