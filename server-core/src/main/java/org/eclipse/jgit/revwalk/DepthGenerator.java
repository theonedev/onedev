/*
 * Copyright (C) 2010, Garmin International
 * Copyright (C) 2010, Matt Fischer <matt.fischer@garmin.com> and others
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
import org.eclipse.jgit.lib.ObjectId;

/**
 * Only produce commits which are below a specified depth.
 *
 * @see DepthWalk
 */
class DepthGenerator extends Generator {
	private final FIFORevQueue pending;

	private final int depth;

	private final int deepenSince;

	private final RevWalk walk;

	/**
	 * Commits which used to be shallow in the client, but which are
	 * being extended as part of this fetch.  These commits should be
	 * returned to the caller as UNINTERESTING so that their blobs/trees
	 * can be marked appropriately in the pack writer.
	 */
	private final RevFlag UNSHALLOW;

	/**
	 * Commits which the normal framework has marked as UNINTERESTING,
	 * but which we now care about again.  This happens if a client is
	 * extending a shallow checkout to become deeper--the new commits at
	 * the bottom of the graph need to be sent, even though they are
	 * below other commits which the client already has.
	 */
	private final RevFlag REINTERESTING;

	/**
	 * Commits reachable from commits that the client specified using --shallow-exclude.
	 */
	private final RevFlag DEEPEN_NOT;

	/**
	 * @param w
	 * @param s Parent generator
	 * @throws MissingObjectException
	 * @throws IncorrectObjectTypeException
	 * @throws IOException
	 */
	DepthGenerator(DepthWalk w, Generator s) throws MissingObjectException,
			IncorrectObjectTypeException, IOException {
		super(s.firstParent);
		pending = new FIFORevQueue(firstParent);
		walk = (RevWalk)w;

		this.depth = w.getDepth();
		this.deepenSince = w.getDeepenSince();
		this.UNSHALLOW = w.getUnshallowFlag();
		this.REINTERESTING = w.getReinterestingFlag();
		this.DEEPEN_NOT = w.getDeepenNotFlag();

		s.shareFreeList(pending);

		// Begin by sucking out all of the source's commits, and
		// adding them to the pending queue
		FIFORevQueue unshallowCommits = new FIFORevQueue();
		for (;;) {
			RevCommit c = s.next();
			if (c == null)
				break;
			if (c.has(UNSHALLOW)) {
				unshallowCommits.add(c);
			} else if (((DepthWalk.Commit) c).getDepth() == 0) {
				pending.add(c);
			}
		}
		// Move unshallow commits to the front so that the REINTERESTING flag
		// carry over code is executed first.
		for (;;) {
			RevCommit c = unshallowCommits.next();
			if (c == null) {
				break;
			}
			pending.unpop(c);
		}

		// Mark DEEPEN_NOT on all deepen-not commits and their ancestors.
		// TODO(jonathantanmy): This implementation is somewhat
		// inefficient in that any "deepen-not <ref>" in the request
		// results in all commits reachable from that ref being parsed
		// and marked, even if the commit topology is such that it is
		// not necessary.
		for (ObjectId oid : w.getDeepenNots()) {
			RevCommit c;
			try {
				c = walk.parseCommit(oid);
			} catch (IncorrectObjectTypeException notCommit) {
				// The C Git implementation silently tolerates
				// non-commits, so do the same here.
				continue;
			}

			FIFORevQueue queue = new FIFORevQueue();
			queue.add(c);
			while ((c = queue.next()) != null) {
				if (c.has(DEEPEN_NOT)) {
					continue;
				}

				walk.parseHeaders(c);
				c.add(DEEPEN_NOT);
				for (RevCommit p : c.getParents()) {
					queue.add(p);
				}
			}
		}
	}

	@Override
	int outputType() {
		return pending.outputType() | HAS_UNINTERESTING;
	}

	@Override
	void shareFreeList(BlockRevQueue q) {
		pending.shareFreeList(q);
	}

	@Override
	RevCommit next() throws MissingObjectException,
			IncorrectObjectTypeException, IOException {
		// Perform a breadth-first descent into the commit graph,
		// marking depths as we go.  This means that if a commit is
		// reachable by more than one route, we are guaranteed to
		// arrive by the shortest route first.
		for (;;) {
			final DepthWalk.Commit c = (DepthWalk.Commit) pending.next();
			if (c == null)
				return null;

			if ((c.flags & RevWalk.PARSED) == 0)
				c.parseHeaders(walk);

			if (c.getCommitTime() < deepenSince) {
				continue;
			}

			if (c.has(DEEPEN_NOT)) {
				continue;
			}

			int newDepth = c.depth + 1;

			for (int i = 0; i < c.parents.length; i++) {
				if (firstParent && i > 0) {
					break;
				}
				RevCommit p = c.parents[i];
				DepthWalk.Commit dp = (DepthWalk.Commit) p;

				// If no depth has been assigned to this commit, assign
				// it now.  Since we arrive by the shortest route first,
				// this depth is guaranteed to be the smallest value that
				// any path could produce.
				if (dp.depth == -1) {
					boolean failsDeepenSince = false;
					if (deepenSince != 0) {
						if ((p.flags & RevWalk.PARSED) == 0) {
							p.parseHeaders(walk);
						}
						failsDeepenSince =
							p.getCommitTime() < deepenSince;
					}

					dp.depth = newDepth;

					// If the parent is not too deep and was not excluded, add
					// it to the queue so that we can produce it later
					if (newDepth <= depth && !failsDeepenSince &&
							!p.has(DEEPEN_NOT)) {
						pending.add(p);
					} else {
						dp.makesChildBoundary = true;
					}
				}

				if (dp.makesChildBoundary) {
					c.isBoundary = true;
				}

				// If the current commit has become unshallowed, everything
				// below us is new to the client.  Mark its parent as
				// re-interesting, and carry that flag downward to all
				// of its ancestors.
				if(c.has(UNSHALLOW) || c.has(REINTERESTING)) {
					p.add(REINTERESTING);
					p.flags &= ~RevWalk.UNINTERESTING;
				}
			}

			boolean produce = true;

			// Unshallow commits are uninteresting, but still need to be sent
			// up to the PackWriter so that it will exclude objects correctly.
			// All other uninteresting commits should be omitted.
			if ((c.flags & RevWalk.UNINTERESTING) != 0 && !c.has(UNSHALLOW))
				produce = false;

			if (c.getCommitTime() < deepenSince) {
				produce = false;
			}

			if (produce)
				return c;
		}
	}
}
