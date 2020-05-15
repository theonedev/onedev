/*
 * Copyright (C) 2011, Tomasz Zarna <Tomasz.Zarna@pl.ibm.com> and others
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
import org.eclipse.jgit.errors.StopWalkException;
import org.eclipse.jgit.internal.JGitText;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;

/**
 * Limits the number of commits output.
 */
public class MaxCountRevFilter extends RevFilter {

	private int maxCount;

	private int count;

	/**
	 * Create a new max count filter.
	 *
	 * @param maxCount
	 *            the limit
	 * @return a new filter
	 */
	public static RevFilter create(int maxCount) {
		if (maxCount < 0)
			throw new IllegalArgumentException(
					JGitText.get().maxCountMustBeNonNegative);
		return new MaxCountRevFilter(maxCount);
	}

	private MaxCountRevFilter(int maxCount) {
		this.count = 0;
		this.maxCount = maxCount;
	}

	/** {@inheritDoc} */
	@Override
	public boolean include(RevWalk walker, RevCommit cmit)
			throws StopWalkException, MissingObjectException,
			IncorrectObjectTypeException, IOException {
		count++;
		if (count > maxCount)
			throw StopWalkException.INSTANCE;
		return true;
	}

	/** {@inheritDoc} */
	@Override
	public RevFilter clone() {
		return new MaxCountRevFilter(maxCount);
	}
}
