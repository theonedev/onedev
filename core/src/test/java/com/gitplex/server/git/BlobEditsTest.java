package com.gitplex.server.git;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.eclipse.jgit.lib.FileMode;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.junit.Test;

import com.gitplex.server.git.exception.NotTreeException;
import com.gitplex.server.git.exception.ObjectAlreadyExistsException;
import com.gitplex.server.git.exception.ObjectNotFoundException;
import com.gitplex.server.git.exception.ObsoleteCommitException;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

public class BlobEditsTest extends AbstractGitTest {

	@Test
	public void testRemoveFile() throws IOException {
		createDir("client");
		addFileAndCommit("client/a.java", "a", "add a");
		addFileAndCommit("client/b.java", "b", "add b");
		
		createDir("server/src/com/example/a");
		createDir("server/src/com/example/b");
		addFileAndCommit("server/src/com/example/a/a.java", "a", "add a");
		addFileAndCommit("server/src/com/example/b/b.java", "b", "add b");
		
		String refName = "refs/heads/master";
		ObjectId oldCommitId = git.getRepository().resolve(refName);
		
		BlobEdits edits = new BlobEdits(Sets.newHashSet("/server/src/com/example/a//a.java"), Maps.newHashMap());
		ObjectId newCommitId = edits.commit(git.getRepository(), refName, oldCommitId, oldCommitId, user, "test delete");
		
		try (RevWalk revWalk = new RevWalk(git.getRepository())) {
			RevTree revTree = revWalk.parseCommit(newCommitId).getTree();
			assertNull(TreeWalk.forPath(git.getRepository(), "server/src/com/example/a", revTree));
			assertNotNull(TreeWalk.forPath(git.getRepository(), "server/src/com/example/b/b.java", revTree));
			assertNotNull(TreeWalk.forPath(git.getRepository(), "client/a.java", revTree));
			assertNotNull(TreeWalk.forPath(git.getRepository(), "client/b.java", revTree));
		}
	}
	
	@Test
	public void testRemoveNonExistentFile() throws IOException {
		createDir("client");
		addFileAndCommit("client/a.java", "a", "add a");
		addFileAndCommit("client/b.java", "b", "add b");
		
		createDir("server/src/com/example/a");
		createDir("server/src/com/example/b");
		addFileAndCommit("server/src/com/example/a/a.java", "a", "add a");
		addFileAndCommit("server/src/com/example/b/b.java", "b", "add b");
		
		String refName = "refs/heads/master";
		ObjectId oldCommitId = git.getRepository().resolve(refName);

		BlobEdits edits = new BlobEdits(Sets.newHashSet("/server/src/com/example/c//c.java"), Maps.newHashMap());
		try {
			edits.commit(git.getRepository(), refName, oldCommitId, oldCommitId, user, "test delete");
			assertTrue("An ObjectNotExistException should be thrown", false);
		} catch (ObjectNotFoundException e) {
		}
	}
	
	@Test
	public void testMoveFiles() throws IOException {
		createDir("client");
		addFileAndCommit("client/a.java", "a", "add a");
		addFileAndCommit("client/b.java", "b", "add b");
		
		createDir("server/src/com/example/a");
		createDir("server/src/com/example/b");
		addFileAndCommit("server/src/com/example/a/a.java", "a", "add a");
		addFileAndCommit("server/src/com/example/b/b.java", "b", "add b");
		
		String refName = "refs/heads/master";
		ObjectId oldCommitId = git.getRepository().resolve(refName);

		Set<String> oldPaths = Sets.newHashSet("server/src/com/example/a/a.java", 
				"server/src/com/example/b/b.java");
		Map<String, BlobContent> newBlobs = new HashMap<>();
		newBlobs.put("client/c.java", new BlobContent.Immutable("a".getBytes(), FileMode.REGULAR_FILE));
		newBlobs.put("client/d.java", new BlobContent.Immutable("a".getBytes(), FileMode.REGULAR_FILE));
		BlobEdits edits = new BlobEdits(oldPaths, newBlobs);
		ObjectId newCommitId = edits.commit(git.getRepository(), refName, oldCommitId, oldCommitId, user, "test rename");
		try (RevWalk revWalk = new RevWalk(git.getRepository())) {
			RevTree revTree = revWalk.parseCommit(newCommitId).getTree();
			assertNotNull(TreeWalk.forPath(git.getRepository(), "client/a.java", revTree));
			assertNotNull(TreeWalk.forPath(git.getRepository(), "client/b.java", revTree));
			assertNotNull(TreeWalk.forPath(git.getRepository(), "client/c.java", revTree));
			assertNotNull(TreeWalk.forPath(git.getRepository(), "client/d.java", revTree));
			assertNull(TreeWalk.forPath(git.getRepository(), "server/src/com/example/a", revTree));
			assertNull(TreeWalk.forPath(git.getRepository(), "server/src/com/example/b", revTree));
		}
	}
	
	@Test
	public void shouldFailIfOldPathIsTreeWhenRename() throws IOException {
		createDir("client");
		addFileAndCommit("client/a.java", "a", "add a");
		addFileAndCommit("client/b.java", "b", "add b");
		
		createDir("server/src/com/example/a");
		createDir("server/src/com/example/b");
		addFileAndCommit("server/src/com/example/a/a.java", "a", "add a");
		addFileAndCommit("server/src/com/example/b/b.java", "b", "add b");
		
		String refName = "refs/heads/master";
		ObjectId oldCommitId = git.getRepository().resolve(refName);
		
		Map<String, BlobContent> newBlobs = new HashMap<>();
		newBlobs.put("client/c.java", new BlobContent.Immutable("a".getBytes(), FileMode.REGULAR_FILE));
		BlobEdits edits = new BlobEdits(Sets.newHashSet("server/src/com/example/a"), newBlobs);
		ObjectId newCommitId = edits.commit(git.getRepository(), refName, oldCommitId, oldCommitId, user, 
				"test rename");
		try (RevWalk revWalk = new RevWalk(git.getRepository())) {
			RevTree revTree = revWalk.parseCommit(newCommitId).getTree();
			assertNotNull(TreeWalk.forPath(git.getRepository(), "client/c.java", revTree));
			assertNull(TreeWalk.forPath(git.getRepository(), "server/src/com/example/a", revTree));
		}
	}
	
	@Test
	public void shouldFailIfNewPathIsNotUnderTreeWhenRename() throws IOException {
		createDir("client");
		addFileAndCommit("client/a.java", "a", "add a");
		addFileAndCommit("client/b.java", "b", "add b");
		
		createDir("server/src/com/example/a");
		createDir("server/src/com/example/b");
		addFileAndCommit("server/src/com/example/a/a.java", "a", "add a");
		addFileAndCommit("server/src/com/example/b/b.java", "b", "add b");
		
		String refName = "refs/heads/master";
		ObjectId oldCommitId = git.getRepository().resolve(refName);
		
		Map<String, BlobContent> newBlobs = new HashMap<>();
		newBlobs.put("client/a.java/a.java", new BlobContent.Immutable("a".getBytes(), FileMode.REGULAR_FILE));
		BlobEdits edits = new BlobEdits(Sets.newHashSet("server/src/com/example/a/a.java"), newBlobs);
		try {
			edits.commit(git.getRepository(), refName, oldCommitId, oldCommitId, user, "test rename tree");
			assertTrue("A NotTreeException should be thrown", false);
		} catch (NotTreeException e) {
		}
	}
	
	@Test
	public void testAddFile() throws IOException {
		createDir("client");
		addFileAndCommit("client/a.java", "a", "add a");
		addFileAndCommit("client/b.java", "b", "add b");
		
		createDir("server/src/com/example/a");
		createDir("server/src/com/example/b");
		addFileAndCommit("server/src/com/example/a/a.java", "a", "add a");
		addFileAndCommit("server/src/com/example/b/b.java", "b", "add b");
		
		String refName = "refs/heads/master";
		ObjectId oldCommitId = git.getRepository().resolve(refName);

		Map<String, BlobContent> newBlobs = new HashMap<>();
		newBlobs.put("/server/src/com/example/c/c.java", new BlobContent.Immutable("c".getBytes(), FileMode.REGULAR_FILE));
		newBlobs.put("/server/src/com/example/d/d.java", new BlobContent.Immutable("d".getBytes(), FileMode.REGULAR_FILE));
		BlobEdits edits = new BlobEdits(Sets.newHashSet(), newBlobs);
		ObjectId newCommitId = edits.commit(git.getRepository(), refName, oldCommitId, oldCommitId, user, "test add");
		
		try (RevWalk revWalk = new RevWalk(git.getRepository())) {
			RevTree revTree = revWalk.parseCommit(newCommitId).getTree();
			assertNotNull(TreeWalk.forPath(git.getRepository(), "server/src/com/example/a/a.java", revTree));
			assertNotNull(TreeWalk.forPath(git.getRepository(), "server/src/com/example/b/b.java", revTree));
			assertNotNull(TreeWalk.forPath(git.getRepository(), "server/src/com/example/c/c.java", revTree));
			assertNotNull(TreeWalk.forPath(git.getRepository(), "server/src/com/example/d/d.java", revTree));
			assertNotNull(TreeWalk.forPath(git.getRepository(), "client/a.java", revTree));
			assertNotNull(TreeWalk.forPath(git.getRepository(), "client/b.java", revTree));
		}
		
		oldCommitId = newCommitId;
		newBlobs = new HashMap<>();
		newBlobs.put("/common/common.java", new BlobContent.Immutable("common".getBytes(), FileMode.REGULAR_FILE));
		newCommitId = edits.commit(git.getRepository(), refName, oldCommitId, oldCommitId, user, "test add");
		
		try (RevWalk revWalk = new RevWalk(git.getRepository())) {
			RevTree revTree = revWalk.parseCommit(newCommitId).getTree();
			assertNotNull(TreeWalk.forPath(git.getRepository(), "server/src/com/example/a/a.java", revTree));
			assertNotNull(TreeWalk.forPath(git.getRepository(), "server/src/com/example/b/b.java", revTree));
			assertNotNull(TreeWalk.forPath(git.getRepository(), "server/src/com/example/c/c.java", revTree));
			assertNotNull(TreeWalk.forPath(git.getRepository(), "client/a.java", revTree));
			assertNotNull(TreeWalk.forPath(git.getRepository(), "client/b.java", revTree));
			assertNotNull(TreeWalk.forPath(git.getRepository(), "common/common.java", revTree));
		}
	}
	
	@Test
	public void testAddExistFile() throws IOException {
		createDir("client");
		addFileAndCommit("client/a.java", "a", "add a");
		addFileAndCommit("client/b.java", "b", "add b");
		
		createDir("server/src/com/example/a");
		createDir("server/src/com/example/b");
		addFileAndCommit("server/src/com/example/a/a.java", "a", "add a");
		addFileAndCommit("server/src/com/example/b/b.java", "b", "add b");
		
		String refName = "refs/heads/master";
		ObjectId oldCommitId = git.getRepository().resolve(refName);
		
		Map<String, BlobContent> newBlobs = new HashMap<>();
		newBlobs.put("client/a.java", new BlobContent.Immutable("a".getBytes(), FileMode.REGULAR_FILE));
		BlobEdits edits = new BlobEdits(Sets.newHashSet(), newBlobs);
		try {
			edits.commit(git.getRepository(), refName, oldCommitId, oldCommitId, user, "test rename tree");
			assertTrue("An ObjectAlreadyExistException should be thrown", false);
		} catch (ObjectAlreadyExistsException e) {
		}
	}
	
	@Test
	public void testObsoleteOldCommit() throws IOException {
		addFileAndCommit("a.java", "a", "add a");
		
		String refName = "refs/heads/master";
		ObjectId oldCommitId = git.getRepository().resolve(refName);
		
		addFileAndCommit("b.java", "b", "add b");
		
		ObjectId newCommitId = git.getRepository().resolve(refName);

		Map<String, BlobContent> newBlobs = new HashMap<>();
		newBlobs.put("/server/src/com/example/c/c.java", new BlobContent.Immutable("c".getBytes(), FileMode.REGULAR_FILE));
		BlobEdits edits = new BlobEdits(Sets.newHashSet(), newBlobs);
		try {
			edits.commit(git.getRepository(), refName, oldCommitId, oldCommitId, user, "test add");
			assertTrue("An ObsoleteCommitException should be thrown", false);
		} catch (ObsoleteCommitException e) {
			assertTrue(newCommitId.equals(e.getOldCommitId()));
		}
	}
	
}
