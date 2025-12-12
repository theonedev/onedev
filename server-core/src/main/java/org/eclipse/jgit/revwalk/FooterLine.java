/*
 * Copyright (C) 2009, Google Inc. and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0 which is available at
 * https://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package org.eclipse.jgit.revwalk;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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
	 * Extract the footer lines from the given message.
	 *
	 * @param str
	 *            the message to extract footers from.
	 * @return ordered list of footer lines; empty list if no footers found.
	 * @see RevCommit#getFooterLines()
	 * @since 6.7
	 */
	public static List<FooterLine> fromMessage(
			String str) {
		return fromMessage(str.getBytes());
	}

	/**
	 * Extract the footer lines from the given message.
	 *
	 * @param raw
	 *            the raw message to extract footers from.
	 * @return ordered list of footer lines; empty list if no footers found.
	 * @see RevCommit#getFooterLines()
	 * @since 6.7
	 */
	public static List<FooterLine> fromMessage(
			byte[] raw) {
		// Find the end of the last paragraph.
		int parEnd = raw.length;
		for (; parEnd > 0 && (raw[parEnd - 1] == '\n'
				|| raw[parEnd - 1] == ' '); --parEnd) {
			// empty
		}

		// The first non-header line is never a footer.
		int msgB = RawParseUtils.nextLfSkippingSplitLines(raw,
				RawParseUtils.hasAnyKnownHeaders(raw)
						? RawParseUtils.commitMessage(raw, 0)
						: 0);
		ArrayList<FooterLine> r = new ArrayList<>(4);
		Charset enc = RawParseUtils.guessEncoding(raw);

		// Search for the beginning of last paragraph
		int parStart = parEnd;
		for (; parStart > msgB; --parStart) {
			if (parStart < 2) {
				// Too close to beginning: this is not a raw message
				parStart = 0;
				break;
			}
			if (raw[parStart - 1] == '\n' && raw[parStart - 2] == '\n') {
				break;
			}
		}

		for (int ptr = parStart; ptr < parEnd;) {
			int keyStart = ptr;
			int keyEnd = RawParseUtils.endOfFooterLineKey(raw, ptr);
			if (keyEnd < 0) {
				// Not a well-formed footer line, skip it.
				ptr = RawParseUtils.nextLF(raw, ptr);
				continue;
			}

			// Skip over the ': *' at the end of the key before the value.
			int valStart;
			int valEnd;
			for (valStart = keyEnd + 1; valStart < raw.length
					&& raw[valStart] == ' '; ++valStart) {
				// empty
			}

			for(ptr = valStart;;) {
				ptr = RawParseUtils.nextLF(raw, ptr);
				// Next line starts with whitespace for a multiline footer.
				if (ptr == raw.length || raw[ptr] != ' ') {
					valEnd = raw[ptr - 1] == '\n' ? ptr - 1 : ptr;
					break;
				}
			}
			if (keyStart == msgB) {
				// Fist line cannot be a footer
				continue;
			}
			r.add(new FooterLine(raw, enc, keyStart, keyEnd, valStart, valEnd));
		}

		return r;
	}

	/**
	 * Get the values of all footer lines with the given key.
	 *
	 * @param footers
	 *            list of footers to find the values in.
	 * @param keyName
	 *            footer key to find values of, case-insensitive.
	 * @return values of footers with key of {@code keyName}, ordered by their
	 *         order of appearance. Duplicates may be returned if the same
	 *         footer appeared more than once. Empty list if no footers appear
	 *         with the specified key, or there are no footers at all.
	 * @see #fromMessage
	 * @since 6.7
	 */
	public static List<String> getValues(List<FooterLine> footers, String keyName) {
		return getValues(footers, new FooterKey(keyName));
	}

	/**
	 * Get the values of all footer lines with the given key.
	 *
	 * @param footers
	 *            list of footers to find the values in.
	 * @param key
	 *            footer key to find values of, case-insensitive.
	 * @return values of footers with key of {@code keyName}, ordered by their
	 *         order of appearance. Duplicates may be returned if the same
	 *         footer appeared more than once. Empty list if no footers appear
	 *         with the specified key, or there are no footers at all.
	 * @see #fromMessage
	 * @since 6.7
	 */
	public static List<String> getValues(List<FooterLine> footers, FooterKey key) {
		if (footers.isEmpty())
			return Collections.emptyList();
		ArrayList<String> r = new ArrayList<>(footers.size());
		for (FooterLine f : footers) {
			if (f.matches(key))
				r.add(f.getValue());
		}
		return r;
	}

	/**
	 * Whether keys match
	 *
	 * @param key
	 *            key to test this line's key name against.
	 * @return true if {@code key.getName().equalsIgnorecase(getKey())}.
	 */
	public boolean matches(FooterKey key) {
		final byte[] kRaw = key.raw;
		final int len = kRaw.length;
		int bPtr = keyStart;
		if (keyEnd - bPtr != len)
			return false;
		for (int kPtr = 0; kPtr < len;) {
			byte b = buffer[bPtr++];
			if ('A' <= b && b <= 'Z')
				b += (byte) ('a' - 'A');
			if (b != kRaw[kPtr++])
				return false;
		}
		return true;
	}

	/**
	 * Get key name of this footer.
	 *
	 * @return key name of this footer; that is the text before the ":" on the
	 *         line footer's line. The text is decoded according to the commit's
	 *         specified (or assumed) character encoding.
	 */
	public String getKey() {
		return RawParseUtils.decode(enc, buffer, keyStart, keyEnd);
	}

	/**
	 * Get value of this footer.
	 *
	 * @return value of this footer; that is the text after the ":" and any
	 *         leading whitespace has been skipped. May be the empty string if
	 *         the footer has no value (line ended with ":"). The text is
	 *         decoded according to the commit's specified (or assumed)
	 *         character encoding.
	 */
	public String getValue() {
		return RawParseUtils.decode(enc, buffer, valStart, valEnd).replaceAll("\n +", " "); //$NON-NLS-1$ //$NON-NLS-2$
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

	/**
	 * @return start offset of the footer relative to the original raw message
	 *         byte buffer
	 *
	 * @see #fromMessage(byte[])
	 * @since 6.9
	 */
	public int getStartOffset() {
		return keyStart;
	}

	/**
	 * @return end offset of the footer relative to the original raw message
	 *         byte buffer
	 *
	 * @see #fromMessage(byte[])
	 * @since 6.9
	 */
	public int getEndOffset() {
		return valEnd;
	}

	@Override
	public String toString() {
		return getKey() + ": " + getValue(); //$NON-NLS-1$
	}
}
