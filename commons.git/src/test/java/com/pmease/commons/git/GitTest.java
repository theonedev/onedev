package com.pmease.commons.git;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.junit.Test;

import com.pmease.commons.util.Charsets;
import com.pmease.commons.util.MediaTypes;

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
		
		List<DiffTreeNode> diffs = git.listTreeWithDiff("master", "dev", null);
		assertEquals(7, diffs.size());
		assertEquals("dir1", diffs.get(0).getPath());
		assertEquals(DiffTreeNode.Status.MODIFY, diffs.get(0).getStatus());
		assertEquals("dir2", diffs.get(1).getPath());
		assertEquals(DiffTreeNode.Status.MODIFY, diffs.get(1).getStatus());
		assertEquals("dir3", diffs.get(2).getPath());
		assertEquals(DiffTreeNode.Status.ADD, diffs.get(2).getStatus());
		assertEquals("dir2", diffs.get(3).getPath());
		assertEquals(DiffTreeNode.Status.ADD, diffs.get(3).getStatus());
		assertEquals(false, diffs.get(3).isFolder());
		assertEquals("file1", diffs.get(4).getPath());
		assertEquals(DiffTreeNode.Status.UNCHANGE, diffs.get(4).getStatus());
		assertEquals("file2", diffs.get(5).getPath());
		assertEquals(DiffTreeNode.Status.DELETE, diffs.get(5).getStatus());
		assertEquals("file3", diffs.get(6).getPath());
		assertEquals(DiffTreeNode.Status.ADD, diffs.get(6).getStatus());
		
		diffs = git.listTreeWithDiff("master", "dev", "dir2/");
		assertEquals(1, diffs.size());
		assertEquals("dir2/file1", diffs.get(0).getPath());
		assertEquals(DiffTreeNode.Status.DELETE, diffs.get(0).getStatus());
		
		diffs = git.listTreeWithDiff("master", "dev", "dir3/");
		assertEquals(2, diffs.size());
		assertEquals("dir3/file1", diffs.get(0).getPath());
		assertEquals(DiffTreeNode.Status.ADD, diffs.get(0).getStatus());
		assertEquals("dir3/file2", diffs.get(1).getPath());
		assertEquals(DiffTreeNode.Status.ADD, diffs.get(1).getStatus());
		
		diffs = git.listTreeWithDiff("master", "dev", "dir3");
		assertEquals(1, diffs.size());
		assertEquals("dir3", diffs.get(0).getPath());
		assertEquals(DiffTreeNode.Status.ADD, diffs.get(0).getStatus());

		rm("dir3/file1");
		rm("dir3/file2");
		commit("delete dir3");

		diffs = git.listTreeWithDiff("dev~1", "dev", null);
		assertEquals(5, diffs.size());
		assertEquals("dir3", diffs.get(1).getPath());
		assertEquals(DiffTreeNode.Status.DELETE, diffs.get(1).getStatus());
		
		diffs = git.listTreeWithDiff("dev~1", "dev", "dir3");
		assertEquals(1, diffs.size());
		assertEquals("dir3", diffs.get(0).getPath());
		assertEquals(DiffTreeNode.Status.DELETE, diffs.get(0).getStatus());
	}

	@Test
	public void test() {
		try {
			System.out.println(MediaTypes.detectFrom(FileUtils.readFileToByteArray(new File("w:\\temp\\file.txt")), null));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			System.out.println(Charsets.detectFrom(FileUtils.readFileToByteArray(new File("w:\\temp\\file.txt"))));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
