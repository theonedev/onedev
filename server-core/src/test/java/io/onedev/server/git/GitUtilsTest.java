package io.onedev.server.git;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.lib.FileMode;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.junit.Test;

import com.google.common.collect.Sets;
import com.google.common.io.Resources;

import io.onedev.commons.utils.FileUtils;
import io.onedev.commons.utils.ZipUtils;

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
	public void testRebaseWithoutChange() throws Exception {
		addFileAndCommit("initial", "", "initial");
		git.checkout().setCreateBranch(true).setName("dev").call();
		String commitHash1 = addFileAndCommit("dev1", "", "dev1");
		String commitHash2 = addFileAndCommit("dev2", "", "dev2");
		ObjectId masterId = git.getRepository().resolve("master");
		ObjectId newCommitId = GitUtils.rebase(git.getRepository(), git.getRepository().resolve("dev"), masterId, user);
		try (	RevWalk revWalk = new RevWalk(git.getRepository())) {
			assertEquals(commitHash2, newCommitId.name());
			assertEquals(commitHash1, revWalk.parseCommit(newCommitId).getParent(0).name());
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
		ObjectId mergeCommitId = GitUtils.merge(git.getRepository(), masterId, devId, false, 
				user, user, "merge commit", false);
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
	public void testMergeWithContentConflict() throws Exception {
		addFileAndCommit("initial", "", "initial");
		git.checkout().setCreateBranch(true).setName("dev").call();
		addFileAndCommit("dev1", "", "dev1");
		addFileAndCommit("conflict", "1", "dev2");
		git.checkout().setName("master").call();
		addFileAndCommit("master1", "", "master1");
		addFileAndCommit("conflict", "2", "master2");
		assertNull(GitUtils.merge(git.getRepository(), git.getRepository().resolve("master"), 
				git.getRepository().resolve("dev"), false, user, user, "merge commit", false));
		
		ObjectId mergeCommitId = GitUtils.merge(git.getRepository(), git.getRepository().resolve("master"), 
				git.getRepository().resolve("dev"), false, user, user, "merge commit", true);
		assertNotNull(mergeCommitId);
		
		try (	RevWalk revWalk = new RevWalk(git.getRepository())) {
			RevCommit mergeCommit = revWalk.parseCommit(mergeCommitId);
			TreeWalk treeWalk = TreeWalk.forPath(git.getRepository(), "conflict", mergeCommit.getTree());
			BlobIdent blobIdent = new BlobIdent(mergeCommit.name(), "conflict", FileMode.REGULAR_FILE.getBits());
			Blob blob = new Blob(blobIdent, treeWalk.getObjectId(0), treeWalk.getObjectReader());
			assertEquals("2", blob.getText().getContent());
		}
		
	}
	
	@Test
	public void testMergeWithDeletionAndModificationConflict() throws Exception {
		addFileAndCommit("file", "", "initial commit");
		git.checkout().setCreateBranch(true).setName("dev").call();
		removeFileAndCommit("file", "remove file");
		git.checkout().setName("master").call();
		addFileAndCommit("file", "1", "master");

		assertNull(GitUtils.merge(git.getRepository(), git.getRepository().resolve("master"), 
				git.getRepository().resolve("dev"), false, user, user, "merge commit", false));
		
		ObjectId mergeCommitId = GitUtils.merge(git.getRepository(), git.getRepository().resolve("master"), 
				git.getRepository().resolve("dev"), false, user, user, "merge commit", true);
		assertNotNull(mergeCommitId);
		
		try (	RevWalk revWalk = new RevWalk(git.getRepository())) {
			RevCommit mergeCommit = revWalk.parseCommit(mergeCommitId);
			TreeWalk treeWalk = TreeWalk.forPath(git.getRepository(), "file", mergeCommit.getTree());
			BlobIdent blobIdent = new BlobIdent(mergeCommit.name(), "file", FileMode.REGULAR_FILE.getBits());
			Blob blob = new Blob(blobIdent, treeWalk.getObjectId(0), treeWalk.getObjectReader());
			assertEquals("1", blob.getText().getContent());
		}
	}
	
	@Test
	public void testMergeWithBindaryConflict() throws Exception {
		addFileAndCommit("file", "", "initial commit");
		git.checkout().setCreateBranch(true).setName("dev").call();
		
		createDir("dir1");
		File file = new File(gitDir, "dir1/file");
		byte[] content = new byte[1000];
		for (int i=0; i<content.length; i++)
			content[i] = 1;
		FileUtils.writeByteArrayToFile(file, content);
		add("dir1/file");
		commit("add dir1/file");
		
		addFileAndCommit("dir2/file", "111\n222\n333\n444\n555\n", "add dir2/file");
		
		git.checkout().setName("master").call();
		
		createDir("dir1");
		file = new File(gitDir, "dir1/file");
		content = new byte[2000];
		for (int i=0; i<content.length; i++)
			content[i] = 2;
		FileUtils.writeByteArrayToFile(file, content);
		add("dir1/file");
		commit("add dir1/file");
		
		addFileAndCommit("dir2/file", "111\n222\n333\n444\n555\naaa\n", "add dir2/file");
		
		assertNull(GitUtils.merge(git.getRepository(), git.getRepository().resolve("master"), 
				git.getRepository().resolve("dev"), false, user, user, "merge commit", false));
		
		ObjectId mergeCommitId = GitUtils.merge(git.getRepository(), git.getRepository().resolve("master"), 
				git.getRepository().resolve("dev"), false, user, user, "merge commit", true);
		assertNotNull(mergeCommitId);
		
		try (	RevWalk revWalk = new RevWalk(git.getRepository())) {
			RevCommit mergeCommit = revWalk.parseCommit(mergeCommitId);
			TreeWalk treeWalk = TreeWalk.forPath(git.getRepository(), "dir1/file", mergeCommit.getTree());
			BlobIdent blobIdent = new BlobIdent(mergeCommit.name(), "dir1/file", FileMode.REGULAR_FILE.getBits());
			Blob blob = new Blob(blobIdent, treeWalk.getObjectId(0), treeWalk.getObjectReader());
			assertEquals(2000, blob.getBytes().length);
			
			treeWalk = TreeWalk.forPath(git.getRepository(), "dir2/file", mergeCommit.getTree());
			blobIdent = new BlobIdent(mergeCommit.name(), "dir2/file", FileMode.REGULAR_FILE.getBits());
			blob = new Blob(blobIdent, treeWalk.getObjectId(0), treeWalk.getObjectReader());
			assertEquals("111\n222\n333\n444\n555\naaa\n", blob.getText().getContent());
		}
		
		mergeCommitId = GitUtils.merge(git.getRepository(), git.getRepository().resolve("dev"), 
				git.getRepository().resolve("master"), false, user, user, "merge commit", true);
		assertNotNull(mergeCommitId);
		
		try (	RevWalk revWalk = new RevWalk(git.getRepository())) {
			RevCommit mergeCommit = revWalk.parseCommit(mergeCommitId);
			TreeWalk treeWalk = TreeWalk.forPath(git.getRepository(), "dir1/file", mergeCommit.getTree());
			BlobIdent blobIdent = new BlobIdent(mergeCommit.name(), "dir1/file", FileMode.REGULAR_FILE.getBits());
			Blob blob = new Blob(blobIdent, treeWalk.getObjectId(0), treeWalk.getObjectReader());
			assertEquals(1000, blob.getBytes().length);
			
			treeWalk = TreeWalk.forPath(git.getRepository(), "dir2/file", mergeCommit.getTree());
			blobIdent = new BlobIdent(mergeCommit.name(), "dir2/file", FileMode.REGULAR_FILE.getBits());
			blob = new Blob(blobIdent, treeWalk.getObjectId(0), treeWalk.getObjectReader());
			assertEquals("111\n222\n333\n444\n555\n", blob.getText().getContent());
		}
	}
	
	@Test
	public void testMergeWithFileAndFolderConflict() throws Exception {
		addFileAndCommit("file", "", "initial commit");
		git.checkout().setCreateBranch(true).setName("dev").call();
		
		git.checkout().setName("master").call();
		createDir("newpath");
		addFileAndCommit("newpath/file", "1", "new path");
		
		git.checkout().setName("dev").call();
		addFileAndCommit("newpath", "2", "new path");

		ObjectId mergeCommitId;

		mergeCommitId = GitUtils.merge(git.getRepository(), git.getRepository().resolve("master"), 
				git.getRepository().resolve("dev"), false, user, user, "merge commit", false);
		
		assertNull(mergeCommitId);
		
		mergeCommitId = GitUtils.merge(git.getRepository(), git.getRepository().resolve("master"), 
				git.getRepository().resolve("dev"), false, user, user, "merge commit", true);
		assertNotNull(mergeCommitId);
		
		try (	RevWalk revWalk = new RevWalk(git.getRepository())) {
			RevCommit mergeCommit = revWalk.parseCommit(mergeCommitId);
			TreeWalk treeWalk = TreeWalk.forPath(git.getRepository(), "newpath", mergeCommit.getTree());
			assertEquals(FileMode.TREE, treeWalk.getFileMode());
			
			treeWalk = TreeWalk.forPath(git.getRepository(), "newpath/file", mergeCommit.getTree());
			BlobIdent blobIdent = new BlobIdent(mergeCommit.name(), "newpath/file", FileMode.REGULAR_FILE.getBits());
			Blob blob = new Blob(blobIdent, treeWalk.getObjectId(0), treeWalk.getObjectReader());
			assertEquals("1", blob.getText().getContent());
		}
		
		mergeCommitId = GitUtils.merge(git.getRepository(), git.getRepository().resolve("dev"), 
				git.getRepository().resolve("master"), false, user, user, "merge commit", true);
		assertNotNull(mergeCommitId);
		
		try (	RevWalk revWalk = new RevWalk(git.getRepository())) {
			RevCommit mergeCommit = revWalk.parseCommit(mergeCommitId);
			TreeWalk treeWalk = TreeWalk.forPath(git.getRepository(), "newpath", mergeCommit.getTree());
			BlobIdent blobIdent = new BlobIdent(mergeCommit.name(), "newpath", FileMode.REGULAR_FILE.getBits());
			Blob blob = new Blob(blobIdent, treeWalk.getObjectId(0), treeWalk.getObjectReader());
			assertEquals("2", blob.getText().getContent());
		}
	}	
	
	@Test
	public void testMergeWithLinkAndLinkConflict() throws Exception {
		File tempDir;
		
		tempDir = FileUtils.createTempDir();
		try (InputStream is = Resources.getResource(GitUtilsTest.class, "git-conflict-link-link.zip").openStream()) {
			ZipUtils.unzip(is, tempDir);
			try (Git git = Git.open(tempDir)) {
				ObjectId mergeCommitId;

				mergeCommitId = GitUtils.merge(git.getRepository(), git.getRepository().resolve("master"), 
						git.getRepository().resolve("dev"), false, user, user, "merge commit", false);
				
				assertNull(mergeCommitId);

				mergeCommitId = GitUtils.merge(git.getRepository(), git.getRepository().resolve("master"), 
						git.getRepository().resolve("dev"), false, user, user, "merge commit", true);
				
				assertNotNull(mergeCommitId);
				
				try (	RevWalk revWalk = new RevWalk(git.getRepository())) {
					RevCommit mergeCommit = revWalk.parseCommit(mergeCommitId);
					TreeWalk treeWalk = TreeWalk.forPath(git.getRepository(), "lib", mergeCommit.getTree());
					assertTrue(treeWalk != null && treeWalk.getFileMode(0) == FileMode.GITLINK);
					treeWalk = TreeWalk.forPath(git.getRepository(), ".gitmodules", mergeCommit.getTree());
					BlobIdent blobIdent = new BlobIdent(mergeCommit.name(), ".gitmodules", FileMode.GITLINK.getBits());
					Blob blob = new Blob(blobIdent, treeWalk.getObjectId(0), treeWalk.getObjectReader());
					assertTrue(blob.getText().getContent().trim().endsWith("/home/robin/temp/lib"));
				}
			}
		} finally {
			deleteDir(tempDir, 3);
		}
	}
	
	@Test
	public void testMergeWithLinkAndFileConflict() throws Exception {
		File tempDir;
		
		tempDir = FileUtils.createTempDir();
		try (InputStream is = Resources.getResource(GitUtilsTest.class, "git-conflict-link-file.zip").openStream()) {
			ZipUtils.unzip(is, tempDir);
			try (Git git = Git.open(tempDir)) {
				ObjectId mergeCommitId;

				mergeCommitId = GitUtils.merge(git.getRepository(), git.getRepository().resolve("master"), 
						git.getRepository().resolve("dev"), false, user, user, "merge commit", false);
				
				assertNull(mergeCommitId);
				
				mergeCommitId = GitUtils.merge(git.getRepository(), git.getRepository().resolve("master"), 
						git.getRepository().resolve("dev"), false, user, user, "merge commit", true);
				
				assertNotNull(mergeCommitId);
				
				try (	RevWalk revWalk = new RevWalk(git.getRepository())) {
					RevCommit mergeCommit = revWalk.parseCommit(mergeCommitId);
					TreeWalk treeWalk = TreeWalk.forPath(git.getRepository(), "lib", mergeCommit.getTree());
					assertTrue(treeWalk != null && treeWalk.getFileMode(0) == FileMode.GITLINK);
					treeWalk = TreeWalk.forPath(git.getRepository(), ".gitmodules", mergeCommit.getTree());
					BlobIdent blobIdent = new BlobIdent(mergeCommit.name(), ".gitmodules", FileMode.GITLINK.getBits());
					Blob blob = new Blob(blobIdent, treeWalk.getObjectId(0), treeWalk.getObjectReader());
					assertTrue(blob.getText().getContent().trim().endsWith("/home/robin/temp/lib"));
				}
				
				mergeCommitId = GitUtils.merge(git.getRepository(), git.getRepository().resolve("dev"), 
						git.getRepository().resolve("master"), false, user, user, "merge commit", true);
				
				assertNotNull(mergeCommitId);
				
				try (	RevWalk revWalk = new RevWalk(git.getRepository())) {
					RevCommit mergeCommit = revWalk.parseCommit(mergeCommitId);
					TreeWalk treeWalk = TreeWalk.forPath(git.getRepository(), "lib", mergeCommit.getTree());
					BlobIdent blobIdent = new BlobIdent(mergeCommit.name(), "lib", FileMode.REGULAR_FILE.getBits());
					Blob blob = new Blob(blobIdent, treeWalk.getObjectId(0), treeWalk.getObjectReader());
					assertEquals("lib", blob.getText().getContent().trim());
				}
			}
		} finally {
			deleteDir(tempDir, 3);
		}	
		
	}	
	
	@Test
	public void testMergeWithLinkAndDirConflict() throws Exception {
		File tempDir;
		
		tempDir = FileUtils.createTempDir();
		try (InputStream is = Resources.getResource(GitUtilsTest.class, "git-conflict-link-dir.zip").openStream()) {
			ZipUtils.unzip(is, tempDir);
			try (Git git = Git.open(tempDir)) {
				ObjectId mergeCommitId;

				mergeCommitId = GitUtils.merge(git.getRepository(), git.getRepository().resolve("master"), 
						git.getRepository().resolve("dev"), false, user, user, "merge commit", false);
				
				assertNull(mergeCommitId);
				
				mergeCommitId = GitUtils.merge(git.getRepository(), git.getRepository().resolve("master"), 
						git.getRepository().resolve("dev"), false, user, user, "merge commit", true);
				
				assertNotNull(mergeCommitId);
				
				try (	RevWalk revWalk = new RevWalk(git.getRepository())) {
					RevCommit mergeCommit = revWalk.parseCommit(mergeCommitId);
					TreeWalk treeWalk = TreeWalk.forPath(git.getRepository(), "lib", mergeCommit.getTree());
					assertTrue(treeWalk != null && treeWalk.getFileMode(0) == FileMode.GITLINK);
					treeWalk = TreeWalk.forPath(git.getRepository(), ".gitmodules", mergeCommit.getTree());
					BlobIdent blobIdent = new BlobIdent(mergeCommit.name(), ".gitmodules", FileMode.GITLINK.getBits());
					Blob blob = new Blob(blobIdent, treeWalk.getObjectId(0), treeWalk.getObjectReader());
					assertTrue(blob.getText().getContent().trim().endsWith("/home/robin/temp/lib/"));
				}
				
				mergeCommitId = GitUtils.merge(git.getRepository(), git.getRepository().resolve("dev"), 
						git.getRepository().resolve("master"), false, user, user, "merge commit", true);
				
				assertNotNull(mergeCommitId);
				
				try (	RevWalk revWalk = new RevWalk(git.getRepository())) {
					RevCommit mergeCommit = revWalk.parseCommit(mergeCommitId);
					TreeWalk treeWalk = TreeWalk.forPath(git.getRepository(), "lib/file", mergeCommit.getTree());
					BlobIdent blobIdent = new BlobIdent(mergeCommit.name(), "lib/file", FileMode.REGULAR_FILE.getBits());
					Blob blob = new Blob(blobIdent, treeWalk.getObjectId(0), treeWalk.getObjectReader());
					assertTrue(blob.getText().getContent().length() == 0);
				}
			}
		} finally {
			deleteDir(tempDir, 3);
		}			
	}	
}
