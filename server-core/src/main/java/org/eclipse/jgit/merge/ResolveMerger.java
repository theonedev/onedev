/*
 * Copyright (C) 2010, Christian Halstrick <christian.halstrick@sap.com>,
 * Copyright (C) 2010-2012, Matthias Sohn <matthias.sohn@sap.com>
 * Copyright (C) 2012, Research In Motion Limited
 * Copyright (C) 2017, Obeo (mathieu.cartaud@obeo.fr)
 * Copyright (C) 2018, Thomas Wolf <thomas.wolf@paranor.ch> and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0 which is available at
 * https://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */
package org.eclipse.jgit.merge;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.time.Instant.EPOCH;
import static org.eclipse.jgit.diff.DiffAlgorithm.SupportedAlgorithm.HISTOGRAM;
import static org.eclipse.jgit.lib.ConfigConstants.CONFIG_DIFF_SECTION;
import static org.eclipse.jgit.lib.ConfigConstants.CONFIG_KEY_ALGORITHM;
import static org.eclipse.jgit.lib.Constants.OBJ_BLOB;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.eclipse.jgit.attributes.Attributes;
import org.eclipse.jgit.diff.DiffAlgorithm;
import org.eclipse.jgit.diff.DiffAlgorithm.SupportedAlgorithm;
import org.eclipse.jgit.diff.RawText;
import org.eclipse.jgit.diff.RawTextComparator;
import org.eclipse.jgit.diff.Sequence;
import org.eclipse.jgit.dircache.DirCache;
import org.eclipse.jgit.dircache.DirCacheBuildIterator;
import org.eclipse.jgit.dircache.DirCacheBuilder;
import org.eclipse.jgit.dircache.DirCacheCheckout;
import org.eclipse.jgit.dircache.DirCacheCheckout.CheckoutMetadata;
import org.eclipse.jgit.dircache.DirCacheEntry;
import org.eclipse.jgit.errors.BinaryBlobException;
import org.eclipse.jgit.errors.CorruptObjectException;
import org.eclipse.jgit.errors.IncorrectObjectTypeException;
import org.eclipse.jgit.errors.IndexWriteException;
import org.eclipse.jgit.errors.MissingObjectException;
import org.eclipse.jgit.errors.NoWorkTreeException;
import org.eclipse.jgit.lib.Config;
import org.eclipse.jgit.lib.ConfigConstants;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.CoreConfig.EolStreamType;
import org.eclipse.jgit.lib.FileMode;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectInserter;
import org.eclipse.jgit.lib.ObjectLoader;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.storage.pack.PackConfig;
import org.eclipse.jgit.submodule.SubmoduleConflict;
import org.eclipse.jgit.treewalk.AbstractTreeIterator;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;
import org.eclipse.jgit.treewalk.NameConflictTreeWalk;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.eclipse.jgit.treewalk.TreeWalk.OperationType;
import org.eclipse.jgit.treewalk.WorkingTreeIterator;
import org.eclipse.jgit.treewalk.WorkingTreeOptions;
import org.eclipse.jgit.treewalk.filter.TreeFilter;
import org.eclipse.jgit.util.FS;
import org.eclipse.jgit.util.LfsFactory;
import org.eclipse.jgit.util.LfsFactory.LfsInputStream;
import org.eclipse.jgit.util.TemporaryBuffer;
import org.eclipse.jgit.util.io.EolStreamTypeUtil;

import io.onedev.server.git.UseOursOnConflict;

/**
 * A three-way merger performing a content-merge if necessary
 */
public class ResolveMerger extends ThreeWayMerger {
	/**
	 * If the merge fails (means: not stopped because of unresolved conflicts)
	 * this enum is used to explain why it failed
	 */
	public enum MergeFailureReason {
		/** the merge failed because of a dirty index */
		DIRTY_INDEX,
		/** the merge failed because of a dirty workingtree */
		DIRTY_WORKTREE,
		/** the merge failed because of a file could not be deleted */
		COULD_NOT_DELETE
	}

	/**
	 * The tree walk which we'll iterate over to merge entries.
	 *
	 * @since 3.4
	 */
	protected NameConflictTreeWalk tw;

	/**
	 * string versions of a list of commit SHA1s
	 *
	 * @since 3.0
	 */
	protected String[] commitNames;

	/**
	 * Index of the base tree within the {@link #tw tree walk}.
	 *
	 * @since 3.4
	 */
	protected static final int T_BASE = 0;

	/**
	 * Index of our tree in withthe {@link #tw tree walk}.
	 *
	 * @since 3.4
	 */
	protected static final int T_OURS = 1;

	/**
	 * Index of their tree within the {@link #tw tree walk}.
	 *
	 * @since 3.4
	 */
	protected static final int T_THEIRS = 2;

	/**
	 * Index of the index tree within the {@link #tw tree walk}.
	 *
	 * @since 3.4
	 */
	protected static final int T_INDEX = 3;

	/**
	 * Index of the working directory tree within the {@link #tw tree walk}.
	 *
	 * @since 3.4
	 */
	protected static final int T_FILE = 4;

	/**
	 * Builder to update the cache during this merge.
	 *
	 * @since 3.4
	 */
	protected DirCacheBuilder builder;

	/**
	 * merge result as tree
	 *
	 * @since 3.0
	 */
	protected ObjectId resultTree;

	/**
	 * Paths that could not be merged by this merger because of an unsolvable
	 * conflict.
	 *
	 * @since 3.4
	 */
	protected List<String> unmergedPaths = new ArrayList<>();

	/**
	 * Files modified during this merge operation.
	 *
	 * @since 3.4
	 */
	protected List<String> modifiedFiles = new LinkedList<>();

	/**
	 * If the merger has nothing to do for a file but check it out at the end of
	 * the operation, it can be added here.
	 *
	 * @since 3.4
	 */
	protected Map<String, DirCacheEntry> toBeCheckedOut = new HashMap<>();

	/**
	 * Paths in this list will be deleted from the local copy at the end of the
	 * operation.
	 *
	 * @since 3.4
	 */
	protected List<String> toBeDeleted = new ArrayList<>();

	/**
	 * Low-level textual merge results. Will be passed on to the callers in case
	 * of conflicts.
	 *
	 * @since 3.4
	 */
	protected Map<String, MergeResult<? extends Sequence>> mergeResults = new HashMap<>();

	/**
	 * Paths for which the merge failed altogether.
	 *
	 * @since 3.4
	 */
	protected Map<String, MergeFailureReason> failingPaths = new HashMap<>();

	/**
	 * Updated as we merge entries of the tree walk. Tells us whether we should
	 * recurse into the entry if it is a subtree.
	 *
	 * @since 3.4
	 */
	protected boolean enterSubtree;

	/**
	 * Set to true if this merge should work in-memory. The repos dircache and
	 * workingtree are not touched by this method. Eventually needed files are
	 * created as temporary files and a new empty, in-memory dircache will be
	 * used instead the repo's one. Often used for bare repos where the repo
	 * doesn't even have a workingtree and dircache.
	 * @since 3.0
	 */
	protected boolean inCore;

	/**
	 * Set to true if this merger should use the default dircache of the
	 * repository and should handle locking and unlocking of the dircache. If
	 * this merger should work in-core or if an explicit dircache was specified
	 * during construction then this field is set to false.
	 * @since 3.0
	 */
	protected boolean implicitDirCache;

	/**
	 * Directory cache
	 * @since 3.0
	 */
	protected DirCache dircache;

	/**
	 * The iterator to access the working tree. If set to <code>null</code> this
	 * merger will not touch the working tree.
	 * @since 3.0
	 */
	protected WorkingTreeIterator workingTreeIterator;

	/**
	 * our merge algorithm
	 * @since 3.0
	 */
	protected MergeAlgorithm mergeAlgorithm;

	/**
	 * The {@link WorkingTreeOptions} are needed to determine line endings for
	 * merged files.
	 *
	 * @since 4.11
	 */
	protected WorkingTreeOptions workingTreeOptions;

	/**
	 * The size limit (bytes) which controls a file to be stored in {@code Heap}
	 * or {@code LocalFile} during the merge.
	 */
	private int inCoreLimit;

	/**
	 * Keeps {@link CheckoutMetadata} for {@link #checkout()} and
	 * {@link #cleanUp()}.
	 */
	private Map<String, CheckoutMetadata> checkoutMetadata;

	private static MergeAlgorithm getMergeAlgorithm(Config config) {
		SupportedAlgorithm diffAlg = config.getEnum(
				CONFIG_DIFF_SECTION, null, CONFIG_KEY_ALGORITHM,
				HISTOGRAM);
		return new MergeAlgorithm(DiffAlgorithm.getAlgorithm(diffAlg));
	}

	private static int getInCoreLimit(Config config) {
		return config.getInt(
				ConfigConstants.CONFIG_MERGE_SECTION, ConfigConstants.CONFIG_KEY_IN_CORE_LIMIT, 10 << 20);
	}

	private static String[] defaultCommitNames() {
		return new String[] { "BASE", "OURS", "THEIRS" }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	}

	private static final Attributes NO_ATTRIBUTES = new Attributes();

	/**
	 * Constructor for ResolveMerger.
	 *
	 * @param local
	 *            the {@link org.eclipse.jgit.lib.Repository}.
	 * @param inCore
	 *            a boolean.
	 */
	protected ResolveMerger(Repository local, boolean inCore) {
		super(local);
		Config config = local.getConfig();
		mergeAlgorithm = getMergeAlgorithm(config);
		inCoreLimit = getInCoreLimit(config);
		commitNames = defaultCommitNames();
		this.inCore = inCore;

		if (inCore) {
			implicitDirCache = false;
			dircache = DirCache.newInCore();
		} else {
			implicitDirCache = true;
			workingTreeOptions = local.getConfig().get(WorkingTreeOptions.KEY);
		}
	}

	/**
	 * Constructor for ResolveMerger.
	 *
	 * @param local
	 *            the {@link org.eclipse.jgit.lib.Repository}.
	 */
	protected ResolveMerger(Repository local) {
		this(local, false);
	}

	/**
	 * Constructor for ResolveMerger.
	 *
	 * @param inserter
	 *            an {@link org.eclipse.jgit.lib.ObjectInserter} object.
	 * @param config
	 *            the repository configuration
	 * @since 4.8
	 */
	protected ResolveMerger(ObjectInserter inserter, Config config) {
		super(inserter);
		mergeAlgorithm = getMergeAlgorithm(config);
		commitNames = defaultCommitNames();
		inCore = true;
		implicitDirCache = false;
		dircache = DirCache.newInCore();
	}

	/** {@inheritDoc} */
	@Override
	protected boolean mergeImpl() throws IOException {
		if (implicitDirCache) {
			dircache = nonNullRepo().lockDirCache();
		}
		if (!inCore) {
			checkoutMetadata = new HashMap<>();
		}
		try {
			return mergeTrees(mergeBase(), sourceTrees[0], sourceTrees[1],
					false);
		} finally {
			checkoutMetadata = null;
			if (implicitDirCache) {
				dircache.unlock();
			}
		}
	}

	private void checkout() throws NoWorkTreeException, IOException {
		// Iterate in reverse so that "folder/file" is deleted before
		// "folder". Otherwise this could result in a failing path because
		// of a non-empty directory, for which delete() would fail.
		for (int i = toBeDeleted.size() - 1; i >= 0; i--) {
			String fileName = toBeDeleted.get(i);
			File f = new File(nonNullRepo().getWorkTree(), fileName);
			if (!f.delete())
				if (!f.isDirectory())
					failingPaths.put(fileName,
							MergeFailureReason.COULD_NOT_DELETE);
			modifiedFiles.add(fileName);
		}
		for (Map.Entry<String, DirCacheEntry> entry : toBeCheckedOut
				.entrySet()) {
			DirCacheEntry cacheEntry = entry.getValue();
			if (cacheEntry.getFileMode() == FileMode.GITLINK) {
				new File(nonNullRepo().getWorkTree(), entry.getKey()).mkdirs();
			} else {
				DirCacheCheckout.checkoutEntry(db, cacheEntry, reader, false,
						checkoutMetadata.get(entry.getKey()));
				modifiedFiles.add(entry.getKey());
			}
		}
	}

	/**
	 * Reverts the worktree after an unsuccessful merge. We know that for all
	 * modified files the old content was in the old index and the index
	 * contained only stage 0. In case if inCore operation just clear the
	 * history of modified files.
	 *
	 * @throws java.io.IOException
	 * @throws org.eclipse.jgit.errors.CorruptObjectException
	 * @throws org.eclipse.jgit.errors.NoWorkTreeException
	 * @since 3.4
	 */
	protected void cleanUp() throws NoWorkTreeException,
			CorruptObjectException,
			IOException {
		if (inCore) {
			modifiedFiles.clear();
			return;
		}

		DirCache dc = nonNullRepo().readDirCache();
		Iterator<String> mpathsIt=modifiedFiles.iterator();
		while(mpathsIt.hasNext()) {
			String mpath = mpathsIt.next();
			DirCacheEntry entry = dc.getEntry(mpath);
			if (entry != null) {
				DirCacheCheckout.checkoutEntry(db, entry, reader, false,
						checkoutMetadata.get(mpath));
			}
			mpathsIt.remove();
		}
	}

	/**
	 * adds a new path with the specified stage to the index builder
	 *
	 * @param path
	 * @param p
	 * @param stage
	 * @param lastMod
	 * @param len
	 * @return the entry which was added to the index
	 */
	private DirCacheEntry add(byte[] path, CanonicalTreeParser p, int stage,
			Instant lastMod, long len) {
		if (p != null && !p.getEntryFileMode().equals(FileMode.TREE)) {
			DirCacheEntry e = new DirCacheEntry(path, stage);
			e.setFileMode(p.getEntryFileMode());
			e.setObjectId(p.getEntryObjectId());
			e.setLastModified(lastMod);
			e.setLength(len);
			builder.add(e);
			return e;
		}
		return null;
	}

	/**
	 * adds a entry to the index builder which is a copy of the specified
	 * DirCacheEntry
	 *
	 * @param e
	 *            the entry which should be copied
	 *
	 * @return the entry which was added to the index
	 */
	private DirCacheEntry keep(DirCacheEntry e) {
		DirCacheEntry newEntry = new DirCacheEntry(e.getRawPath(),
				e.getStage());
		newEntry.setFileMode(e.getFileMode());
		newEntry.setObjectId(e.getObjectId());
		newEntry.setLastModified(e.getLastModifiedInstant());
		newEntry.setLength(e.getLength());
		builder.add(newEntry);
		return newEntry;
	}

	/**
	 * Remembers the {@link CheckoutMetadata} for the given path; it may be
	 * needed in {@link #checkout()} or in {@link #cleanUp()}.
	 *
	 * @param path
	 *            of the current node
	 * @param attributes
	 *            for the current node
	 * @throws IOException
	 *             if the smudge filter cannot be determined
	 * @since 5.1
	 */
	protected void addCheckoutMetadata(String path, Attributes attributes)
			throws IOException {
		if (checkoutMetadata != null) {
			EolStreamType eol = EolStreamTypeUtil.detectStreamType(
					OperationType.CHECKOUT_OP, workingTreeOptions, attributes);
			CheckoutMetadata data = new CheckoutMetadata(eol,
					tw.getFilterCommand(Constants.ATTR_FILTER_TYPE_SMUDGE));
			checkoutMetadata.put(path, data);
		}
	}

	/**
	 * Adds a {@link DirCacheEntry} for direct checkout and remembers its
	 * {@link CheckoutMetadata}.
	 *
	 * @param path
	 *            of the entry
	 * @param entry
	 *            to add
	 * @param attributes
	 *            for the current entry
	 * @throws IOException
	 *             if the {@link CheckoutMetadata} cannot be determined
	 * @since 5.1
	 */
	protected void addToCheckout(String path, DirCacheEntry entry,
			Attributes attributes) throws IOException {
		toBeCheckedOut.put(path, entry);
		addCheckoutMetadata(path, attributes);
	}

	/**
	 * Remember a path for deletion, and remember its {@link CheckoutMetadata}
	 * in case it has to be restored in {@link #cleanUp()}.
	 *
	 * @param path
	 *            of the entry
	 * @param isFile
	 *            whether it is a file
	 * @param attributes
	 *            for the entry
	 * @throws IOException
	 *             if the {@link CheckoutMetadata} cannot be determined
	 * @since 5.1
	 */
	protected void addDeletion(String path, boolean isFile,
			Attributes attributes) throws IOException {
		toBeDeleted.add(path);
		if (isFile) {
			addCheckoutMetadata(path, attributes);
		}
	}

	/**
	 * Processes one path and tries to merge taking git attributes in account.
	 * This method will do all trivial (not content) merges and will also detect
	 * if a merge will fail. The merge will fail when one of the following is
	 * true
	 * <ul>
	 * <li>the index entry does not match the entry in ours. When merging one
	 * branch into the current HEAD, ours will point to HEAD and theirs will
	 * point to the other branch. It is assumed that the index matches the HEAD
	 * because it will only not match HEAD if it was populated before the merge
	 * operation. But the merge commit should not accidentally contain
	 * modifications done before the merge. Check the <a href=
	 * "http://www.kernel.org/pub/software/scm/git/docs/git-read-tree.html#_3_way_merge"
	 * >git read-tree</a> documentation for further explanations.</li>
	 * <li>A conflict was detected and the working-tree file is dirty. When a
	 * conflict is detected the content-merge algorithm will try to write a
	 * merged version into the working-tree. If the file is dirty we would
	 * override unsaved data.</li>
	 * </ul>
	 *
	 * @param base
	 *            the common base for ours and theirs
	 * @param ours
	 *            the ours side of the merge. When merging a branch into the
	 *            HEAD ours will point to HEAD
	 * @param theirs
	 *            the theirs side of the merge. When merging a branch into the
	 *            current HEAD theirs will point to the branch which is merged
	 *            into HEAD.
	 * @param index
	 *            the index entry
	 * @param work
	 *            the file in the working tree
	 * @param ignoreConflicts
	 *            see
	 *            {@link org.eclipse.jgit.merge.ResolveMerger#mergeTrees(AbstractTreeIterator, RevTree, RevTree, boolean)}
	 * @param attributes
	 *            the attributes defined for this entry
	 * @return <code>false</code> if the merge will fail because the index entry
	 *         didn't match ours or the working-dir file was dirty and a
	 *         conflict occurred
	 * @throws org.eclipse.jgit.errors.MissingObjectException
	 * @throws org.eclipse.jgit.errors.IncorrectObjectTypeException
	 * @throws org.eclipse.jgit.errors.CorruptObjectException
	 * @throws java.io.IOException
	 * @since 4.9
	 */
	protected boolean processEntry(CanonicalTreeParser base,
			CanonicalTreeParser ours, CanonicalTreeParser theirs,
			DirCacheBuildIterator index, WorkingTreeIterator work,
			boolean ignoreConflicts, Attributes attributes)
			throws MissingObjectException, IncorrectObjectTypeException,
			CorruptObjectException, IOException {
		boolean useOursOnConflict = UseOursOnConflict.get();
		enterSubtree = true;
		final int modeO = tw.getRawMode(T_OURS);
		final int modeT = tw.getRawMode(T_THEIRS);
		final int modeB = tw.getRawMode(T_BASE);

		if (modeO == 0 && modeT == 0 && modeB == 0)
			// File is either untracked or new, staged but uncommitted
			return true;

		if (isIndexDirty())
			return false;

		DirCacheEntry ourDce = null;

		if (index == null || index.getDirCacheEntry() == null) {
			// create a fake DCE, but only if ours is valid. ours is kept only
			// in case it is valid, so a null ourDce is ok in all other cases.
			if (nonTree(modeO)) {
				ourDce = new DirCacheEntry(tw.getRawPath());
				ourDce.setObjectId(tw.getObjectId(T_OURS));
				ourDce.setFileMode(tw.getFileMode(T_OURS));
			}
		} else {
			ourDce = index.getDirCacheEntry();
		}

		if (nonTree(modeO) && nonTree(modeT) && tw.idEqual(T_OURS, T_THEIRS)) {
			// OURS and THEIRS have equal content. Check the file mode
			if (modeO == modeT) {
				// content and mode of OURS and THEIRS are equal: it doesn't
				// matter which one we choose. OURS is chosen. Since the index
				// is clean (the index matches already OURS) we can keep the existing one
				keep(ourDce);
				// no checkout needed!
				return true;
			}
			// same content but different mode on OURS and THEIRS.
			// Try to merge the mode and report an error if this is
			// not possible.
			int newMode = mergeFileModes(modeB, modeO, modeT);
			if (newMode != FileMode.MISSING.getBits()) {
				if (newMode == modeO) {
					// ours version is preferred
					keep(ourDce);
				} else {
					// the preferred version THEIRS has a different mode
					// than ours. Check it out!
					if (isWorktreeDirty(work, ourDce)) {
						return false;
					}
					// we know about length and lastMod only after we have
					// written the new content.
					// This will happen later. Set these values to 0 for know.
					DirCacheEntry e = add(tw.getRawPath(), theirs,
							DirCacheEntry.STAGE_0, EPOCH, 0);
					addToCheckout(tw.getPathString(), e, attributes);
				}
				return true;
			}
			// FileModes are not mergeable. We found a conflict on modes.
			// For conflicting entries we don't know lastModified and
			// length.
			add(tw.getRawPath(), base, DirCacheEntry.STAGE_1, EPOCH, 0);
			add(tw.getRawPath(), ours, DirCacheEntry.STAGE_2, EPOCH, 0);
			add(tw.getRawPath(), theirs, DirCacheEntry.STAGE_3, EPOCH, 0);
			unmergedPaths.add(tw.getPathString());
			mergeResults.put(tw.getPathString(),
					new MergeResult<>(Collections.<RawText> emptyList()));
			return true;
		}

		if (modeB == modeT && tw.idEqual(T_BASE, T_THEIRS)) {
			// THEIRS was not changed compared to BASE. All changes must be in
			// OURS. OURS is chosen. We can keep the existing entry.
			if (ourDce != null)
				keep(ourDce);
			// no checkout needed!
			return true;
		}

		if (modeB == modeO && tw.idEqual(T_BASE, T_OURS)) {
			// OURS was not changed compared to BASE. All changes must be in
			// THEIRS. THEIRS is chosen.

			// Check worktree before checking out THEIRS
			if (isWorktreeDirty(work, ourDce))
				return false;
			if (nonTree(modeT)) {
				// we know about length and lastMod only after we have written
				// the new content.
				// This will happen later. Set these values to 0 for know.
				DirCacheEntry e = add(tw.getRawPath(), theirs,
						DirCacheEntry.STAGE_0, EPOCH, 0);
				if (e != null) {
					addToCheckout(tw.getPathString(), e, attributes);
				}
				return true;
			}
			// we want THEIRS ... but THEIRS contains a folder or the
			// deletion of the path. Delete what's in the working tree,
			// which we know to be clean.
			if (tw.getTreeCount() > T_FILE && tw.getRawMode(T_FILE) == 0) {
				// Not present in working tree, so nothing to delete
				return true;
			}
			if (modeT != 0 && modeT == modeB) {
				// Base, ours, and theirs all contain a folder: don't delete
				return true;
			}
			addDeletion(tw.getPathString(), nonTree(modeO), attributes);
			return true;
		}

		if (tw.isSubtree()) {
			// file/folder conflicts: here I want to detect only file/folder
			// conflict between ours and theirs. file/folder conflicts between
			// base/index/workingTree and something else are not relevant or
			// detected later
			if (nonTree(modeO) && !nonTree(modeT)) {
				if (useOursOnConflict) {
					if (ourDce != null)
						keep(ourDce);
				} else {
					if (nonTree(modeB))
						add(tw.getRawPath(), base, DirCacheEntry.STAGE_1, EPOCH, 0);
					add(tw.getRawPath(), ours, DirCacheEntry.STAGE_2, EPOCH, 0);
					unmergedPaths.add(tw.getPathString());
				}
				enterSubtree = false;
				return true;
			}
			if (nonTree(modeT) && !nonTree(modeO)) {
				if (useOursOnConflict) {
					if (ourDce != null)
						keep(ourDce);
				} else {
					if (nonTree(modeB))
						add(tw.getRawPath(), base, DirCacheEntry.STAGE_1, EPOCH, 0);
					add(tw.getRawPath(), theirs, DirCacheEntry.STAGE_3, EPOCH, 0);
					unmergedPaths.add(tw.getPathString());
					enterSubtree = false;
				}
				return true;
			}

			// ours and theirs are both folders or both files (and treewalk
			// tells us we are in a subtree because of index or working-dir).
			// If they are both folders no content-merge is required - we can
			// return here.
			if (!nonTree(modeO))
				return true;

			// ours and theirs are both files, just fall out of the if block
			// and do the content merge
		}

		if (nonTree(modeO) && nonTree(modeT)) {
			// Check worktree before modifying files
			boolean worktreeDirty = isWorktreeDirty(work, ourDce);
			if (!attributes.canBeContentMerged() && worktreeDirty) {
				return false;
			}

			boolean gitlinkConflict = isGitLink(modeO) || isGitLink(modeT);
			// Don't attempt to resolve submodule link conflicts
			if (gitlinkConflict || !attributes.canBeContentMerged()) {
				if (useOursOnConflict) {
					if (ourDce != null)
						keep(ourDce);
				} else {
					add(tw.getRawPath(), base, DirCacheEntry.STAGE_1, EPOCH, 0);
					add(tw.getRawPath(), ours, DirCacheEntry.STAGE_2, EPOCH, 0);
					add(tw.getRawPath(), theirs, DirCacheEntry.STAGE_3, EPOCH, 0);
	
					if (gitlinkConflict) {
						MergeResult<SubmoduleConflict> result = new MergeResult<>(
								Arrays.asList(
										new SubmoduleConflict(base == null ? null
												: base.getEntryObjectId()),
										new SubmoduleConflict(ours == null ? null
												: ours.getEntryObjectId()),
										new SubmoduleConflict(theirs == null ? null
												: theirs.getEntryObjectId())));
						result.setContainsConflicts(true);
						mergeResults.put(tw.getPathString(), result);
						if (!ignoreConflicts) {
							unmergedPaths.add(tw.getPathString());
						}
					} else {
						// attribute merge issues are conflicts but not failures
						unmergedPaths.add(tw.getPathString());
					}
				}
				return true;
			}

			// Check worktree before modifying files
			if (worktreeDirty) {
				return false;
			}

			MergeResult<RawText> result = contentMerge(base, ours, theirs,
					attributes);
			if (ignoreConflicts) {
				result.setContainsConflicts(false);
			}
			updateIndex(base, ours, theirs, result, attributes);
			String currentPath = tw.getPathString();
			if (result.containsConflicts() && !ignoreConflicts) {
				unmergedPaths.add(currentPath);
			}
			modifiedFiles.add(currentPath);
			addCheckoutMetadata(currentPath, attributes);
		} else if (modeO != modeT) {
			// OURS or THEIRS has been deleted
			if (((modeO != 0 && !tw.idEqual(T_BASE, T_OURS)) || (modeT != 0 && !tw
					.idEqual(T_BASE, T_THEIRS)))) {
				if (useOursOnConflict) {
					if (ourDce != null)
						keep(ourDce);
				} else {
					MergeResult<RawText> result = contentMerge(base, ours, theirs,
							attributes);
	
					add(tw.getRawPath(), base, DirCacheEntry.STAGE_1, EPOCH, 0);
					add(tw.getRawPath(), ours, DirCacheEntry.STAGE_2, EPOCH, 0);
					DirCacheEntry e = add(tw.getRawPath(), theirs,
							DirCacheEntry.STAGE_3, EPOCH, 0);
	
					// OURS was deleted checkout THEIRS
					if (modeO == 0) {
						// Check worktree before checking out THEIRS
						if (isWorktreeDirty(work, ourDce))
							return false;
						if (nonTree(modeT)) {
							if (e != null) {
								addToCheckout(tw.getPathString(), e, attributes);
							}
						}
					}
	
					unmergedPaths.add(tw.getPathString());
	
					// generate a MergeResult for the deleted file
					mergeResults.put(tw.getPathString(), result);
				}
			}
		}
		return true;
	}

	/**
	 * Does the content merge. The three texts base, ours and theirs are
	 * specified with {@link CanonicalTreeParser}. If any of the parsers is
	 * specified as <code>null</code> then an empty text will be used instead.
	 *
	 * @param base
	 * @param ours
	 * @param theirs
	 * @param attributes
	 *
	 * @return the result of the content merge
	 * @throws IOException
	 */
	private MergeResult<RawText> contentMerge(CanonicalTreeParser base,
			CanonicalTreeParser ours, CanonicalTreeParser theirs,
			Attributes attributes)
			throws IOException {
		RawText baseText;
		RawText ourText;
		RawText theirsText;

		try {
			baseText = base == null ? RawText.EMPTY_TEXT : getRawText(
							base.getEntryObjectId(), attributes);
			ourText = ours == null ? RawText.EMPTY_TEXT : getRawText(
							ours.getEntryObjectId(), attributes);
			theirsText = theirs == null ? RawText.EMPTY_TEXT : getRawText(
							theirs.getEntryObjectId(), attributes);
		} catch (BinaryBlobException e) {
			MergeResult<RawText> r = new MergeResult<>(Collections.<RawText>emptyList());
			r.setContainsConflicts(true);
			return r;
		}
		return (mergeAlgorithm.merge(RawTextComparator.DEFAULT, baseText,
				ourText, theirsText));
	}

	private boolean isIndexDirty() {
		if (inCore)
			return false;

		final int modeI = tw.getRawMode(T_INDEX);
		final int modeO = tw.getRawMode(T_OURS);

		// Index entry has to match ours to be considered clean
		final boolean isDirty = nonTree(modeI)
				&& !(modeO == modeI && tw.idEqual(T_INDEX, T_OURS));
		if (isDirty)
			failingPaths
					.put(tw.getPathString(), MergeFailureReason.DIRTY_INDEX);
		return isDirty;
	}

	private boolean isWorktreeDirty(WorkingTreeIterator work,
			DirCacheEntry ourDce) throws IOException {
		if (work == null)
			return false;

		final int modeF = tw.getRawMode(T_FILE);
		final int modeO = tw.getRawMode(T_OURS);

		// Worktree entry has to match ours to be considered clean
		boolean isDirty;
		if (ourDce != null)
			isDirty = work.isModified(ourDce, true, reader);
		else {
			isDirty = work.isModeDifferent(modeO);
			if (!isDirty && nonTree(modeF))
				isDirty = !tw.idEqual(T_FILE, T_OURS);
		}

		// Ignore existing empty directories
		if (isDirty && modeF == FileMode.TYPE_TREE
				&& modeO == FileMode.TYPE_MISSING)
			isDirty = false;
		if (isDirty)
			failingPaths.put(tw.getPathString(),
					MergeFailureReason.DIRTY_WORKTREE);
		return isDirty;
	}

	/**
	 * Updates the index after a content merge has happened. If no conflict has
	 * occurred this includes persisting the merged content to the object
	 * database. In case of conflicts this method takes care to write the
	 * correct stages to the index.
	 *
	 * @param base
	 * @param ours
	 * @param theirs
	 * @param result
	 * @param attributes
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	private void updateIndex(CanonicalTreeParser base,
			CanonicalTreeParser ours, CanonicalTreeParser theirs,
			MergeResult<RawText> result, Attributes attributes)
			throws FileNotFoundException,
			IOException {
		TemporaryBuffer rawMerged = null;
		try {
			rawMerged = doMerge(result);
			File mergedFile = inCore ? null
					: writeMergedFile(rawMerged, attributes);
			if (result.containsConflicts()) {
				// A conflict occurred, the file will contain conflict markers
				// the index will be populated with the three stages and the
				// workdir (if used) contains the halfway merged content.
				add(tw.getRawPath(), base, DirCacheEntry.STAGE_1, EPOCH, 0);
				add(tw.getRawPath(), ours, DirCacheEntry.STAGE_2, EPOCH, 0);
				add(tw.getRawPath(), theirs, DirCacheEntry.STAGE_3, EPOCH, 0);
				mergeResults.put(tw.getPathString(), result);
				return;
			}

			// No conflict occurred, the file will contain fully merged content.
			// The index will be populated with the new merged version.
			DirCacheEntry dce = new DirCacheEntry(tw.getPathString());

			// Set the mode for the new content. Fall back to REGULAR_FILE if
			// we can't merge modes of OURS and THEIRS.
			int newMode = mergeFileModes(tw.getRawMode(0), tw.getRawMode(1),
					tw.getRawMode(2));
			dce.setFileMode(newMode == FileMode.MISSING.getBits()
					? FileMode.REGULAR_FILE : FileMode.fromBits(newMode));
			if (mergedFile != null) {
				dce.setLastModified(
						nonNullRepo().getFS().lastModifiedInstant(mergedFile));
				dce.setLength((int) mergedFile.length());
			}
			dce.setObjectId(insertMergeResult(rawMerged, attributes));
			builder.add(dce);
		} finally {
			if (rawMerged != null) {
				rawMerged.destroy();
			}
		}
	}

	/**
	 * Writes merged file content to the working tree.
	 *
	 * @param rawMerged
	 *            the raw merged content
	 * @param attributes
	 *            the files .gitattributes entries
	 * @return the working tree file to which the merged content was written.
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	private File writeMergedFile(TemporaryBuffer rawMerged,
			Attributes attributes)
			throws FileNotFoundException, IOException {
		File workTree = nonNullRepo().getWorkTree();
		FS fs = nonNullRepo().getFS();
		File of = new File(workTree, tw.getPathString());
		File parentFolder = of.getParentFile();
		if (!fs.exists(parentFolder)) {
			parentFolder.mkdirs();
		}
		EolStreamType streamType = EolStreamTypeUtil.detectStreamType(
				OperationType.CHECKOUT_OP, workingTreeOptions,
				attributes);
		try (OutputStream os = EolStreamTypeUtil.wrapOutputStream(
				new BufferedOutputStream(new FileOutputStream(of)),
				streamType)) {
			rawMerged.writeTo(os, null);
		}
		return of;
	}

	private TemporaryBuffer doMerge(MergeResult<RawText> result)
			throws IOException {
		TemporaryBuffer.LocalFile buf = new TemporaryBuffer.LocalFile(
				db != null ? nonNullRepo().getDirectory() : null, inCoreLimit);
		boolean success = false;
		try {
			new MergeFormatter().formatMerge(buf, result,
					Arrays.asList(commitNames), UTF_8);
			buf.close();
			success = true;
		} finally {
			if (!success) {
				buf.destroy();
			}
		}
		return buf;
	}

	private ObjectId insertMergeResult(TemporaryBuffer buf,
			Attributes attributes) throws IOException {
		InputStream in = buf.openInputStream();
		try (LfsInputStream is = LfsFactory.getInstance().applyCleanFilter(
				getRepository(), in,
				buf.length(), attributes.get(Constants.ATTR_MERGE))) {
			return getObjectInserter().insert(OBJ_BLOB, is.getLength(), is);
		}
	}

	/**
	 * Try to merge filemodes. If only ours or theirs have changed the mode
	 * (compared to base) we choose that one. If ours and theirs have equal
	 * modes return that one. If also that is not the case the modes are not
	 * mergeable. Return {@link FileMode#MISSING} int that case.
	 *
	 * @param modeB
	 *            filemode found in BASE
	 * @param modeO
	 *            filemode found in OURS
	 * @param modeT
	 *            filemode found in THEIRS
	 *
	 * @return the merged filemode or {@link FileMode#MISSING} in case of a
	 *         conflict
	 */
	private int mergeFileModes(int modeB, int modeO, int modeT) {
		if (modeO == modeT)
			return modeO;
		if (modeB == modeO)
			// Base equal to Ours -> chooses Theirs if that is not missing
			return (modeT == FileMode.MISSING.getBits()) ? modeO : modeT;
		if (modeB == modeT)
			// Base equal to Theirs -> chooses Ours if that is not missing
			return (modeO == FileMode.MISSING.getBits()) ? modeT : modeO;
		
		if (UseOursOnConflict.get())
			return (modeO == FileMode.MISSING.getBits()) ? modeT : modeO;
		else 
			return FileMode.MISSING.getBits();
	}

	private RawText getRawText(ObjectId id,
			Attributes attributes)
			throws IOException, BinaryBlobException {
		if (id.equals(ObjectId.zeroId()))
			return new RawText(new byte[] {});

		ObjectLoader loader = LfsFactory.getInstance().applySmudgeFilter(
				getRepository(), reader.open(id, OBJ_BLOB),
				attributes.get(Constants.ATTR_MERGE));
		int threshold = PackConfig.DEFAULT_BIG_FILE_THRESHOLD;
		return RawText.load(loader, threshold);
	}

	private static boolean nonTree(int mode) {
		return mode != 0 && !FileMode.TREE.equals(mode);
	}

	private static boolean isGitLink(int mode) {
		return FileMode.GITLINK.equals(mode);
	}

	/** {@inheritDoc} */
	@Override
	public ObjectId getResultTreeId() {
		return (resultTree == null) ? null : resultTree.toObjectId();
	}

	/**
	 * Set the names of the commits as they would appear in conflict markers
	 *
	 * @param commitNames
	 *            the names of the commits as they would appear in conflict
	 *            markers
	 */
	public void setCommitNames(String[] commitNames) {
		this.commitNames = commitNames;
	}

	/**
	 * Get the names of the commits as they would appear in conflict markers.
	 *
	 * @return the names of the commits as they would appear in conflict
	 *         markers.
	 */
	public String[] getCommitNames() {
		return commitNames;
	}

	/**
	 * Get the paths with conflicts. This is a subset of the files listed by
	 * {@link #getModifiedFiles()}
	 *
	 * @return the paths with conflicts. This is a subset of the files listed by
	 *         {@link #getModifiedFiles()}
	 */
	public List<String> getUnmergedPaths() {
		return unmergedPaths;
	}

	/**
	 * Get the paths of files which have been modified by this merge.
	 *
	 * @return the paths of files which have been modified by this merge. A file
	 *         will be modified if a content-merge works on this path or if the
	 *         merge algorithm decides to take the theirs-version. This is a
	 *         superset of the files listed by {@link #getUnmergedPaths()}.
	 */
	public List<String> getModifiedFiles() {
		return modifiedFiles;
	}

	/**
	 * Get a map which maps the paths of files which have to be checked out
	 * because the merge created new fully-merged content for this file into the
	 * index.
	 *
	 * @return a map which maps the paths of files which have to be checked out
	 *         because the merge created new fully-merged content for this file
	 *         into the index. This means: the merge wrote a new stage 0 entry
	 *         for this path.
	 */
	public Map<String, DirCacheEntry> getToBeCheckedOut() {
		return toBeCheckedOut;
	}

	/**
	 * Get the mergeResults
	 *
	 * @return the mergeResults
	 */
	public Map<String, MergeResult<? extends Sequence>> getMergeResults() {
		return mergeResults;
	}

	/**
	 * Get list of paths causing this merge to fail (not stopped because of a
	 * conflict).
	 *
	 * @return lists paths causing this merge to fail (not stopped because of a
	 *         conflict). <code>null</code> is returned if this merge didn't
	 *         fail.
	 */
	public Map<String, MergeFailureReason> getFailingPaths() {
		return failingPaths.isEmpty() ? null : failingPaths;
	}

	/**
	 * Returns whether this merge failed (i.e. not stopped because of a
	 * conflict)
	 *
	 * @return <code>true</code> if a failure occurred, <code>false</code>
	 *         otherwise
	 */
	public boolean failed() {
		return !failingPaths.isEmpty();
	}

	/**
	 * Sets the DirCache which shall be used by this merger. If the DirCache is
	 * not set explicitly and if this merger doesn't work in-core, this merger
	 * will implicitly get and lock a default DirCache. If the DirCache is
	 * explicitly set the caller is responsible to lock it in advance. Finally
	 * the merger will call {@link org.eclipse.jgit.dircache.DirCache#commit()}
	 * which requires that the DirCache is locked. If the {@link #mergeImpl()}
	 * returns without throwing an exception the lock will be released. In case
	 * of exceptions the caller is responsible to release the lock.
	 *
	 * @param dc
	 *            the DirCache to set
	 */
	public void setDirCache(DirCache dc) {
		this.dircache = dc;
		implicitDirCache = false;
	}

	/**
	 * Sets the WorkingTreeIterator to be used by this merger. If no
	 * WorkingTreeIterator is set this merger will ignore the working tree and
	 * fail if a content merge is necessary.
	 * <p>
	 * TODO: enhance WorkingTreeIterator to support write operations. Then this
	 * merger will be able to merge with a different working tree abstraction.
	 *
	 * @param workingTreeIterator
	 *            the workingTreeIt to set
	 */
	public void setWorkingTreeIterator(WorkingTreeIterator workingTreeIterator) {
		this.workingTreeIterator = workingTreeIterator;
	}


	/**
	 * The resolve conflict way of three way merging
	 *
	 * @param baseTree
	 *            a {@link org.eclipse.jgit.treewalk.AbstractTreeIterator}
	 *            object.
	 * @param headTree
	 *            a {@link org.eclipse.jgit.revwalk.RevTree} object.
	 * @param mergeTree
	 *            a {@link org.eclipse.jgit.revwalk.RevTree} object.
	 * @param ignoreConflicts
	 *            Controls what to do in case a content-merge is done and a
	 *            conflict is detected. The default setting for this should be
	 *            <code>false</code>. In this case the working tree file is
	 *            filled with new content (containing conflict markers) and the
	 *            index is filled with multiple stages containing BASE, OURS and
	 *            THEIRS content. Having such non-0 stages is the sign to git
	 *            tools that there are still conflicts for that path.
	 *            <p>
	 *            If <code>true</code> is specified the behavior is different.
	 *            In case a conflict is detected the working tree file is again
	 *            filled with new content (containing conflict markers). But
	 *            also stage 0 of the index is filled with that content. No
	 *            other stages are filled. Means: there is no conflict on that
	 *            path but the new content (including conflict markers) is
	 *            stored as successful merge result. This is needed in the
	 *            context of {@link org.eclipse.jgit.merge.RecursiveMerger}
	 *            where when determining merge bases we don't want to deal with
	 *            content-merge conflicts.
	 * @return whether the trees merged cleanly
	 * @throws java.io.IOException
	 * @since 3.5
	 */
	protected boolean mergeTrees(AbstractTreeIterator baseTree,
			RevTree headTree, RevTree mergeTree, boolean ignoreConflicts)
			throws IOException {

		builder = dircache.builder();
		DirCacheBuildIterator buildIt = new DirCacheBuildIterator(builder);

		tw = new NameConflictTreeWalk(db, reader);
		tw.addTree(baseTree);
		tw.addTree(headTree);
		tw.addTree(mergeTree);
		int dciPos = tw.addTree(buildIt);
		if (workingTreeIterator != null) {
			tw.addTree(workingTreeIterator);
			workingTreeIterator.setDirCacheIterator(tw, dciPos);
		} else {
			tw.setFilter(TreeFilter.ANY_DIFF);
		}

		if (!mergeTreeWalk(tw, ignoreConflicts)) {
			return false;
		}

		if (!inCore) {
			// No problem found. The only thing left to be done is to
			// checkout all files from "theirs" which have been selected to
			// go into the new index.
			checkout();

			// All content-merges are successfully done. If we can now write the
			// new index we are on quite safe ground. Even if the checkout of
			// files coming from "theirs" fails the user can work around such
			// failures by checking out the index again.
			if (!builder.commit()) {
				cleanUp();
				throw new IndexWriteException();
			}
			builder = null;

		} else {
			builder.finish();
			builder = null;
		}

		if (getUnmergedPaths().isEmpty() && !failed()) {
			resultTree = dircache.writeTree(getObjectInserter());
			return true;
		}
		resultTree = null;
		return false;
	}

	/**
	 * Process the given TreeWalk's entries.
	 *
	 * @param treeWalk
	 *            The walk to iterate over.
	 * @param ignoreConflicts
	 *            see
	 *            {@link org.eclipse.jgit.merge.ResolveMerger#mergeTrees(AbstractTreeIterator, RevTree, RevTree, boolean)}
	 * @return Whether the trees merged cleanly.
	 * @throws java.io.IOException
	 * @since 3.5
	 */
	protected boolean mergeTreeWalk(TreeWalk treeWalk, boolean ignoreConflicts)
			throws IOException {
		boolean hasWorkingTreeIterator = tw.getTreeCount() > T_FILE;
		boolean hasAttributeNodeProvider = treeWalk
				.getAttributesNodeProvider() != null;
		while (treeWalk.next()) {
			if (!processEntry(
					treeWalk.getTree(T_BASE, CanonicalTreeParser.class),
					treeWalk.getTree(T_OURS, CanonicalTreeParser.class),
					treeWalk.getTree(T_THEIRS, CanonicalTreeParser.class),
					treeWalk.getTree(T_INDEX, DirCacheBuildIterator.class),
					hasWorkingTreeIterator ? treeWalk.getTree(T_FILE,
							WorkingTreeIterator.class) : null,
					ignoreConflicts, hasAttributeNodeProvider
							? treeWalk.getAttributes()
							: NO_ATTRIBUTES)) {
				cleanUp();
				return false;
			}
			if (treeWalk.isSubtree() && enterSubtree)
				treeWalk.enterSubtree();
		}
		return true;
	}
}
