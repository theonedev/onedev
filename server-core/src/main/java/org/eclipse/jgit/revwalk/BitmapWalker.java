/*
 * Copyright (C) 2012, Google Inc. and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0 which is available at
 * https://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package org.eclipse.jgit.revwalk;

import java.io.IOException;
import java.util.Arrays;

import org.eclipse.jgit.errors.IncorrectObjectTypeException;
import org.eclipse.jgit.errors.MissingObjectException;
import org.eclipse.jgit.internal.revwalk.AddToBitmapFilter;
import org.eclipse.jgit.internal.revwalk.AddUnseenToBitmapFilter;
import org.eclipse.jgit.lib.AnyObjectId;
import org.eclipse.jgit.lib.BitmapIndex;
import org.eclipse.jgit.lib.BitmapIndex.Bitmap;
import org.eclipse.jgit.lib.BitmapIndex.BitmapBuilder;
import org.eclipse.jgit.lib.NullProgressMonitor;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ProgressMonitor;
import org.eclipse.jgit.revwalk.filter.ObjectFilter;

/**
 * Helper class to do ObjectWalks with pack index bitmaps.
 *
 * @since 4.10
 */
public final class BitmapWalker {

	private final ObjectWalk walker;

	private final BitmapIndex bitmapIndex;

	private final ProgressMonitor pm;

	private long countOfBitmapIndexMisses;

	/**
	 * Create a BitmapWalker.
	 *
	 * @param walker walker to use when traversing the object graph.
	 * @param bitmapIndex index to obtain bitmaps from.
	 * @param pm progress monitor to report progress on.
	 */
	public BitmapWalker(
			ObjectWalk walker, BitmapIndex bitmapIndex, ProgressMonitor pm) {
		this.walker = walker;
		this.bitmapIndex = bitmapIndex;
		this.pm = (pm == null) ? NullProgressMonitor.INSTANCE : pm;
	}

	/**
	 * Return the number of objects that had to be walked because they were not covered by a
	 * bitmap.
	 *
	 * @return the number of objects that had to be walked because they were not covered by a
	 *     bitmap.
	 */
	public long getCountOfBitmapIndexMisses() {
		return countOfBitmapIndexMisses;
	}

	/**
	 * Return, as a bitmap, the objects reachable from the objects in start.
	 *
	 * @param start
	 *            the objects to start the object traversal from.
	 * @param seen
	 *            the objects to skip if encountered during traversal.
	 * @param ignoreMissing
	 *            true to ignore missing objects, false otherwise.
	 * @return as a bitmap, the objects reachable from the objects in start.
	 * @throws org.eclipse.jgit.errors.MissingObjectException
	 *             the object supplied is not available from the object
	 *             database. This usually indicates the supplied object is
	 *             invalid, but the reference was constructed during an earlier
	 *             invocation to
	 *             {@link org.eclipse.jgit.revwalk.RevWalk#lookupAny(AnyObjectId, int)}.
	 * @throws org.eclipse.jgit.errors.IncorrectObjectTypeException
	 *             the object was not parsed yet and it was discovered during
	 *             parsing that it is not actually the type of the instance
	 *             passed in. This usually indicates the caller used the wrong
	 *             type in a
	 *             {@link org.eclipse.jgit.revwalk.RevWalk#lookupAny(AnyObjectId, int)}
	 *             call.
	 * @throws java.io.IOException
	 *             a pack file or loose object could not be read.
	 */
	public BitmapBuilder findObjects(Iterable<? extends ObjectId> start, BitmapBuilder seen,
			boolean ignoreMissing)
			throws MissingObjectException, IncorrectObjectTypeException,
				   IOException {
		if (!ignoreMissing) {
			return findObjectsWalk(start, seen, false);
		}

		try {
			return findObjectsWalk(start, seen, true);
		} catch (MissingObjectException ignore) {
			// An object reachable from one of the "start"s is missing.
			// Walk from the "start"s one at a time so it can be excluded.
		}

		final BitmapBuilder result = bitmapIndex.newBitmapBuilder();
		for (ObjectId obj : start) {
			Bitmap bitmap = bitmapIndex.getBitmap(obj);
			if (bitmap != null) {
				result.or(bitmap);
			}
		}

		for (ObjectId obj : start) {
			if (result.contains(obj)) {
				continue;
			}
			try {
				result.or(findObjectsWalk(Arrays.asList(obj), result, false));
			} catch (MissingObjectException ignore) {
				// An object reachable from this "start" is missing.
				//
				// This can happen when the client specified a "have" line
				// pointing to an object that is present but unreachable:
				// "git prune" and "git fsck" only guarantee that the object
				// database will continue to contain all objects reachable
				// from a ref and does not guarantee connectivity for other
				// objects in the object database.
				//
				// In this situation, skip the relevant "start" and move on
				// to the next one.
				//
				// TODO(czhen): Make findObjectsWalk resume the walk instead
				// once RevWalk and ObjectWalk support that.
			}
		}
		return result;
	}

	private BitmapBuilder findObjectsWalk(Iterable<? extends ObjectId> start, BitmapBuilder seen,
			boolean ignoreMissingStart)
			throws MissingObjectException, IncorrectObjectTypeException,
			IOException {
		walker.reset();
		final BitmapBuilder bitmapResult = bitmapIndex.newBitmapBuilder();

		for (ObjectId obj : start) {
			Bitmap bitmap = bitmapIndex.getBitmap(obj);
			if (bitmap != null)
				bitmapResult.or(bitmap);
		}

		boolean marked = false;
		for (ObjectId obj : start) {
			try {
				if (!bitmapResult.contains(obj)) {
					walker.markStart(walker.parseAny(obj));
					marked = true;
				}
			} catch (MissingObjectException e) {
				if (ignoreMissingStart)
					continue;
				throw e;
			}
		}

		if (marked) {
			if (seen == null) {
				walker.setRevFilter(new AddToBitmapFilter(bitmapResult));
			} else {
				walker.setRevFilter(
						new AddUnseenToBitmapFilter(seen, bitmapResult));
			}
			walker.setObjectFilter(new BitmapObjectFilter(bitmapResult));

			while (walker.next() != null) {
				// Iterate through all of the commits. The BitmapRevFilter does
				// the work.
				//
				// filter.include returns true for commits that do not have
				// a bitmap in bitmapIndex and are not reachable from a
				// bitmap in bitmapIndex encountered earlier in the walk.
				// Thus the number of commits returned by next() measures how
				// much history was traversed without being able to make use
				// of bitmaps.
				pm.update(1);
				countOfBitmapIndexMisses++;
			}

			RevObject ro;
			while ((ro = walker.nextObject()) != null) {
				bitmapResult.addObject(ro, ro.getType());
				pm.update(1);
			}
		}

		return bitmapResult;
	}

	/**
	 * Filter that excludes objects already in the given bitmap.
	 */
	static class BitmapObjectFilter extends ObjectFilter {
		private final BitmapBuilder bitmap;

		BitmapObjectFilter(BitmapBuilder bitmap) {
			this.bitmap = bitmap;
		}

		@Override
		public final boolean include(ObjectWalk walker, AnyObjectId objid)
			throws MissingObjectException, IncorrectObjectTypeException,
			       IOException {
			return !bitmap.contains(objid);
		}
	}
}
