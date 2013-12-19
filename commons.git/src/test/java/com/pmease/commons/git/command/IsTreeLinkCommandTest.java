package com.pmease.commons.git.command;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import org.junit.Assert;
import org.junit.Test;

import com.pmease.commons.git.AbstractGitTest;
import com.pmease.commons.git.Git;
import com.pmease.commons.util.ClassUtils;
import com.pmease.commons.util.FileUtils;
import com.pmease.commons.util.ZipUtils;

public class IsTreeLinkCommandTest extends AbstractGitTest {

	@Test
	public void test() throws IOException {
	    File tempDir = FileUtils.createTempDir();
	    try {
	    	// Use a pre-built repo to test symbol link behaviors as Windows does not 
	    	// support symbol links.
		    try (InputStream is = ClassUtils.getResourceAsStream(IsTreeLinkCommandTest.class, "symlink_repo.zip");) {
		    	ZipUtils.unzip(is, tempDir, null);
		    	
		    	Git git = new Git(tempDir);
		    	Assert.assertFalse(git.isTreeLink("file_link", "master"));
		    	Assert.assertFalse(git.isTreeLink("link_link", "master"));
		    	Assert.assertFalse(git.isTreeLink("external_link", "master"));
		    	Assert.assertTrue(git.isTreeLink("dir_link", "master"));
		    }
	    } finally {
	    	FileUtils.deleteDir(tempDir);
	    }
	}

}
