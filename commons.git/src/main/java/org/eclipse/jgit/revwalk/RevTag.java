/*
 * Copyright (C) 2008-2009, Google Inc.
 * Copyright (C) 2008, Marek Zawirski <marek.zawirski@gmail.com>
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

package org.eclipse.jgit.revwalk;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.IllegalCharsetNameException;
import java.nio.charset.UnsupportedCharsetException;

import org.eclipse.jgit.errors.CorruptObjectException;
import org.eclipse.jgit.errors.IncorrectObjectTypeException;
import org.eclipse.jgit.errors.MissingObjectException;
import org.eclipse.jgit.lib.AnyObjectId;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.ObjectInserter;
import org.eclipse.jgit.lib.ObjectReader;
import org.eclipse.jgit.lib.PersonIdent;
import org.eclipse.jgit.util.MutableInteger;
import org.eclipse.jgit.util.RawParseUtils;
import org.eclipse.jgit.util.StringUtils;

/** An annotated tag. */
public class RevTag extends RevObject {
	/**
	 * Parse an annotated tag from its canonical format.
	 *
	 * This method constructs a temporary revision pool, parses the tag as
	 * supplied, and returns it to the caller. Since the tag was built inside of
	 * a private revision pool its object pointer will be initialized, but will
	 * not have its headers loaded.
	 *
	 * Applications are discouraged from using this API. Callers usually need
	 * more than one object. Use {@link RevWalk#parseTag(AnyObjectId)} to obtain
	 * a RevTag from an existing repository.
	 *
	 * @param raw
	 *            the canonical formatted tag to be parsed.
	 * @return the parsed tag, in an isolated revision pool that is not
	 *         available to the caller.
	 * @throws CorruptObjectException
	 *             the tag contains a malformed header that cannot be handled.
	 */
	public static RevTag parse(byte[] raw) throws CorruptObjectException {
		return parse(new RevWalk((ObjectReader) null), raw);
	}

	/**
	 * Parse an annotated tag from its canonical format.
	 * <p>
	 * This method inserts the tag directly into the caller supplied revision
	 * pool, making it appear as though the tag exists in the repository, even
	 * if it doesn't. The repository under the pool is not affected.
	 * <p>
	 * The body of the tag (message, tagger, signature) is always retained in
	 * the returned {@code RevTag}, even if the supplied {@code RevWalk} has
	 * been configured with {@code setRetainBody(false)}.
	 *
	 * @param rw
	 *            the revision pool to allocate the tag within. The tag's object
	 *            pointer will be obtained from this pool.
	 * @param raw
	 *            the canonical formatted tag to be parsed. This buffer will be
	 *            retained by the returned {@code RevTag} and must not be
	 *            modified by the caller.
	 * @return the parsed tag, in an isolated revision pool that is not
	 *         available to the caller.
	 * @throws CorruptObjectException
	 *             the tag contains a malformed header that cannot be handled.
	 */
	public static RevTag parse(RevWalk rw, byte[] raw)
			throws CorruptObjectException {
		try (ObjectInserter.Formatter fmt = new ObjectInserter.Formatter()) {
			RevTag r = rw.lookupTag(fmt.idFor(Constants.OBJ_TAG, raw));
			r.parseCanonical(rw, raw);
			r.buffer = raw;
			return r;
		}
	}

	private RevObject object;

	private byte[] buffer;

	private String tagName;

	/**
	 * Create a new tag reference.
	 *
	 * @param id
	 *            object name for the tag.
	 */
	protected RevTag(final AnyObjectId id) {
		super(id);
	}

	@Override
	void parseHeaders(final RevWalk walk) throws MissingObjectException,
			IncorrectObjectTypeException, IOException {
		parseCanonical(walk, walk.getCachedBytes(this));
	}

	@Override
	void parseBody(final RevWalk walk) throws MissingObjectException,
			IncorrectObjectTypeException, IOException {
		if (buffer == null) {
			buffer = walk.getCachedBytes(this);
			if ((flags & PARSED) == 0)
				parseCanonical(walk, buffer);
		}
	}

	void parseCanonical(final RevWalk walk, final byte[] rawTag)
			throws CorruptObjectException {
		final MutableInteger pos = new MutableInteger();
		final int oType;

		pos.value = 53; // "object $sha1\ntype "
		oType = Constants.decodeTypeString(this, rawTag, (byte) '\n', pos);
		walk.idBuffer.fromString(rawTag, 7);
		object = walk.lookupAny(walk.idBuffer, oType);

		int p = pos.value += 4; // "tag "
		final int nameEnd = RawParseUtils.nextLF(rawTag, p) - 1;
		tagName = RawParseUtils.decode(UTF_8, rawTag, p, nameEnd);

		if (walk.isRetainBody())
			buffer = rawTag;
		flags |= PARSED;
	}

	@Override
	public final int getType() {
		return Constants.OBJ_TAG;
	}

	/**
	 * Parse the tagger identity from the raw buffer.
	 * <p>
	 * This method parses and returns the content of the tagger line, after
	 * taking the tag's character set into account and decoding the tagger
	 * name and email address. This method is fairly expensive and produces a
	 * new PersonIdent instance on each invocation. Callers should invoke this
	 * method only if they are certain they will be outputting the result, and
	 * should cache the return value for as long as necessary to use all
	 * information from it.
	 *
	 * @return identity of the tagger (name, email) and the time the tag
	 *         was made by the tagger; null if no tagger line was found.
	 */
	public final PersonIdent getTaggerIdent() {
		final byte[] raw = buffer;
		final int nameB = RawParseUtils.tagger(raw, 0);
		if (nameB < 0)
			return null;
		return RawParseUtils.parsePersonIdent(raw, nameB);
	}

	/**
	 * Parse the complete tag message and decode it to a string.
	 * <p>
	 * This method parses and returns the message portion of the tag buffer,
	 * after taking the tag's character set into account and decoding the buffer
	 * using that character set. This method is a fairly expensive operation and
	 * produces a new string on each invocation.
	 *
	 * @return decoded tag message as a string. Never null.
	 */
	public final String getFullMessage() {
		byte[] raw = buffer;
		int msgB = RawParseUtils.tagMessage(raw, 0);
		if (msgB < 0) {
			return ""; //$NON-NLS-1$
		}
		return RawParseUtils.decode(guessEncoding(), raw, msgB, raw.length);
	}

	/**
	 * Parse the tag message and return the first "line" of it.
	 * <p>
	 * The first line is everything up to the first pair of LFs. This is the
	 * "oneline" format, suitable for output in a single line display.
	 * <p>
	 * This method parses and returns the message portion of the tag buffer,
	 * after taking the tag's character set into account and decoding the buffer
	 * using that character set. This method is a fairly expensive operation and
	 * produces a new string on each invocation.
	 *
	 * @return decoded tag message as a string. Never null. The returned string
	 *         does not contain any LFs, even if the first paragraph spanned
	 *         multiple lines. Embedded LFs are converted to spaces.
	 */
	public final String getShortMessage() {
		byte[] raw = buffer;
		int msgB = RawParseUtils.tagMessage(raw, 0);
		if (msgB < 0) {
			return ""; //$NON-NLS-1$
		}

		int msgE = RawParseUtils.endOfParagraph(raw, msgB);
		String str = RawParseUtils.decode(guessEncoding(), raw, msgB, msgE);
		if (RevCommit.hasLF(raw, msgB, msgE)) {
			str = StringUtils.replaceLineBreaksWithSpace(str);
		}
		return str;
	}

	private Charset guessEncoding() {
		try {
			return RawParseUtils.parseEncoding(buffer);
		} catch (IllegalCharsetNameException | UnsupportedCharsetException e) {
			return UTF_8;
		}
	}

	/**
	 * Get a reference to the object this tag was placed on.
	 * <p>
	 * Note that the returned object has only been looked up (see
	 * {@link RevWalk#lookupAny(AnyObjectId, int)}. To access the contents it
	 * needs to be parsed, see {@link RevWalk#parseHeaders(RevObject)} and
	 * {@link RevWalk#parseBody(RevObject)}.
	 * <p>
	 * As an alternative, use {@link RevWalk#peel(RevObject)} and pass this
	 * {@link RevTag} to peel it until the first non-tag object.
	 *
	 * @return object this tag refers to (only looked up, not parsed)
	 */
	public final RevObject getObject() {
		return object;
	}

	/**
	 * Get the name of this tag, from the tag header.
	 *
	 * @return name of the tag, according to the tag header.
	 */
	public final String getTagName() {
		return tagName;
	}

	/**
	 * Discard the message buffer to reduce memory usage.
	 * <p>
	 * After discarding the memory usage of the {@code RevTag} is reduced to
	 * only the {@link #getObject()} pointer and {@link #getTagName()}.
	 * Accessing other properties such as {@link #getTaggerIdent()} or either
	 * message function requires reloading the buffer by invoking
	 * {@link RevWalk#parseBody(RevObject)}.
	 *
	 * @since 4.0
	 */
	public final void disposeBody() {
		buffer = null;
	}
}
