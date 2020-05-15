/*
 * Copyright (C) 2008-2013, Google Inc.
 * Copyright (C) 2016, Laurent Delaigue <laurent.delaigue@obeo.fr> and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0 which is available at
 * https://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package org.eclipse.jgit.merge;

import java.io.IOException;
import java.text.MessageFormat;

import org.eclipse.jgit.annotations.Nullable;
import org.eclipse.jgit.errors.IncorrectObjectTypeException;
import org.eclipse.jgit.errors.NoMergeBaseException;
import org.eclipse.jgit.errors.NoMergeBaseException.MergeBaseFailureReason;
import org.eclipse.jgit.internal.JGitText;
import org.eclipse.jgit.lib.AnyObjectId;
import org.eclipse.jgit.lib.NullProgressMonitor;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectInserter;
import org.eclipse.jgit.lib.ObjectReader;
import org.eclipse.jgit.lib.ProgressMonitor;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevObject;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.revwalk.filter.RevFilter;
import org.eclipse.jgit.treewalk.AbstractTreeIterator;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;

/**
 * Instance of a specific {@link org.eclipse.jgit.merge.MergeStrategy} for a
 * single {@link org.eclipse.jgit.lib.Repository}.
 */
public abstract class Merger {
	/**
	 * The repository this merger operates on.
	 * <p>
	 * Null if and only if the merger was constructed with {@link
	 * #Merger(ObjectInserter)}. Callers that want to assume the repo is not null
	 * (e.g. because of a previous check that the merger is not in-core) may use
	 * {@link #nonNullRepo()}.
	 */
	@Nullable
	protected final Repository db;

	/** Reader to support {@link #walk} and other object loading. */
	protected ObjectReader reader;

	/** A RevWalk for computing merge bases, or listing incoming commits. */
	protected RevWalk walk;

	private ObjectInserter inserter;

	/** The original objects supplied in the merge; this can be any tree-ish. */
	protected RevObject[] sourceObjects;

	/** If {@link #sourceObjects}[i] is a commit, this is the commit. */
	protected RevCommit[] sourceCommits;

	/** The trees matching every entry in {@link #sourceObjects}. */
	protected RevTree[] sourceTrees;

	/**
	 * A progress monitor.
	 *
	 * @since 4.2
	 */
	protected ProgressMonitor monitor = NullProgressMonitor.INSTANCE;

	/**
	 * Create a new merge instance for a repository.
	 *
	 * @param local
	 *            the repository this merger will read and write data on.
	 */
	protected Merger(Repository local) {
		if (local == null) {
			throw new NullPointerException(JGitText.get().repositoryIsRequired);
		}
		db = local;
		inserter = local.newObjectInserter();
		reader = inserter.newReader();
		walk = new RevWalk(reader);
	}

	/**
	 * Create a new in-core merge instance from an inserter.
	 *
	 * @param oi
	 *            the inserter to write objects to. Will be closed at the
	 *            conclusion of {@code merge}, unless {@code flush} is false.
	 * @since 4.8
	 */
	protected Merger(ObjectInserter oi) {
		db = null;
		inserter = oi;
		reader = oi.newReader();
		walk = new RevWalk(reader);
	}

	/**
	 * Get the repository this merger operates on.
	 *
	 * @return the repository this merger operates on.
	 */
	@Nullable
	public Repository getRepository() {
		return db;
	}

	/**
	 * Get non-null repository instance
	 *
	 * @return non-null repository instance
	 * @throws java.lang.NullPointerException
	 *             if the merger was constructed without a repository.
	 * @since 4.8
	 */
	protected Repository nonNullRepo() {
		if (db == null) {
			throw new NullPointerException(JGitText.get().repositoryIsRequired);
		}
		return db;
	}

	/**
	 * Get an object writer to create objects, writing objects to
	 * {@link #getRepository()}
	 *
	 * @return an object writer to create objects, writing objects to
	 *         {@link #getRepository()} (if a repository was provided).
	 */
	public ObjectInserter getObjectInserter() {
		return inserter;
	}

	/**
	 * Set the inserter this merger will use to create objects.
	 * <p>
	 * If an inserter was already set on this instance (such as by a prior set,
	 * or a prior call to {@link #getObjectInserter()}), the prior inserter as
	 * well as the in-progress walk will be released.
	 *
	 * @param oi
	 *            the inserter instance to use. Must be associated with the
	 *            repository instance returned by {@link #getRepository()} (if a
	 *            repository was provided). Will be closed at the conclusion of
	 *            {@code merge}, unless {@code flush} is false.
	 */
	public void setObjectInserter(ObjectInserter oi) {
		walk.close();
		reader.close();
		inserter.close();
		inserter = oi;
		reader = oi.newReader();
		walk = new RevWalk(reader);
	}

	/**
	 * Merge together two or more tree-ish objects.
	 * <p>
	 * Any tree-ish may be supplied as inputs. Commits and/or tags pointing at
	 * trees or commits may be passed as input objects.
	 *
	 * @param tips
	 *            source trees to be combined together. The merge base is not
	 *            included in this set.
	 * @return true if the merge was completed without conflicts; false if the
	 *         merge strategy cannot handle this merge or there were conflicts
	 *         preventing it from automatically resolving all paths.
	 * @throws IncorrectObjectTypeException
	 *             one of the input objects is not a commit, but the strategy
	 *             requires it to be a commit.
	 * @throws java.io.IOException
	 *             one or more sources could not be read, or outputs could not
	 *             be written to the Repository.
	 */
	public boolean merge(AnyObjectId... tips) throws IOException {
		return merge(true, tips);
	}

	/**
	 * Merge together two or more tree-ish objects.
	 * <p>
	 * Any tree-ish may be supplied as inputs. Commits and/or tags pointing at
	 * trees or commits may be passed as input objects.
	 *
	 * @since 3.5
	 * @param flush
	 *            whether to flush and close the underlying object inserter when
	 *            finished to store any content-merged blobs and virtual merged
	 *            bases; if false, callers are responsible for flushing.
	 * @param tips
	 *            source trees to be combined together. The merge base is not
	 *            included in this set.
	 * @return true if the merge was completed without conflicts; false if the
	 *         merge strategy cannot handle this merge or there were conflicts
	 *         preventing it from automatically resolving all paths.
	 * @throws IncorrectObjectTypeException
	 *             one of the input objects is not a commit, but the strategy
	 *             requires it to be a commit.
	 * @throws java.io.IOException
	 *             one or more sources could not be read, or outputs could not
	 *             be written to the Repository.
	 */
	public boolean merge(boolean flush, AnyObjectId... tips)
			throws IOException {
		sourceObjects = new RevObject[tips.length];
		for (int i = 0; i < tips.length; i++)
			sourceObjects[i] = walk.parseAny(tips[i]);

		sourceCommits = new RevCommit[sourceObjects.length];
		for (int i = 0; i < sourceObjects.length; i++) {
			try {
				sourceCommits[i] = walk.parseCommit(sourceObjects[i]);
			} catch (IncorrectObjectTypeException err) {
				sourceCommits[i] = null;
			}
		}

		sourceTrees = new RevTree[sourceObjects.length];
		for (int i = 0; i < sourceObjects.length; i++)
			sourceTrees[i] = walk.parseTree(sourceObjects[i]);

		try {
			boolean ok = mergeImpl();
			if (ok && flush)
				inserter.flush();
			return ok;
		} finally {
			if (flush)
				inserter.close();
			reader.close();
		}
	}

	/**
	 * Get the ID of the commit that was used as merge base for merging
	 *
	 * @return the ID of the commit that was used as merge base for merging, or
	 *         null if no merge base was used or it was set manually
	 * @since 3.2
	 */
	public abstract ObjectId getBaseCommitId();

	/**
	 * Return the merge base of two commits.
	 *
	 * @param a
	 *            the first commit in {@link #sourceObjects}.
	 * @param b
	 *            the second commit in {@link #sourceObjects}.
	 * @return the merge base of two commits
	 * @throws org.eclipse.jgit.errors.IncorrectObjectTypeException
	 *             one of the input objects is not a commit.
	 * @throws java.io.IOException
	 *             objects are missing or multiple merge bases were found.
	 * @since 3.0
	 */
	protected RevCommit getBaseCommit(RevCommit a, RevCommit b)
			throws IncorrectObjectTypeException, IOException {
		walk.reset();
		walk.setRevFilter(RevFilter.MERGE_BASE);
		walk.markStart(a);
		walk.markStart(b);
		final RevCommit base = walk.next();
		if (base == null)
			return null;
		final RevCommit base2 = walk.next();
		if (base2 != null) {
			throw new NoMergeBaseException(
					MergeBaseFailureReason.MULTIPLE_MERGE_BASES_NOT_SUPPORTED,
					MessageFormat.format(
					JGitText.get().multipleMergeBasesFor, a.name(), b.name(),
					base.name(), base2.name()));
		}
		return base;
	}

	/**
	 * Open an iterator over a tree.
	 *
	 * @param treeId
	 *            the tree to scan; must be a tree (not a treeish).
	 * @return an iterator for the tree.
	 * @throws org.eclipse.jgit.errors.IncorrectObjectTypeException
	 *             the input object is not a tree.
	 * @throws java.io.IOException
	 *             the tree object is not found or cannot be read.
	 */
	protected AbstractTreeIterator openTree(AnyObjectId treeId)
			throws IncorrectObjectTypeException, IOException {
		return new CanonicalTreeParser(null, reader, treeId);
	}

	/**
	 * Execute the merge.
	 * <p>
	 * This method is called from {@link #merge(AnyObjectId[])} after the
	 * {@link #sourceObjects}, {@link #sourceCommits} and {@link #sourceTrees}
	 * have been populated.
	 *
	 * @return true if the merge was completed without conflicts; false if the
	 *         merge strategy cannot handle this merge or there were conflicts
	 *         preventing it from automatically resolving all paths.
	 * @throws IncorrectObjectTypeException
	 *             one of the input objects is not a commit, but the strategy
	 *             requires it to be a commit.
	 * @throws java.io.IOException
	 *             one or more sources could not be read, or outputs could not
	 *             be written to the Repository.
	 */
	protected abstract boolean mergeImpl() throws IOException;

	/**
	 * Get resulting tree.
	 *
	 * @return resulting tree, if {@link #merge(AnyObjectId[])} returned true.
	 */
	public abstract ObjectId getResultTreeId();

	/**
	 * Set a progress monitor.
	 *
	 * @param monitor
	 *            Monitor to use, can be null to indicate no progress reporting
	 *            is desired.
	 * @since 4.2
	 */
	public void setProgressMonitor(ProgressMonitor monitor) {
		if (monitor == null) {
			this.monitor = NullProgressMonitor.INSTANCE;
		} else {
			this.monitor = monitor;
		}
	}
}
