package com.gitplex.server.git.command;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import com.gitplex.server.git.AbstractGitTest;
import com.google.common.collect.Lists;

public class LogCommandTest extends AbstractGitTest {

	@Test
	public void test() throws Exception {
		addFile("测试文件", ""
				+ "1st line\n"
				+ "2nd line\n"
				+ "3rd line\n"
				+ "4th line\n"
				+ "5th line\n"
				+ "6th line\n"
				+ "7th line\n"
				+ "8th line\n"
				+ "9th line\n");
		addFile("另一个 测试文件", ""
				+ "1st line\n"
				+ "2nd line\n"
				+ "3rd line\n"
				+ "4th line\n"
				+ "5th line\n"
				+ "6th line\n"
				+ "7th line\n"
				+ "8th line\n"
				+ "9th line\n");
		commit("初始commit");
		rm("测试文件");
		addFile("改名后 测试文件", ""
				+ "1st line\n"
				+ "2nd line\n"
				+ "3rd line\n"
				+ "4th line\n"
				+ "5th line\n"
				+ "6th line\n"
				+ "7th line\n"
				+ "8th line\n"
				+ "9th line\n");
		addFile("另一个 测试文件", ""
				+ "1st line\n"
				+ "2nd line\n"
				+ "3rd line\n"
				+ "4th line\n"
				+ "6th line\n"
				+ "7th line\n"
				+ "8th line\n"
				+ "9th line\n");
		addFile("第三个 测试文件", ""
				+ "1st line\n"
				+ "2nd line\n"
				+ "3rd line\n"
				+ "4th line\n"
				+ "6th line\n"
				+ "7th line\n"
				+ "8th line\n"
				+ "9th line\n");
		commit("第二个commit");
		
		List<GitCommit> commits = new ArrayList<>();
		
		new LogCommand(git.getRepository().getDirectory()) {

			@Override
			protected void consume(GitCommit commit) {
				commits.add(commit);
			}
			
		}.revisions(Lists.newArrayList("master")).call();
		
		assertEquals(2, commits.size());

		GitCommit commit = commits.get(0);
		
		assertEquals(3, commit.getFileChanges().size());

		assertEquals("另一个 测试文件", commit.getFileChanges().get(0).getPath());
		assertEquals(0, commit.getFileChanges().get(0).getAdditions());
		assertEquals(1, commit.getFileChanges().get(0).getDeletions());
		
		assertEquals("改名后 测试文件", commit.getFileChanges().get(1).getPath());
		assertEquals("测试文件", commit.getFileChanges().get(1).getOldPath());
		
		assertEquals("第三个 测试文件", commit.getFileChanges().get(2).getPath());
		assertEquals(8, commit.getFileChanges().get(2).getAdditions());
		assertEquals(0, commit.getFileChanges().get(2).getDeletions());
	}

}