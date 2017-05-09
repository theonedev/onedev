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

package org.eclipse.jgit.revwalk;

import java.util.AbstractSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 * Multiple application level mark bits for {@link RevObject}s.
 *
 * @see RevFlag
 */
public class RevFlagSet extends AbstractSet<RevFlag> {
	int mask;

	private final List<RevFlag> active;

	/** Create an empty set of flags. */
	public RevFlagSet() {
		active = new ArrayList<>();
	}

	/**
	 * Create a set of flags, copied from an existing set.
	 *
	 * @param s
	 *            the set to copy flags from.
	 */
	public RevFlagSet(final RevFlagSet s) {
		mask = s.mask;
		active = new ArrayList<>(s.active);
	}

	/**
	 * Create a set of flags, copied from an existing collection.
	 *
	 * @param s
	 *            the collection to copy flags from.
	 */
	public RevFlagSet(final Collection<RevFlag> s) {
		this();
		addAll(s);
	}

	@Override
	public boolean contains(final Object o) {
		if (o instanceof RevFlag)
			return (mask & ((RevFlag) o).mask) != 0;
		return false;
	}

	@Override
	public boolean containsAll(final Collection<?> c) {
		if (c instanceof RevFlagSet) {
			final int cMask = ((RevFlagSet) c).mask;
			return (mask & cMask) == cMask;
		}
		return super.containsAll(c);
	}

	@Override
	public boolean add(final RevFlag flag) {
		if ((mask & flag.mask) != 0)
			return false;
		mask |= flag.mask;
		int p = 0;
		while (p < active.size() && active.get(p).mask < flag.mask)
			p++;
		active.add(p, flag);
		return true;
	}

	@Override
	public boolean remove(final Object o) {
		final RevFlag flag = (RevFlag) o;
		if ((mask & flag.mask) == 0)
			return false;
		mask &= ~flag.mask;
		for (int i = 0; i < active.size(); i++)
			if (active.get(i).mask == flag.mask)
				active.remove(i);
		return true;
	}

	@Override
	public Iterator<RevFlag> iterator() {
		final Iterator<RevFlag> i = active.iterator();
		return new Iterator<RevFlag>() {
			private RevFlag current;

			@Override
			public boolean hasNext() {
				return i.hasNext();
			}

			@Override
			public RevFlag next() {
				return current = i.next();
			}

			@Override
			public void remove() {
				mask &= ~current.mask;
				i.remove();
			}
		};
	}

	@Override
	public int size() {
		return active.size();
	}
}
