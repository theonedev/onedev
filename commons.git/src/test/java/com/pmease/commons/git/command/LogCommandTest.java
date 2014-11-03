package com.pmease.commons.git.command;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.util.List;

import org.junit.Test;

import com.pmease.commons.git.AbstractGitTest;
import com.pmease.commons.git.Commit;
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
		assertEquals(commits.get(0).getBody(), "add dir/file to test files under a directory");
		assertEquals("hello\nworld", commits.get(0).getNote());
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

}
