package com.turbodev.server.git.command;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import com.turbodev.server.git.AbstractGitTest;
import com.turbodev.server.git.command.LogCommand;
import com.turbodev.server.git.command.LogCommit;
import com.turbodev.server.git.command.FileChange.Action;

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
		
		List<LogCommit> commits = new ArrayList<>();
		
		new LogCommand(git.getRepository().getDirectory()) {

			@Override
			protected void consume(LogCommit commit) {
				commits.add(commit);
			}
			
		}.call();
		
		assertEquals(2, commits.size());
		
		assertEquals(3, commits.get(0).getFileChanges().size());
		assertEquals(Action.MODIFY, commits.get(0).getFileChanges().get(0).getAction());
		assertEquals("另一个 测试文件", commits.get(0).getFileChanges().get(0).getNewPath());
		assertEquals(Action.RENAME, commits.get(0).getFileChanges().get(1).getAction());
		assertEquals("改名后 测试文件", commits.get(0).getFileChanges().get(1).getNewPath());
		assertEquals(Action.ADD, commits.get(0).getFileChanges().get(2).getAction());
		assertEquals("第三个 测试文件", commits.get(0).getFileChanges().get(2).getNewPath());
		
	}

}