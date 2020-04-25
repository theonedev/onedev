package io.onedev.server.git;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.junit.Test;

import com.google.common.collect.Sets;

public class GitUtilsTest extends AbstractGitTest {

	@Test
	public void testRebaseWithoutConflicts() throws Exception {
		addFileAndCommit("initial", "", "initial");
		git.checkout().setCreateBranch(true).setName("dev").call();
		addFileAndCommit("dev1", "", "dev1");
		addFileAndCommit("dev2", "", "dev2");
		git.checkout().setName("master").call();
		addFileAndCommit("master1", "", "master1");
		addFileAndCommit("master2", "", "master2");
		ObjectId masterId = git.getRepository().resolve("master");
		ObjectId newCommitId = GitUtils.rebase(git.getRepository(), git.getRepository().resolve("dev"), masterId, user);
		try (	RevWalk revWalk = new RevWalk(git.getRepository());
				TreeWalk treeWalk = new TreeWalk(git.getRepository())) {
			RevCommit newCommit = revWalk.parseCommit(newCommitId);
			treeWalk.addTree(newCommit.getTree());
			Set<String> files = new HashSet<>();
			while (treeWalk.next()) {
				files.add(treeWalk.getPathString());
			}
			assertEquals(Sets.newHashSet("initial", "dev1", "dev2", "master1", "master2"), files);
			assertEquals("dev2", newCommit.getFullMessage());
			assertEquals(1, newCommit.getParentCount());
			
			treeWalk.reset();
			newCommit = revWalk.parseCommit(newCommit.getParent(0));
			treeWalk.addTree(newCommit.getTree());
			files = new HashSet<>();
			while (treeWalk.next()) {
				files.add(treeWalk.getPathString());
			}
			assertEquals(Sets.newHashSet("initial", "dev1", "master1", "master2"), files);
			assertEquals("dev1", newCommit.getFullMessage());
			assertEquals(1, newCommit.getParentCount());
			
			assertEquals(masterId, newCommit.getParent(0));
		}
	}

	@Test
	public void testRebaseWithConflicts() throws Exception {
		addFileAndCommit("initial", "", "initial");
		git.checkout().setCreateBranch(true).setName("dev").call();
		addFileAndCommit("dev1", "", "dev1");
		addFileAndCommit("conflict", "1", "dev2");
		git.checkout().setName("master").call();
		addFileAndCommit("master1", "", "master1");
		addFileAndCommit("conflict", "2", "master2");
		assertNull(GitUtils.rebase(git.getRepository(), git.getRepository().resolve("dev"), 
				git.getRepository().resolve("master"), user));
	}
	
	@Test
	public void testRebaseWithEmptyCommit() throws Exception {
		addFileAndCommit("initial", "", "initial");
		git.checkout().setCreateBranch(true).setName("dev").call();
		addFileAndCommit("dev1", "", "dev1");
		git.commit().setAllowEmpty(true).setMessage("empty").call();
		addFileAndCommit("dev2", "", "dev2");
		git.checkout().setName("master").call();
		addFileAndCommit("master1", "", "master1");
		addFileAndCommit("master2", "", "master2");
		ObjectId masterId = git.getRepository().resolve("master");
		ObjectId newCommitId = GitUtils.rebase(git.getRepository(), git.getRepository().resolve("dev"), masterId, user);
		try (	RevWalk revWalk = new RevWalk(git.getRepository());
				TreeWalk treeWalk = new TreeWalk(git.getRepository())) {
			RevCommit newCommit = revWalk.parseCommit(newCommitId);
			treeWalk.addTree(newCommit.getTree());
			Set<String> files = new HashSet<>();
			while (treeWalk.next()) {
				files.add(treeWalk.getPathString());
			}
			assertEquals(Sets.newHashSet("initial", "dev1", "dev2", "master1", "master2"), files);
			assertEquals("dev2", newCommit.getFullMessage());
			assertEquals(1, newCommit.getParentCount());
			
			treeWalk.reset();
			newCommit = revWalk.parseCommit(newCommit.getParent(0));
			treeWalk.addTree(newCommit.getTree());
			files = new HashSet<>();
			while (treeWalk.next()) {
				files.add(treeWalk.getPathString());
			}
			assertEquals(Sets.newHashSet("initial", "dev1", "master1", "master2"), files);
			assertEquals("dev1", newCommit.getFullMessage());
			assertEquals(1, newCommit.getParentCount());
			
			assertEquals(masterId, newCommit.getParent(0));
		}
	}
	
	@Test
	public void testMergeWithoutConflicts() throws Exception {
		addFileAndCommit("initial", "", "initial");
		git.checkout().setCreateBranch(true).setName("dev").call();
		addFileAndCommit("dev1", "", "dev1");
		addFileAndCommit("dev2", "", "dev2");
		git.checkout().setName("master").call();
		addFileAndCommit("master1", "", "master1");
		addFileAndCommit("master2", "", "master2");
		ObjectId masterId = git.getRepository().resolve("master");
		ObjectId devId = git.getRepository().resolve("dev");
		ObjectId mergeCommitId = GitUtils.merge(git.getRepository(), devId, masterId, false, user, user, "merge commit");
		try (	RevWalk revWalk = new RevWalk(git.getRepository());
				TreeWalk treeWalk = new TreeWalk(git.getRepository())) {
			RevCommit mergeCommit = revWalk.parseCommit(mergeCommitId);
			treeWalk.addTree(mergeCommit.getTree());
			Set<String> files = new HashSet<>();
			while (treeWalk.next()) {
				files.add(treeWalk.getPathString());
			}
			assertEquals(Sets.newHashSet("initial", "dev1", "dev2", "master1", "master2"), files);
			assertEquals(2, mergeCommit.getParentCount());
			assertEquals(masterId, mergeCommit.getParent(0));
			assertEquals(devId, mergeCommit.getParent(1));
		}
	}

	@Test
	public void testMergeWithConflicts() throws Exception {
		addFileAndCommit("initial", "", "initial");
		git.checkout().setCreateBranch(true).setName("dev").call();
		addFileAndCommit("dev1", "", "dev1");
		addFileAndCommit("conflict", "1", "dev2");
		git.checkout().setName("master").call();
		addFileAndCommit("master1", "", "master1");
		addFileAndCommit("conflict", "2", "master2");
		assertNull(GitUtils.merge(git.getRepository(), git.getRepository().resolve("dev"), 
				git.getRepository().resolve("master"), false, user, user, "merge commit"));
	}
	
}
