/*
 * Copyright (C) 2010, Google Inc.
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

import org.eclipse.jgit.diff.DiffConfig;
import org.eclipse.jgit.errors.IncorrectObjectTypeException;
import org.eclipse.jgit.errors.MissingObjectException;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.eclipse.jgit.treewalk.filter.PathFilter;
import org.eclipse.jgit.treewalk.filter.TreeFilter;

/**
 * Updates the internal path filter to follow copy/renames.
 * <p>
 * This is a special filter that performs {@code AND(path, ANY_DIFF)}, but also
 * triggers rename detection so that the path node is updated to include a prior
 * file name as the RevWalk traverses history.
 *
 * The renames found will be reported to a {@link RenameCallback} if one is set.
 * <p>
 * Results with this filter are unpredictable if the path being followed is a
 * subdirectory.
 */
public class FollowFilter extends TreeFilter {
	/**
	 * Create a new tree filter for a user supplied path.
	 * <p>
	 * Path strings are relative to the root of the repository. If the user's
	 * input should be assumed relative to a subdirectory of the repository the
	 * caller must prepend the subdirectory's path prior to creating the filter.
	 * <p>
	 * Path strings use '/' to delimit directories on all platforms.
	 *
	 * @param path
	 *            the path to filter on. Must not be the empty string. All
	 *            trailing '/' characters will be trimmed before string's length
	 *            is checked or is used as part of the constructed filter.
	 * @param cfg
	 *            diff config specifying rename detection options.
	 * @return a new filter for the requested path.
	 * @throws IllegalArgumentException
	 *             the path supplied was the empty string.
	 * @since 3.0
	 */
	public static FollowFilter create(String path, DiffConfig cfg) {
		return new FollowFilter(PathFilter.create(path), cfg);
	}

	private final PathFilter path;
	final DiffConfig cfg;

	private RenameCallback renameCallback;

	FollowFilter(final PathFilter path, final DiffConfig cfg) {
		this.path = path;
		this.cfg = cfg;
	}

	/** @return the path this filter matches. */
	public String getPath() {
		return path.getPath();
	}

	@Override
	public boolean include(final TreeWalk walker)
			throws MissingObjectException, IncorrectObjectTypeException,
			IOException {
		return path.include(walker) && ANY_DIFF.include(walker);
	}

	@Override
	public boolean shouldBeRecursive() {
		return path.shouldBeRecursive() || ANY_DIFF.shouldBeRecursive();
	}

	@Override
	public TreeFilter clone() {
		return new FollowFilter(path.clone(), cfg);
	}

	@SuppressWarnings("nls")
	@Override
	public String toString() {
		return "(FOLLOW(" + path.toString() + ")" //
				+ " AND " //
				+ ANY_DIFF.toString() + ")";
	}

	/**
	 * @return the callback to which renames are reported, or <code>null</code>
	 *         if none
	 */
	public RenameCallback getRenameCallback() {
		return renameCallback;
	}

	/**
	 * Sets the callback to which renames shall be reported.
	 *
	 * @param callback
	 *            the callback to use
	 */
	public void setRenameCallback(RenameCallback callback) {
		renameCallback = callback;
	}
}
