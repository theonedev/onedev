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
 * Filter that includes commits after a configured number are skipped.
 */
public class SkipRevFilter extends RevFilter {

	private final int skip;

	private int count;

	/**
	 * Create a new skip filter.
	 *
	 * @param skip
	 *            the number of commits to skip
	 * @return a new filter
	 */
	public static RevFilter create(int skip) {
		if (skip < 0)
			throw new IllegalArgumentException(
					JGitText.get().skipMustBeNonNegative);
		return new SkipRevFilter(skip);
	}

	private SkipRevFilter(int skip) {
		this.skip = skip;
	}

	/** {@inheritDoc} */
	@Override
	public boolean include(RevWalk walker, RevCommit cmit)
			throws StopWalkException, MissingObjectException,
			IncorrectObjectTypeException, IOException {
		if (skip > count++)
			return false;
		return true;
	}

	/** {@inheritDoc} */
	@Override
	public RevFilter clone() {
		return new SkipRevFilter(skip);
	}
}
