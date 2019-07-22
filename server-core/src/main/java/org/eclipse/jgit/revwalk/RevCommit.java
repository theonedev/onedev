/*
 * Copyright (C) 2008-2009, Google Inc.
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.eclipse.jgit.annotations.Nullable;
import org.eclipse.jgit.errors.IncorrectObjectTypeException;
import org.eclipse.jgit.errors.MissingObjectException;
import org.eclipse.jgit.lib.AnyObjectId;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.MutableObjectId;
import org.eclipse.jgit.lib.ObjectInserter;
import org.eclipse.jgit.lib.ObjectReader;
import org.eclipse.jgit.lib.PersonIdent;
import org.eclipse.jgit.util.RawParseUtils;
import org.eclipse.jgit.util.StringUtils;

/**
 * A commit reference to a commit in the DAG.
 */
public class RevCommit extends RevObject {
	private static final int STACK_DEPTH = 500;

	/**
	 * Parse a commit from its canonical format.
	 *
	 * This method constructs a temporary revision pool, parses the commit as
	 * supplied, and returns it to the caller. Since the commit was built inside
	 * of a private revision pool its parent pointers will be initialized, but
	 * will not have their headers loaded.
	 *
	 * Applications are discouraged from using this API. Callers usually need
	 * more than one commit. Use
	 * {@link org.eclipse.jgit.revwalk.RevWalk#parseCommit(AnyObjectId)} to
	 * obtain a RevCommit from an existing repository.
	 *
	 * @param raw
	 *            the canonical formatted commit to be parsed.
	 * @return the parsed commit, in an isolated revision pool that is not
	 *         available to the caller.
	 */
	public static RevCommit parse(byte[] raw) {
		try {
			return parse(new RevWalk((ObjectReader) null), raw);
		} catch (IOException ex) {
			throw new RuntimeException(ex);
		}
	}

	/**
	 * Parse a commit from its canonical format.
	 * <p>
	 * This method inserts the commit directly into the caller supplied revision
	 * pool, making it appear as though the commit exists in the repository,
	 * even if it doesn't. The repository under the pool is not affected.
	 * <p>
	 * The body of the commit (message, author, committer) is always retained in
	 * the returned {@code RevCommit}, even if the supplied {@code RevWalk} has
	 * been configured with {@code setRetainBody(false)}.
	 *
	 * @param rw
	 *            the revision pool to allocate the commit within. The commit's
	 *            tree and parent pointers will be obtained from this pool.
	 * @param raw
	 *            the canonical formatted commit to be parsed. This buffer will
	 *            be retained by the returned {@code RevCommit} and must not be
	 *            modified by the caller.
	 * @return the parsed commit, in an isolated revision pool that is not
	 *         available to the caller.
	 * @throws java.io.IOException
	 *             in case of RevWalk initialization fails
	 */
	public static RevCommit parse(RevWalk rw, byte[] raw) throws IOException {
		try (ObjectInserter.Formatter fmt = new ObjectInserter.Formatter()) {
			RevCommit r = rw.lookupCommit(fmt.idFor(Constants.OBJ_COMMIT, raw));
			r.parseCanonical(rw, raw);
			r.buffer = raw;
			return r;
		}
	}

	static final RevCommit[] NO_PARENTS = {};

	private RevTree tree;

	RevCommit[] parents;

	int commitTime; // An int here for performance, overflows in 2038

	int inDegree;

	private byte[] buffer;

	/**
	 * Create a new commit reference.
	 *
	 * @param id
	 *            object name for the commit.
	 */
	protected RevCommit(AnyObjectId id) {
		super(id);
	}

	@Override
	void parseHeaders(RevWalk walk) throws MissingObjectException,
			IncorrectObjectTypeException, IOException {
		parseCanonical(walk, walk.getCachedBytes(this));
	}

	@Override
	void parseBody(RevWalk walk) throws MissingObjectException,
			IncorrectObjectTypeException, IOException {
		if (buffer == null) {
			buffer = walk.getCachedBytes(this);
			if ((flags & PARSED) == 0)
				parseCanonical(walk, buffer);
		}
	}

	void parseCanonical(RevWalk walk, byte[] raw) throws IOException {
		if (!walk.shallowCommitsInitialized) {
			walk.initializeShallowCommits(this);
		}

		final MutableObjectId idBuffer = walk.idBuffer;
		idBuffer.fromString(raw, 5);
		tree = walk.lookupTree(idBuffer);

		int ptr = 46;
		if (parents == null) {
			RevCommit[] pList = new RevCommit[1];
			int nParents = 0;
			for (;;) {
				if (raw[ptr] != 'p') {
					break;
				}
				idBuffer.fromString(raw, ptr + 7);
				final RevCommit p = walk.lookupCommit(idBuffer);
				if (nParents == 0) {
					pList[nParents++] = p;
				} else if (nParents == 1) {
					pList = new RevCommit[] { pList[0], p };
					nParents = 2;
				} else {
					if (pList.length <= nParents) {
						RevCommit[] old = pList;
						pList = new RevCommit[pList.length + 32];
						System.arraycopy(old, 0, pList, 0, nParents);
					}
					pList[nParents++] = p;
				}
				ptr += 48;
			}
			if (nParents != pList.length) {
				RevCommit[] old = pList;
				pList = new RevCommit[nParents];
				System.arraycopy(old, 0, pList, 0, nParents);
			}
			parents = pList;
		}

		// extract time from "committer "
		ptr = RawParseUtils.committer(raw, ptr);
		if (ptr > 0) {
			ptr = RawParseUtils.nextLF(raw, ptr, '>');

			// In 2038 commitTime will overflow unless it is changed to long.
			commitTime = RawParseUtils.parseBase10(raw, ptr, null);
		}

		if (walk.isRetainBody()) {
			buffer = raw;
		}
		flags |= PARSED;
	}

	/** {@inheritDoc} */
	@Override
	public final int getType() {
		return Constants.OBJ_COMMIT;
	}

	static void carryFlags(RevCommit c, int carry) {
		FIFORevQueue q = carryFlags1(c, carry, 0);
		if (q != null)
			slowCarryFlags(q, carry);
	}

	private static FIFORevQueue carryFlags1(RevCommit c, int carry, int depth) {
		for(;;) {
			RevCommit[] pList = c.parents;
			if (pList == null || pList.length == 0)
				return null;
			if (pList.length != 1) {
				if (depth == STACK_DEPTH)
					return defer(c);
				for (int i = 1; i < pList.length; i++) {
					RevCommit p = pList[i];
					if ((p.flags & carry) == carry)
						continue;
					p.flags |= carry;
					FIFORevQueue q = carryFlags1(p, carry, depth + 1);
					if (q != null)
						return defer(q, carry, pList, i + 1);
				}
			}

			c = pList[0];
			if ((c.flags & carry) == carry)
				return null;
			c.flags |= carry;
		}
	}

	private static FIFORevQueue defer(RevCommit c) {
		FIFORevQueue q = new FIFORevQueue();
		q.add(c);
		return q;
	}

	private static FIFORevQueue defer(FIFORevQueue q, int carry,
			RevCommit[] pList, int i) {
		// In normal case the caller will run pList[0] in a tail recursive
		// fashion by updating the variable. However the caller is unwinding
		// the stack and will skip that pList[0] execution step.
		carryOneStep(q, carry, pList[0]);

		// Remaining parents (if any) need to have flags checked and be
		// enqueued if they have ancestors.
		for (; i < pList.length; i++)
			carryOneStep(q, carry, pList[i]);
		return q;
	}

	private static void slowCarryFlags(FIFORevQueue q, int carry) {
		// Commits in q have non-null parent arrays and have set all
		// flags in carry. This loop finishes copying over the graph.
		for (RevCommit c; (c = q.next()) != null;) {
			for (RevCommit p : c.parents)
				carryOneStep(q, carry, p);
		}
	}

	private static void carryOneStep(FIFORevQueue q, int carry, RevCommit c) {
		if ((c.flags & carry) != carry) {
			c.flags |= carry;
			if (c.parents != null)
				q.add(c);
		}
	}

	/**
	 * Carry a RevFlag set on this commit to its parents.
	 * <p>
	 * If this commit is parsed, has parents, and has the supplied flag set on
	 * it we automatically add it to the parents, grand-parents, and so on until
	 * an unparsed commit or a commit with no parents is discovered. This
	 * permits applications to force a flag through the history chain when
	 * necessary.
	 *
	 * @param flag
	 *            the single flag value to carry back onto parents.
	 */
	public void carry(RevFlag flag) {
		final int carry = flags & flag.mask;
		if (carry != 0)
			carryFlags(this, carry);
	}

	/**
	 * Time from the "committer " line of the buffer.
	 *
	 * @return commit time
	 */
	public final int getCommitTime() {
		return commitTime;
	}

	/**
	 * Get a reference to this commit's tree.
	 *
	 * @return tree of this commit.
	 */
	public final RevTree getTree() {
		return tree;
	}

	/**
	 * Get the number of parent commits listed in this commit.
	 *
	 * @return number of parents; always a positive value but can be 0.
	 */
	public final int getParentCount() {
		return parents.length;
	}

	/**
	 * Get the nth parent from this commit's parent list.
	 *
	 * @param nth
	 *            parent index to obtain. Must be in the range 0 through
	 *            {@link #getParentCount()}-1.
	 * @return the specified parent.
	 * @throws java.lang.ArrayIndexOutOfBoundsException
	 *             an invalid parent index was specified.
	 */
	public final RevCommit getParent(int nth) {
		return parents[nth];
	}

	/**
	 * Obtain an array of all parents (<b>NOTE - THIS IS NOT A COPY</b>).
	 * <p>
	 * This method is exposed only to provide very fast, efficient access to
	 * this commit's parent list. Applications relying on this list should be
	 * very careful to ensure they do not modify its contents during their use
	 * of it.
	 *
	 * @return the array of parents.
	 */
	public final RevCommit[] getParents() {
		return parents;
	}

	/**
	 * Obtain the raw unparsed commit body (<b>NOTE - THIS IS NOT A COPY</b>).
	 * <p>
	 * This method is exposed only to provide very fast, efficient access to
	 * this commit's message buffer within a RevFilter. Applications relying on
	 * this buffer should be very careful to ensure they do not modify its
	 * contents during their use of it.
	 *
	 * @return the raw unparsed commit body. This is <b>NOT A COPY</b>.
	 *         Altering the contents of this buffer may alter the walker's
	 *         knowledge of this commit, and the results it produces.
	 */
	public final byte[] getRawBuffer() {
		return buffer;
	}

	/**
	 * Parse the gpg signature from the raw buffer.
	 * <p>
	 * This method parses and returns the raw content of the gpgsig lines. This
	 * method is fairly expensive and produces a new byte[] instance on each
	 * invocation. Callers should invoke this method only if they are certain
	 * they will need, and should cache the return value for as long as
	 * necessary to use all information from it.
	 * <p>
	 * RevFilter implementations should try to use
	 * {@link org.eclipse.jgit.util.RawParseUtils} to scan the
	 * {@link #getRawBuffer()} instead, as this will allow faster evaluation of
	 * commits.
	 *
	 * @return contents of the gpg signature; null if the commit was not signed.
	 * @since 5.1
	 */
	public final @Nullable byte[] getRawGpgSignature() {
		final byte[] raw = buffer;
		final byte[] header = {'g', 'p', 'g', 's', 'i', 'g'};
		final int start = RawParseUtils.headerStart(header, raw, 0);
		if (start < 0) {
			return null;
		}
		final int end = RawParseUtils.headerEnd(raw, start);
		return Arrays.copyOfRange(raw, start, end);
	}

	/**
	 * Parse the author identity from the raw buffer.
	 * <p>
	 * This method parses and returns the content of the author line, after
	 * taking the commit's character set into account and decoding the author
	 * name and email address. This method is fairly expensive and produces a
	 * new PersonIdent instance on each invocation. Callers should invoke this
	 * method only if they are certain they will be outputting the result, and
	 * should cache the return value for as long as necessary to use all
	 * information from it.
	 * <p>
	 * RevFilter implementations should try to use
	 * {@link org.eclipse.jgit.util.RawParseUtils} to scan the
	 * {@link #getRawBuffer()} instead, as this will allow faster evaluation of
	 * commits.
	 *
	 * @return identity of the author (name, email) and the time the commit was
	 *         made by the author; null if no author line was found.
	 */
	public final PersonIdent getAuthorIdent() {
		final byte[] raw = buffer;
		final int nameB = RawParseUtils.author(raw, 0);
		if (nameB < 0)
			return null;
		return RawParseUtils.parsePersonIdent(raw, nameB);
	}

	/**
	 * Parse the committer identity from the raw buffer.
	 * <p>
	 * This method parses and returns the content of the committer line, after
	 * taking the commit's character set into account and decoding the committer
	 * name and email address. This method is fairly expensive and produces a
	 * new PersonIdent instance on each invocation. Callers should invoke this
	 * method only if they are certain they will be outputting the result, and
	 * should cache the return value for as long as necessary to use all
	 * information from it.
	 * <p>
	 * RevFilter implementations should try to use
	 * {@link org.eclipse.jgit.util.RawParseUtils} to scan the
	 * {@link #getRawBuffer()} instead, as this will allow faster evaluation of
	 * commits.
	 *
	 * @return identity of the committer (name, email) and the time the commit
	 *         was made by the committer; null if no committer line was found.
	 */
	public final PersonIdent getCommitterIdent() {
		final byte[] raw = buffer;
		final int nameB = RawParseUtils.committer(raw, 0);
		if (nameB < 0)
			return null;
		return RawParseUtils.parsePersonIdent(raw, nameB);
	}

	/**
	 * Parse the complete commit message and decode it to a string.
	 * <p>
	 * This method parses and returns the message portion of the commit buffer,
	 * after taking the commit's character set into account and decoding the
	 * buffer using that character set. This method is a fairly expensive
	 * operation and produces a new string on each invocation.
	 *
	 * @return decoded commit message as a string. Never null.
	 */
	public final String getFullMessage() {
		byte[] raw = buffer;
		int msgB = RawParseUtils.commitMessage(raw, 0);
		if (msgB < 0) {
			return ""; //$NON-NLS-1$
		}
		return RawParseUtils.decode(guessEncoding(), raw, msgB, raw.length);
	}

	/**
	 * Parse the commit message and return the first "line" of it.
	 * <p>
	 * The first line is everything up to the first pair of LFs. This is the
	 * "oneline" format, suitable for output in a single line display.
	 * <p>
	 * This method parses and returns the message portion of the commit buffer,
	 * after taking the commit's character set into account and decoding the
	 * buffer using that character set. This method is a fairly expensive
	 * operation and produces a new string on each invocation.
	 *
	 * @return decoded commit message as a string. Never null. The returned
	 *         string does not contain any LFs, even if the first paragraph
	 *         spanned multiple lines. Embedded LFs are converted to spaces.
	 */
	public final String getShortMessage() {
		byte[] raw = buffer;
		int msgB = RawParseUtils.commitMessage(raw, 0);
		if (msgB < 0) {
			return ""; //$NON-NLS-1$
		}

		int msgE = RawParseUtils.endOfParagraph(raw, msgB);
		String str = RawParseUtils.decode(guessEncoding(), raw, msgB, msgE);
		if (hasLF(raw, msgB, msgE)) {
			str = StringUtils.replaceLineBreaksWithSpace(str);
		}
		return str;
	}

	static boolean hasLF(byte[] r, int b, int e) {
		while (b < e)
			if (r[b++] == '\n')
				return true;
		return false;
	}

	/**
	 * Determine the encoding of the commit message buffer.
	 * <p>
	 * Locates the "encoding" header (if present) and returns its value. Due to
	 * corruption in the wild this may be an invalid encoding name that is not
	 * recognized by any character encoding library.
	 * <p>
	 * If no encoding header is present, null.
	 *
	 * @return the preferred encoding of {@link #getRawBuffer()}; or null.
	 * @since 4.2
	 */
	@Nullable
	public final String getEncodingName() {
		return RawParseUtils.parseEncodingName(buffer);
	}

	/**
	 * Determine the encoding of the commit message buffer.
	 * <p>
	 * Locates the "encoding" header (if present) and then returns the proper
	 * character set to apply to this buffer to evaluate its contents as
	 * character data.
	 * <p>
	 * If no encoding header is present {@code UTF-8} is assumed.
	 *
	 * @return the preferred encoding of {@link #getRawBuffer()}.
	 * @throws IllegalCharsetNameException
	 *             if the character set requested by the encoding header is
	 *             malformed and unsupportable.
	 * @throws UnsupportedCharsetException
	 *             if the JRE does not support the character set requested by
	 *             the encoding header.
	 */
	public final Charset getEncoding() {
		return RawParseUtils.parseEncoding(buffer);
	}

	private Charset guessEncoding() {
		try {
			return getEncoding();
		} catch (IllegalCharsetNameException | UnsupportedCharsetException e) {
			return UTF_8;
		}
	}

	/**
	 * Parse the footer lines (e.g. "Signed-off-by") for machine processing.
	 * <p>
	 * This method splits all of the footer lines out of the last paragraph of
	 * the commit message, providing each line as a key-value pair, ordered by
	 * the order of the line's appearance in the commit message itself.
	 * <p>
	 * A footer line's key must match the pattern {@code ^[A-Za-z0-9-]+:}, while
	 * the value is free-form, but must not contain an LF. Very common keys seen
	 * in the wild are:
	 * <ul>
	 * <li>{@code Signed-off-by} (agrees to Developer Certificate of Origin)
	 * <li>{@code Acked-by} (thinks change looks sane in context)
	 * <li>{@code Reported-by} (originally found the issue this change fixes)
	 * <li>{@code Tested-by} (validated change fixes the issue for them)
	 * <li>{@code CC}, {@code Cc} (copy on all email related to this change)
	 * <li>{@code Bug} (link to project's bug tracking system)
	 * </ul>
	 *
	 * @return ordered list of footer lines; empty list if no footers found.
	 */
	public final List<FooterLine> getFooterLines() {
		final byte[] raw = buffer;
		int ptr = raw.length - 1;
		while (raw[ptr] == '\n') // trim any trailing LFs, not interesting
			ptr--;

		final int msgB = RawParseUtils.commitMessage(raw, 0);
		final ArrayList<FooterLine> r = new ArrayList<>(4);
		final Charset enc = guessEncoding();
		for (;;) {
			ptr = RawParseUtils.prevLF(raw, ptr);
			if (ptr <= msgB)
				break; // Don't parse commit headers as footer lines.

			final int keyStart = ptr + 2;
			if (raw[keyStart] == '\n')
				break; // Stop at first paragraph break, no footers above it.

			final int keyEnd = RawParseUtils.endOfFooterLineKey(raw, keyStart);
			if (keyEnd < 0)
				continue; // Not a well formed footer line, skip it.

			// Skip over the ': *' at the end of the key before the value.
			//
			int valStart = keyEnd + 1;
			while (valStart < raw.length && raw[valStart] == ' ')
				valStart++;

			// Value ends at the LF, and does not include it.
			//
			int valEnd = RawParseUtils.nextLF(raw, valStart);
			if (raw[valEnd - 1] == '\n')
				valEnd--;

			r.add(new FooterLine(raw, enc, keyStart, keyEnd, valStart, valEnd));
		}
		Collections.reverse(r);
		return r;
	}

	/**
	 * Get the values of all footer lines with the given key.
	 *
	 * @param keyName
	 *            footer key to find values of, case insensitive.
	 * @return values of footers with key of {@code keyName}, ordered by their
	 *         order of appearance. Duplicates may be returned if the same
	 *         footer appeared more than once. Empty list if no footers appear
	 *         with the specified key, or there are no footers at all.
	 * @see #getFooterLines()
	 */
	public final List<String> getFooterLines(String keyName) {
		return getFooterLines(new FooterKey(keyName));
	}

	/**
	 * Get the values of all footer lines with the given key.
	 *
	 * @param keyName
	 *            footer key to find values of, case insensitive.
	 * @return values of footers with key of {@code keyName}, ordered by their
	 *         order of appearance. Duplicates may be returned if the same
	 *         footer appeared more than once. Empty list if no footers appear
	 *         with the specified key, or there are no footers at all.
	 * @see #getFooterLines()
	 */
	public final List<String> getFooterLines(FooterKey keyName) {
		final List<FooterLine> src = getFooterLines();
		if (src.isEmpty())
			return Collections.emptyList();
		final ArrayList<String> r = new ArrayList<>(src.size());
		for (FooterLine f : src) {
			if (f.matches(keyName))
				r.add(f.getValue());
		}
		return r;
	}

	/**
	 * Reset this commit to allow another RevWalk with the same instances.
	 * <p>
	 * Subclasses <b>must</b> call <code>super.reset()</code> to ensure the
	 * basic information can be correctly cleared out.
	 */
	public void reset() {
		inDegree = 0;
	}

	/**
	 * Discard the message buffer to reduce memory usage.
	 * <p>
	 * After discarding the memory usage of the {@code RevCommit} is reduced to
	 * only the {@link #getTree()} and {@link #getParents()} pointers and the
	 * time in {@link #getCommitTime()}. Accessing other properties such as
	 * {@link #getAuthorIdent()}, {@link #getCommitterIdent()} or either message
	 * function requires reloading the buffer by invoking
	 * {@link org.eclipse.jgit.revwalk.RevWalk#parseBody(RevObject)}.
	 *
	 * @since 4.0
	 */
	public final void disposeBody() {
		buffer = null;
	}

	/** {@inheritDoc} */
	@Override
	public String toString() {
		final StringBuilder s = new StringBuilder();
		s.append(Constants.typeString(getType()));
		s.append(' ');
		s.append(name());
		s.append(' ');
		s.append(commitTime);
		s.append(' ');
		appendCoreFlags(s);
		return s.toString();
	}
}
