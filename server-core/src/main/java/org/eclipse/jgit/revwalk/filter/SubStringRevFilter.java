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
import org.eclipse.jgit.util.RawCharSequence;
import org.eclipse.jgit.util.RawSubStringPattern;

/**
 * Abstract filter that searches text using only substring search.
 */
public abstract class SubStringRevFilter extends RevFilter {
	/**
	 * Can this string be safely handled by a substring filter?
	 *
	 * @param pattern
	 *            the pattern text proposed by the user.
	 * @return true if a substring filter can perform this pattern match; false
	 *         if {@link org.eclipse.jgit.revwalk.filter.PatternMatchRevFilter}
	 *         must be used instead.
	 */
	public static boolean safe(String pattern) {
		for (int i = 0; i < pattern.length(); i++) {
			final char c = pattern.charAt(i);
			switch (c) {
			case '.':
			case '?':
			case '*':
			case '+':
			case '{':
			case '}':
			case '(':
			case ')':
			case '[':
			case ']':
			case '\\':
				return false;
			}
		}
		return true;
	}

	private final RawSubStringPattern pattern;

	/**
	 * Construct a new matching filter.
	 *
	 * @param patternText
	 *            text to locate. This should be a safe string as described by
	 *            the {@link #safe(String)} as regular expression meta
	 *            characters are treated as literals.
	 */
	protected SubStringRevFilter(String patternText) {
		pattern = new RawSubStringPattern(patternText);
	}

	/** {@inheritDoc} */
	@Override
	public boolean include(RevWalk walker, RevCommit cmit)
			throws MissingObjectException, IncorrectObjectTypeException,
			IOException {
		return pattern.match(text(cmit)) >= 0;
	}

	/** {@inheritDoc} */
	@Override
	public boolean requiresCommitBody() {
		return true;
	}

	/**
	 * Obtain the raw text to match against.
	 *
	 * @param cmit
	 *            current commit being evaluated.
	 * @return sequence for the commit's content that we need to match on.
	 */
	protected abstract RawCharSequence text(RevCommit cmit);

	/** {@inheritDoc} */
	@Override
	public RevFilter clone() {
		return this; // Typically we are actually thread-safe.
	}

	/** {@inheritDoc} */
	@SuppressWarnings("nls")
	@Override
	public String toString() {
		return super.toString() + "(\"" + pattern.pattern() + "\")";
	}
}
