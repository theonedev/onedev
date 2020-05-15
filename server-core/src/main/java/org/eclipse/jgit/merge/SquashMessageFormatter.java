/*
 * Copyright (C) 2012, IBM Corporation and others. and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0 which is available at
 * https://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */
package org.eclipse.jgit.merge;

import java.util.List;

import org.eclipse.jgit.lib.PersonIdent;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.util.GitDateFormatter;
import org.eclipse.jgit.util.GitDateFormatter.Format;

/**
 * Formatter for constructing the commit message for a squashed commit.
 * <p>
 * The format should be the same as C Git does it, for compatibility.
 */
public class SquashMessageFormatter {

	private GitDateFormatter dateFormatter;

	/**
	 * Create a new squash message formatter.
	 */
	public SquashMessageFormatter() {
		dateFormatter = new GitDateFormatter(Format.DEFAULT);
	}
	/**
	 * Construct the squashed commit message.
	 *
	 * @param squashedCommits
	 *            the squashed commits
	 * @param target
	 *            the target branch
	 * @return squashed commit message
	 */
	public String format(List<RevCommit> squashedCommits, Ref target) {
		StringBuilder sb = new StringBuilder();
		sb.append("Squashed commit of the following:\n"); //$NON-NLS-1$
		for (RevCommit c : squashedCommits) {
			sb.append("\ncommit "); //$NON-NLS-1$
			sb.append(c.getName());
			sb.append("\n"); //$NON-NLS-1$
			sb.append(toString(c.getAuthorIdent()));
			sb.append("\n\t"); //$NON-NLS-1$
			sb.append(c.getShortMessage());
			sb.append("\n"); //$NON-NLS-1$
		}
		return sb.toString();
	}

	private String toString(PersonIdent author) {
		final StringBuilder a = new StringBuilder();

		a.append("Author: "); //$NON-NLS-1$
		a.append(author.getName());
		a.append(" <"); //$NON-NLS-1$
		a.append(author.getEmailAddress());
		a.append(">\n"); //$NON-NLS-1$
		a.append("Date:   "); //$NON-NLS-1$
		a.append(dateFormatter.formatDate(author));
		a.append("\n"); //$NON-NLS-1$

		return a.toString();
	}
}
