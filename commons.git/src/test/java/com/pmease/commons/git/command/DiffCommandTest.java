package com.pmease.commons.git.command;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.List;

import org.junit.Test;

import com.pmease.commons.git.FileChangeWithDiffs;
import com.pmease.commons.git.Git;
import com.pmease.commons.util.FileUtils;

public class DiffCommandTest {

	@Test
	public void shouldGenerateDiffCorrectly() {
	    assertTrue(GitCommand.checkError() == null);
	    
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
		    
		    workGit.checkout("dev", true);

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
    		bareGit.clone(workGit.repoDir().getAbsolutePath(), true);
    		
    		List<FileChangeWithDiffs> fileChanges = bareGit.diff("master", "dev", null, 3);
    		assertEquals(fileChanges.size(), 4);
    		
    		fileChanges = bareGit.diff("master", "dev", "dir", 3);
    		assertEquals(fileChanges.size(), 3);
    		
    		for (FileChangeWithDiffs fileChange: fileChanges) {
    			assertEquals(40, fileChange.getCommitHash1().length());
    			assertEquals(40, fileChange.getCommitHash2().length());
    			if (fileChange.getAction() == FileChangeWithDiffs.Action.MODIFY) {
    				assertEquals(fileChange.getPath(), "dir/file1");
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

}
