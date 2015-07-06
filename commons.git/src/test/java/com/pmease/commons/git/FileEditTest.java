package com.pmease.commons.git;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;

import org.eclipse.jgit.internal.storage.file.FileRepository;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.PersonIdent;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.junit.Test;

import com.pmease.commons.git.exception.NotFileException;
import com.pmease.commons.git.exception.NotTreeException;
import com.pmease.commons.git.exception.ObjectAlreadyExistException;
import com.pmease.commons.git.exception.ObjectNotExistException;
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
		
		try (Repository repo = new FileRepository(new File(git.repoDir(), ".git"))) {
			String refName = "refs/heads/master";
			ObjectId oldCommitId = repo.resolve(refName);
			PersonIdent person = new PersonIdent(repo);
			
			FileEdit edit = new FileEdit("/server/src/com/example/a//a.java", null);
			ObjectId newCommitId = edit.commit(repo, refName, oldCommitId, oldCommitId, person, "test delete");
			
			try (RevWalk revWalk = new RevWalk(repo)) {
				RevTree revTree = revWalk.parseCommit(newCommitId).getTree();
				assertNull(TreeWalk.forPath(repo, "server/src/com/example/a", revTree));
				assertNotNull(TreeWalk.forPath(repo, "server/src/com/example/b/b.java", revTree));
				assertNotNull(TreeWalk.forPath(repo, "client/a.java", revTree));
				assertNotNull(TreeWalk.forPath(repo, "client/b.java", revTree));
			}
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
		
		try (Repository repo = new FileRepository(new File(git.repoDir(), ".git"))) {
			String refName = "refs/heads/master";
			ObjectId oldCommitId = repo.resolve(refName);
			PersonIdent person = new PersonIdent(repo);

			FileEdit edit = new FileEdit("/server/src/com/example/c//c.java", null);
			try {
				edit.commit(repo, refName, oldCommitId, oldCommitId, person, "test delete");
				assertTrue("An ObjectNotExistException should be thrown", false);
			} catch (ObjectNotExistException e) {
			}
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
		
		try (Repository repo = new FileRepository(new File(git.repoDir(), ".git"))) {
			String refName = "refs/heads/master";
			ObjectId oldCommitId = repo.resolve(refName);
			PersonIdent person = new PersonIdent(repo);

			FileEdit edit = new FileEdit("server/src/com/example/a/a.java", 
					new PathAndContent("client/c.java", "a".getBytes()));
			ObjectId newCommitId = edit.commit(repo, refName, oldCommitId, oldCommitId, person, "test rename");
			try (RevWalk revWalk = new RevWalk(repo)) {
				RevTree revTree = revWalk.parseCommit(newCommitId).getTree();
				assertNotNull(TreeWalk.forPath(repo, "client/a.java", revTree));
				assertNotNull(TreeWalk.forPath(repo, "client/b.java", revTree));
				assertNotNull(TreeWalk.forPath(repo, "client/c.java", revTree));
				assertNull(TreeWalk.forPath(repo, "server/src/com/example/a", revTree));
				assertNotNull(TreeWalk.forPath(repo, "server/src/com/example/b/b.java", revTree));
			}
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
		
		try (Repository repo = new FileRepository(new File(git.repoDir(), ".git"))) {
			String refName = "refs/heads/master";
			ObjectId oldCommitId = repo.resolve(refName);
			PersonIdent person = new PersonIdent(repo);
			
			FileEdit edit = new FileEdit("server/src/com/example/a", 
					new PathAndContent("client/c.java", "a".getBytes()));
			try {
				edit.commit(repo, refName, oldCommitId, oldCommitId, person, "test rename tree");
				assertTrue("A NotFileException should be thrown", false);
			} catch (NotFileException e) {
			}
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
		
		try (Repository repo = new FileRepository(new File(git.repoDir(), ".git"))) {
			String refName = "refs/heads/master";
			ObjectId oldCommitId = repo.resolve(refName);
			PersonIdent person = new PersonIdent(repo);
			
			FileEdit edit = new FileEdit("server/src/com/example/a/a.java", 
					new PathAndContent("client/a.java/a.java", "a".getBytes()));
			try {
				edit.commit(repo, refName, oldCommitId, oldCommitId, person, "test rename tree");
				assertTrue("A NotTreeException should be thrown", false);
			} catch (NotTreeException e) {
			}
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
		
		try (Repository repo = new FileRepository(new File(git.repoDir(), ".git"))) {
			String refName = "refs/heads/master";
			ObjectId oldCommitId = repo.resolve(refName);
			PersonIdent person = new PersonIdent(repo);
			
			FileEdit edit = new FileEdit(null, new PathAndContent("/server/src/com/example/c/c.java", "c".getBytes()));
			ObjectId newCommitId = edit.commit(repo, refName, oldCommitId, oldCommitId, person, "test add");
			
			try (RevWalk revWalk = new RevWalk(repo)) {
				RevTree revTree = revWalk.parseCommit(newCommitId).getTree();
				assertNotNull(TreeWalk.forPath(repo, "server/src/com/example/a/a.java", revTree));
				assertNotNull(TreeWalk.forPath(repo, "server/src/com/example/b/b.java", revTree));
				assertNotNull(TreeWalk.forPath(repo, "server/src/com/example/c/c.java", revTree));
				assertNotNull(TreeWalk.forPath(repo, "client/a.java", revTree));
				assertNotNull(TreeWalk.forPath(repo, "client/b.java", revTree));
			}
			
			oldCommitId = newCommitId;
			edit = new FileEdit(null, new PathAndContent("/common/common.java", "common".getBytes()));
			newCommitId = edit.commit(repo, refName, oldCommitId, oldCommitId, person, "test add");
			
			try (RevWalk revWalk = new RevWalk(repo)) {
				RevTree revTree = revWalk.parseCommit(newCommitId).getTree();
				assertNotNull(TreeWalk.forPath(repo, "server/src/com/example/a/a.java", revTree));
				assertNotNull(TreeWalk.forPath(repo, "server/src/com/example/b/b.java", revTree));
				assertNotNull(TreeWalk.forPath(repo, "server/src/com/example/c/c.java", revTree));
				assertNotNull(TreeWalk.forPath(repo, "client/a.java", revTree));
				assertNotNull(TreeWalk.forPath(repo, "client/b.java", revTree));
				assertNotNull(TreeWalk.forPath(repo, "common/common.java", revTree));
			}
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
		
		try (Repository repo = new FileRepository(new File(git.repoDir(), ".git"))) {
			String refName = "refs/heads/master";
			ObjectId oldCommitId = repo.resolve(refName);
			PersonIdent person = new PersonIdent(repo);
			
			FileEdit edit = new FileEdit(null, new PathAndContent("client/a.java", "a".getBytes()));
			try {
				edit.commit(repo, refName, oldCommitId, oldCommitId, person, "test rename tree");
				assertTrue("An ObjectAlreadyExistException should be thrown", false);
			} catch (ObjectAlreadyExistException e) {
			}
		}
	}
	
	@Test
	public void testObsoleteOldCommit() throws IOException {
		addFileAndCommit("a.java", "a", "add a");
		
		try (Repository repo = new FileRepository(new File(git.repoDir(), ".git"))) {
			String refName = "refs/heads/master";
			ObjectId oldCommitId = repo.resolve(refName);
			PersonIdent person = new PersonIdent(repo);
			
			addFileAndCommit("b.java", "b", "add b");
			
			ObjectId newCommitId = repo.resolve(refName);

			FileEdit edit = new FileEdit(null, new PathAndContent("/server/src/com/example/c/c.java", "c".getBytes()));
			try {
				edit.commit(repo, refName, oldCommitId, oldCommitId, person, "test add");
				assertTrue("An ObsoleteCommitException should be thrown", false);
			} catch (ObsoleteCommitException e) {
				assertTrue(newCommitId.equals(e.getOldCommitId()));
			}
		}
	}
	
}
