package org.eclipse.jgit.revwalk;

import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

import javax.annotation.Nullable;

import org.eclipse.jgit.errors.IncorrectObjectTypeException;
import org.eclipse.jgit.errors.MissingObjectException;
import org.eclipse.jgit.errors.StopWalkException;
import org.eclipse.jgit.lib.AnyObjectId;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.LastCommitsOfChildren.Value;
import org.eclipse.jgit.revwalk.filter.RevFilter;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.eclipse.jgit.treewalk.filter.TreeFilter;
import org.eclipse.jgit.util.RawParseUtils;

import com.google.common.base.Throwables;
import com.pmease.commons.git.GitUtils;

public final class LastCommitsOfChildren extends HashMap<String, Value> {

	private static final long serialVersionUID = 1L;

	private static final int PARSED = RevWalk.PARSED;
	
	public LastCommitsOfChildren(Repository repo, AnyObjectId until) {
		this(repo, until, null);
	}
	
	public LastCommitsOfChildren(Repository repo, AnyObjectId until, @Nullable String treePath) {
		this(repo, until, treePath, null);
	}
	
	public LastCommitsOfChildren(final Repository repo, AnyObjectId until, 
			@Nullable String treePath, @Nullable final Cache cache) {
		try {
			treePath = GitUtils.normalizePath(treePath);
			if (treePath == null) 
				treePath = "";
			
			final byte[] treePathRaw = Constants.encode(treePath);
			final Set<String> children = new HashSet<>();
			final Set<String> modifiedChildren = new HashSet<>();

			RevWalk revWalk = new RevWalk(repo);
			RevCommit untilCommit = revWalk.parseCommit(until);
			
			if (treePath.length() != 0) {
				TreeWalk treeWalk = TreeWalk.forPath(repo, treePath, untilCommit.getTree());
				treeWalk.enterSubtree();
				treeWalk.setRecursive(false);
				while (treeWalk.next())
					children.add(treeWalk.getPathString().substring(treePath.length()+1));
			} else {
				TreeWalk treeWalk = new TreeWalk(repo);
				treeWalk.addTree(untilCommit.getTree());
				treeWalk.setRecursive(false);
				while (treeWalk.next())
					children.add(treeWalk.getPathString().substring(treePath.length()));
			}
			
			revWalk.markStart(untilCommit);
			revWalk.setRewriteParents(false);

			final AtomicReference<Map<String, Value>> lastCommitsRef = new AtomicReference<>();
			final RevFlag visited = revWalk.newFlag("visited");
			final RevFlag cached = revWalk.newFlag("cached");
			
			revWalk.setRevFilter(new RevFilter() {

				@Override
				public boolean include(RevWalk revWalker, RevCommit commit)
						throws StopWalkException, MissingObjectException, IncorrectObjectTypeException, IOException {
					commit.add(visited);

					Map<String, Value> lastCommits = lastCommitsRef.get();
					if (lastCommits == null && cache != null) {
						lastCommits = cache.getLastCommitsOfChildren(commit.getId());
						if (lastCommits != null) {
							commit.add(cached);
							lastCommitsRef.set(lastCommits);
						}
					}
					
					if (commit.has(cached))
						cacheParents(commit.parents, cached, visited);
					
					DateRevQueue queue = (DateRevQueue) revWalker.queue;
					if (commit.has(cached) && queue.everbodyHasFlag(cached.mask)) {
						for (Map.Entry<String, Value> entry: lastCommits.entrySet()) {
							String child = entry.getKey();
							if (children.contains(child) && !containsKey(child))
								put(child, entry.getValue());
						}
						commit.parents = RevCommit.NO_PARENTS;
						return true;
					}
					
					TreeWalk treeWalker = new TreeWalk(revWalker.reader);
					treeWalker.setRecursive(true);

					RevCommit[] pList = commit.parents;
					int nParents = pList.length;

					ObjectId[] trees = new ObjectId[nParents + 1];
					for (int i = 0; i < nParents; i++) {
						RevCommit p = commit.parents[i];
						if ((p.flags & PARSED) == 0)
							p.parseHeaders(revWalker);
						trees[i] = p.getTree();
					}
					trees[nParents] = commit.getTree();
					treeWalker.reset(trees);
					
					final boolean[] changed = new boolean[nParents];
					final AtomicReference<String> childRef = new AtomicReference<>();
					
					treeWalker.setFilter(new TreeFilter() {

						@Override
						public boolean include(TreeWalk walker) 
								throws MissingObjectException, IncorrectObjectTypeException, IOException {
							if (treePathRaw.length == 0 || walker.isPathPrefix(treePathRaw, treePathRaw.length) == 0) {
								int walkPathLen = walker.getPathLength();
								if (walkPathLen <= treePathRaw.length) {
									int n = walker.getTreeCount();
									if (n == 1) {
										walker.setRecursive(true);
										return true;
									} else {
										int m = walker.getRawMode(n-1);
										for (int i = 0; i < n-1; i++) {
											if (walker.getRawMode(i) != m || !walker.idEqual(i, n-1)) {
												walker.setRecursive(true);
												return true;
											}
										}
									}
									return false;
								} else {
									boolean modified = false;
									int n = walker.getTreeCount();
									if (n == 1) {
										modified = true;
									} else {
										int m = walker.getRawMode(n-1);
										for (int i = 0; i < n-1; i++) {
											if (walker.getRawMode(i) != m || !walker.idEqual(i, n-1)) {
												modified = true;
												changed[i] = true;
											} else {
												changed[i] = false;
											}
										}
									}
									if (modified) {
										childRef.set(RawParseUtils.decode(
												Constants.CHARSET, 
												walker.getRawPath(), 
												treePathRaw.length!=0?treePathRaw.length+1:0, 
												walkPathLen));
										walker.setRecursive(false);
										return true;
									} else {
										return false;
									}						
								}  
							} else {
								return false;
							}
						}

						@Override
						public boolean shouldBeRecursive() {
							throw new UnsupportedOperationException();
						}

						@Override
						public TreeFilter clone() {
							throw new UnsupportedOperationException();
						}
						
					});

					if (nParents <= 1) {
						while (treeWalker.next()) {
							String child = childRef.get();
							if (children.contains(child) && !containsKey(child))
								modifiedChildren.add(child);
						}
					} else {
						int[] changes = new int[nParents];
						while (treeWalker.next()) {
							String child = childRef.get();
							if (children.contains(child) && !containsKey(child)) {
								boolean same = false;
								for (int i = 0; i < nParents; i++) {
									if (changed[i]) 
										changes[i] ++;
									else
										same = true;
								}
								if (!same)
									modifiedChildren.add(child);
							}
						}

						if (!commit.has(cached)) {
							for (int i = 0; i < nParents; i++) {
								if (changes[i] == 0) {
									RevCommit p = pList[i];
									commit.parents = new RevCommit[]{p};
									return false;
								}
							}
						}
					}
					return !modifiedChildren.isEmpty();
				}

				@Override
				public boolean requiresCommitBody() {
					return false;
				}

				@Override
				public RevFilter clone() {
					throw new UnsupportedOperationException();
				}
				
			});
			
			RevCommit next = revWalk.next();
			while (size() < children.size() && next != null) {
				Value value = new Value(next);
				for (String child: modifiedChildren)
					put(child, value);
				modifiedChildren.clear();
				next = revWalk.next();
			}
		} catch (Exception e) {
			throw Throwables.propagate(e);
		}
	}
	
	private void cacheParents(RevCommit[] parents, RevFlag cached, RevFlag visited) {
		for (RevCommit p: parents) {
			if (!p.has(cached)) {
				p.add(cached);
				if (p.has(visited))
					cacheParents(p.parents, cached, visited);
			}
		}
	}
	
	public static class Value implements Serializable {
		
		private static final long serialVersionUID = 1L;

		private final ObjectId id;
		
		private final String summary;
		
		private final long timestamp;
		
		public Value(RevCommit commit) {
			this.id = commit.copy();
			this.summary = commit.getShortMessage();
			this.timestamp = commit.getCommitTime();
		}

		public ObjectId getId() {
			return id;
		}

		public String getSummary() {
			return summary;
		}

		public long getTimestamp() {
			return timestamp;
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
		
	}
	
	public static interface Cache {
		
		@Nullable
		Map<String, Value> getLastCommitsOfChildren(ObjectId commitId);
		
	}
	
}
