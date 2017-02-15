package com.gitplex.server.git.command;

import static org.junit.Assert.assertEquals;

import java.util.Map;

import org.junit.Test;

import com.gitplex.server.git.AbstractGitTest;
import com.gitplex.server.git.Blame;
import com.gitplex.server.git.command.BlameCommand;

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
		Map<String, Blame> blames = new BlameCommand(git.getRepository().getDirectory())
				.commitHash(commitHash)
				.file("file")
				.call();
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
		
		commitHash = git.getRepository().resolve("master").name();
		blames = new BlameCommand(git.getRepository().getDirectory())
				.commitHash(commitHash)
				.file("file")
				.call();
		assertEquals(2, blames.size());
		
		assertEquals(commitHash + ": 0-1, 8-9", blames.get(commitHash).toString());
		commitHash = git.getRepository().resolve("master~1").name();
		assertEquals(commitHash + ": 1-8", blames.get(commitHash).toString());
		
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
		assertEquals(commitHash + ": 0-2", blames.get(commitHash).toString());
	}

}
