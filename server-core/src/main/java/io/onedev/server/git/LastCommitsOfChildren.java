package io.onedev.server.git;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.jgit.lib.AnyObjectId;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.FileMode;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.PersonIdent;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.DateRevQueue;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.eclipse.jgit.treewalk.filter.TreeFilter;
import org.eclipse.jgit.util.RawParseUtils;
import org.jspecify.annotations.Nullable;

import io.onedev.commons.utils.ExceptionUtils;
import io.onedev.commons.utils.StringUtils;
import io.onedev.server.git.LastCommitsOfChildren.Value;

/**
 * Finds the last commits of all immediate children of a Git tree in one history
 * traversal. Directory entries are compared by tree ID without opening their
 * contents. Only commits contributing a result need their message and author.
 */
public final class LastCommitsOfChildren extends HashMap<String, Value> {

	private static final long serialVersionUID = 1L;

	public LastCommitsOfChildren(Repository repo, AnyObjectId until) {
		this(repo, until, null, null);
	}

	public LastCommitsOfChildren(Repository repo, AnyObjectId until, @Nullable String treePath) {
		this(repo, until, treePath, null);
	}

	public LastCommitsOfChildren(Repository repo, AnyObjectId until, @Nullable Cache cache) {
		this(repo, until, null, cache);
	}

	/**
	 * @param treePath
	 *            directory to list; null or empty refers to the repository root
	 * @param cache
	 *            optional results for the same directory at earlier commits
	 */
	public LastCommitsOfChildren(Repository repo, AnyObjectId until,
			@Nullable String treePath, @Nullable Cache cache) {
		try (RevWalk revWalk = new RevWalk(repo)) {
			revWalk.setRetainBody(false);
			treePath = GitUtils.normalizePath(treePath);
			if (treePath == null)
				treePath = "";
			byte[] treePathRaw = Constants.encode(treePath);
			RevCommit untilCommit = revWalk.parseCommit(until);
			Set<String> remaining = new HashSet<>();
			try (TreeWalk treeWalk = new TreeWalk(revWalk.getObjectReader())) {
				if (treePath.isEmpty()) {
					treeWalk.addTree(untilCommit.getTree());
				} else {
					try (TreeWalk pathWalk = TreeWalk.forPath(revWalk.getObjectReader(), treePath,
							untilCommit.getTree())) {
						if (pathWalk == null || !FileMode.TREE.equals(pathWalk.getFileMode(0)))
							throw new IllegalArgumentException("Path '" + treePath + "' does not exist or is not a tree.");
						treeWalk.addTree(pathWalk.getObjectId(0));
					}
				}
				while (treeWalk.next())
					remaining.add(treeWalk.getPathString());
			}

			// Batch children reaching the same commit, but keep each child's history
			// separate: a merge follows its first parent with an identical entry.
			DateRevQueue pending = new DateRevQueue();
			Map<RevCommit, Set<String>> childrenByCommit = new HashMap<>();
			if (!remaining.isEmpty()) {
				pending.add(untilCommit);
				childrenByCommit.put(untilCommit, remaining);
			}

			RevCommit commit;
			while ((commit = pending.next()) != null) {
				if (Thread.currentThread().isInterrupted())
					throw new InterruptedIOException("Interrupted while loading last commits");
				Set<String> children = childrenByCommit.remove(commit);
				if (cache != null) {
					Map<String, Value> cached = cache.getLastCommitsOfChildren(commit);
					if (cached != null) {
						var iterator = children.iterator();
						while (iterator.hasNext()) {
							String child = iterator.next();
							Value value = cached.get(child);
							if (value != null) {
								put(child, value);
								iterator.remove();
							}
						}
					}
				}
				if (children.isEmpty())
					continue;

				Set<String> modified = new HashSet<>();
				findChanges(revWalk, commit, treePathRaw, children, modified, pending, childrenByCommit);
				if (!modified.isEmpty()) {
					revWalk.parseBody(commit);
					Value value = new Value(commit);
					for (String child : modified)
						put(child, value);
					commit.disposeBody();
				}
				if (!children.isEmpty()) {
					RevCommit parent = commit.getParent(0);
					Set<String> queued = childrenByCommit.putIfAbsent(parent, children);
					if (queued == null)
						pending.add(parent);
					else
						queued.addAll(children);
				}
			}
		} catch (IOException e) {
			throw ExceptionUtils.unchecked(e);
		}
	}

	private static void findChanges(RevWalk revWalk, RevCommit commit, byte[] treePath,
			Set<String> children, Set<String> modified, DateRevQueue pending,
			Map<RevCommit, Set<String>> childrenByCommit) throws IOException {
		RevCommit[] parents = commit.getParents();
		try (TreeWalk treeWalk = new TreeWalk(revWalk.getObjectReader())) {
			for (RevCommit parent : parents) {
				revWalk.parseHeaders(parent);
				treeWalk.addTree(parent.getTree());
			}
			treeWalk.addTree(commit.getTree());
			treeWalk.setFilter(TreeFilter.ANY_DIFF);
			while (treeWalk.next()) {
				if (treePath.length != 0 && treeWalk.isPathPrefix(treePath, treePath.length) != 0)
					continue;
				int pathLength = treeWalk.getPathLength();
				if (pathLength <= treePath.length) {
					if (treeWalk.isSubtree())
						treeWalk.enterSubtree();
				} else {
					String child = RawParseUtils.decode(StandardCharsets.UTF_8, treeWalk.getRawPath(),
							treePath.length != 0 ? treePath.length + 1 : 0, pathLength);
					// TreeWalk reports a file/directory conflict as separate entries.
					// Each routed child exists in this commit; ignore the other type's
					// entry instead of mistaking two missing entries for a match.
					if (!children.contains(child) || treeWalk.getRawMode(parents.length) == 0)
						continue;
					int matchingParent = -1;
					for (int i = 0; i < parents.length; i++) {
						if (treeWalk.getRawMode(i) == treeWalk.getRawMode(parents.length)
								&& treeWalk.idEqual(i, parents.length)) {
							matchingParent = i;
							break;
						}
					}
					if (matchingParent == -1) {
						modified.add(child);
						children.remove(child);
					} else if (matchingParent != 0) {
						// Other children may need a different parent. Do not let
						// those branches contribute changes discarded for this child.
						children.remove(child);
						RevCommit parent = parents[matchingParent];
						Set<String> queued = childrenByCommit.get(parent);
						if (queued == null) {
							queued = new HashSet<>();
							childrenByCommit.put(parent, queued);
							pending.add(parent);
						}
						queued.add(child);
					}
				}
			}
		}
		// Entries identical to every parent are omitted by ANY_DIFF and follow
		// the first parent. There is deliberately no global "visited" flag:
		// clock skew may bring another child's history to this commit later.
	}

	public static class Value implements Serializable {

		private static final long serialVersionUID = 1L;

		private final ObjectId id;

		private final PersonIdent author;

		private final Date commitDate;

		private final String summary;

		public Value(RevCommit commit) {
			this.id = commit.copy();
			this.author = commit.getAuthorIdent();
			this.commitDate = Date.from(commit.getCommitterIdent().getWhenAsInstant());
			this.summary = StringUtils.substringBefore(commit.getFullMessage(), "\n").trim();
		}

		public ObjectId getId() {
			return id;
		}

		public String getSummary() {
			return summary;
		}

		public PersonIdent getAuthor() {
			return author;
		}

		public Date getCommitDate() {
			return commitDate;
		}

		@Override
		public boolean equals(Object obj) {
			if (obj instanceof Value) {
				Value value = (Value) obj;
				return id.equals(value.getId());
			} else {
				return false;
			}
		}

		@Override
		public String toString() {
			return id.name();
		}

	}

	public static interface Cache {

		@Nullable
		Map<String, Value> getLastCommitsOfChildren(ObjectId commitId);

	}

}
