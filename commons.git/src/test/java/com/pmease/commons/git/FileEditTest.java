package com.pmease.commons.git;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.junit.Test;

import com.pmease.commons.git.exception.NotGitFileException;
import com.pmease.commons.git.exception.NotGitTreeException;
import com.pmease.commons.git.exception.GitObjectAlreadyExistsException;
import com.pmease.commons.git.exception.GitObjectNotFoundException;
import com.pmease.commons.git.exception.ObsoleteCommitException;

public class FileEditTest extends AbstractGitTest {

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
		
		FileEdit edit = new FileEdit("/server/src/com/example/a//a.java", null);
		ObjectId newCommitId = edit.commit(git.getRepository(), refName, oldCommitId, oldCommitId, user, "test delete");
		
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

		FileEdit edit = new FileEdit("/server/src/com/example/c//c.java", null);
		try {
			edit.commit(git.getRepository(), refName, oldCommitId, oldCommitId, user, "test delete");
			assertTrue("An ObjectNotExistException should be thrown", false);
		} catch (GitObjectNotFoundException e) {
		}
	}
	
	@Test
	public void testRenameFile() throws IOException {
		createDir("client");
		addFileAndCommit("client/a.java", "a", "add a");
		addFileAndCommit("client/b.java", "b", "add b");
		
		createDir("server/src/com/example/a");
		createDir("server/src/com/example/b");
		addFileAndCommit("server/src/com/example/a/a.java", "a", "add a");
		addFileAndCommit("server/src/com/example/b/b.java", "b", "add b");
		
		String refName = "refs/heads/master";
		ObjectId oldCommitId = git.getRepository().resolve(refName);

		FileEdit edit = new FileEdit("server/src/com/example/a/a.java", 
				new PathAndContent.Immutable("client/c.java", "a".getBytes()));
		ObjectId newCommitId = edit.commit(git.getRepository(), refName, oldCommitId, oldCommitId, user, "test rename");
		try (RevWalk revWalk = new RevWalk(git.getRepository())) {
			RevTree revTree = revWalk.parseCommit(newCommitId).getTree();
			assertNotNull(TreeWalk.forPath(git.getRepository(), "client/a.java", revTree));
			assertNotNull(TreeWalk.forPath(git.getRepository(), "client/b.java", revTree));
			assertNotNull(TreeWalk.forPath(git.getRepository(), "client/c.java", revTree));
			assertNull(TreeWalk.forPath(git.getRepository(), "server/src/com/example/a", revTree));
			assertNotNull(TreeWalk.forPath(git.getRepository(), "server/src/com/example/b/b.java", revTree));
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
		
		FileEdit edit = new FileEdit("server/src/com/example/a", 
				new PathAndContent.Immutable("client/c.java", "a".getBytes()));
		try {
			edit.commit(git.getRepository(), refName, oldCommitId, oldCommitId, user, "test rename tree");
			assertTrue("A NotFileException should be thrown", false);
		} catch (NotGitFileException e) {
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
		
		FileEdit edit = new FileEdit("server/src/com/example/a/a.java", 
				new PathAndContent.Immutable("client/a.java/a.java", "a".getBytes()));
		try {
			edit.commit(git.getRepository(), refName, oldCommitId, oldCommitId, user, "test rename tree");
			assertTrue("A NotTreeException should be thrown", false);
		} catch (NotGitTreeException e) {
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
		
		FileEdit edit = new FileEdit(null, new PathAndContent.Immutable("/server/src/com/example/c/c.java", "c".getBytes()));
		ObjectId newCommitId = edit.commit(git.getRepository(), refName, oldCommitId, oldCommitId, user, "test add");
		
		try (RevWalk revWalk = new RevWalk(git.getRepository())) {
			RevTree revTree = revWalk.parseCommit(newCommitId).getTree();
			assertNotNull(TreeWalk.forPath(git.getRepository(), "server/src/com/example/a/a.java", revTree));
			assertNotNull(TreeWalk.forPath(git.getRepository(), "server/src/com/example/b/b.java", revTree));
			assertNotNull(TreeWalk.forPath(git.getRepository(), "server/src/com/example/c/c.java", revTree));
			assertNotNull(TreeWalk.forPath(git.getRepository(), "client/a.java", revTree));
			assertNotNull(TreeWalk.forPath(git.getRepository(), "client/b.java", revTree));
		}
		
		oldCommitId = newCommitId;
		edit = new FileEdit(null, new PathAndContent.Immutable("/common/common.java", "common".getBytes()));
		newCommitId = edit.commit(git.getRepository(), refName, oldCommitId, oldCommitId, user, "test add");
		
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
		
		FileEdit edit = new FileEdit(null, new PathAndContent.Immutable("client/a.java", "a".getBytes()));
		try {
			edit.commit(git.getRepository(), refName, oldCommitId, oldCommitId, user, "test rename tree");
			assertTrue("An ObjectAlreadyExistException should be thrown", false);
		} catch (GitObjectAlreadyExistsException e) {
		}
	}
	
	@Test
	public void testObsoleteOldCommit() throws IOException {
		addFileAndCommit("a.java", "a", "add a");
		
		String refName = "refs/heads/master";
		ObjectId oldCommitId = git.getRepository().resolve(refName);
		
		addFileAndCommit("b.java", "b", "add b");
		
		ObjectId newCommitId = git.getRepository().resolve(refName);

		FileEdit edit = new FileEdit(null, new PathAndContent.Immutable("/server/src/com/example/c/c.java", "c".getBytes()));
		try {
			edit.commit(git.getRepository(), refName, oldCommitId, oldCommitId, user, "test add");
			assertTrue("An ObsoleteCommitException should be thrown", false);
		} catch (ObsoleteCommitException e) {
			assertTrue(newCommitId.equals(e.getOldCommitId()));
		}
	}
	
}
