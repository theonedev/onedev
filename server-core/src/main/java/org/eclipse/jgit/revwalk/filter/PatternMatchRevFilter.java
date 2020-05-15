/*
 * Copyright (C) 2009, Google Inc.
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jgit.errors.IncorrectObjectTypeException;
import org.eclipse.jgit.errors.MissingObjectException;
import org.eclipse.jgit.internal.JGitText;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;

/**
 * Abstract filter that searches text using extended regular expressions.
 */
public abstract class PatternMatchRevFilter extends RevFilter {
	/**
	 * Encode a string pattern for faster matching on byte arrays.
	 * <p>
	 * Force the characters to our funny UTF-8 only convention that we use on
	 * raw buffers. This avoids needing to perform character set decodes on the
	 * individual commit buffers.
	 *
	 * @param patternText
	 *            original pattern string supplied by the user or the
	 *            application.
	 * @return same pattern, but re-encoded to match our funny raw UTF-8
	 *         character sequence {@link org.eclipse.jgit.util.RawCharSequence}.
	 */
	protected static final String forceToRaw(String patternText) {
		final byte[] b = Constants.encode(patternText);
		final StringBuilder needle = new StringBuilder(b.length);
		for (byte element : b)
			needle.append((char) (element & 0xff));
		return needle.toString();
	}

	private final String patternText;

	private final Matcher compiledPattern;

	/**
	 * Construct a new pattern matching filter.
	 *
	 * @param pattern
	 *            text of the pattern. Callers may want to surround their
	 *            pattern with ".*" on either end to allow matching in the
	 *            middle of the string.
	 * @param innerString
	 *            should .* be wrapped around the pattern of ^ and $ are
	 *            missing? Most users will want this set.
	 * @param rawEncoding
	 *            should {@link #forceToRaw(String)} be applied to the pattern
	 *            before compiling it?
	 * @param flags
	 *            flags from {@link java.util.regex.Pattern} to control how
	 *            matching performs.
	 */
	protected PatternMatchRevFilter(String pattern, final boolean innerString,
			final boolean rawEncoding, final int flags) {
		if (pattern.length() == 0)
			throw new IllegalArgumentException(JGitText.get().cannotMatchOnEmptyString);
		patternText = pattern;

		if (innerString) {
			if (!pattern.startsWith("^") && !pattern.startsWith(".*")) //$NON-NLS-1$ //$NON-NLS-2$
				pattern = ".*" + pattern; //$NON-NLS-1$
			if (!pattern.endsWith("$") && !pattern.endsWith(".*")) //$NON-NLS-1$ //$NON-NLS-2$
				pattern = pattern + ".*"; //$NON-NLS-1$
		}
		final String p = rawEncoding ? forceToRaw(pattern) : pattern;
		compiledPattern = Pattern.compile(p, flags).matcher(""); //$NON-NLS-1$
	}

	/**
	 * Get the pattern this filter uses.
	 *
	 * @return the pattern this filter is applying to candidate strings.
	 */
	public String pattern() {
		return patternText;
	}

	/** {@inheritDoc} */
	@Override
	public boolean include(RevWalk walker, RevCommit cmit)
			throws MissingObjectException, IncorrectObjectTypeException,
			IOException {
		return compiledPattern.reset(text(cmit)).matches();
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
	protected abstract CharSequence text(RevCommit cmit);

	/** {@inheritDoc} */
	@SuppressWarnings("nls")
	@Override
	public String toString() {
		return super.toString() + "(\"" + patternText + "\")";
	}
}
