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

package org.eclipse.jgit.revwalk;

import static org.eclipse.jgit.lib.Constants.OBJ_BLOB;
import static org.eclipse.jgit.lib.Constants.OBJ_COMMIT;
import static org.eclipse.jgit.lib.Constants.OBJ_TREE;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jgit.errors.CorruptObjectException;
import org.eclipse.jgit.errors.IncorrectObjectTypeException;
import org.eclipse.jgit.errors.LargeObjectException;
import org.eclipse.jgit.errors.MissingObjectException;
import org.eclipse.jgit.internal.JGitText;
import org.eclipse.jgit.lib.AnyObjectId;
import org.eclipse.jgit.lib.ObjectReader;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.filter.ObjectFilter;
import org.eclipse.jgit.util.RawParseUtils;

/**
 * Specialized subclass of RevWalk to include trees, blobs and tags.
 * <p>
 * Unlike RevWalk this subclass is able to remember starting roots that include
 * annotated tags, or arbitrary trees or blobs. Once commit generation is
 * complete and all commits have been popped by the application, individual
 * annotated tag, tree and blob objects can be popped through the additional
 * method {@link #nextObject()}.
 * <p>
 * Tree and blob objects reachable from interesting commits are automatically
 * scheduled for inclusion in the results of {@link #nextObject()}, returning
 * each object exactly once. Objects are sorted and returned according to the
 * the commits that reference them and the order they appear within a tree.
 * Ordering can be affected by changing the {@link RevSort} used to order the
 * commits that are returned first.
 */
public class ObjectWalk extends RevWalk {
	private static final int ID_SZ = 20;
	private static final int TYPE_SHIFT = 12;
	private static final int TYPE_TREE = 0040000 >>> TYPE_SHIFT;
	private static final int TYPE_SYMLINK = 0120000 >>> TYPE_SHIFT;
	private static final int TYPE_FILE = 0100000 >>> TYPE_SHIFT;
	private static final int TYPE_GITLINK = 0160000 >>> TYPE_SHIFT;

	/**
	 * Indicates a non-RevCommit is in {@link #pendingObjects}.
	 * <p>
	 * We can safely reuse {@link RevWalk#REWRITE} here for the same value as it
	 * is only set on RevCommit and {@link #pendingObjects} never has RevCommit
	 * instances inserted into it.
	 */
	private static final int IN_PENDING = RevWalk.REWRITE;

	private List<RevObject> rootObjects;

	private BlockObjQueue pendingObjects;

	private ObjectFilter objectFilter;

	private TreeVisit freeVisit;

	private TreeVisit currVisit;

	private byte[] pathBuf;

	private int pathLen;

	private boolean boundary;

	/**
	 * Create a new revision and object walker for a given repository.
	 *
	 * @param repo
	 *            the repository the walker will obtain data from.
	 */
	public ObjectWalk(final Repository repo) {
		this(repo.newObjectReader());
	}

	/**
	 * Create a new revision and object walker for a given repository.
	 *
	 * @param or
	 *            the reader the walker will obtain data from. The reader should
	 *            be released by the caller when the walker is no longer
	 *            required.
	 */
	public ObjectWalk(ObjectReader or) {
		super(or);
		setRetainBody(false);
		rootObjects = new ArrayList<>();
		pendingObjects = new BlockObjQueue();
		objectFilter = ObjectFilter.ALL;
		pathBuf = new byte[256];
	}

	/**
	 * Mark an object or commit to start graph traversal from.
	 * <p>
	 * Callers are encouraged to use {@link RevWalk#parseAny(AnyObjectId)}
	 * instead of {@link RevWalk#lookupAny(AnyObjectId, int)}, as this method
	 * requires the object to be parsed before it can be added as a root for the
	 * traversal.
	 * <p>
	 * The method will automatically parse an unparsed object, but error
	 * handling may be more difficult for the application to explain why a
	 * RevObject is not actually valid. The object pool of this walker would
	 * also be 'poisoned' by the invalid RevObject.
	 * <p>
	 * This method will automatically call {@link RevWalk#markStart(RevCommit)}
	 * if passed RevCommit instance, or a RevTag that directly (or indirectly)
	 * references a RevCommit.
	 *
	 * @param o
	 *            the object to start traversing from. The object passed must be
	 *            from this same revision walker.
	 * @throws MissingObjectException
	 *             the object supplied is not available from the object
	 *             database. This usually indicates the supplied object is
	 *             invalid, but the reference was constructed during an earlier
	 *             invocation to {@link RevWalk#lookupAny(AnyObjectId, int)}.
	 * @throws IncorrectObjectTypeException
	 *             the object was not parsed yet and it was discovered during
	 *             parsing that it is not actually the type of the instance
	 *             passed in. This usually indicates the caller used the wrong
	 *             type in a {@link RevWalk#lookupAny(AnyObjectId, int)} call.
	 * @throws IOException
	 *             a pack file or loose object could not be read.
	 */
	public void markStart(RevObject o) throws MissingObjectException,
			IncorrectObjectTypeException, IOException {
		while (o instanceof RevTag) {
			addObject(o);
			o = ((RevTag) o).getObject();
			parseHeaders(o);
		}

		if (o instanceof RevCommit)
			super.markStart((RevCommit) o);
		else
			addObject(o);
	}

	/**
	 * Mark an object to not produce in the output.
	 * <p>
	 * Uninteresting objects denote not just themselves but also their entire
	 * reachable chain, back until the merge base of an uninteresting commit and
	 * an otherwise interesting commit.
	 * <p>
	 * Callers are encouraged to use {@link RevWalk#parseAny(AnyObjectId)}
	 * instead of {@link RevWalk#lookupAny(AnyObjectId, int)}, as this method
	 * requires the object to be parsed before it can be added as a root for the
	 * traversal.
	 * <p>
	 * The method will automatically parse an unparsed object, but error
	 * handling may be more difficult for the application to explain why a
	 * RevObject is not actually valid. The object pool of this walker would
	 * also be 'poisoned' by the invalid RevObject.
	 * <p>
	 * This method will automatically call {@link RevWalk#markStart(RevCommit)}
	 * if passed RevCommit instance, or a RevTag that directly (or indirectly)
	 * references a RevCommit.
	 *
	 * @param o
	 *            the object to start traversing from. The object passed must be
	 * @throws MissingObjectException
	 *             the object supplied is not available from the object
	 *             database. This usually indicates the supplied object is
	 *             invalid, but the reference was constructed during an earlier
	 *             invocation to {@link RevWalk#lookupAny(AnyObjectId, int)}.
	 * @throws IncorrectObjectTypeException
	 *             the object was not parsed yet and it was discovered during
	 *             parsing that it is not actually the type of the instance
	 *             passed in. This usually indicates the caller used the wrong
	 *             type in a {@link RevWalk#lookupAny(AnyObjectId, int)} call.
	 * @throws IOException
	 *             a pack file or loose object could not be read.
	 */
	public void markUninteresting(RevObject o) throws MissingObjectException,
			IncorrectObjectTypeException, IOException {
		while (o instanceof RevTag) {
			o.flags |= UNINTERESTING;
			if (boundary)
				addObject(o);
			o = ((RevTag) o).getObject();
			parseHeaders(o);
		}

		if (o instanceof RevCommit)
			super.markUninteresting((RevCommit) o);
		else if (o instanceof RevTree)
			markTreeUninteresting((RevTree) o);
		else
			o.flags |= UNINTERESTING;

		if (o.getType() != OBJ_COMMIT && boundary)
			addObject(o);
	}

	@Override
	public void sort(RevSort s) {
		super.sort(s);
		boundary = hasRevSort(RevSort.BOUNDARY);
	}

	@Override
	public void sort(RevSort s, boolean use) {
		super.sort(s, use);
		boundary = hasRevSort(RevSort.BOUNDARY);
	}

	/**
	 * Get the currently configured object filter.
	 *
	 * @return the current filter. Never null as a filter is always needed.
	 *
	 * @since 4.0
	 */
	public ObjectFilter getObjectFilter() {
		return objectFilter;
	}

	/**
	 * Set the object filter for this walker.  This filter affects the objects
	 * visited by {@link #nextObject()}.  It does not affect the commits
	 * listed by {@link #next()}.
	 * <p>
	 * If the filter returns false for an object, then that object is skipped
	 * and objects reachable from it are not enqueued to be walked recursively.
	 * This can be used to speed up the object walk by skipping subtrees that
	 * are known to be uninteresting.
	 *
	 * @param newFilter
	 *            the new filter. If null the special {@link ObjectFilter#ALL}
	 *            filter will be used instead, as it matches every object.
	 *
	 * @since 4.0
	 */
	public void setObjectFilter(ObjectFilter newFilter) {
		assertNotStarted();
		objectFilter = newFilter != null ? newFilter : ObjectFilter.ALL;
	}

	@Override
	public RevCommit next() throws MissingObjectException,
			IncorrectObjectTypeException, IOException {
		for (;;) {
			final RevCommit r = super.next();
			if (r == null) {
				return null;
			}
			final RevTree t = r.getTree();
			if ((r.flags & UNINTERESTING) != 0) {
				if (objectFilter.include(this, t)) {
					markTreeUninteresting(t);
				}
				if (boundary) {
					return r;
				}
				continue;
			}
			if (objectFilter.include(this, t)) {
				pendingObjects.add(t);
			}
			return r;
		}
	}

	/**
	 * Pop the next most recent object.
	 *
	 * @return next most recent object; null if traversal is over.
	 * @throws MissingObjectException
	 *             one or or more of the next objects are not available from the
	 *             object database, but were thought to be candidates for
	 *             traversal. This usually indicates a broken link.
	 * @throws IncorrectObjectTypeException
	 *             one or or more of the objects in a tree do not match the type
	 *             indicated.
	 * @throws IOException
	 *             a pack file or loose object could not be read.
	 */
	public RevObject nextObject() throws MissingObjectException,
			IncorrectObjectTypeException, IOException {
		pathLen = 0;

		TreeVisit tv = currVisit;
		while (tv != null) {
			byte[] buf = tv.buf;
			for (int ptr = tv.ptr; ptr < buf.length;) {
				int startPtr = ptr;
				ptr = findObjectId(buf, ptr);
				idBuffer.fromRaw(buf, ptr);
				ptr += ID_SZ;

				if (!objectFilter.include(this, idBuffer)) {
					continue;
				}

				RevObject obj = objects.get(idBuffer);
				if (obj != null && (obj.flags & SEEN) != 0)
					continue;

				int mode = parseMode(buf, startPtr, ptr, tv);
				int flags;
				switch (mode >>> TYPE_SHIFT) {
				case TYPE_FILE:
				case TYPE_SYMLINK:
					if (obj == null) {
						obj = new RevBlob(idBuffer);
						obj.flags = SEEN;
						objects.add(obj);
						return obj;
					}
					if (!(obj instanceof RevBlob))
						throw new IncorrectObjectTypeException(obj, OBJ_BLOB);
					obj.flags = flags = obj.flags | SEEN;
					if ((flags & UNINTERESTING) == 0)
						return obj;
					if (boundary)
						return obj;
					continue;

				case TYPE_TREE:
					if (obj == null) {
						obj = new RevTree(idBuffer);
						obj.flags = SEEN;
						objects.add(obj);
						return enterTree(obj);
					}
					if (!(obj instanceof RevTree))
						throw new IncorrectObjectTypeException(obj, OBJ_TREE);
					obj.flags = flags = obj.flags | SEEN;
					if ((flags & UNINTERESTING) == 0)
						return enterTree(obj);
					if (boundary)
						return enterTree(obj);
					continue;

				case TYPE_GITLINK:
					continue;

				default:
					throw new CorruptObjectException(MessageFormat.format(
							JGitText.get().corruptObjectInvalidMode3,
							String.format("%o", Integer.valueOf(mode)), //$NON-NLS-1$
							idBuffer.name(),
							RawParseUtils.decode(buf, tv.namePtr, tv.nameEnd),
							tv.obj));
				}
			}

			currVisit = tv.parent;
			releaseTreeVisit(tv);
			tv = currVisit;
		}

		for (;;) {
			RevObject o = pendingObjects.next();
			if (o == null) {
				return null;
			}
			int flags = o.flags;
			if ((flags & SEEN) != 0)
				continue;
			flags |= SEEN;
			o.flags = flags;
			if ((flags & UNINTERESTING) == 0 | boundary) {
				if (o instanceof RevTree) {
					tv = newTreeVisit(o);
					tv.parent = null;
					currVisit = tv;
				}
				return o;
			}
		}
	}

	private RevObject enterTree(RevObject obj) throws MissingObjectException,
			IncorrectObjectTypeException, IOException {
		TreeVisit tv = newTreeVisit(obj);
		tv.parent = currVisit;
		currVisit = tv;
		return obj;
	}

	private static int findObjectId(byte[] buf, int ptr) {
		// Skip over the mode and name until the NUL before the ObjectId
		// can be located. Skip the NUL as the function returns.
		for (;;) {
			if (buf[++ptr] == 0) return ++ptr;
			if (buf[++ptr] == 0) return ++ptr;
			if (buf[++ptr] == 0) return ++ptr;
			if (buf[++ptr] == 0) return ++ptr;

			if (buf[++ptr] == 0) return ++ptr;
			if (buf[++ptr] == 0) return ++ptr;
			if (buf[++ptr] == 0) return ++ptr;
			if (buf[++ptr] == 0) return ++ptr;

			if (buf[++ptr] == 0) return ++ptr;
			if (buf[++ptr] == 0) return ++ptr;
			if (buf[++ptr] == 0) return ++ptr;
			if (buf[++ptr] == 0) return ++ptr;

			if (buf[++ptr] == 0) return ++ptr;
			if (buf[++ptr] == 0) return ++ptr;
			if (buf[++ptr] == 0) return ++ptr;
			if (buf[++ptr] == 0) return ++ptr;
		}
	}

	private static int parseMode(byte[] buf, int startPtr, int recEndPtr, TreeVisit tv) {
		int mode = buf[startPtr] - '0';
		for (;;) {
			byte c = buf[++startPtr];
			if (' ' == c)
				break;
			mode <<= 3;
			mode += c - '0';

			c = buf[++startPtr];
			if (' ' == c)
				break;
			mode <<= 3;
			mode += c - '0';

			c = buf[++startPtr];
			if (' ' == c)
				break;
			mode <<= 3;
			mode += c - '0';

			c = buf[++startPtr];
			if (' ' == c)
				break;
			mode <<= 3;
			mode += c - '0';

			c = buf[++startPtr];
			if (' ' == c)
				break;
			mode <<= 3;
			mode += c - '0';

			c = buf[++startPtr];
			if (' ' == c)
				break;
			mode <<= 3;
			mode += c - '0';

			c = buf[++startPtr];
			if (' ' == c)
				break;
			mode <<= 3;
			mode += c - '0';
		}

		tv.ptr = recEndPtr;
		tv.namePtr = startPtr + 1;
		tv.nameEnd = recEndPtr - (ID_SZ + 1);
		return mode;
	}

	/**
	 * Verify all interesting objects are available, and reachable.
	 * <p>
	 * Callers should populate starting points and ending points with
	 * {@link #markStart(RevObject)} and {@link #markUninteresting(RevObject)}
	 * and then use this method to verify all objects between those two points
	 * exist in the repository and are readable.
	 * <p>
	 * This method returns successfully if everything is connected; it throws an
	 * exception if there is a connectivity problem. The exception message
	 * provides some detail about the connectivity failure.
	 *
	 * @throws MissingObjectException
	 *             one or or more of the next objects are not available from the
	 *             object database, but were thought to be candidates for
	 *             traversal. This usually indicates a broken link.
	 * @throws IncorrectObjectTypeException
	 *             one or or more of the objects in a tree do not match the type
	 *             indicated.
	 * @throws IOException
	 *             a pack file or loose object could not be read.
	 */
	public void checkConnectivity() throws MissingObjectException,
			IncorrectObjectTypeException, IOException {
		for (;;) {
			final RevCommit c = next();
			if (c == null)
				break;
		}
		for (;;) {
			final RevObject o = nextObject();
			if (o == null)
				break;
			if (o instanceof RevBlob && !reader.has(o))
				throw new MissingObjectException(o, OBJ_BLOB);
		}
	}

	/**
	 * Get the current object's complete path.
	 * <p>
	 * This method is not very efficient and is primarily meant for debugging
	 * and final output generation. Applications should try to avoid calling it,
	 * and if invoked do so only once per interesting entry, where the name is
	 * absolutely required for correct function.
	 *
	 * @return complete path of the current entry, from the root of the
	 *         repository. If the current entry is in a subtree there will be at
	 *         least one '/' in the returned string. Null if the current entry
	 *         has no path, such as for annotated tags or root level trees.
	 */
	public String getPathString() {
		if (pathLen == 0) {
			pathLen = updatePathBuf(currVisit);
			if (pathLen == 0)
				return null;
		}
		return RawParseUtils.decode(pathBuf, 0, pathLen);
	}

	/**
	 * Get the current object's path hash code.
	 * <p>
	 * This method computes a hash code on the fly for this path, the hash is
	 * suitable to cluster objects that may have similar paths together.
	 *
	 * @return path hash code; any integer may be returned.
	 */
	public int getPathHashCode() {
		TreeVisit tv = currVisit;
		if (tv == null)
			return 0;

		int nameEnd = tv.nameEnd;
		if (nameEnd == 0) {
			// When nameEnd == 0 the subtree is itself the current path
			// being visited. The name hash must be obtained from its
			// parent tree. If there is no parent, this is a root tree with
			// a hash code of 0.
			tv = tv.parent;
			if (tv == null)
				return 0;
			nameEnd = tv.nameEnd;
		}

		byte[] buf;
		int ptr;

		if (16 <= (nameEnd - tv.namePtr)) {
			buf = tv.buf;
			ptr = nameEnd - 16;
		} else {
			nameEnd = pathLen;
			if (nameEnd == 0) {
				nameEnd = updatePathBuf(currVisit);
				pathLen = nameEnd;
			}
			buf = pathBuf;
			ptr = Math.max(0, nameEnd - 16);
		}

		int hash = 0;
		for (; ptr < nameEnd; ptr++) {
			byte c = buf[ptr];
			if (c != ' ')
				hash = (hash >>> 2) + (c << 24);
		}
		return hash;
	}

	/** @return the internal buffer holding the current path. */
	public byte[] getPathBuffer() {
		if (pathLen == 0)
			pathLen = updatePathBuf(currVisit);
		return pathBuf;
	}

	/** @return length of the path in {@link #getPathBuffer()}. */
	public int getPathLength() {
		if (pathLen == 0)
			pathLen = updatePathBuf(currVisit);
		return pathLen;
	}

	private int updatePathBuf(TreeVisit tv) {
		if (tv == null)
			return 0;

		// If nameEnd == 0 this tree has not yet contributed an entry.
		// Update only for the parent, which if null will be empty.
		int nameEnd = tv.nameEnd;
		if (nameEnd == 0)
			return updatePathBuf(tv.parent);

		int ptr = tv.pathLen;
		if (ptr == 0) {
			ptr = updatePathBuf(tv.parent);
			if (ptr == pathBuf.length)
				growPathBuf(ptr);
			if (ptr != 0)
				pathBuf[ptr++] = '/';
			tv.pathLen = ptr;
		}

		int namePtr = tv.namePtr;
		int nameLen = nameEnd - namePtr;
		int end = ptr + nameLen;
		while (pathBuf.length < end)
			growPathBuf(ptr);
		System.arraycopy(tv.buf, namePtr, pathBuf, ptr, nameLen);
		return end;
	}

	private void growPathBuf(int ptr) {
		byte[] newBuf = new byte[pathBuf.length << 1];
		System.arraycopy(pathBuf, 0, newBuf, 0, ptr);
		pathBuf = newBuf;
	}

	@Override
	public void dispose() {
		super.dispose();
		pendingObjects = new BlockObjQueue();
		currVisit = null;
		freeVisit = null;
	}

	@Override
	protected void reset(final int retainFlags) {
		super.reset(retainFlags);

		for (RevObject obj : rootObjects)
			obj.flags &= ~IN_PENDING;

		rootObjects = new ArrayList<>();
		pendingObjects = new BlockObjQueue();
		currVisit = null;
		freeVisit = null;
	}

	private void addObject(final RevObject o) {
		if ((o.flags & IN_PENDING) == 0) {
			o.flags |= IN_PENDING;
			rootObjects.add(o);
			pendingObjects.add(o);
		}
	}

	private void markTreeUninteresting(final RevTree tree)
			throws MissingObjectException, IncorrectObjectTypeException,
			IOException {
		if ((tree.flags & UNINTERESTING) != 0)
			return;
		tree.flags |= UNINTERESTING;

		byte[] raw = reader.open(tree, OBJ_TREE).getCachedBytes();
		for (int ptr = 0; ptr < raw.length;) {
			byte c = raw[ptr];
			int mode = c - '0';
			for (;;) {
				c = raw[++ptr];
				if (' ' == c)
					break;
				mode <<= 3;
				mode += c - '0';
			}
			while (raw[++ptr] != 0) {
				// Skip entry name.
			}
			ptr++; // Skip NUL after entry name.

			switch (mode >>> TYPE_SHIFT) {
			case TYPE_FILE:
			case TYPE_SYMLINK:
				idBuffer.fromRaw(raw, ptr);
				lookupBlob(idBuffer).flags |= UNINTERESTING;
				break;

			case TYPE_TREE:
				idBuffer.fromRaw(raw, ptr);
				markTreeUninteresting(lookupTree(idBuffer));
				break;

			case TYPE_GITLINK:
				break;

			default:
				idBuffer.fromRaw(raw, ptr);
				throw new CorruptObjectException(MessageFormat.format(
						JGitText.get().corruptObjectInvalidMode3,
						String.format("%o", Integer.valueOf(mode)), //$NON-NLS-1$
						idBuffer.name(), "", tree)); //$NON-NLS-1$
			}
			ptr += ID_SZ;
		}
	}

	private TreeVisit newTreeVisit(RevObject obj) throws LargeObjectException,
			MissingObjectException, IncorrectObjectTypeException, IOException {
		TreeVisit tv = freeVisit;
		if (tv != null) {
			freeVisit = tv.parent;
			tv.ptr = 0;
			tv.namePtr = 0;
			tv.nameEnd = 0;
			tv.pathLen = 0;
		} else {
			tv = new TreeVisit();
		}
		tv.obj = obj;
		tv.buf = reader.open(obj, OBJ_TREE).getCachedBytes();
		return tv;
	}

	private void releaseTreeVisit(TreeVisit tv) {
		tv.buf = null;
		tv.parent = freeVisit;
		freeVisit = tv;
	}

	private static class TreeVisit {
		/** Parent tree visit that entered this tree, null if root tree. */
		TreeVisit parent;

		/** The RevTree currently being iterated through. */
		RevObject obj;

		/** Canonical encoding of the tree named by {@link #obj}. */
		byte[] buf;

		/** Index of next entry to parse in {@link #buf}. */
		int ptr;

		/** Start of the current name entry in {@link #buf}. */
		int namePtr;

		/** One past end of name, {@code nameEnd - namePtr} is the length. */
		int nameEnd;

		/** Number of bytes in the path leading up to this tree. */
		int pathLen;
	}
}
