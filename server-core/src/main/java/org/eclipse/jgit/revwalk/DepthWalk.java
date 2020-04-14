/*
 * Copyright (C) 2010, Garmin International
 * Copyright (C) 2010, Matt Fischer <matt.fischer@garmin.com>
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
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import org.eclipse.jgit.errors.IncorrectObjectTypeException;
import org.eclipse.jgit.errors.MissingObjectException;
import org.eclipse.jgit.lib.AnyObjectId;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectReader;
import org.eclipse.jgit.lib.Repository;

/**
 * Interface for revision walkers that perform depth filtering.
 */
public interface DepthWalk {
	/**
	 * Get depth to filter to.
	 *
	 * @return Depth to filter to.
	 */
	int getDepth();

	/**
	 * @return the deepen-since value; if not 0, this walk only returns commits
	 *         whose commit time is at or after this limit
	 * @since 5.2
	 */
	default int getDeepenSince() {
		return 0;
	}

	/**
	 * @return the objects specified by the client using --shallow-exclude
	 * @since 5.2
	 */
	default List<ObjectId> getDeepenNots() {
		return Collections.emptyList();
	}

	/** @return flag marking commits that should become unshallow. */
	/**
	 * Get flag marking commits that should become unshallow.
	 *
	 * @return flag marking commits that should become unshallow.
	 */
	RevFlag getUnshallowFlag();

	/**
	 * Get flag marking commits that are interesting again.
	 *
	 * @return flag marking commits that are interesting again.
	 */
	RevFlag getReinterestingFlag();

	/**
	 * @return flag marking commits that are to be excluded because of --shallow-exclude
	 * @since 5.2
	 */
	RevFlag getDeepenNotFlag();

	/** RevCommit with a depth (in commits) from a root. */
	public static class Commit extends RevCommit {
		/** Depth of this commit in the graph, via shortest path. */
		int depth;

		boolean isBoundary;

		/**
		 * True if this commit was excluded due to a shallow fetch
		 * setting. All its children are thus boundary commits.
		 */
		boolean makesChildBoundary;

		/** @return depth of this commit, as found by the shortest path. */
		public int getDepth() {
			return depth;
		}

		/**
		 * @return true if at least one of this commit's parents was excluded
		 *         due to a shallow fetch setting, false otherwise
		 * @since 5.2
		 */
		public boolean isBoundary() {
			return isBoundary;
		}

		/**
		 * Initialize a new commit.
		 *
		 * @param id
		 *            object name for the commit.
		 */
		protected Commit(AnyObjectId id) {
			super(id);
			depth = -1;
		}
	}

	/** Subclass of RevWalk that performs depth filtering. */
	public class RevWalk extends org.eclipse.jgit.revwalk.RevWalk implements DepthWalk {
		private final int depth;

		private int deepenSince;

		private List<ObjectId> deepenNots;

		private final RevFlag UNSHALLOW;

		private final RevFlag REINTERESTING;

		private final RevFlag DEEPEN_NOT;

		/**
		 * @param repo Repository to walk
		 * @param depth Maximum depth to return
		 */
		public RevWalk(Repository repo, int depth) {
			super(repo);

			this.depth = depth;
			this.deepenNots = Collections.emptyList();
			this.UNSHALLOW = newFlag("UNSHALLOW"); //$NON-NLS-1$
			this.REINTERESTING = newFlag("REINTERESTING"); //$NON-NLS-1$
			this.DEEPEN_NOT = newFlag("DEEPEN_NOT"); //$NON-NLS-1$
		}

		/**
		 * @param or ObjectReader to use
		 * @param depth Maximum depth to return
		 */
		public RevWalk(ObjectReader or, int depth) {
			super(or);

			this.depth = depth;
			this.deepenNots = Collections.emptyList();
			this.UNSHALLOW = newFlag("UNSHALLOW"); //$NON-NLS-1$
			this.REINTERESTING = newFlag("REINTERESTING"); //$NON-NLS-1$
			this.DEEPEN_NOT = newFlag("DEEPEN_NOT"); //$NON-NLS-1$
		}

		/**
		 * Mark a root commit (i.e., one whose depth should be considered 0.)
		 *
		 * @param c
		 *            Commit to mark
		 * @throws IOException
		 * @throws IncorrectObjectTypeException
		 * @throws MissingObjectException
		 */
		public void markRoot(RevCommit c) throws MissingObjectException,
				IncorrectObjectTypeException, IOException {
			if (c instanceof Commit)
				((Commit) c).depth = 0;
			super.markStart(c);
		}

		@Override
		protected RevCommit createCommit(AnyObjectId id) {
			return new Commit(id);
		}

		@Override
		public int getDepth() {
			return depth;
		}

		@Override
		public int getDeepenSince() {
			return deepenSince;
		}

		/**
		 * Sets the deepen-since value.
		 *
		 * @param limit
		 *            new deepen-since value
		 * @since 5.2
		 */
		public void setDeepenSince(int limit) {
			deepenSince = limit;
		}

		@Override
		public List<ObjectId> getDeepenNots() {
			return deepenNots;
		}

		/**
		 * Mark objects that the client specified using
		 * --shallow-exclude. Objects that are not commits have no
		 * effect.
		 *
		 * @param deepenNots specified objects
		 * @since 5.2
		 */
		public void setDeepenNots(List<ObjectId> deepenNots) {
			this.deepenNots = Objects.requireNonNull(deepenNots);
		}

		@Override
		public RevFlag getUnshallowFlag() {
			return UNSHALLOW;
		}

		@Override
		public RevFlag getReinterestingFlag() {
			return REINTERESTING;
		}

		@Override
		public RevFlag getDeepenNotFlag() {
			return DEEPEN_NOT;
		}

		/**
		 * @since 4.5
		 */
		@Override
		public ObjectWalk toObjectWalkWithSameObjects() {
			ObjectWalk ow = new ObjectWalk(reader, depth);
			ow.deepenSince = deepenSince;
			ow.deepenNots = deepenNots;
			ow.objects = objects;
			ow.freeFlags = freeFlags;
			return ow;
		}
	}

	/** Subclass of ObjectWalk that performs depth filtering. */
	public class ObjectWalk extends org.eclipse.jgit.revwalk.ObjectWalk implements DepthWalk {
		private final int depth;

		private int deepenSince;

		private List<ObjectId> deepenNots;

		private final RevFlag UNSHALLOW;

		private final RevFlag REINTERESTING;

		private final RevFlag DEEPEN_NOT;

		/**
		 * @param repo Repository to walk
		 * @param depth Maximum depth to return
		 */
		public ObjectWalk(Repository repo, int depth) {
			super(repo);

			this.depth = depth;
			this.deepenNots = Collections.emptyList();
			this.UNSHALLOW = newFlag("UNSHALLOW"); //$NON-NLS-1$
			this.REINTERESTING = newFlag("REINTERESTING"); //$NON-NLS-1$
			this.DEEPEN_NOT = newFlag("DEEPEN_NOT"); //$NON-NLS-1$
		}

		/**
		 * @param or Object Reader
		 * @param depth Maximum depth to return
		 */
		public ObjectWalk(ObjectReader or, int depth) {
			super(or);

			this.depth = depth;
			this.deepenNots = Collections.emptyList();
			this.UNSHALLOW = newFlag("UNSHALLOW"); //$NON-NLS-1$
			this.REINTERESTING = newFlag("REINTERESTING"); //$NON-NLS-1$
			this.DEEPEN_NOT = newFlag("DEEPEN_NOT"); //$NON-NLS-1$
		}

		/**
		 * Mark a root commit (i.e., one whose depth should be considered 0.)
		 *
		 * @param o
		 *            Commit to mark
		 * @throws IOException
		 * @throws IncorrectObjectTypeException
		 * @throws MissingObjectException
		 */
		public void markRoot(RevObject o) throws MissingObjectException,
				IncorrectObjectTypeException, IOException {
			RevObject c = o;
			while (c instanceof RevTag) {
				c = ((RevTag) c).getObject();
				parseHeaders(c);
			}
			if (c instanceof Commit)
				((Commit) c).depth = 0;
			super.markStart(o);
		}

		/**
		 * Mark an element which used to be shallow in the client, but which
		 * should now be considered a full commit. Any ancestors of this commit
		 * should be included in the walk, even if they are the ancestor of an
		 * uninteresting commit.
		 *
		 * @param c
		 *            Commit to mark
		 * @throws MissingObjectException
		 * @throws IncorrectObjectTypeException
		 * @throws IOException
		 */
		public void markUnshallow(RevObject c) throws MissingObjectException,
				IncorrectObjectTypeException, IOException {
			if (c instanceof RevCommit)
				c.add(UNSHALLOW);
			super.markStart(c);
		}

		@Override
		protected RevCommit createCommit(AnyObjectId id) {
			return new Commit(id);
		}

		@Override
		public int getDepth() {
			return depth;
		}

		@Override
		public int getDeepenSince() {
			return deepenSince;
		}

		@Override
		public List<ObjectId> getDeepenNots() {
			return deepenNots;
		}

		@Override
		public RevFlag getUnshallowFlag() {
			return UNSHALLOW;
		}

		@Override
		public RevFlag getReinterestingFlag() {
			return REINTERESTING;
		}

		@Override
		public RevFlag getDeepenNotFlag() {
			return DEEPEN_NOT;
		}
	}
}
