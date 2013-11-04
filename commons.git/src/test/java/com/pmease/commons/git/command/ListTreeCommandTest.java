package com.pmease.commons.git.command;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import com.pmease.commons.git.DirNode;
import com.pmease.commons.git.FileNode;
import com.pmease.commons.git.Git;
import com.pmease.commons.git.TreeNode;
import com.pmease.commons.util.FileUtils;

public class ListTreeCommandTest {

	@Test
	public void shouldListTreeAndReadFileCorrectly() {
	    Assert.assertTrue(GitCommand.checkError() == null);
	    File tempDir = FileUtils.createTempDir();
	    
	    Git workGit = new Git(new File(tempDir, "work"));
	    workGit.init(false);
	        
	    try {
    		FileUtils.touchFile(new File(workGit.repoDir(), "a"));
    		workGit.add("a");
    		workGit.commit("commit", false);
    		
    		FileUtils.touchFile(new File(workGit.repoDir(), "b"));
    		workGit.add("b");
    		workGit.commit("commit", false);
    		
    		FileUtils.touchFile(new File(workGit.repoDir(), "c"));
    		workGit.add("c");
    		workGit.commit("commit", false);
    		
    		FileUtils.touchFile(new File(workGit.repoDir(), "d"));
    		workGit.add("d");
    		workGit.commit("commit", false);
    		
    		FileUtils.writeFile(new File(workGit.repoDir(), "a"), "a");
    		workGit.add("a");
    		workGit.commit("commit", false);
    		
    		FileUtils.createDir(new File(workGit.repoDir(), "dir"));
    		FileUtils.writeFile(new File(workGit.repoDir(), "dir/file"), "hello world");
    		workGit.add("dir/file");
    		workGit.commit("commit", false);
    		
    		workGit.checkout("dev", true);
    		workGit.remove("dir/file");
    		workGit.commit("commit", false);
    		
    		Git bareGit = new Git(new File(tempDir, "bare"));
    		bareGit.clone(workGit.repoDir().getAbsolutePath(), true);

    		List<TreeNode> treeNodes = bareGit.listTree("master", null, false);

    		assertEquals(treeNodes.size(), 5);
    		assertEquals(treeNodes.get(4).getPath(), "dir");
    		assertEquals(treeNodes.get(4).getName(), "dir");
    		assertTrue(treeNodes.get(4) instanceof DirNode);
    		
    		DirNode dirNode = (DirNode) treeNodes.get(4);
    		treeNodes = dirNode.listChildren();
    		
    		assertEquals(treeNodes.size(), 1);
    		assertEquals(treeNodes.get(0).getPath(), "dir/file");
    		assertEquals(treeNodes.get(0).getName(), "file");
    		
    		FileNode fileNode = (FileNode) treeNodes.get(0);
    		assertEquals(new String(fileNode.read()), "hello world");
    		assertEquals(fileNode.getParent().getName(), "dir");
    		
	    } finally {
	        FileUtils.deleteDir(tempDir);
	    }
	}

}
