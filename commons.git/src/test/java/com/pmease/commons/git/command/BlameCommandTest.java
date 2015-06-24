package com.pmease.commons.git.command;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.util.Map;

import org.junit.Test;

import com.pmease.commons.git.AbstractGitTest;
import com.pmease.commons.git.Blame;
import com.pmease.commons.git.Git;

public class BlameCommandTest extends AbstractGitTest {

	@Test
	public void test() {
		addFileAndCommit(
				"file", 
				"1st line\n"
				+ "2nd line\n"
				+ "3rd line\n"
				+ "4th line\n"
				+ "5th line\n"
				+ "6th line\n"
				+ "7th line\n"
				+ "8th line\n"
				+ "9th line\n", 
				"initial commit");
		
		String commitHash = git.parseRevision("master", true);
		Map<String, Blame> blames = git.blame(commitHash, "file");
		assertEquals(1, blames.size());
		assertEquals(commitHash + ": 0-9", blames.get(commitHash).toString());
		
		addFileAndCommit(
				"file", 
				"first line\n"
				+ "2nd line\n"
				+ "3rd line\n"
				+ "4th line\n"
				+ "5th line\n"
				+ "6th line\n"
				+ "7th line\n"
				+ "8th line\n"
				+ "nineth line\n",
				"second commit");
		
		commitHash = git.parseRevision("master", true);
		blames = git.blame(commitHash, "file");
		assertEquals(2, blames.size());
		
		assertEquals(commitHash + ": 0-1, 8-9", blames.get(commitHash).toString());
		commitHash = git.parseRevision("master~1", true);
		assertEquals(commitHash + ": 1-8", blames.get(commitHash).toString());
		
		addFileAndCommit(
				"file", 
				"first line\n"
				+ "nineth line\n", 
				"third commit");
		
		Git bareGit = new Git(new File(tempDir, "bare"));
		bareGit.clone(git, true, false, false, null);
		commitHash = bareGit.parseRevision("master", true);
		blames = bareGit.blame(commitHash, "file");
		
		commitHash = bareGit.parseRevision("master~1", true);
		
		assertEquals(1, blames.size());
		assertEquals(commitHash + ": 0-2", blames.get(commitHash).toString());
	}

}
