package com.pmease.commons.git;

import static org.junit.Assert.assertEquals;

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
