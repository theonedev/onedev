package com.pmease.commons.git.command;

import java.io.File;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import static org.junit.Assert.*;

import com.pmease.commons.git.Blame;
import com.pmease.commons.git.Git;
import com.pmease.commons.util.FileUtils;

public class BlameCommandTest {

	@Test
	public void test() {
	    Assert.assertTrue(GitCommand.checkError() == null);

	    File tempDir = FileUtils.createTempDir();
		
		try {
			Git workGit = new Git(new File(tempDir, "work"));
			workGit.init(false);
			
			File file = new File(workGit.repoDir(), "file");
			FileUtils.writeFile(file, 
					  "1st line\n"
					+ "2nd line\n"
					+ "3rd line\n"
					+ "4th line\n"
					+ "5th line\n"
					+ "6th line\n"
					+ "7th line\n"
					+ "8th line\n"
					+ "9th line\n");
			workGit.add("file").commit("initial commit", false);
			
			List<Blame> blames = workGit.blame("file", "master");
			assertEquals(1, blames.size());
			
			FileUtils.writeFile(file, 
					  "first line\n"
					+ "2nd line\n"
					+ "3rd line\n"
					+ "4th line\n"
					+ "5th line\n"
					+ "6th line\n"
					+ "7th line\n"
					+ "8th line\n"
					+ "nineth line\n");
			workGit.add("file").commit("second commit", false);
			
			blames = workGit.blame("file", "master");
			assertEquals(3, blames.size());
			assertEquals(1, blames.get(0).getLines().size());
			assertEquals(7, blames.get(1).getLines().size());
			assertEquals(1, blames.get(2).getLines().size());
			assertEquals(blames.get(0).getCommit().getHash(), blames.get(2).getCommit().getHash());

			FileUtils.writeFile(file, 
					  "first line\n"
					+ "nineth line\n");
			workGit.add("file").commit("third commit", false);
			
			Git bareGit = new Git(new File(tempDir, "bare"));
			bareGit.clone(workGit.repoDir().getAbsolutePath(), true);
			blames = bareGit.blame("file", "master");
			
			assertEquals(1, blames.size());
		} finally {
			FileUtils.deleteDir(tempDir);
		}
	}

}
