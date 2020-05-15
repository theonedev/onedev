/*
 * Copyright (C) 2010-2012, Robin Stocker <robin@nibor.org> and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0 which is available at
 * https://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */
package org.eclipse.jgit.merge;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.util.ChangeIdUtil;
import org.eclipse.jgit.util.StringUtils;

/**
 * Formatter for constructing the commit message for a merge commit.
 * <p>
 * The format should be the same as C Git does it, for compatibility.
 */
public class MergeMessageFormatter {
	/**
	 * Construct the merge commit message.
	 *
	 * @param refsToMerge
	 *            the refs which will be merged
	 * @param target
	 *            the branch ref which will be merged into
	 * @return merge commit message
	 */
	public String format(List<Ref> refsToMerge, Ref target) {
		StringBuilder sb = new StringBuilder();
		sb.append("Merge "); //$NON-NLS-1$

		List<String> branches = new ArrayList<>();
		List<String> remoteBranches = new ArrayList<>();
		List<String> tags = new ArrayList<>();
		List<String> commits = new ArrayList<>();
		List<String> others = new ArrayList<>();
		for (Ref ref : refsToMerge) {
			if (ref.getName().startsWith(Constants.R_HEADS)) {
				branches.add("'" + Repository.shortenRefName(ref.getName()) //$NON-NLS-1$
						+ "'"); //$NON-NLS-1$
			} else if (ref.getName().startsWith(Constants.R_REMOTES)) {
				remoteBranches.add("'" //$NON-NLS-1$
						+ Repository.shortenRefName(ref.getName()) + "'"); //$NON-NLS-1$
			} else if (ref.getName().startsWith(Constants.R_TAGS)) {
				tags.add("'" + Repository.shortenRefName(ref.getName()) + "'"); //$NON-NLS-1$ //$NON-NLS-2$
			} else {
				ObjectId objectId = ref.getObjectId();
				if (objectId != null && ref.getName().equals(objectId.getName())) {
					commits.add("'" + ref.getName() + "'"); //$NON-NLS-1$ //$NON-NLS-2$
				} else {
					others.add(ref.getName());
				}
			}
		}

		List<String> listings = new ArrayList<>();

		if (!branches.isEmpty())
			listings.add(joinNames(branches, "branch", "branches")); //$NON-NLS-1$//$NON-NLS-2$

		if (!remoteBranches.isEmpty())
			listings.add(joinNames(remoteBranches, "remote-tracking branch", //$NON-NLS-1$
					"remote-tracking branches")); //$NON-NLS-1$

		if (!tags.isEmpty())
			listings.add(joinNames(tags, "tag", "tags")); //$NON-NLS-1$ //$NON-NLS-2$

		if (!commits.isEmpty())
			listings.add(joinNames(commits, "commit", "commits")); //$NON-NLS-1$ //$NON-NLS-2$

		if (!others.isEmpty())
			listings.add(StringUtils.join(others, ", ", " and ")); //$NON-NLS-1$ //$NON-NLS-2$

		sb.append(StringUtils.join(listings, ", ")); //$NON-NLS-1$

		String targetName = target.getLeaf().getName();
		if (!targetName.equals(Constants.R_HEADS + Constants.MASTER)) {
			String targetShortName = Repository.shortenRefName(targetName);
			sb.append(" into " + targetShortName); //$NON-NLS-1$
		}

		return sb.toString();
	}

	/**
	 * Add section with conflicting paths to merge message.
	 *
	 * @param message
	 *            the original merge message
	 * @param conflictingPaths
	 *            the paths with conflicts
	 * @return merge message with conflicting paths added
	 */
	public String formatWithConflicts(String message,
			List<String> conflictingPaths) {
		StringBuilder sb = new StringBuilder();
		String[] lines = message.split("\n"); //$NON-NLS-1$
		int firstFooterLine = ChangeIdUtil.indexOfFirstFooterLine(lines);
		for (int i = 0; i < firstFooterLine; i++)
			sb.append(lines[i]).append('\n');
		if (firstFooterLine == lines.length && message.length() != 0)
			sb.append('\n');
		addConflictsMessage(conflictingPaths, sb);
		if (firstFooterLine < lines.length)
			sb.append('\n');
		for (int i = firstFooterLine; i < lines.length; i++)
			sb.append(lines[i]).append('\n');
		return sb.toString();
	}

	private static void addConflictsMessage(List<String> conflictingPaths,
			StringBuilder sb) {
		sb.append("Conflicts:\n"); //$NON-NLS-1$
		for (String conflictingPath : conflictingPaths) {
			sb.append('\t').append(conflictingPath).append('\n');
		}
	}

	private static String joinNames(List<String> names, String singular,
			String plural) {
		if (names.size() == 1) {
			return singular + " " + names.get(0); //$NON-NLS-1$
		}
		return plural + " " + StringUtils.join(names, ", ", " and "); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	}
}
