/*
 * Copyright (C) 2009, Christian Halstrick <christian.halstrick@sap.com> and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0 which is available at
 * https://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package org.eclipse.jgit.merge;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jgit.diff.RawText;

/**
 * A class to convert merge results into a Git conformant textual presentation
 */
public class MergeFormatter {
	/**
	 * Formats the results of a merge of {@link org.eclipse.jgit.diff.RawText}
	 * objects in a Git conformant way. This method also assumes that the
	 * {@link org.eclipse.jgit.diff.RawText} objects being merged are line
	 * oriented files which use LF as delimiter. This method will also use LF to
	 * separate chunks and conflict metadata, therefore it fits only to texts
	 * that are LF-separated lines.
	 *
	 * @param out
	 *            the output stream where to write the textual presentation
	 * @param res
	 *            the merge result which should be presented
	 * @param seqName
	 *            When a conflict is reported each conflicting range will get a
	 *            name. This name is following the "&lt;&lt;&lt;&lt;&lt;&lt;&lt;
	 *            " or "&gt;&gt;&gt;&gt;&gt;&gt;&gt; " conflict markers. The
	 *            names for the sequences are given in this list
	 * @param charsetName
	 *            the name of the character set used when writing conflict
	 *            metadata
	 * @throws java.io.IOException
	 * @deprecated Use
	 *             {@link #formatMerge(OutputStream, MergeResult, List, Charset)}
	 *             instead.
	 */
	@Deprecated
	public void formatMerge(OutputStream out, MergeResult<RawText> res,
			List<String> seqName, String charsetName) throws IOException {
		formatMerge(out, res, seqName, Charset.forName(charsetName));
	}

	/**
	 * Formats the results of a merge of {@link org.eclipse.jgit.diff.RawText}
	 * objects in a Git conformant way. This method also assumes that the
	 * {@link org.eclipse.jgit.diff.RawText} objects being merged are line
	 * oriented files which use LF as delimiter. This method will also use LF to
	 * separate chunks and conflict metadata, therefore it fits only to texts
	 * that are LF-separated lines.
	 *
	 * @param out
	 *            the output stream where to write the textual presentation
	 * @param res
	 *            the merge result which should be presented
	 * @param seqName
	 *            When a conflict is reported each conflicting range will get a
	 *            name. This name is following the "&lt;&lt;&lt;&lt;&lt;&lt;&lt;
	 *            " or "&gt;&gt;&gt;&gt;&gt;&gt;&gt; " conflict markers. The
	 *            names for the sequences are given in this list
	 * @param charset
	 *            the character set used when writing conflict metadata
	 * @throws java.io.IOException
	 * @since 5.2
	 */
	public void formatMerge(OutputStream out, MergeResult<RawText> res,
			List<String> seqName, Charset charset) throws IOException {
		new MergeFormatterPass(out, res, seqName, charset).formatMerge();
	}

	/**
	 * Formats the results of a merge of exactly two
	 * {@link org.eclipse.jgit.diff.RawText} objects in a Git conformant way.
	 * This convenience method accepts the names for the three sequences (base
	 * and the two merged sequences) as explicit parameters and doesn't require
	 * the caller to specify a List
	 *
	 * @param out
	 *            the {@link java.io.OutputStream} where to write the textual
	 *            presentation
	 * @param res
	 *            the merge result which should be presented
	 * @param baseName
	 *            the name ranges from the base should get
	 * @param oursName
	 *            the name ranges from ours should get
	 * @param theirsName
	 *            the name ranges from theirs should get
	 * @param charsetName
	 *            the name of the character set used when writing conflict
	 *            metadata
	 * @throws java.io.IOException
	 * @deprecated use
	 *             {@link #formatMerge(OutputStream, MergeResult, String, String, String, Charset)}
	 *             instead.
	 */
	@SuppressWarnings("rawtypes")
	@Deprecated
	public void formatMerge(OutputStream out, MergeResult res, String baseName,
			String oursName, String theirsName, String charsetName) throws IOException {
		formatMerge(out, res, baseName, oursName, theirsName,
				Charset.forName(charsetName));
	}

	/**
	 * Formats the results of a merge of exactly two
	 * {@link org.eclipse.jgit.diff.RawText} objects in a Git conformant way.
	 * This convenience method accepts the names for the three sequences (base
	 * and the two merged sequences) as explicit parameters and doesn't require
	 * the caller to specify a List
	 *
	 * @param out
	 *            the {@link java.io.OutputStream} where to write the textual
	 *            presentation
	 * @param res
	 *            the merge result which should be presented
	 * @param baseName
	 *            the name ranges from the base should get
	 * @param oursName
	 *            the name ranges from ours should get
	 * @param theirsName
	 *            the name ranges from theirs should get
	 * @param charset
	 *            the character set used when writing conflict metadata
	 * @throws java.io.IOException
	 * @since 5.2
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void formatMerge(OutputStream out, MergeResult res, String baseName,
			String oursName, String theirsName, Charset charset)
			throws IOException {
		List<String> names = new ArrayList<>(3);
		names.add(baseName);
		names.add(oursName);
		names.add(theirsName);
		formatMerge(out, res, names, charset);
	}
}
