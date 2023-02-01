package io.onedev.server.git.command;

import static org.junit.Assert.assertEquals;

import java.util.Collection;

import org.eclipse.jgit.lib.ObjectId;
import org.junit.Test;

import io.onedev.commons.utils.LinearRange;
import io.onedev.server.git.AbstractGitTest;
import io.onedev.server.git.BlameBlock;

public class BlameCommandTest extends AbstractGitTest {

	@Test
	public void test() throws Exception {
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
		
		ObjectId commitId = git.getRepository().resolve("main");
		Collection<BlameBlock> blames = new BlameCommand(git.getRepository().getDirectory(), commitId, "file")
				.run();
		assertEquals(1, blames.size());
		assertEquals(commitId.name() + ": 0-8", blames.iterator().next().toString());
		
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
		
		commitId = git.getRepository().resolve("main");
		blames = new BlameCommand(git.getRepository().getDirectory(), commitId, "file")
				.range(new LinearRange(5, 8))
				.run();
		assertEquals(2, blames.size());
		
		assertEquals(commitId.name() + ": 8-8", getBlock(blames, commitId.name()).toString());
		commitId = git.getRepository().resolve("main~1");
		assertEquals(commitId.name() + ": 5-7", getBlock(blames, commitId.name()).toString());
		
		addFileAndCommit(
				"file", 
				"first line\n"
				+ "nineth line\n", 
				"third commit");
		commitId = git.getRepository().resolve("main");
		blames = new BlameCommand(git.getRepository().getDirectory(), commitId, "file")
				.run();
		
		commitId = git.getRepository().resolve("main~1");
		
		assertEquals(1, blames.size());
		assertEquals(commitId.name() + ": 0-1", getBlock(blames, commitId.name()).toString());
	}

	private BlameBlock getBlock(Collection<BlameBlock> blameBlocks, String commitHash) {
		for (BlameBlock block: blameBlocks) {
			if (block.getCommit().getHash().equals(commitHash))
				return block;
		}
		return null;
	}
}
