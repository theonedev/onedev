package org.eclipse.jgit.revwalk;

import java.io.IOException;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.util.Date;
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
import org.eclipse.jgit.lib.FileMode;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.PersonIdent;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.LastCommitsOfChildren.Value;
import org.eclipse.jgit.revwalk.filter.RevFilter;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.eclipse.jgit.treewalk.filter.TreeFilter;
import org.eclipse.jgit.util.RawParseUtils;

import io.onedev.commons.utils.ExceptionUtils;
import io.onedev.commons.utils.StringUtils;
import io.onedev.server.git.GitUtils;

/**
 * This class calculates last commits of children of a git tree.
 * 
 * @author robin
 *
 */
public final class LastCommitsOfChildren extends HashMap<String, Value> {

	private static final long serialVersionUID = 1L;

	private static final int PARSED = RevWalk.PARSED;
	
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
	 * Constructs a hashmap with key representing child name under specified tree, and value 
	 * represents last commit info of the child.
	 * 
	 * @param repo
	 * 			repository to get last commits info
	 * @param until
	 * 			get last commits no newer than this commit
	 * @param treePath
	 * 			parent directory to get children commit info under, use 
	 * 			empty string or <tt>null</tt> to refer to repository root
	 * @param cache
	 * 			optional cache to speed up calculation
	 */
	public LastCommitsOfChildren(final Repository repo, AnyObjectId until, 
			@Nullable String treePath, @Nullable final Cache cache) {
		try (RevWalk revWalk = new RevWalk(repo)) {
			treePath = GitUtils.normalizePath(treePath);
			if (treePath == null) 
				treePath = "";
			
			final byte[] treePathRaw = Constants.encode(treePath);
			final Set<String> children = new HashSet<>();
			final Set<String> modifiedChildren = new HashSet<>();

			RevCommit untilCommit = revWalk.parseCommit(until);

			/*
			 * Find out child directory or file names under the tree
			 */
			if (treePath.length() != 0) {
				TreeWalk treeWalk = TreeWalk.forPath(repo, treePath, untilCommit.getTree());
				if (treeWalk == null || !FileMode.TREE.equals(treeWalk.getFileMode(0)))
					throw new IllegalArgumentException("Path '" + treePath + "' does not exist or is not a tree.");
				treeWalk.enterSubtree();
				treeWalk.setRecursive(false);
				while (treeWalk.next())
					children.add(treeWalk.getPathString().substring(treePath.length()+1));
			} else {
				try (TreeWalk treeWalk = new TreeWalk(repo)) {
					treeWalk.addTree(untilCommit.getTree());
					treeWalk.setRecursive(false);
					while (treeWalk.next())
						children.add(treeWalk.getPathString().substring(treePath.length()));
				}
			}
			
			revWalk.markStart(untilCommit);
			revWalk.setRewriteParents(false);

			/* 
			 * Records last commits info of first encountered commit in cache, and we 
			 * will mark this commit and all its ancestor commits with cached flag
			 */
			final AtomicReference<Map<String, Value>> lastCommitsRef = new AtomicReference<>();
			
			// flag to mark visited commits
			final RevFlag visited = revWalk.newFlag("visited");
			
			// flag to mark cached commits
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
					
					/*
					 * If a commit is marked as cached, then mark all its parents as
					 * cached, and when those parents are visited, parents of those
					 * parents will be marked as cached in turn. This way we can
					 * mark the whole tree (may leave some part inevitably) rooted 
					 * at the commit whose last commits are already cached.
					 */
					if (commit.has(cached))
						cacheParents(commit.parents, cached, visited);

					/*
					 * Check if current commit and all pending commits in queue 
					 * are all marked as cached, if yes, we will be confident that
					 * cached last commits info can complement our last commits 
					 * info. Note that if some of the queued commit is not marked 
					 * as cached, then we can not do the complement as ancestors of 
					 * those non-cached commits in queue affecting some children 
					 * might be newer than last commit being cached
					 */
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
					
					try (TreeWalk treeWalker = new TreeWalk(revWalker.reader)) {
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
									// we will enter into this block if current walking path is either parent of 
									// tree path, or the same as tree path, or child of tree path 
									
									int walkPathLen = walker.getPathLength();
									if (walkPathLen <= treePathRaw.length) {
										int n = walker.getTreeCount();
										if (n == 1) {
											// we are comparing against an empty tree, so current commit
											// is the initial commit
											walker.setRecursive(true);
											return true;
										} else {
											int m = walker.getRawMode(n-1);
											for (int i = 0; i < n-1; i++) {
												if (walker.getRawMode(i) != m || !walker.idEqual(i, n-1)) {
													// current path has been changed since its parents, and 
													// we need to walk down to further check the children
													walker.setRecursive(true);
													return true;
												}
											}
										}
										// otherwise this path is not relevant
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
													StandardCharsets.UTF_8, 
													walker.getRawPath(), 
													treePathRaw.length!=0?treePathRaw.length+1:0, 
													walkPathLen));
											// this child has been modified, and this info is sufficient to 
											// us and we no longer need to recurse into the sub tree
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
									
									// consider child as modified only if it is different
									// from all parents; otherwise, postpone checking of 
									// child to parents of this commit
									if (!same)
										modifiedChildren.add(child);
								}
							}
	
							for (int i = 0; i < nParents; i++) {
								/*
								 * Jump to parent whose children we care about are 
								 * the same as current commit. This will speed up 
								 * the calculation as we no longer need to walk 
								 * unnecessary branches
								 */
								if (changes[i] == 0) {
									RevCommit p = pList[i];
									commit.parents = new RevCommit[]{p};
									return false;
								}
							}
						}
						// include current commit only if it is determined as last commit
						// of some children
						return !modifiedChildren.isEmpty();
					}
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
			throw ExceptionUtils.unchecked(e);
		}
	}
	
	private void cacheParents(RevCommit[] parents, RevFlag cached, RevFlag visited) {
		for (RevCommit p: parents) {
			if (!p.has(cached)) {
				p.add(cached);
				
				/*
				 * Further caches parent of parent if the parent has already been 
				 * visited as otherwise it will not have a chance to get its parents 
				 * cached 
				 */
				if (p.has(visited))
					cacheParents(p.parents, cached, visited);
			}
		}
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
			this.commitDate = commit.getCommitterIdent().getWhen();
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
