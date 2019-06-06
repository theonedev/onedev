/*
 * Copyright (C) 2009, Google Inc.
 * Copyright (C) 2009, Jonas Fonseca <fonseca@diku.dk>
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

import java.text.MessageFormat;
import java.util.AbstractList;

import org.eclipse.jgit.internal.JGitText;

/**
 * An ordered list of {@link org.eclipse.jgit.revwalk.RevObject} subclasses.
 *
 * @param <E>
 *            type of subclass of RevObject the list is storing.
 */
public class RevObjectList<E extends RevObject> extends AbstractList<E> {
	static final int BLOCK_SHIFT = 8;

	static final int BLOCK_SIZE = 1 << BLOCK_SHIFT;

	/**
	 * Items stored in this list.
	 * <p>
	 * If {@link Block#shift} = 0 this block holds the list elements; otherwise
	 * it holds pointers to other {@link Block} instances which use a shift that
	 * is {@link #BLOCK_SHIFT} smaller.
	 */
	protected Block contents = new Block(0);

	/** Current number of elements in the list. */
	protected int size = 0;

	/**
	 * Create an empty object list.
	 */
	public RevObjectList() {
		// Initialized above.
	}

	/** {@inheritDoc} */
	@Override
	public void add(int index, E element) {
		if (index != size)
			throw new UnsupportedOperationException(MessageFormat.format(
					JGitText.get().unsupportedOperationNotAddAtEnd,
					Integer.valueOf(index)));
		set(index, element);
		size++;
	}

	/** {@inheritDoc} */
	@Override
	@SuppressWarnings("unchecked")
	public E set(int index, E element) {
		Block s = contents;
		while (index >> s.shift >= BLOCK_SIZE) {
			s = new Block(s.shift + BLOCK_SHIFT);
			s.contents[0] = contents;
			contents = s;
		}
		while (s.shift > 0) {
			final int i = index >> s.shift;
			index -= i << s.shift;
			if (s.contents[i] == null)
				s.contents[i] = new Block(s.shift - BLOCK_SHIFT);
			s = (Block) s.contents[i];
		}
		final Object old = s.contents[index];
		s.contents[index] = element;
		return (E) old;
	}

	/** {@inheritDoc} */
	@Override
	@SuppressWarnings("unchecked")
	public E get(int index) {
		Block s = contents;
		if (index >> s.shift >= 1024)
			return null;
		while (s != null && s.shift > 0) {
			final int i = index >> s.shift;
			index -= i << s.shift;
			s = (Block) s.contents[i];
		}
		return s != null ? (E) s.contents[index] : null;
	}

	/** {@inheritDoc} */
	@Override
	public int size() {
		return size;
	}

	/** {@inheritDoc} */
	@Override
	public void clear() {
		contents = new Block(0);
		size = 0;
	}

	/** One level of contents, either an intermediate level or a leaf level. */
	protected static class Block {
		final Object[] contents = new Object[BLOCK_SIZE];

		final int shift;

		Block(int s) {
			shift = s;
		}
	}
}
