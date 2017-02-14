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

package org.eclipse.jgit.revwalk.filter;

import java.io.IOException;

import org.eclipse.jgit.errors.IncorrectObjectTypeException;
import org.eclipse.jgit.errors.MissingObjectException;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.util.RawCharSequence;
import org.eclipse.jgit.util.RawSubStringPattern;

/** Abstract filter that searches text using only substring search. */
public abstract class SubStringRevFilter extends RevFilter {
	/**
	 * Can this string be safely handled by a substring filter?
	 *
	 * @param pattern
	 *            the pattern text proposed by the user.
	 * @return true if a substring filter can perform this pattern match; false
	 *         if {@link PatternMatchRevFilter} must be used instead.
	 */
	public static boolean safe(final String pattern) {
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
	protected SubStringRevFilter(final String patternText) {
		pattern = new RawSubStringPattern(patternText);
	}

	@Override
	public boolean include(final RevWalk walker, final RevCommit cmit)
			throws MissingObjectException, IncorrectObjectTypeException,
			IOException {
		return pattern.match(text(cmit)) >= 0;
	}

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

	@Override
	public RevFilter clone() {
		return this; // Typically we are actually thread-safe.
	}

	@SuppressWarnings("nls")
	@Override
	public String toString() {
		return super.toString() + "(\"" + pattern.pattern() + "\")";
	}
}
