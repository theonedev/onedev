package com.pmease.commons.git;

import static com.pmease.commons.git.Change.Status.ADDED;
import static com.pmease.commons.git.Change.Status.DELETED;
import static com.pmease.commons.git.Change.Status.MODIFIED;
import static com.pmease.commons.git.Change.Status.RENAMED;
import static com.pmease.commons.git.Change.Status.UNCHANGED;
import static org.junit.Assert.assertEquals;

import java.io.File;
import java.util.List;

import org.junit.Test;

public class GitTest extends AbstractGitTest {

	@Test
	public void testListTreeWithDiff() {
		addFileAndCommit("file1", "file1", "file1");
		addFileAndCommit("file2", "file2", "file2");
		createDir("dir1");
		addFileAndCommit("dir1/file1", "dir1/file1", "dir1/file1");
		createDir("dir2");
		addFileAndCommit("dir2/file1", "dir2/file1", "dir2/file1");
		
		git.checkout("master", "dev");
		
		addFileAndCommit("dir1/file1", "dir1/file11", "dir1/file11");
		addFileAndCommit("dir1/file2", "", "dir1/file2");
		rm("file2", "dir2/file1");
		addFileAndCommit("dir2", "dir2", "dir2");
		
		addFileAndCommit("file3", "file3", "file3");
		createDir("dir3");
		addFileAndCommit("dir3/file1", "dir3/file1", "dir3/file1");
		addFileAndCommit("dir3/file2", "dir3/file2", "dir3/file2");
		
		List<Change> diffs = git.listTree("master", "dev", null, null);
		assertEquals(7, diffs.size());
		assertEquals("dir1", diffs.get(0).getNewPath());
		assertEquals(MODIFIED, diffs.get(0).getStatus());
		assertEquals("dir2", diffs.get(1).getNewPath());
		assertEquals(MODIFIED, diffs.get(1).getStatus());
		assertEquals("dir3", diffs.get(2).getNewPath());
		assertEquals(ADDED, diffs.get(2).getStatus());
		assertEquals("dir2", diffs.get(3).getNewPath());
		assertEquals(ADDED, diffs.get(3).getStatus());
		assertEquals(false, diffs.get(3).isFolder());
		assertEquals("file1", diffs.get(4).getNewPath());
		assertEquals(UNCHANGED, diffs.get(4).getStatus());
		assertEquals("file2", diffs.get(5).getOldPath());
		assertEquals(DELETED, diffs.get(5).getStatus());
		assertEquals("file3", diffs.get(6).getNewPath());
		assertEquals(ADDED, diffs.get(6).getStatus());
		
		diffs = git.listTree("master", "dev", "dir2/", null);
		assertEquals(1, diffs.size());
		assertEquals("dir2/file1", diffs.get(0).getOldPath());
		assertEquals(DELETED, diffs.get(0).getStatus());
		
		diffs = git.listTree("master", "dev", "dir3/", null);
		assertEquals(2, diffs.size());
		assertEquals("dir3/file1", diffs.get(0).getNewPath());
		assertEquals(ADDED, diffs.get(0).getStatus());
		assertEquals("dir3/file2", diffs.get(1).getNewPath());
		assertEquals(ADDED, diffs.get(1).getStatus());
		
		rm("dir3/file1");
		rm("dir3/file2");
		commit("delete dir3");

		diffs = git.listTree("dev~1", "dev", null, null);
		assertEquals(5, diffs.size());
		assertEquals("dir3", diffs.get(1).getOldPath());
		assertEquals(DELETED, diffs.get(1).getStatus());
		
		addFileAndCommit("dir1/somefile", "hello world", "commit1");
		removeFileAndCommit("dir1/somefile", "commit2");
		addFileAndCommit("somefile2", "hello world", "commit3");
		
		diffs = git.listTree("dev~2", "dev", null, null);
		assertEquals(5, diffs.size());
		assertEquals("somefile2", diffs.get(4).getNewPath());
		assertEquals(RENAMED, diffs.get(4).getStatus());
	}

	@Test
	public void test() {
		Git git = new Git(new File("w:\\linux\\.git"));
		long time = System.currentTimeMillis();
		git.log("master~1000", "master", null, 0, 0);
		System.out.println(System.currentTimeMillis()-time);
	}
}
