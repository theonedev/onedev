/**
 * Copyright (C) 2015, Google Inc.
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

import java.io.IOException;

import org.eclipse.jgit.errors.IncorrectObjectTypeException;
import org.eclipse.jgit.errors.MissingObjectException;
import org.eclipse.jgit.lib.AnyObjectId;
import org.eclipse.jgit.revwalk.ObjectWalk;

/**
 * Selects interesting objects when walking.
 * <p>
 * Applications should install the filter on an ObjectWalk by
 * {@link org.eclipse.jgit.revwalk.ObjectWalk#setObjectFilter(ObjectFilter)}
 * prior to starting traversal.
 *
 * @since 4.0
 */
public abstract class ObjectFilter {
	/** Default filter that always returns true. */
	public static final ObjectFilter ALL = new AllFilter();

	private static final class AllFilter extends ObjectFilter {
		@Override
		public boolean include(ObjectWalk walker, AnyObjectId o) {
			return true;
		}
	}

	/**
	 * Determine if the named object should be included in the walk.
	 *
	 * @param walker
	 *            the active walker this filter is being invoked from within.
	 * @param objid
	 *            the object currently being tested.
	 * @return {@code true} if the named object should be included in the walk.
	 * @throws org.eclipse.jgit.errors.MissingObjectException
	 *             an object the filter needed to consult to determine its
	 *             answer was missing
	 * @throws org.eclipse.jgit.errors.IncorrectObjectTypeException
	 *             an object the filter needed to consult to determine its
	 *             answer was of the wrong type
	 * @throws java.io.IOException
	 *             an object the filter needed to consult to determine its
	 *             answer could not be read.
	 */
	public abstract boolean include(ObjectWalk walker, AnyObjectId objid)
			throws MissingObjectException, IncorrectObjectTypeException,
			       IOException;
}
