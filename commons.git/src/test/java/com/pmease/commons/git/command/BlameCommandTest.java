package com.pmease.commons.git.command;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.util.List;

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
		
		List<Blame> blames = git.blame("file", "master", 3, -1);
		assertEquals(1, blames.size());
		assertEquals(7, blames.get(0).getLines().size());
		
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
		
		blames = git.blame("file", "master", -1, -1);
		assertEquals(3, blames.size());
		assertEquals(1, blames.get(0).getLines().size());
		assertEquals(7, blames.get(1).getLines().size());
		assertEquals(1, blames.get(2).getLines().size());
		assertEquals(blames.get(0).getCommit().getHash(), blames.get(2).getCommit().getHash());

		blames = git.blame("file", "master", 5, 9);
		assertEquals(2, blames.size());
		assertEquals(4, blames.get(0).getLines().size());

		blames = git.blame("file", "master", 10, 9);
		assertEquals(0, blames.size());

		blames = git.blame("file", "master", 8, 100);
		assertEquals(2, blames.size());

		blames = git.blame("file", "master", 80, 100);
		assertEquals(0, blames.size());

		addFileAndCommit(
				"file", 
				"first line\n"
				+ "nineth line\n", 
				"third commit");
		
		Git bareGit = new Git(new File(tempDir, "bare"));
		bareGit.clone(git, true, false, false, null);
		blames = bareGit.blame("file", "master", -1, -1);
		
		assertEquals(1, blames.size());
	}

}
