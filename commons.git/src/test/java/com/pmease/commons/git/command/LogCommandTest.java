package com.pmease.commons.git.command;

import java.io.File;
import java.util.List;

import static org.junit.Assert.*;

import org.junit.Test;

import com.pmease.commons.git.Commit;
import com.pmease.commons.git.FileChange;
import com.pmease.commons.git.Git;
import com.pmease.commons.util.FileUtils;

public class LogCommandTest {

	@Test
	public void shouldParseLogCorrectly() {
	    assertTrue(GitCommand.checkError() == null);
	    File tempDir = FileUtils.createTempDir();
	    
	    try {
		    Git workGit = new Git(new File(tempDir, "work"));
		    workGit.init(false);
		        
    		FileUtils.touchFile(new File(workGit.repoDir(), "a"));
    		workGit.add("a");
    		workGit.commit("add a", false, false);
    		
    		FileUtils.touchFile(new File(workGit.repoDir(), "b"));
    		workGit.add("b");
    		workGit.commit("add b", false, false);
    		
    		FileUtils.touchFile(new File(workGit.repoDir(), "c"));
    		workGit.add("c");
    		workGit.commit("add c", false, false);
    		
    		FileUtils.touchFile(new File(workGit.repoDir(), "d"));
    		workGit.add("d");
    		workGit.commit("add d", false, false);
    		
    		FileUtils.writeFile(new File(workGit.repoDir(), "a"), "a");
    		workGit.add("a");
    		workGit.commit("modify a", false, false);
    		
    		FileUtils.createDir(new File(workGit.repoDir(), "dir"));
    		FileUtils.writeFile(new File(workGit.repoDir(), "dir/file"), "hello world");
    		FileUtils.writeFile(new File(workGit.repoDir(), "dir/file2"), "hello world");
    		workGit.add("dir/file");
    		workGit.add("dir/file2");
    		workGit.commit("add dir/file\nadd dir/file to test files under a directory", false, false);
    		
    		workGit.checkout("head", "dev");
    		workGit.remove("dir/file");
    		workGit.commit("remove dir/file", false, false);
    		
    		Git bareGit = new Git(new File(tempDir, "bare"));
    		bareGit.clone(workGit.repoDir().getAbsolutePath(), true);

    		bareGit.addNote("master", "hello\nworld");

    		List<Commit> commits = bareGit.log(null, "master", null, 0);
    		assertEquals(commits.size(), 6);
    		assertEquals(commits.get(0).getSubject(), "add dir/file");
    		assertEquals(commits.get(0).getMessage(), "add dir/file\n\nadd dir/file to test files under a directory");
    		assertEquals("hello\nworld", commits.get(0).getNote());
    		assertEquals(commits.get(0).getFileChanges().size(), 2);
    		assertEquals(commits.get(0).getFileChanges().get(0).getNewPath(), "dir/file");
    		assertEquals(commits.get(0).getFileChanges().get(0).getAction(), FileChange.Action.ADD);
    		assertEquals(commits.get(0).getParentHashes().size(), 1);
    		assertEquals(commits.get(0).getParentHashes().iterator().next(), commits.get(1).getHash());
    		
    		assertEquals(null, commits.get(1).getNote());
    		
    		workGit.checkout("master", null).remove("a").commit("remove a", false, false);
    		FileUtils.writeFile(new File(workGit.repoDir(), "dir/file2"), "file2");
    		workGit.add("dir/file2").commit("add dir/file2", false, false);
    		workGit.merge("dev", null, null, null);
    		
    		commits = workGit.log(null, "master", null, 0);
    		
    		assertEquals(commits.get(0).getParentHashes().size(), 2);
    		
    		commits = workGit.log(null, "master", "a", 0);
    		assertEquals(commits.size(), 3);
    		
    		commits = workGit.log("dev", "master", "dir", 0);
    		assertEquals(commits.size(), 2);

    		assertEquals(workGit.resolveRevision(commits.get(0).getHash()).getHash(), commits.get(0).getHash()); 
    		assertEquals(workGit.resolveRevision(commits.get(1).getHash()).getHash(), commits.get(1).getHash()); 
	    } finally {
	        FileUtils.deleteDir(tempDir);
	    }
	}

	@Test
	public void shouldHandleRenameAndCopyCorrectly() {
	    assertTrue(GitCommand.checkError() == null);
	    File tempDir = FileUtils.createTempDir();
	    
	    try {
		    Git git = new Git(tempDir);
		    git.init(false);

		    FileUtils.writeFile(new File(tempDir, "a"), "1111\n2222\n3333\n");
		    git.add("a");
		    git.commit("add a", false, false);
		    
		    FileUtils.writeFile(new File(tempDir, "a"), "1111\n2222\n3333\n4444\n");
		    FileUtils.writeFile(new File(tempDir, "a2"), "1111\n2222\n3333\n4444\n");
		    git.add("a", "a2");
		    git.commit("copy a to a2", false, false);
		    
		    FileUtils.writeFile(new File(tempDir, "b"), "1111\n2222\n3333\n4444\n");
		    git.add("b");
		    git.commit("add b", false, false);

		    FileUtils.writeFile(new File(tempDir, "b2"), "1111\n2222\n3333\n4444\n");
		    git.remove("b");
		    git.add("b2");
		    git.commit("move b to b2", false, false);

		    List<Commit> commits = git.log("master~3", "master", null, 0);
		    FileChange change = commits.get(0).getFileChanges().get(0);
		    assertEquals("RENAME\tb->b2", change.toString());
		    change = commits.get(2).getFileChanges().get(1);
		    assertEquals("COPY\ta->a2", change.toString());
	    } finally {
	        FileUtils.deleteDir(tempDir);
	    }
	}
}
