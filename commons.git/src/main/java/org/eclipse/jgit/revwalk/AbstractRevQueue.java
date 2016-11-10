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

abstract class AbstractRevQueue extends Generator {
	static final AbstractRevQueue EMPTY_QUEUE = new AlwaysEmptyQueue();

	/** Current output flags set for this generator instance. */
	int outputType;

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
	public final void add(final RevCommit c, final RevFlag queueControl) {
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
	public final void addParents(final RevCommit c, final RevFlag queueControl) {
		final RevCommit[] pList = c.parents;
		if (pList == null)
			return;
		for (RevCommit p : pList)
			add(p, queueControl);
	}

	/**
	 * Remove the first commit from the queue.
	 *
	 * @return the first commit of this queue.
	 */
	public abstract RevCommit next();

	/** Remove all entries from this queue. */
	public abstract void clear();

	abstract boolean everbodyHasFlag(int f);

	abstract boolean anybodyHasFlag(int f);

	@Override
	int outputType() {
		return outputType;
	}

	protected static void describe(final StringBuilder s, final RevCommit c) {
		s.append(c.toString());
		s.append('\n');
	}

	private static class AlwaysEmptyQueue extends AbstractRevQueue {
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
