package com.pmease.commons.git.command;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import com.pmease.commons.git.Git;
import com.pmease.commons.git.TreeNode;
import com.pmease.commons.util.FileUtils;

public class ListTreeCommandTest {

	@Test
	public void shouldListTreeAndReadFileCorrectly() throws IOException {
	    Assert.assertTrue(GitCommand.checkError() == null);
	    File tempDir = FileUtils.createTempDir();
	    
	    try {
		    Git workGit = new Git(new File(tempDir, "work"));
		    workGit.init(false);
		        
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
    		
			Git moduleGit = new Git(new File(tempDir, "module"));
			moduleGit.init(false);
			FileUtils.writeFile(new File(moduleGit.repoDir(), "readme"), "readme");
			moduleGit.add("readme").commit("commit", false);
			
			workGit.addSubModule(moduleGit.repoDir().getAbsolutePath(), "module");
			workGit.commit("commit", false);

    		workGit.checkout("dev", true);
    		workGit.remove("dir/file");
    		workGit.commit("commit", false);
    		
			Git bareGit = new Git(new File(tempDir, "bare"));
    		bareGit.clone(workGit.repoDir().getAbsolutePath(), true);

    		List<TreeNode> treeNodes = bareGit.listTree("master", null, false);

    		assertEquals(7, treeNodes.size());
    		assertEquals("dir", treeNodes.get(5).getPath());
    		assertEquals("dir", treeNodes.get(5).getName());
    		assertEquals(TreeNode.Type.DIRECTORY, treeNodes.get(5).getType());
    		assertEquals(moduleGit.repoDir().getAbsolutePath(), 
    				new File(new String(treeNodes.get(6).show())).getCanonicalPath());
    		
    		TreeNode dirNode = treeNodes.get(5);
    		treeNodes = dirNode.listChildren();
    		
    		assertEquals(1, treeNodes.size());
    		assertEquals("dir/file", treeNodes.get(0).getPath());
    		assertEquals("file", treeNodes.get(0).getName());
    		
    		TreeNode fileNode = treeNodes.get(0);
    		assertEquals("hello world", new String(fileNode.show()));
    		assertEquals("dir", fileNode.getParent().getName());
    		
	    } finally {
	        FileUtils.deleteDir(tempDir);
	    }
	}

}
