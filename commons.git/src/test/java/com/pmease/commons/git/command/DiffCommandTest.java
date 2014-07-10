package com.pmease.commons.git.command;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.util.List;

import org.junit.Test;

import com.pmease.commons.git.AbstractGitTest;
import com.pmease.commons.git.FileChangeWithDiffs;
import com.pmease.commons.git.Git;
import com.pmease.commons.util.FileUtils;

public class DiffCommandTest extends AbstractGitTest {

	@Test
	public void shouldGenerateDiffCorrectly() {
	    File tempDir = FileUtils.createTempDir();
	    
	    try {
		    Git workGit = new Git(new File(tempDir, "work"));
		    workGit.init(false);

		    File readme = new File(workGit.repoDir(), "readme");
		    FileUtils.writeFile(readme, "readme");
		    workGit.add("readme");
		    
		    File dir = new File(workGit.repoDir(), "dir");
		    FileUtils.createDir(dir);
		    File file1 = new File(dir, "file1");
		    FileUtils.writeFile(file1, 
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
		    
		    File file2 = new File(dir, "file2");
		    FileUtils.writeFile(file2, "file2");
		    
		    workGit.add("dir").commit("add readme, dir/file1 and dir/file2", false, false);
		    
		    workGit.checkout("head", "dev");

		    FileUtils.writeFile(readme, "reame for dev");
		    workGit.add("readme");
		    
		    FileUtils.writeFile(file1, 
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
		    
		    workGit.add("dir/file1");
		    
		    workGit.remove("dir/file2");
	        
		    File file3 = new File(dir, "file3");
		    FileUtils.writeFile(file3, "file3");
		    
		    workGit.add("dir/file3");
		    
		    workGit.commit("modify readme, modify dir/file1, remove dir/file2, and add dir/file3", false, false);
		    
    		Git bareGit = new Git(new File(tempDir, "bare"));
    		bareGit.clone(workGit.repoDir().getAbsolutePath(), true, false, false, null);
    		
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

	    } finally {
	        FileUtils.deleteDir(tempDir);
	    }
	}

	@Test
	public void shouldHandleRenameAndCopyCorrectly() {
	    File tempDir = FileUtils.createTempDir();
	    
	    try {
		    Git git = new Git(tempDir);
		    git.init(false);

		    FileUtils.writeFile(new File(tempDir, "a"), "1111\n2222\n3333\n");
		    git.add("a");
		    git.commit("add a", false, false);
		    
		    FileUtils.writeFile(new File(tempDir, "a"), "1111\n2222\n3333\n4444\n");
		    FileUtils.writeFile(new File(tempDir, "a2"), "1111\n2222\n3333\n4444\n");
		    git.add("a", "a2");
		    git.commit("copy a to a2", false, false);
		    
		    FileUtils.writeFile(new File(tempDir, "b"), "1111\n2222\n3333\n4444\n");
		    git.add("b");
		    git.commit("add b", false, false);

		    FileUtils.writeFile(new File(tempDir, "b2"), "1111\n2222\n3333\n4444\n");
		    git.remove("b");
		    git.add("b2");
		    git.commit("move b to b2", false, false);

		    List<FileChangeWithDiffs> changes = git.diff("master~3..master", null, 4);
		    assertEquals("COPY\ta->a2", changes.get(1).toString());
		    assertEquals("COPY\ta->b2", changes.get(2).toString());
		    
		    changes = git.diff("master~1..master", null, 4);
		    assertEquals("RENAME\tb->b2", changes.get(0).toString());
		    
	    } finally {
	        FileUtils.deleteDir(tempDir);
	    }
	}

}
