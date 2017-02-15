/*
 * Copyright (C) 2009, Google Inc.
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

package org.eclipse.jgit.revwalk;

import java.nio.charset.Charset;

import org.eclipse.jgit.util.RawParseUtils;

/**
 * Single line at the end of a message, such as a "Signed-off-by: someone".
 * <p>
 * These footer lines tend to be used to represent additional information about
 * a commit, like the path it followed through reviewers before finally being
 * accepted into the project's main repository as an immutable commit.
 *
 * @see RevCommit#getFooterLines()
 */
public final class FooterLine {
	private final byte[] buffer;

	private final Charset enc;

	private final int keyStart;

	private final int keyEnd;

	private final int valStart;

	private final int valEnd;

	FooterLine(final byte[] b, final Charset e, final int ks, final int ke,
			final int vs, final int ve) {
		buffer = b;
		enc = e;
		keyStart = ks;
		keyEnd = ke;
		valStart = vs;
		valEnd = ve;
	}

	/**
	 * @param key
	 *            key to test this line's key name against.
	 * @return true if {@code key.getName().equalsIgnorecase(getKey())}.
	 */
	public boolean matches(final FooterKey key) {
		final byte[] kRaw = key.raw;
		final int len = kRaw.length;
		int bPtr = keyStart;
		if (keyEnd - bPtr != len)
			return false;
		for (int kPtr = 0; kPtr < len;) {
			byte b = buffer[bPtr++];
			if ('A' <= b && b <= 'Z')
				b += 'a' - 'A';
			if (b != kRaw[kPtr++])
				return false;
		}
		return true;
	}

	/**
	 * @return key name of this footer; that is the text before the ":" on the
	 *         line footer's line. The text is decoded according to the commit's
	 *         specified (or assumed) character encoding.
	 */
	public String getKey() {
		return RawParseUtils.decode(enc, buffer, keyStart, keyEnd);
	}

	/**
	 * @return value of this footer; that is the text after the ":" and any
	 *         leading whitespace has been skipped. May be the empty string if
	 *         the footer has no value (line ended with ":"). The text is
	 *         decoded according to the commit's specified (or assumed)
	 *         character encoding.
	 */
	public String getValue() {
		return RawParseUtils.decode(enc, buffer, valStart, valEnd);
	}

	/**
	 * Extract the email address (if present) from the footer.
	 * <p>
	 * If there is an email address looking string inside of angle brackets
	 * (e.g. "&lt;a@b&gt;"), the return value is the part extracted from inside the
	 * brackets. If no brackets are found, then {@link #getValue()} is returned
	 * if the value contains an '@' sign. Otherwise, null.
	 *
	 * @return email address appearing in the value of this footer, or null.
	 */
	public String getEmailAddress() {
		final int lt = RawParseUtils.nextLF(buffer, valStart, '<');
		if (valEnd <= lt) {
			final int at = RawParseUtils.nextLF(buffer, valStart, '@');
			if (valStart < at && at < valEnd)
				return getValue();
			return null;
		}
		final int gt = RawParseUtils.nextLF(buffer, lt, '>');
		if (valEnd < gt)
			return null;
		return RawParseUtils.decode(enc, buffer, lt, gt - 1);
	}

	@Override
	public String toString() {
		return getKey() + ": " + getValue(); //$NON-NLS-1$
	}
}
