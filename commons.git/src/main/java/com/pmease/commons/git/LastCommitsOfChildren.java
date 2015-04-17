package com.pmease.commons.git;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import javax.annotation.Nullable;

import org.eclipse.jgit.lib.AnyObjectId;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.AffectedChildrenAwareTreeRevFilter;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.treewalk.TreeWalk;

import com.google.common.base.Throwables;

public final class LastCommitsOfChildren extends HashMap<String, LastCommitInfo> {

	private static final long serialVersionUID = 1L;

	public LastCommitsOfChildren(Repository repo, AnyObjectId until, @Nullable String treePath) {
		try {
			treePath = GitUtils.normalizePath(treePath);
			if (treePath == null) 
				treePath = "";

			AffectedChildrenAwareTreeRevFilter revFilter = new AffectedChildrenAwareTreeRevFilter(treePath);
			
			Set<String> children = new HashSet<>();

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

			revWalk.setRevFilter(revFilter);
			
			RevCommit next = revWalk.next();
			while (size() < children.size() && next != null) {
				Set<String> affectedChildren = revFilter.getAffectedChildren();
				for (String each: affectedChildren) {
					if (children.contains(each) && !containsKey(each)) 
						put(each, new LastCommitInfo(next));
				}
				affectedChildren.clear();
				next = revWalk.next();
			}
		} catch (Exception e) {
			throw Throwables.propagate(e);
		}
	}
}
