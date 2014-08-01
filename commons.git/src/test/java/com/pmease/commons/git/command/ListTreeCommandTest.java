package com.pmease.commons.git.command;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.eclipse.jgit.lib.FileMode;
import org.junit.Test;

import com.pmease.commons.git.AbstractGitTest;
import com.pmease.commons.git.Git;
import com.pmease.commons.git.TreeNode;
import com.pmease.commons.util.FileUtils;

public class ListTreeCommandTest extends AbstractGitTest {

	@Test
	public void shouldListTreeAndReadFileCorrectly() throws IOException {
		addFileAndCommit("a", "", "commit");
		addFileAndCommit("b", "", "commit");
		addFileAndCommit("c", "", "commit");
		addFileAndCommit("d", "", "commit");
		addFileAndCommit("a", "a", "commit");
		createDir("dir");
		addFileAndCommit("dir/file", "hello world", "commit");
		
		Git moduleGit = new Git(new File(tempDir, "module"));
		moduleGit.init(false);
		FileUtils.writeFile(new File(moduleGit.repoDir(), "readme"), "readme");
		moduleGit.add("readme").commit("commit", false, false);
		
		git.addSubModule(moduleGit.repoDir().getAbsolutePath(), "module");
		git.commit("commit", false, false);

		git.checkout("head", "dev");
		rm("dir/file");
		commit("commit");
		
		Git bareGit = new Git(new File(tempDir, "bare"));
		bareGit.clone(git, true, false, false, null);
		
		List<TreeNode> treeNodes = bareGit.listTree("master", null);

		assertEquals(7, treeNodes.size());
		assertEquals("dir", treeNodes.get(0).getPath());
		assertEquals("dir", treeNodes.get(0).getName());
		assertEquals(FileMode.TREE, treeNodes.get(0).getMode());
		
		assertEquals(moduleGit.repoDir().getAbsolutePath(), 
				new File(new String(treeNodes.get(6).show(bareGit))).getCanonicalPath());
		
		TreeNode dirNode = treeNodes.get(0);
		treeNodes = bareGit.listTree(dirNode.getRevision(), dirNode.getPath());
		
		assertEquals(1, treeNodes.size());
		assertEquals("dir/file", treeNodes.get(0).getPath());
		assertEquals("file", treeNodes.get(0).getName());
		
		TreeNode fileNode = treeNodes.get(0);
		assertEquals("hello world", new String(fileNode.show(bareGit)));
	}

}
