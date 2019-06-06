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
