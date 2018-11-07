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

import java.io.IOException;

import org.eclipse.jgit.errors.IncorrectObjectTypeException;
import org.eclipse.jgit.errors.MissingObjectException;
import org.eclipse.jgit.lib.AnyObjectId;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectIdOwnerMap;

/**
 * Base object type accessed during revision walking.
 */
public abstract class RevObject extends ObjectIdOwnerMap.Entry {
	static final int PARSED = 1;

	int flags;

	RevObject(AnyObjectId name) {
		super(name);
	}

	abstract void parseHeaders(RevWalk walk) throws MissingObjectException,
			IncorrectObjectTypeException, IOException;

	abstract void parseBody(RevWalk walk) throws MissingObjectException,
			IncorrectObjectTypeException, IOException;

	/**
	 * Get Git object type. See {@link org.eclipse.jgit.lib.Constants}.
	 *
	 * @return object type
	 */
	public abstract int getType();

	/**
	 * Get the name of this object.
	 *
	 * @return unique hash of this object.
	 */
	public final ObjectId getId() {
		return this;
	}

	/**
	 * Test to see if the flag has been set on this object.
	 *
	 * @param flag
	 *            the flag to test.
	 * @return true if the flag has been added to this object; false if not.
	 */
	public final boolean has(RevFlag flag) {
		return (flags & flag.mask) != 0;
	}

	/**
	 * Test to see if any flag in the set has been set on this object.
	 *
	 * @param set
	 *            the flags to test.
	 * @return true if any flag in the set has been added to this object; false
	 *         if not.
	 */
	public final boolean hasAny(RevFlagSet set) {
		return (flags & set.mask) != 0;
	}

	/**
	 * Test to see if all flags in the set have been set on this object.
	 *
	 * @param set
	 *            the flags to test.
	 * @return true if all flags of the set have been added to this object;
	 *         false if some or none have been added.
	 */
	public final boolean hasAll(RevFlagSet set) {
		return (flags & set.mask) == set.mask;
	}

	/**
	 * Add a flag to this object.
	 * <p>
	 * If the flag is already set on this object then the method has no effect.
	 *
	 * @param flag
	 *            the flag to mark on this object, for later testing.
	 */
	public final void add(RevFlag flag) {
		flags |= flag.mask;
	}

	/**
	 * Add a set of flags to this object.
	 *
	 * @param set
	 *            the set of flags to mark on this object, for later testing.
	 */
	public final void add(RevFlagSet set) {
		flags |= set.mask;
	}

	/**
	 * Remove a flag from this object.
	 * <p>
	 * If the flag is not set on this object then the method has no effect.
	 *
	 * @param flag
	 *            the flag to remove from this object.
	 */
	public final void remove(RevFlag flag) {
		flags &= ~flag.mask;
	}

	/**
	 * Remove a set of flags from this object.
	 *
	 * @param set
	 *            the flag to remove from this object.
	 */
	public final void remove(RevFlagSet set) {
		flags &= ~set.mask;
	}

	/** {@inheritDoc} */
	@Override
	public String toString() {
		final StringBuilder s = new StringBuilder();
		s.append(Constants.typeString(getType()));
		s.append(' ');
		s.append(name());
		s.append(' ');
		appendCoreFlags(s);
		return s.toString();
	}

	/**
	 * Append a debug description of core RevFlags to a buffer.
	 *
	 * @param s
	 *            buffer to append a debug description of core RevFlags onto.
	 */
	protected void appendCoreFlags(StringBuilder s) {
		s.append((flags & RevWalk.TOPO_DELAY) != 0 ? 'o' : '-');
		s.append((flags & RevWalk.TEMP_MARK) != 0 ? 't' : '-');
		s.append((flags & RevWalk.REWRITE) != 0 ? 'r' : '-');
		s.append((flags & RevWalk.UNINTERESTING) != 0 ? 'u' : '-');
		s.append((flags & RevWalk.SEEN) != 0 ? 's' : '-');
		s.append((flags & RevWalk.PARSED) != 0 ? 'p' : '-');
	}
}
