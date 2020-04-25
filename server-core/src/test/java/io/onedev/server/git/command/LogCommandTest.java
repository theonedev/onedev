package io.onedev.server.git.command;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import org.junit.Test;

import com.google.common.collect.Lists;

import io.onedev.server.git.AbstractGitTest;

public class LogCommandTest extends AbstractGitTest {

	@Test
	public void test() throws Exception {
		addFile("testfile", ""
				+ "1st line\n"
				+ "2nd line\n"
				+ "3rd line\n"
				+ "4th line\n"
				+ "5th line\n"
				+ "6th line\n"
				+ "7th line\n"
				+ "8th line\n"
				+ "9th line\n");
		addFile("another testfile", ""
				+ "1st line\n"
				+ "2nd line\n"
				+ "3rd line\n"
				+ "4th line\n"
				+ "5th line\n"
				+ "6th line\n"
				+ "7th line\n"
				+ "8th line\n"
				+ "9th line\n");
		commit("initial commit\n\nfirst line\nsecond line");
		rm("testfile");
		addFile("renamed testfile", ""
				+ "1st line\n"
				+ "2nd line\n"
				+ "3rd line\n"
				+ "4th line\n"
				+ "5th line\n"
				+ "6th line\n"
				+ "7th line\n"
				+ "8th line\n"
				+ "9th line\n");
		addFile("another testfile", ""
				+ "1st line\n"
				+ "2nd line\n"
				+ "3rd line\n"
				+ "4th line\n"
				+ "6th line\n"
				+ "7th line\n"
				+ "8th line\n"
				+ "9th line\n");
		addFile("3rd testfile", ""
				+ "1st line\n"
				+ "2nd line\n"
				+ "3rd line\n"
				+ "4th line\n"
				+ "6th line\n"
				+ "7th line\n"
				+ "8th line\n"
				+ "9th line\n");
		commit("second commit\n\nfirst line\n\nsecond line\nthird line\n");
		
		List<GitCommit> commits = new ArrayList<>();

		EnumSet<LogCommand.Field> fields = EnumSet.allOf(LogCommand.Field.class);
		fields.remove(LogCommand.Field.LINE_CHANGES);
		new LogCommand(git.getRepository().getDirectory()) {

			@Override
			protected void consume(GitCommit commit) {
				commits.add(commit);
			}
			
		}.revisions(Lists.newArrayList("master")).fields(fields).call();
		
		assertEquals(2, commits.size());

		GitCommit commit1 = commits.get(1);
		assertEquals("initial commit", commit1.getSubject());
		assertEquals("first line\nsecond line", commit1.getBody());
		
		GitCommit commit2 = commits.get(0);
		assertEquals(3, commit2.getFileChanges().size());
		assertEquals("3rd testfile", commit2.getFileChanges().get(0).getNewPath());
		assertEquals("another testfile", commit2.getFileChanges().get(1).getNewPath());
		assertEquals("another testfile", commit2.getFileChanges().get(1).getOldPath());
		assertEquals("renamed testfile", commit2.getFileChanges().get(2).getNewPath());
		assertEquals("second commit", commit2.getSubject());
		assertEquals("first line\n\nsecond line\nthird line", commit2.getBody());

		commits.clear();
		fields = EnumSet.allOf(LogCommand.Field.class);
		new LogCommand(git.getRepository().getDirectory()) {

			@Override
			protected void consume(GitCommit commit) {
				commits.add(commit);
			}
			
		}.revisions(Lists.newArrayList("master")).fields(fields).call();
		
		GitCommit commit = commits.get(0);
		
		assertEquals(3, commit.getFileChanges().size());
		assertEquals("3rd testfile", commit.getFileChanges().get(0).getNewPath());
		assertEquals(8, commit.getFileChanges().get(0).getAdditions());
		assertEquals(0, commit.getFileChanges().get(0).getDeletions());
		
		assertEquals("another testfile", commit.getFileChanges().get(1).getNewPath());
		assertEquals(null, commit.getFileChanges().get(1).getOldPath());
		
		assertEquals("renamed testfile", commit.getFileChanges().get(2).getNewPath());
		assertEquals(0, commit.getFileChanges().get(2).getAdditions());
		assertEquals(0, commit.getFileChanges().get(2).getDeletions());		
	}

}