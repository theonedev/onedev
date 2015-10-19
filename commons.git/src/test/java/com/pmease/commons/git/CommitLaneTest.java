package com.pmease.commons.git;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.pmease.commons.git.command.LogCommand;
import com.pmease.commons.git.command.LogCommand.Order;
import com.pmease.commons.git.command.MergeCommand.FastForwardMode;

public class CommitLaneTest extends AbstractGitTest {

	@Test
	public void testLinear() {
		addFileAndCommit("file1", "", "file1");
		addFileAndCommit("file2", "", "file2");
		addFileAndCommit("file3", "", "file3");
		
		LogCommand logCommand = new LogCommand(git.repoDir()).order(Order.DATE);
		CommitLane lane = new CommitLane(logCommand.call(), Integer.MAX_VALUE);
		
		assertEquals(3, lane.getRows().size());
		assertEquals((Integer)0, lane.getRows().get(0).get(new CommitLane.Line(0, 0)));
		assertEquals((Integer)0, lane.getRows().get(1).get(new CommitLane.Line(1, 1)));
		assertEquals((Integer)0, lane.getRows().get(2).get(new CommitLane.Line(2, 2)));
		
		lane = new CommitLane(logCommand.call(), 1);
		
		assertEquals(3, lane.getRows().size());
		assertEquals((Integer)0, lane.getRows().get(0).get(new CommitLane.Line(0, 0)));
		assertEquals((Integer)0, lane.getRows().get(1).get(new CommitLane.Line(1, 1)));
		assertEquals((Integer)0, lane.getRows().get(2).get(new CommitLane.Line(2, 2)));
	}

	@Test
	public void testNonFastForwardMerge() {
		addFileAndCommit("file1", "", "file1");
		git.checkout("master", "dev");
		addFileAndCommit("file2", "", "file2");
		git.checkout("master", null);
		git.merge("dev", FastForwardMode.NO_FF, null, null, null);
		
		LogCommand logCommand = new LogCommand(git.repoDir()).order(Order.DATE);
		CommitLane lane = new CommitLane(logCommand.call(), Integer.MAX_VALUE);
		
		assertEquals(3, lane.getRows().size());
		assertEquals((Integer)0, lane.getRows().get(0).get(new CommitLane.Line(0, 0)));
		assertEquals((Integer)0, lane.getRows().get(1).get(new CommitLane.Line(0, 2)));
		assertEquals((Integer)1, lane.getRows().get(1).get(new CommitLane.Line(1, 1)));
		assertEquals((Integer)0, lane.getRows().get(2).get(new CommitLane.Line(2, 2)));
		
		lane = new CommitLane(logCommand.call(), 1);
		
		assertEquals(3, lane.getRows().size());
		assertEquals((Integer)0, lane.getRows().get(0).get(new CommitLane.Line(0, 0)));
		assertEquals((Integer)0, lane.getRows().get(1).get(new CommitLane.Line(0, 2)));
		assertEquals((Integer)1, lane.getRows().get(1).get(new CommitLane.Line(1, 1)));
		assertEquals((Integer)0, lane.getRows().get(2).get(new CommitLane.Line(2, 2)));
	}
	
	@Test
	public void testMerge() {
		addFileAndCommit("file1", "", "file1");
		addFileAndCommit("file2", "", "file2");
		git.checkout("master~1", "dev");
		addFileAndCommit("file3", "", "file3");
		git.merge("master", null, null, null, null);
		
		LogCommand logCommand = new LogCommand(git.repoDir()).order(Order.DATE);
		CommitLane lane = new CommitLane(logCommand.call(), Integer.MAX_VALUE);
		
		assertEquals(4, lane.getRows().size());
		assertEquals((Integer)0, lane.getRows().get(0).get(new CommitLane.Line(0, 0)));
		assertEquals((Integer)0, lane.getRows().get(1).get(new CommitLane.Line(1, 1)));
		assertEquals((Integer)1, lane.getRows().get(1).get(new CommitLane.Line(0, 2)));
		assertEquals((Integer)0, lane.getRows().get(2).get(new CommitLane.Line(1, 3)));
		assertEquals((Integer)1, lane.getRows().get(2).get(new CommitLane.Line(2, 2)));
		assertEquals((Integer)0, lane.getRows().get(3).get(new CommitLane.Line(3, 3)));
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
		
		assertEquals(4, lane.getRows().size());
		assertEquals((Integer)0, lane.getRows().get(0).get(new CommitLane.Line(0, 0)));
		assertEquals((Integer)0, lane.getRows().get(1).get(new CommitLane.Line(0, 2)));
		assertEquals((Integer)1, lane.getRows().get(1).get(new CommitLane.Line(1, 1)));
		assertEquals((Integer)0, lane.getRows().get(2).get(new CommitLane.Line(2, 2)));
		assertEquals((Integer)0, lane.getRows().get(3).get(new CommitLane.Line(3, 3)));
	}
	
}
