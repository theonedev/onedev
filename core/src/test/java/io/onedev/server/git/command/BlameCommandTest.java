package io.onedev.server.git.command;

import static org.junit.Assert.assertEquals;

import java.util.Collection;

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
		
		String commitHash = git.getRepository().resolve("master").name();
		Collection<BlameBlock> blames = new BlameCommand(git.getRepository().getDirectory())
				.commitHash(commitHash)
				.file("file")
				.call();
		assertEquals(1, blames.size());
		assertEquals(commitHash + ": 0-8", blames.iterator().next().toString());
		
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
		
		commitHash = git.getRepository().resolve("master").name();
		blames = new BlameCommand(git.getRepository().getDirectory())
				.commitHash(commitHash)
				.file("file")
				.range(new LinearRange(5, 8))
				.call();
		assertEquals(2, blames.size());
		
		assertEquals(commitHash + ": 8-8", getBlock(blames, commitHash).toString());
		commitHash = git.getRepository().resolve("master~1").name();
		assertEquals(commitHash + ": 5-7", getBlock(blames, commitHash).toString());
		
		addFileAndCommit(
				"file", 
				"first line\n"
				+ "nineth line\n", 
				"third commit");
		commitHash = git.getRepository().resolve("master").name();
		blames = new BlameCommand(git.getRepository().getDirectory())
				.commitHash(commitHash)
				.file("file")
				.call();
		
		commitHash = git.getRepository().resolve("master~1").name();
		
		assertEquals(1, blames.size());
		assertEquals(commitHash + ": 0-1", getBlock(blames, commitHash).toString());
	}

	private BlameBlock getBlock(Collection<BlameBlock> blameBlocks, String commitHash) {
		for (BlameBlock block: blameBlocks) {
			if (block.getCommit().getHash().equals(commitHash))
				return block;
		}
		return null;
	}
}
