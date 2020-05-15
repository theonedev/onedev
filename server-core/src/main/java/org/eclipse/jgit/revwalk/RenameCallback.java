/*
 * Copyright (C) 2011, GEBIT Solutions and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0 which is available at
 * https://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */
package org.eclipse.jgit.revwalk;

import org.eclipse.jgit.diff.DiffEntry;

/**
 * An instance of this class can be used in conjunction with a
 * {@link org.eclipse.jgit.revwalk.FollowFilter}. Whenever a rename has been
 * detected during a revision walk, it will be reported here.
 *
 * @see FollowFilter#setRenameCallback(RenameCallback)
 */
public abstract class RenameCallback {
	/**
	 * Called whenever a diff was found that is actually a rename or copy of a
	 * file.
	 *
	 * @param entry
	 *            the entry representing the rename/copy
	 */
	public abstract void renamed(DiffEntry entry);
}
