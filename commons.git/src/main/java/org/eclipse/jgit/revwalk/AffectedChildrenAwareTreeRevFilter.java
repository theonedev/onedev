package org.eclipse.jgit.revwalk;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.jgit.errors.IncorrectObjectTypeException;
import org.eclipse.jgit.errors.MissingObjectException;
import org.eclipse.jgit.errors.StopWalkException;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.revwalk.filter.RevFilter;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.eclipse.jgit.treewalk.filter.TreeFilter;
import org.eclipse.jgit.util.RawParseUtils;

public class AffectedChildrenAwareTreeRevFilter extends RevFilter {

	private static final int PARSED = RevWalk.PARSED;

	private final byte[] treePathRaw;
	
	private final Set<String> affectedChildren = new HashSet<>();
	
	public AffectedChildrenAwareTreeRevFilter(String treePath) {
		treePathRaw = Constants.encode(treePath);
	}
	
	@Override
	public boolean include(RevWalk revWalker, RevCommit commit)
			throws StopWalkException, MissingObjectException,
			IncorrectObjectTypeException, IOException {
		final RevCommit[] pList = commit.getParents();
		final int nParents = pList.length;
		final TreeWalk treeWalker = new TreeWalk(revWalker.reader);
		treeWalker.setFilter(new TreeFilter() {

			@Override
			public boolean include(TreeWalk walker)
					throws MissingObjectException,
					IncorrectObjectTypeException, IOException {
				if (treePathRaw.length == 0 || walker.isPathPrefix(treePathRaw, treePathRaw.length) == 0) {
					int walkPathLen = walker.getPathLength();
					if (walkPathLen <= treePathRaw.length) {
						boolean modified = false;
						
						final int n = walker.getTreeCount();
						if (n == 1) {
							modified = true;
						} else {
							final int m = walker.getRawMode(n-1);
							for (int i = 0; i < n-1; i++) {
								if (walker.getRawMode(i) != m || !walker.idEqual(i, n-1)) {
									modified = true;
									break;
								}
							}
						}
						if (modified) {
							walker.setRecursive(true);
							return true;
						} else {
							return false;
						}
					} else {
						boolean modified = false;
						boolean same = false;
						final int n = walker.getTreeCount();
						if (n == 1) {
							modified = true;
						} else {
							final int m = walker.getRawMode(n-1);
							for (int i = 0; i < n-1; i++) {
								if (walker.getRawMode(i) != m || !walker.idEqual(i, n-1))
									modified = true;
								if (walker.getRawMode(i) == m && walker.idEqual(i, n-1))
									same = true;
							}
						}
						if (modified) {
							if (!same) {
								String child = RawParseUtils.decode(
										Constants.CHARSET, 
										walker.getRawPath(), 
										treePathRaw.length!=0?treePathRaw.length+1:0, 
										walkPathLen);
								affectedChildren.add(child);
							}
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
				return true;
			}

			@Override
			public TreeFilter clone() {
				return this;
			}
			
		});
		treeWalker.setRecursive(true);
		final ObjectId[] trees = new ObjectId[nParents + 1];
		for (int i = 0; i < nParents; i++) {
			final RevCommit p = commit.parents[i];
			if ((p.flags & PARSED) == 0)
				p.parseHeaders(revWalker);
			trees[i] = p.getTree();
		}
		trees[nParents] = commit.getTree();
		treeWalker.reset(trees);

		if (nParents <= 1) {
			boolean chged = false;
			while (treeWalker.next())
				chged = true;

			return chged;
		}

		final int[] chgs = new int[nParents];
		while (treeWalker.next()) {
			final int myMode = treeWalker.getRawMode(nParents);
			for (int i = 0; i < nParents; i++) {
				final int pMode = treeWalker.getRawMode(i);
				if (myMode == pMode && treeWalker.idEqual(i, nParents))
					continue;

				chgs[i]++;
			}
		}

		for (int i = 0; i < nParents; i++) {
			if (chgs[i] == 0) {
				final RevCommit p = pList[i];
				commit.parents = new RevCommit[] {p};
				return false;
			}
		}

		return true;
	}

	@Override
	public RevFilter clone() {
		throw new UnsupportedOperationException();
	}

	public Set<String> getAffectedChildren() {
		return affectedChildren;
	}

}
