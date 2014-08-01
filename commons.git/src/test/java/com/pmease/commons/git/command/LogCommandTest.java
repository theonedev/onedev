package com.pmease.commons.git.command;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.util.List;

import org.junit.Test;

import com.pmease.commons.git.AbstractGitTest;
import com.pmease.commons.git.Commit;
import com.pmease.commons.git.FileChange;
import com.pmease.commons.git.Git;

public class LogCommandTest extends AbstractGitTest {

	@Test
	public void shouldParseLogCorrectly() {
		addFileAndCommit("a", "", "add a");
		addFileAndCommit("b", "", "add b");
		addFileAndCommit("c", "", "add c");
		addFileAndCommit("d", "", "add d");
		addFileAndCommit("a", "a", "modify a");
		
		createDir("dir");
		addFile("dir/file", "hello world");
		addFile("dir/file2", "hello world");
		commit("add dir/file\nadd dir/file to test files under a directory");
		
		git.checkout("head", "dev");
		rm("dir/file");
		commit("remove dir/file");

		Git bareGit = new Git(new File(tempDir, "bare"));
		bareGit.clone(git, true, false, false, null);

		bareGit.addNote("master", "hello\nworld");

		List<Commit> commits = bareGit.log(null, "master", null, 0, 0);
		assertEquals(commits.size(), 6);
		assertEquals(commits.get(0).getSubject(), "add dir/file");
		assertEquals(commits.get(0).getMessage(), "add dir/file\nadd dir/file to test files under a directory");
		assertEquals("hello\nworld", commits.get(0).getNote());
		assertEquals(commits.get(0).getFileChanges().size(), 2);
		assertEquals(commits.get(0).getFileChanges().get(0).getNewPath(), "dir/file");
		assertEquals(commits.get(0).getFileChanges().get(0).getAction(), FileChange.Action.ADD);
		assertEquals(commits.get(0).getParentHashes().size(), 1);
		assertEquals(commits.get(0).getParentHashes().iterator().next(), commits.get(1).getHash());
		
		assertEquals(null, commits.get(1).getNote());
		
		git.checkout("master", null).rm("a").commit("remove a", false, false);
		addFileAndCommit("dir/file2", "file2", "add dir/file2");
		git.merge("dev", null, null, null, null);
		
		commits = git.log(null, "master", null, 0, 0);
		
		assertEquals(commits.get(0).getParentHashes().size(), 2);
		
		commits = git.log(null, "master", "a", 0, 0);
		assertEquals(commits.size(), 3);
		
		commits = git.log("dev", "master", "dir", 0, 0);
		assertEquals(commits.size(), 2);

		assertEquals(git.showRevision(commits.get(0).getHash()).getHash(), commits.get(0).getHash()); 
		assertEquals(git.showRevision(commits.get(1).getHash()).getHash(), commits.get(1).getHash()); 
	}

	@Test
	public void shouldHandleRenameAndCopyCorrectly() {
		addFileAndCommit("a", "1111\n2222\n3333\n", "add a");
		addFile("a", "1111\n2222\n3333\n4444\n");
		addFile("a2", "1111\n2222\n3333\n4444\n");
	    commit("copy a to a2");

	    addFileAndCommit("b", "1111\n2222\n3333\n4444\n", "add b");
	    
	    rm("b");
	    addFileAndCommit("b2", "1111\n2222\n3333\n4444\n", "move b to b2");

	    List<Commit> commits = git.log("master~3", "master", null, 0, 0);
	    FileChange change = commits.get(0).getFileChanges().get(0);
	    assertEquals("RENAME\tb->b2", change.toString());
	    change = commits.get(2).getFileChanges().get(1);
	    assertEquals("COPY\ta->a2", change.toString());
	}
}
