package com.pmease.commons.git;

import java.io.File;
import java.util.Collection;

import org.junit.Assert;
import org.junit.Test;

import com.google.common.collect.Sets;
import com.pmease.commons.util.FileUtils;

public class ListChangedFilesCommandTest {

	@Test
	public void shouldListChangedFiles() {
	    Assert.assertTrue(Git.checkError() == null);
	    Git git = new Git(FileUtils.createTempDir()).init().call();
	        
	    try {
    		FileUtils.touchFile(new File(git.repoDir(), "a"));
    		git.add().addPath("a").call();
    		git.commit().message("commit").call();
    		
    		FileUtils.touchFile(new File(git.repoDir(), "b"));
    		git.add().addPath("b").call();
    		git.commit().message("commit").call();
    		
    		FileUtils.touchFile(new File(git.repoDir(), "c"));
    		git.add().addPath("c").call();
    		git.commit().message("commit").call();
    		
    		FileUtils.touchFile(new File(git.repoDir(), "d"));
    		git.add().addPath("d").call();
    		git.commit().message("commit").call();
    		
    		FileUtils.writeFile(new File(git.repoDir(), "a"), "a");
    		git.add().addPath("a").call();
    		git.commit().message("commit").call();
    		
    		Collection<String> changedFiles = git.findChangedFiles().fromRev("HEAD~4").toRev("HEAD").call();
    		
    		Assert.assertTrue(changedFiles.containsAll(Sets.newHashSet("a", "b", "c", "d")));
	    } finally {
	        FileUtils.deleteDir(git.repoDir());
	    }
	}

}
