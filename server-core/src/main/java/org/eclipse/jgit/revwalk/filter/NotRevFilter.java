/*
 * Copyright (C) 2008, Shawn O. Pearce <spearce@spearce.org> and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0 which is available at
 * https://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package org.eclipse.jgit.revwalk.filter;

import java.io.IOException;

import org.eclipse.jgit.errors.IncorrectObjectTypeException;
import org.eclipse.jgit.errors.MissingObjectException;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;

/**
 * Includes a commit only if the subfilter does not include the commit.
 */
public class NotRevFilter extends RevFilter {
	/**
	 * Create a filter that negates the result of another filter.
	 *
	 * @param a
	 *            filter to negate.
	 * @return a filter that does the reverse of <code>a</code>.
	 */
	public static RevFilter create(RevFilter a) {
		return new NotRevFilter(a);
	}

	private final RevFilter a;

	private NotRevFilter(RevFilter one) {
		a = one;
	}

	/** {@inheritDoc} */
	@Override
	public RevFilter negate() {
		return a;
	}

	/** {@inheritDoc} */
	@Override
	public boolean include(RevWalk walker, RevCommit c)
			throws MissingObjectException, IncorrectObjectTypeException,
			IOException {
		return !a.include(walker, c);
	}

	/** {@inheritDoc} */
	@Override
	public boolean requiresCommitBody() {
		return a.requiresCommitBody();
	}

	/** {@inheritDoc} */
	@Override
	public RevFilter clone() {
		return new NotRevFilter(a.clone());
	}

	/** {@inheritDoc} */
	@Override
	public String toString() {
		return "NOT " + a.toString(); //$NON-NLS-1$
	}
}
