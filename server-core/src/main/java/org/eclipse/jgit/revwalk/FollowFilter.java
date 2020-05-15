/*
 * Copyright (C) 2010, Google Inc. and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0 which is available at
 * https://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
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
 * The renames found will be reported to a
 * {@link org.eclipse.jgit.revwalk.RenameCallback} if one is set.
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
	 * @throws java.lang.IllegalArgumentException
	 *             the path supplied was the empty string.
	 * @since 3.0
	 */
	public static FollowFilter create(String path, DiffConfig cfg) {
		return new FollowFilter(PathFilter.create(path), cfg);
	}

	private final PathFilter path;
	final DiffConfig cfg;

	private RenameCallback renameCallback;

	FollowFilter(PathFilter path, DiffConfig cfg) {
		this.path = path;
		this.cfg = cfg;
	}

	/** @return the path this filter matches. */
	/**
	 * Get the path this filter matches.
	 *
	 * @return the path this filter matches.
	 */
	public String getPath() {
		return path.getPath();
	}

	/** {@inheritDoc} */
	@Override
	public boolean include(TreeWalk walker)
			throws MissingObjectException, IncorrectObjectTypeException,
			IOException {
		return path.include(walker) && ANY_DIFF.include(walker);
	}

	/** {@inheritDoc} */
	@Override
	public boolean shouldBeRecursive() {
		return path.shouldBeRecursive() || ANY_DIFF.shouldBeRecursive();
	}

	/** {@inheritDoc} */
	@Override
	public TreeFilter clone() {
		return new FollowFilter(path.clone(), cfg);
	}

	/** {@inheritDoc} */
	@SuppressWarnings("nls")
	@Override
	public String toString() {
		return "(FOLLOW(" + path.toString() + ")" //
				+ " AND " //
				+ ANY_DIFF.toString() + ")";
	}

	/**
	 * Get the callback to which renames are reported.
	 *
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
