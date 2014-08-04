package com.pmease.commons.git.command;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.apache.commons.lang3.SystemUtils;
import org.junit.Test;

import com.pmease.commons.git.AbstractGitTest;
import com.pmease.commons.git.FileChange;

public class ListFileChangesCommandTest extends AbstractGitTest {

	@Test
	public void test() {
		addFileAndCommit("initial", "", "0");
		addFileAndCommit("file name with space", "hello world", "1");
		removeFileAndCommit("file name with space", "2");
		addFileAndCommit("another file name with space", "hello world", "3");
		
		List<FileChange> changes = git.listFileChanges("master~3", "master~2", null, true);
		assertEquals(1, changes.size());
		assertEquals("file name with space", changes.get(0).getNewPath());
		
		changes = git.listFileChanges("master~2", "master", null, true);
		assertEquals(1, changes.size());
		assertEquals(FileChange.Status.RENAME, changes.get(0).getStatus());
		assertEquals("file name with space", changes.get(0).getOldPath());
		assertEquals("another file name with space", changes.get(0).getNewPath());
		
		if (!SystemUtils.IS_OS_WINDOWS) {
			addFileAndCommit("file name with \"quote\" ", "hello world", "1");
			rm("file name with \"quote\" ");
			addFile(" another file name with \"quote\" ", "hello world");
			commit("2");
			
			changes = git.listFileChanges("master~1", "master", null, true);
			assertEquals(1, changes.size());
			assertEquals(FileChange.Status.RENAME, changes.get(0).getStatus());
			assertEquals("file name with \"quote\" ", changes.get(0).getOldPath());
			assertEquals(" another file name with \"quote\" ", changes.get(0).getNewPath());
		}
		
	}
}
