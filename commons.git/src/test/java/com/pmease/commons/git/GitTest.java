package com.pmease.commons.git;

import static org.junit.Assert.assertEquals;

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
		rm("file2", "dir2/file1");
		addFileAndCommit("dir2", "dir2", "dir2");
		
		addFileAndCommit("file3", "file3", "file3");
		createDir("dir3");
		addFileAndCommit("dir3/file1", "dir3/file1", "dir3/file1");
		addFileAndCommit("dir3/file2", "dir3/file2", "dir3/file2");
		
		List<DiffTreeNode> diffs = git.listTreeWithDiff("master", "dev", null);
		assertEquals(7, diffs.size());
		assertEquals("dir1", diffs.get(0).getPath());
		assertEquals(DiffTreeNode.Action.MODIFY, diffs.get(0).getAction());
		assertEquals("dir2", diffs.get(1).getPath());
		assertEquals(DiffTreeNode.Action.MODIFY, diffs.get(1).getAction());
		assertEquals("dir3", diffs.get(2).getPath());
		assertEquals(DiffTreeNode.Action.ADD, diffs.get(2).getAction());
		assertEquals("dir2", diffs.get(3).getPath());
		assertEquals(DiffTreeNode.Action.ADD, diffs.get(3).getAction());
		assertEquals(false, diffs.get(3).isFolder());
		assertEquals("file1", diffs.get(4).getPath());
		assertEquals(DiffTreeNode.Action.EQUAL, diffs.get(4).getAction());
		assertEquals("file2", diffs.get(5).getPath());
		assertEquals(DiffTreeNode.Action.DELETE, diffs.get(5).getAction());
		assertEquals("file3", diffs.get(6).getPath());
		assertEquals(DiffTreeNode.Action.ADD, diffs.get(6).getAction());
		
		diffs = git.listTreeWithDiff("master", "dev", "dir2");
		assertEquals(1, diffs.size());
		assertEquals("dir2/file1", diffs.get(0).getPath());
		assertEquals(DiffTreeNode.Action.DELETE, diffs.get(0).getAction());
		
		diffs = git.listTreeWithDiff("master", "dev", "dir3");
		assertEquals(2, diffs.size());
		assertEquals("dir3/file1", diffs.get(0).getPath());
		assertEquals(DiffTreeNode.Action.ADD, diffs.get(0).getAction());
		assertEquals("dir3/file2", diffs.get(1).getPath());
		assertEquals(DiffTreeNode.Action.ADD, diffs.get(1).getAction());
	}

}
