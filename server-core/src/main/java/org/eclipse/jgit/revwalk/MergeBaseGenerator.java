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
import java.text.MessageFormat;
import java.util.LinkedList;

import org.eclipse.jgit.errors.IncorrectObjectTypeException;
import org.eclipse.jgit.errors.MissingObjectException;
import org.eclipse.jgit.internal.JGitText;

/**
 * Computes the merge base(s) of the starting commits.
 * <p>
 * This generator is selected if the RevFilter is only
 * {@link org.eclipse.jgit.revwalk.filter.RevFilter#MERGE_BASE}.
 * <p>
 * To compute the merge base we assign a temporary flag to each of the starting
 * commits. The maximum number of starting commits is bounded by the number of
 * free flags available in the RevWalk when the generator is initialized. These
 * flags will be automatically released on the next reset of the RevWalk, but
 * not until then, as they are assigned to commits throughout the history.
 * <p>
 * Several internal flags are reused here for a different purpose, but this
 * should not have any impact as this generator should be run alone, and without
 * any other generators wrapped around it.
 */
class MergeBaseGenerator extends Generator {
	private static final int PARSED = RevWalk.PARSED;
	private static final int IN_PENDING = RevWalk.SEEN;
	private static final int POPPED = RevWalk.TEMP_MARK;
	private static final int MERGE_BASE = RevWalk.REWRITE;

	private final RevWalk walker;
	private final DateRevQueue pending;

	private int branchMask;
	private int recarryTest;
	private int recarryMask;
	private int mergeBaseAncestor = -1;
	private LinkedList<RevCommit> ret = new LinkedList<>();

	private CarryStack stack;

	MergeBaseGenerator(RevWalk w) {
		super(w.isFirstParent());
		walker = w;
		pending = new DateRevQueue(firstParent);
	}

	void init(AbstractRevQueue p) throws IOException {
		try {
			for (;;) {
				final RevCommit c = p.next();
				if (c == null)
					break;
				add(c);
			}
			// Setup the condition used by carryOntoOne to detect a late
			// merge base and produce it on the next round.
			//
			recarryTest = branchMask | POPPED;
			recarryMask = branchMask | POPPED | MERGE_BASE;
			mergeBaseAncestor = walker.allocFlag();

			for (;;) {
				RevCommit c = _next();
				if (c == null) {
					break;
				}
				ret.add(c);
			}
		} finally {
			// Always free the flags immediately. This ensures the flags
			// will be available for reuse when the walk resets.
			//
			walker.freeFlag(branchMask | mergeBaseAncestor);
		}
	}

	private void add(RevCommit c) {
		final int flag = walker.allocFlag();
		branchMask |= flag;
		if ((c.flags & branchMask) != 0) {
			// This should never happen. RevWalk ensures we get a
			// commit admitted to the initial queue only once. If
			// we see this marks aren't correctly erased.
			//
			throw new IllegalStateException(MessageFormat.format(JGitText.get().staleRevFlagsOn, c.name()));
		}
		c.flags |= flag;
		pending.add(c);
	}

	@Override
	int outputType() {
		return 0;
	}

	private RevCommit _next() throws MissingObjectException,
			IncorrectObjectTypeException, IOException {
		for (;;) {
			final RevCommit c = pending.next();
			if (c == null) {
				return null;
			}

			for (RevCommit p : c.parents) {
				if ((p.flags & IN_PENDING) != 0)
					continue;
				if ((p.flags & PARSED) == 0)
					p.parseHeaders(walker);
				p.flags |= IN_PENDING;
				pending.add(p);
			}

			int carry = c.flags & branchMask;
			boolean mb = carry == branchMask;
			if (mb) {
				// If we are a merge base make sure our ancestors are
				// also flagged as being popped, so that they do not
				// generate to the caller.
				//
				carry |= MERGE_BASE | mergeBaseAncestor;
			}
			carryOntoHistory(c, carry);

			if ((c.flags & MERGE_BASE) != 0) {
				// This commit is an ancestor of a merge base we already
				// popped back to the caller. If everyone in pending is
				// that way we are done traversing; if not we just need
				// to move to the next available commit and try again.
				//
				if (pending.everbodyHasFlag(MERGE_BASE))
					return null;
				continue;
			}
			c.flags |= POPPED;

			if (mb) {
				c.flags |= MERGE_BASE;
				return c;
			}
		}
	}

	@Override
	RevCommit next() throws MissingObjectException,
			IncorrectObjectTypeException, IOException {
		while (!ret.isEmpty()) {
			RevCommit commit = ret.remove();
			if ((commit.flags & mergeBaseAncestor) == 0) {
				return commit;
			}
		}
		return null;
	}

	private void carryOntoHistory(RevCommit c, int carry) {
		stack = null;
		for (;;) {
			carryOntoHistoryInnerLoop(c, carry);
			if (stack == null) {
				break;
			}
			c = stack.c;
			carry = stack.carry;
			stack = stack.prev;
		}
	}

	private void carryOntoHistoryInnerLoop(RevCommit c, int carry) {
		for (;;) {
			RevCommit[] parents = c.parents;
			if (parents == null || parents.length == 0) {
				break;
			}

			int e = parents.length - 1;
			for (int i = 0; i < e; i++) {
				RevCommit p = parents[i];
				if (carryOntoOne(p, carry) == CONTINUE) {
					// Walking p will be required, buffer p on stack.
					stack = new CarryStack(stack, p, carry);
				}
				// For other results from carryOntoOne:
				// HAVE_ALL: p has all bits, do nothing to skip that path.
				// CONTINUE_ON_STACK: callee pushed StackElement for p.
			}

			c = parents[e];
			if (carryOntoOne(c, carry) != CONTINUE) {
				break;
			}
		}
	}

	private static final int CONTINUE = 0;
	private static final int HAVE_ALL = 1;
	private static final int CONTINUE_ON_STACK = 2;

	private int carryOntoOne(RevCommit p, int carry) {
		// If we already had all carried flags, our parents do too.
		// Return HAVE_ALL to stop caller from running down this leg
		// of the revision graph any further.
		//
		// Otherwise return CONTINUE to ask the caller to walk history.
		int rc = (p.flags & carry) == carry ? HAVE_ALL : CONTINUE;
		p.flags |= carry;

		if ((p.flags & recarryMask) == recarryTest) {
			// We were popped without being a merge base, but we just got
			// voted to be one. Inject ourselves back at the front of the
			// pending queue and tell all of our ancestors they are within
			// the merge base now.
			p.flags &= ~POPPED;
			pending.add(p);
			stack = new CarryStack(stack, p, branchMask | MERGE_BASE);
			return CONTINUE_ON_STACK;
		}
		return rc;
	}

	private static class CarryStack {
		final CarryStack prev;
		final RevCommit c;
		final int carry;

		CarryStack(CarryStack prev, RevCommit c, int carry) {
			this.prev = prev;
			this.c = c;
			this.carry = carry;
		}
	}
}
