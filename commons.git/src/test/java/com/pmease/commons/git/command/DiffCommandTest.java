package com.pmease.commons.git.command;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.util.List;

import org.junit.Test;

import com.pmease.commons.git.AbstractGitTest;
import com.pmease.commons.git.FileChangeWithDiffs;
import com.pmease.commons.git.Git;

public class DiffCommandTest extends AbstractGitTest {

	@Test
	public void shouldGenerateDiffCorrectly() {
	    addFile("readme", "readme");
	    createDir("dir");

	    addFile("dir/file1", 
	    		  "1st line of file1\n"
	    		+ "2nd line of file1\n"
	    		+ "3rd line of file1\n"
	    		+ "4th line of file1\n"
	    		+ "5th line of file1\n"
	    		+ "6th line of file1\n"
	    		+ "7th line of file1\n"
	    		+ "8th line of file1\n"
	    		+ "9th line of file1\n"
	    		+ "10th line of file1\n");
	 
	    addFile("dir/file2", "file2");
	    
	    commit("add readme, dir/file1 and dir/file2");
	    
	    git.checkout("head", "dev");

	    addFile("readme", "readme for dev");
	    addFile("dir/file1", 
	    		  "first line of file1\n"
	    		+ "2nd line of file1\n"
	    		+ "3rd line of file1\n"
	    		+ "4th line of file1\n"
	    		+ "5th line of file1\n"
	    		+ "6th line of file1\n"
	    		+ "7th line of file1\n"
	    		+ "some info here\n"
	    		+ "8th line of file1\n"
	    		+ "9th line of file1\n"
	    		+ "10th line of file1\n");
	    rm("dir/file2");
	    addFile("dir/file3", "file3");
	    
	    commit("modify readme, modify dir/file1, remove dir/file2, and add dir/file3");
	    
		Git bareGit = new Git(new File(tempDir, "bare"));
		bareGit.clone(git, true, false, false, null);
		
		List<FileChangeWithDiffs> fileChanges = bareGit.diff("master..dev", null, 3);
		assertEquals(fileChanges.size(), 4);
		
		fileChanges = bareGit.diff("master..dev", "dir", 3);
		assertEquals(fileChanges.size(), 3);
		
		for (FileChangeWithDiffs fileChange: fileChanges) {
			assertEquals(40, fileChange.getOldCommit().length());
			assertEquals(40, fileChange.getNewCommit().length());
			if (fileChange.getAction() == FileChangeWithDiffs.Action.MODIFY) {
				assertEquals(fileChange.getNewPath(), "dir/file1");
				assertEquals(fileChange.getDiffChunks().get(0).toString(), 
						  "@@ -1 +1 @@\n"
						+ "-1st line of file1\n"
						+ "+first line of file1\n" 
						+ " 2nd line of file1\n" 
						+ " 3rd line of file1\n" 
						+ " 4th line of file1\n" 
						+ " 5th line of file1\n"
						+ " 6th line of file1\n"
						+ " 7th line of file1\n"
						+ "+some info here\n" 
						+ " 8th line of file1\n"
						+ " 9th line of file1\n" 
						+ " 10th line of file1\n");
			}
		}
	}

	@Test
	public void shouldHandleRenameAndCopyCorrectly() {
		addFileAndCommit("a", "1111\n2222\n3333\n", "add a");
		
		addFile("a", "1111\n2222\n3333\n4444\n");
		addFile("a2", "1111\n2222\n3333\n4444\n");
		commit("copy a to a2");

		addFileAndCommit("b", "1111\n2222\n3333\n4444\n", "add b");

		rm("b");
		addFileAndCommit("b2", "1111\n2222\n3333\n4444\n", "move b to b2");

	    List<FileChangeWithDiffs> changes = git.diff("master~3..master", null, 4);
	    assertEquals("COPY\ta->a2", changes.get(1).toString());
	    assertEquals("COPY\ta->b2", changes.get(2).toString());
	    
	    changes = git.diff("master~1..master", null, 4);
	    assertEquals("RENAME\tb->b2", changes.get(0).toString());
	}

}
