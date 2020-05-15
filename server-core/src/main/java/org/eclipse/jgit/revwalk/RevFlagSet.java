/*
 * Copyright (C) 2008, Shawn O. Pearce <spearce@spearce.org> and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0 which is available at
 * https://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package org.eclipse.jgit.revwalk;

import java.util.AbstractSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 * Multiple application level mark bits for
 * {@link org.eclipse.jgit.revwalk.RevObject}s.
 *
 * @see RevFlag
 */
public class RevFlagSet extends AbstractSet<RevFlag> {
	int mask;

	private final List<RevFlag> active;

	/**
	 * Create an empty set of flags.
	 */
	public RevFlagSet() {
		active = new ArrayList<>();
	}

	/**
	 * Create a set of flags, copied from an existing set.
	 *
	 * @param s
	 *            the set to copy flags from.
	 */
	public RevFlagSet(RevFlagSet s) {
		mask = s.mask;
		active = new ArrayList<>(s.active);
	}

	/**
	 * Create a set of flags, copied from an existing collection.
	 *
	 * @param s
	 *            the collection to copy flags from.
	 */
	public RevFlagSet(Collection<RevFlag> s) {
		this();
		addAll(s);
	}

	/** {@inheritDoc} */
	@Override
	public boolean contains(Object o) {
		if (o instanceof RevFlag)
			return (mask & ((RevFlag) o).mask) != 0;
		return false;
	}

	/** {@inheritDoc} */
	@Override
	public boolean containsAll(Collection<?> c) {
		if (c instanceof RevFlagSet) {
			final int cMask = ((RevFlagSet) c).mask;
			return (mask & cMask) == cMask;
		}
		return super.containsAll(c);
	}

	/** {@inheritDoc} */
	@Override
	public boolean add(RevFlag flag) {
		if ((mask & flag.mask) != 0)
			return false;
		mask |= flag.mask;
		int p = 0;
		while (p < active.size() && active.get(p).mask < flag.mask)
			p++;
		active.add(p, flag);
		return true;
	}

	/** {@inheritDoc} */
	@Override
	public boolean remove(Object o) {
		final RevFlag flag = (RevFlag) o;
		if ((mask & flag.mask) == 0)
			return false;
		mask &= ~flag.mask;
		for (int i = 0; i < active.size(); i++)
			if (active.get(i).mask == flag.mask)
				active.remove(i);
		return true;
	}

	/** {@inheritDoc} */
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

	/** {@inheritDoc} */
	@Override
	public int size() {
		return active.size();
	}
}
