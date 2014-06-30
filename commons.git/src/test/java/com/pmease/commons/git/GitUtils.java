package com.pmease.commons.git;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.eclipse.jgit.api.GitCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.AnyObjectId;
import org.eclipse.jgit.lib.ObjectReader;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.treewalk.AbstractTreeIterator;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;
import org.eclipse.jgit.treewalk.TreeWalk;

public class GitUtils {

	public static <T> T call(GitCommand<T> command) {
		try {
			return command.call();
		} catch (GitAPIException e) {
			throw new RuntimeException(e);
		}
	}
	
	public static AbstractTreeIterator getTreeIterator(Repository repo, AnyObjectId commitId) {
		CanonicalTreeParser p = new CanonicalTreeParser();
		ObjectReader or = repo.newObjectReader();
		try {
			p.reset(or, new RevWalk(repo).parseTree(commitId));
			return p;
		} catch (IOException e) {
			throw new RuntimeException(e);
		} finally {
			or.release();
		}
	}
	
	public static byte[] readFile(Repository repo, AnyObjectId commitId, String path) {
		RevWalk revWalk = new RevWalk(repo);
		try {
			RevCommit commit = revWalk.parseCommit(commitId);
			TreeWalk treeWalk = TreeWalk.forPath(repo, "file", commit.getTree());
			try {
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				repo.open(treeWalk.getObjectId(0)).copyTo(baos);
				return baos.toByteArray();
			} finally {
				treeWalk.release();
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		} finally {
			revWalk.release();
		}
		
	}
}
