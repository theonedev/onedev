package com.pmease.commons.git;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import org.junit.Test;

import com.pmease.commons.git.command.LogCommand;
import com.pmease.commons.git.command.LogCommand.Order;
import com.pmease.commons.git.command.MergeCommand.FastForwardMode;

public class CommitLaneTest extends AbstractGitTest {

	private void assertLine(CommitLane lane, int row, int child, int parent, int column) {
		assertEquals((Integer)column, lane.getRows().get(row).get(new CommitLane.Line(child, parent)));
	}
	
	@Test
	public void testLinear() {
		addFileAndCommit("file1", "", "file1");
		addFileAndCommit("file2", "", "file2");
		addFileAndCommit("file3", "", "file3");
		
		LogCommand logCommand = new LogCommand(git.repoDir()).order(Order.DATE);
		CommitLane lane = new CommitLane(logCommand.call(), Integer.MAX_VALUE);
		
		assertEquals(3, lane.getRows().size());
		assertLine(lane, 0, 0, 0, 0);
		assertLine(lane, 1, 1, 1, 0);
		assertLine(lane, 2, 2, 2, 0);
		
		lane = new CommitLane(logCommand.call(), 1);
		
		assertLine(lane, 0, 0, 0, 0);
		assertLine(lane, 1, 1, 1, 0);
		assertLine(lane, 2, 2, 2, 0);
	}

	@Test
	public void testNonFastForwardMerge() {
		addFileAndCommit("file1", "", "file1");
		git.checkout("master", "dev");
		sleep();
		addFileAndCommit("file2", "", "file2");
		git.checkout("master", null);
		git.merge("dev", FastForwardMode.NO_FF, null, null, null);
		
		LogCommand logCommand = new LogCommand(git.repoDir()).order(Order.DATE);
		CommitLane lane = new CommitLane(logCommand.call(), Integer.MAX_VALUE);
		
		assertLine(lane, 0, 0, 0, 0);
		assertLine(lane, 1, 0, 2, 0);
		assertLine(lane, 1, 1, 1, 1);
		assertLine(lane, 2, 2, 2, 0);
		
		lane = new CommitLane(logCommand.call(), 1);
		
		assertLine(lane, 0, 0, 0, 0);
		assertLine(lane, 1, 0, 2, 0);
		assertLine(lane, 1, 1, 1, 1);
		assertLine(lane, 2, 2, 2, 0);
	}
	
	private void sleep() {
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
	}
	
	@Test
	public void testMerge() {
		addFileAndCommit("file1", "", "file1");
		addFileAndCommit("file2", "", "file2");
		git.checkout("master~1", "dev");
		sleep();
		addFileAndCommit("file3", "", "file3");
		git.checkout("master", null);
		git.merge("dev", null, null, null, null);
		
		LogCommand logCommand = new LogCommand(git.repoDir()).order(Order.DATE);
		CommitLane lane = new CommitLane(logCommand.call(), Integer.MAX_VALUE);
		
		assertLine(lane, 0, 0, 0, 0);
		assertLine(lane, 1, 0, 2, 0);
		assertLine(lane, 1, 1, 1, 1);
		assertLine(lane, 2, 2, 2, 0);
		assertLine(lane, 2, 1, 3, 1);
		assertLine(lane, 3, 3, 3, 0);
		
		lane = new CommitLane(logCommand.call(), 1);
		
		assertLine(lane, 0, 0, 0, 0);
		assertLine(lane, 1, 0, 2, 0);
		assertLine(lane, 1, 1, 1, 1);
		assertLine(lane, 2, 1, 3, 0);
		assertLine(lane, 2, 2, 2, 1);
		assertLine(lane, 3, 3, 3, 0);
	}
	
	@Test
	public void testMultiHeads() {
		addFileAndCommit("file1", "", "file1");
		addFileAndCommit("file2", "", "file2");
		addFileAndCommit("file3", "", "file3");
		git.checkout("master~1", "dev");
		addFileAndCommit("file4", "", "file4");
		
		LogCommand logCommand = new LogCommand(git.repoDir()).order(Order.DATE);
		logCommand.allBranchesAndTags(true);
		CommitLane lane = new CommitLane(logCommand.call(), Integer.MAX_VALUE);
		
		assertLine(lane, 0, 0, 0, 0);
		assertLine(lane, 1, 0, 2, 0);
		assertLine(lane, 1, 1, 1, 1);
		assertLine(lane, 2, 2, 2, 0);
		assertLine(lane, 3, 3, 3, 0);
	}
	
	@Test
	public void test() {
		LogCommand log = new LogCommand(new File("w:\\temp\\gitplex_storage\\repositories\\1"));
		log.maxCount(550);
		log.allBranchesAndTags(true);
		
		List<Commit> commits = log.call();
		final Map<String, Long> hash2index = new HashMap<>();
		Map<String, Commit> hash2commit = new HashMap<>();
		
		for (int i=0; i<commits.size(); i++) {
			Commit commit = commits.get(i);
			hash2index.put(commit.getHash(), 1L*i*commits.size());
			hash2commit.put(commit.getHash(), commit);
		}

		Stack<Commit> stack = new Stack<>();
		
		for (int i=commits.size()-1; i>=0; i--)
			stack.push(commits.get(i));

		// commits are nearly ordered, so this should be fast
		while (!stack.isEmpty()) {
			Commit commit = stack.pop();
			long commitIndex = hash2index.get(commit.getHash());
			int count = 1;
			for (String parentHash: commit.getParentHashes()) {
				Long parentIndex = hash2index.get(parentHash);
				if (parentIndex != null && parentIndex.longValue()<commitIndex) {
					stack.push(hash2commit.get(parentHash));
					hash2index.put(parentHash, commitIndex+(count++));
				}
			}
		}
		
		Collections.sort(commits, new Comparator<Commit>() {

			@Override
			public int compare(Commit o1, Commit o2) {
				long value = hash2index.get(o1.getHash()) - hash2index.get(o2.getHash());
				if (value < 0)
					return -1;
				else if (value > 0)
					return 1;
				else
					return 0;
			}
			
		});
		
		new CommitLane(commits, 20);
	}
	
	@Test
	public void testCompexGraph() {
		addFileAndCommit("i", "", "i");
		git.checkout("master", "dev2");
		addFileAndCommit("h", "", "h");
		git.checkout("master", null);
		git.merge("dev2", FastForwardMode.NO_FF, null, null, "g");
		git.checkout("master~1", "dev3");
		sleep();
		addFileAndCommit("f", null, "f");
		git.checkout("master~1", "dev1");
		sleep();
		addFileAndCommit("e", null, "e");
		git.merge("dev2", FastForwardMode.NO_FF, null, null, "d");
		git.merge("dev3", null, null, null, "c");
		git.checkout("master", null);
		sleep();
		addFileAndCommit("b", "", "b");
		git.merge("dev1", null, null, null, "a");
		
		LogCommand logCommand = new LogCommand(git.repoDir()).order(Order.DATE);
		CommitLane lane = new CommitLane(logCommand.call(), Integer.MAX_VALUE);
		assertLine(lane, 0, 0, 0, 0);
		assertLine(lane, 1, 1, 1, 0);
		assertLine(lane, 1, 0, 2, 1);
		assertLine(lane, 2, 1, 6, 0);
		assertLine(lane, 2, 2, 2, 1);
		assertLine(lane, 3, 1, 6, 0);
		assertLine(lane, 3, 3, 3, 1);
		assertLine(lane, 3, 2, 5, 2);
		assertLine(lane, 4, 1, 6, 0);
		assertLine(lane, 4, 4, 4, 1);
		assertLine(lane, 4, 3, 7, 2);
		assertLine(lane, 4, 2, 5, 3);
		assertLine(lane, 5, 1, 6, 0);
		assertLine(lane, 5, 4, 8, 1);
		assertLine(lane, 5, 3, 7, 2);
		assertLine(lane, 5, 5, 5, 3);
		assertLine(lane, 6, 6, 6, 0);
		assertLine(lane, 6, 4, 8, 1);
		assertLine(lane, 6, 3, 7, 2);
		assertLine(lane, 6, 5, 8, 3);
		assertLine(lane, 7, 6, 8, 0);
		assertLine(lane, 7, 7, 7, 1);
		assertLine(lane, 7, 4, 8, 2);
		assertLine(lane, 7, 5, 8, 3);
		assertLine(lane, 8, 8, 8, 0);
		
		lane = new CommitLane(logCommand.call(), 3);
		assertLine(lane, 0, 0, 0, 0);
		assertLine(lane, 1, 1, 1, 0);
		assertLine(lane, 1, 0, 2, 1);
		assertLine(lane, 2, 1, 6, 0);
		assertLine(lane, 2, 2, 2, 1);
		assertLine(lane, 3, 1, 6, 0);
		assertLine(lane, 3, 3, 3, 1);
		assertLine(lane, 3, 2, 5, 2);
		assertLine(lane, 4, 1, 6, 0);
		assertLine(lane, 4, 4, 4, 1);
		assertLine(lane, 4, 3, -7, 2);
		assertLine(lane, 4, 2, 5, 3);
		assertLine(lane, 5, 1, 6, 0);
		assertLine(lane, 5, 4, 8, 1);
		assertLine(lane, 5, 5, 5, 2);
		assertLine(lane, 6, 6, 6, 0);
		assertLine(lane, 6, 3, -7, 1);
		assertLine(lane, 6, 4, 8, 2);
		assertLine(lane, 6, 5, 8, 3);
		assertLine(lane, 7, 6, 8, 0);
		assertLine(lane, 7, 7, 7, 1);
		assertLine(lane, 7, 4, 8, 2);
		assertLine(lane, 7, 5, 8, 3);
		assertLine(lane, 8, 8, 8, 0);
	}
	
}
