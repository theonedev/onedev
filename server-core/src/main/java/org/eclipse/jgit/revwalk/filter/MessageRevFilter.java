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

import java.util.regex.Pattern;

import org.eclipse.jgit.internal.JGitText;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.util.RawCharSequence;
import org.eclipse.jgit.util.RawParseUtils;

/**
 * Matches only commits whose message matches the pattern.
 */
public class MessageRevFilter {
	/**
	 * Create a message filter.
	 * <p>
	 * An optimized substring search may be automatically selected if the
	 * pattern does not contain any regular expression meta-characters.
	 * <p>
	 * The search is performed using a case-insensitive comparison. The
	 * character encoding of the commit message itself is not respected. The
	 * filter matches on raw UTF-8 byte sequences.
	 *
	 * @param pattern
	 *            regular expression pattern to match.
	 * @return a new filter that matches the given expression against the
	 *         message body of the commit.
	 */
	public static RevFilter create(String pattern) {
		if (pattern.length() == 0)
			throw new IllegalArgumentException(JGitText.get().cannotMatchOnEmptyString);
		if (SubStringRevFilter.safe(pattern))
			return new SubStringSearch(pattern);
		return new PatternSearch(pattern);
	}

	private MessageRevFilter() {
		// Don't permit us to be created.
	}

	static RawCharSequence textFor(RevCommit cmit) {
		final byte[] raw = cmit.getRawBuffer();
		final int b = RawParseUtils.commitMessage(raw, 0);
		if (b < 0)
			return RawCharSequence.EMPTY;
		return new RawCharSequence(raw, b, raw.length);
	}

	private static class PatternSearch extends PatternMatchRevFilter {
		PatternSearch(String patternText) {
			super(patternText, true, true, Pattern.CASE_INSENSITIVE
					| Pattern.DOTALL);
		}

		@Override
		protected CharSequence text(RevCommit cmit) {
			return textFor(cmit);
		}

		@Override
		public RevFilter clone() {
			return new PatternSearch(pattern());
		}
	}

	private static class SubStringSearch extends SubStringRevFilter {
		SubStringSearch(String patternText) {
			super(patternText);
		}

		@Override
		protected RawCharSequence text(RevCommit cmit) {
			return textFor(cmit);
		}
	}
}
