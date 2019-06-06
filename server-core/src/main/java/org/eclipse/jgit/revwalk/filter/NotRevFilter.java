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

import java.io.IOException;

import org.eclipse.jgit.errors.IncorrectObjectTypeException;
import org.eclipse.jgit.errors.MissingObjectException;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;

/**
 * Includes a commit only if the subfilter does not include the commit.
 */
public class NotRevFilter extends RevFilter {
	/**
	 * Create a filter that negates the result of another filter.
	 *
	 * @param a
	 *            filter to negate.
	 * @return a filter that does the reverse of <code>a</code>.
	 */
	public static RevFilter create(RevFilter a) {
		return new NotRevFilter(a);
	}

	private final RevFilter a;

	private NotRevFilter(RevFilter one) {
		a = one;
	}

	/** {@inheritDoc} */
	@Override
	public RevFilter negate() {
		return a;
	}

	/** {@inheritDoc} */
	@Override
	public boolean include(RevWalk walker, RevCommit c)
			throws MissingObjectException, IncorrectObjectTypeException,
			IOException {
		return !a.include(walker, c);
	}

	/** {@inheritDoc} */
	@Override
	public boolean requiresCommitBody() {
		return a.requiresCommitBody();
	}

	/** {@inheritDoc} */
	@Override
	public RevFilter clone() {
		return new NotRevFilter(a.clone());
	}

	/** {@inheritDoc} */
	@Override
	public String toString() {
		return "NOT " + a.toString(); //$NON-NLS-1$
	}
}
