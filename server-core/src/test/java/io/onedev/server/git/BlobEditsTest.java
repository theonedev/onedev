package io.onedev.server.git;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.jgit.lib.FileMode;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.junit.Test;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import io.onedev.server.git.exception.NotTreeException;
import io.onedev.server.git.exception.ObjectAlreadyExistsException;
import io.onedev.server.git.exception.ObjectNotFoundException;
import io.onedev.server.git.exception.ObsoleteCommitException;

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
		edits = new BlobEdits(Sets.newHashSet(), newBlobs);
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
	public void testAddDuplicates() throws IOException {
		addFileAndCommit("file", "", "first commit");
				
		String refName = "refs/heads/master";
		ObjectId oldCommitId = git.getRepository().resolve(refName);

		Map<String, BlobContent> newBlobs = new HashMap<>();
		newBlobs.put("/dir/file", new BlobContent.Immutable("c".getBytes(), FileMode.REGULAR_FILE));
		newBlobs.put("/dir/file/a.java", new BlobContent.Immutable("d".getBytes(), FileMode.REGULAR_FILE));
		BlobEdits edits = new BlobEdits(Sets.newHashSet(), newBlobs);
		try {
			edits.commit(git.getRepository(), refName, oldCommitId, oldCommitId, user, "test add");
			assertTrue("An ObjectAlreadyException should be thrown", false);
		} catch (ObjectAlreadyExistsException e) {
		}
	}
	
	@Test
	public void testOrder() throws IOException {
		addFileAndCommit("file", "", "first commit");
		createDir("client/security");
		addFileAndCommit("client/security/file", "a", "add a");
				
		String refName = "refs/heads/master";
		ObjectId oldCommitId = git.getRepository().resolve(refName);

		Map<String, BlobContent> newBlobs = new HashMap<>();
		newBlobs.put("client/dir2/file4", new BlobContent.Immutable("c".getBytes(), FileMode.REGULAR_FILE));
		newBlobs.put("client/security/dir1/file2", new BlobContent.Immutable("d".getBytes(), FileMode.REGULAR_FILE));
		newBlobs.put("client/security/dir2/file3", new BlobContent.Immutable("d".getBytes(), FileMode.REGULAR_FILE));
		newBlobs.put("client/dir1/file1", new BlobContent.Immutable("d".getBytes(), FileMode.REGULAR_FILE));
		newBlobs.put("client/security/dir1/subdir/file", new BlobContent.Immutable("d".getBytes(), FileMode.REGULAR_FILE));
		newBlobs.put("client/dir1/file0", new BlobContent.Immutable("d".getBytes(), FileMode.REGULAR_FILE));
		newBlobs.put("server/file2", new BlobContent.Immutable("d".getBytes(), FileMode.REGULAR_FILE));
		newBlobs.put("server/file1", new BlobContent.Immutable("d".getBytes(), FileMode.REGULAR_FILE));
		BlobEdits edits = new BlobEdits(Sets.newHashSet(), newBlobs);
		ObjectId newCommitId = edits.commit(git.getRepository(), refName, oldCommitId, oldCommitId, user, "test add");

		List<String> paths = new ArrayList<>();
		try (	RevWalk revWalk = new RevWalk(git.getRepository());
				TreeWalk treeWalk = new TreeWalk(git.getRepository());) {
			treeWalk.addTree(revWalk.parseCommit(newCommitId).getTree());
			treeWalk.setRecursive(true);
			while (treeWalk.next()) {
				paths.add(treeWalk.getPathString());
			}
		}

		assertEquals(10, paths.size());
		assertEquals("client/dir1/file0", paths.get(0));
		assertEquals("client/dir1/file1", paths.get(1));
		assertEquals("client/dir2/file4", paths.get(2));
		assertEquals("client/security/dir1/file2", paths.get(3));
		assertEquals("client/security/dir1/subdir/file", paths.get(4));
		assertEquals("client/security/dir2/file3", paths.get(5));
		assertEquals("client/security/file", paths.get(6));
		assertEquals("file", paths.get(7));
		assertEquals("server/file1", paths.get(8));
		assertEquals("server/file2", paths.get(9));
	}
	
	@Test
	public void testModifyFile() throws IOException {
		createDir("a");
		addFileAndCommit("a/file.java", "", "add a/file.java");
		
		createDir("a.b");
		addFileAndCommit("a.b/file2.java", "", "add a.b/file.java");
		addFileAndCommit("a.b/file1.java", "", "add a.b/file.java");
		addFileAndCommit("a.b/file1", "", "add a.b/file.java");
		
		String refName = "refs/heads/master";
		ObjectId oldCommitId = git.getRepository().resolve(refName);

		List<String> oldPaths = new ArrayList<>();
		try (	RevWalk revWalk = new RevWalk(git.getRepository());
				TreeWalk treeWalk = new TreeWalk(git.getRepository());) {
			treeWalk.addTree(revWalk.parseCommit(oldCommitId).getTree());
			while (treeWalk.next())
				oldPaths.add(treeWalk.getPathString());
		}
		
		Map<String, BlobContent> newBlobs = new HashMap<>();
		newBlobs.put("a.b/file2.java", new BlobContent.Immutable("hello".getBytes(), FileMode.REGULAR_FILE));
		BlobEdits edits = new BlobEdits(Sets.newHashSet("a.b/file2.java"), newBlobs);
		ObjectId newCommitId = edits.commit(git.getRepository(), refName, oldCommitId, oldCommitId, user, "test modify");

		List<String> newPaths = new ArrayList<>();
		try (	RevWalk revWalk = new RevWalk(git.getRepository());
				TreeWalk treeWalk = new TreeWalk(git.getRepository());) {
			treeWalk.addTree(revWalk.parseCommit(newCommitId).getTree());
			while (treeWalk.next())
				newPaths.add(treeWalk.getPathString());
		}
		
		assertEquals(oldPaths, newPaths);
	}
	
	@Test
	public void testOverwriteExistFile() throws IOException {
		createDir("client");
		addFileAndCommit("client/a.java", "a", "add a");
		addFileAndCommit("client/b.java", "b", "add b");
		
		createDir("server/src/com/example/a");
		createDir("server/src/com/example/b");
		addFileAndCommit("server/src/com/example/a/a.java", "a", "add a");
		addFileAndCommit("server/src/com/example/b/b.java", "b", "add b");
		
		String refName = "refs/heads/master";
		ObjectId oldCommitId = git.getRepository().resolve(refName);

		Set<String> oldPaths = Sets.newHashSet("server/src/com/example/b/b.java");
		Map<String, BlobContent> newBlobs = new HashMap<>();
		newBlobs.put("client/a.java", new BlobContent.Immutable("a".getBytes(), FileMode.REGULAR_FILE));
		newBlobs.put("server/src/a.java", new BlobContent.Immutable("a".getBytes(), FileMode.REGULAR_FILE));
		newBlobs.put("server/src/com/example/b/b.java", new BlobContent.Immutable("a".getBytes(), FileMode.REGULAR_FILE));
		
		BlobEdits edits = new BlobEdits(oldPaths, newBlobs);
		ObjectId newCommitId = edits.commit(git.getRepository(), refName, 
				oldCommitId, oldCommitId, user, "test rename tree");
		
		List<String> paths = new ArrayList<>();
		try (	RevWalk revWalk = new RevWalk(git.getRepository());
				TreeWalk treeWalk = new TreeWalk(git.getRepository());) {
			treeWalk.addTree(revWalk.parseCommit(newCommitId).getTree());
			treeWalk.setRecursive(true);
			while (treeWalk.next()) {
				paths.add(treeWalk.getPathString());
			}
		}

		assertEquals(5, paths.size());
		assertEquals("client/a.java", paths.get(0));
		assertEquals("client/b.java", paths.get(1));
		assertEquals("server/src/a.java", paths.get(2));
		assertEquals("server/src/com/example/a/a.java", paths.get(3));
		assertEquals("server/src/com/example/b/b.java", paths.get(4));
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
