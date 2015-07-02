package com.pmease.commons.git;

import static org.junit.Assert.*;

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

import com.google.common.collect.Lists;

public class GitUtilsTest extends AbstractGitTest {

	@Test
	public void testReadLines() {
		String content = "中文测试中文测试中文测试中文测试\r\nsecond line";
		BlobText result = BlobText.from(content.getBytes()); 
		assertEquals(Lists.newArrayList("中文测试中文测试中文测试中文测试\r", "second line"), result.getLines());
		assertEquals(false, result.isHasEolAtEof());

		result = BlobText.from("\nhello\n\nworld\n".getBytes()); 
		assertEquals(Lists.newArrayList("", "hello", "", "world"), result.getLines());
	}
	
	@Test
	public void testComparePath() {
		assertTrue(GitUtils.comparePath("dir1", "dir1/")==0);
		assertTrue(GitUtils.comparePath("/dir1", "dir1/")==0);
		assertTrue(GitUtils.comparePath("dir1", "dir2")<0);
		assertTrue(GitUtils.comparePath("dir1", "dir1/dir2")<0);
		assertTrue(GitUtils.comparePath("dir1/dir2/dir3/file", "dir1/dir3/file")<0);
		assertTrue(GitUtils.comparePath("dir12", "dir1/dir2")>0);
	}
	
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
			
			ObjectId newCommitId = GitUtils.commitFile(repo, refName, oldCommitId, oldCommitId, 
					person, "test delete", "/server/src/com/example/a//a.java", null);
			
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
			
			ObjectId newCommitId = GitUtils.commitFile(repo, refName, oldCommitId, oldCommitId, 
					person, "test delete", "/server/src/com/example/c//c.java", null);
			
			try (RevWalk revWalk = new RevWalk(repo)) {
				RevTree revTree = revWalk.parseCommit(newCommitId).getTree();
				assertNotNull(TreeWalk.forPath(repo, "server/src/com/example/a/a.java", revTree));
				assertNotNull(TreeWalk.forPath(repo, "server/src/com/example/b/b.java", revTree));
				assertNotNull(TreeWalk.forPath(repo, "client/a.java", revTree));
				assertNotNull(TreeWalk.forPath(repo, "client/b.java", revTree));
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
			
			ObjectId newCommitId = GitUtils.commitFile(repo, refName, oldCommitId, oldCommitId, 
					person, "test add", "/server/src/com/example/c/c.java", "c".getBytes());
			
			try (RevWalk revWalk = new RevWalk(repo)) {
				RevTree revTree = revWalk.parseCommit(newCommitId).getTree();
				assertNotNull(TreeWalk.forPath(repo, "server/src/com/example/a/a.java", revTree));
				assertNotNull(TreeWalk.forPath(repo, "server/src/com/example/b/b.java", revTree));
				assertNotNull(TreeWalk.forPath(repo, "server/src/com/example/c/c.java", revTree));
				assertNotNull(TreeWalk.forPath(repo, "client/a.java", revTree));
				assertNotNull(TreeWalk.forPath(repo, "client/b.java", revTree));
			}
			
			oldCommitId = newCommitId;
			newCommitId = GitUtils.commitFile(repo, refName, oldCommitId, oldCommitId, 
					person, "test add", "/common/common.java", "common".getBytes());
			
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
	public void testObsoleteOldCommit() throws IOException {
		addFileAndCommit("a.java", "a", "add a");
		
		try (Repository repo = new FileRepository(new File(git.repoDir(), ".git"))) {
			String refName = "refs/heads/master";
			ObjectId oldCommitId = repo.resolve(refName);
			PersonIdent person = new PersonIdent(repo);
			
			addFileAndCommit("b.java", "b", "add b");
			
			ObjectId newCommitId = repo.resolve(refName);

			try {
				GitUtils.commitFile(repo, refName, oldCommitId, oldCommitId, 
						person, "test add", "/server/src/com/example/c/c.java", "c".getBytes());
				assertTrue("An ObsoleteOldCommitException should be thrown", false);
			} catch (ObsoleteOldCommitException e) {
				assertTrue(newCommitId.equals(e.getOldCommitId()));
			}
		}
	}
	
}
