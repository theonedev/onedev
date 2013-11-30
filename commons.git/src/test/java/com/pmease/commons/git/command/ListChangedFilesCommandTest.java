package com.pmease.commons.git.command;

import java.io.File;
import java.util.Collection;

import org.junit.Assert;
import org.junit.Test;

import com.google.common.collect.Sets;
import com.pmease.commons.git.Git;
import com.pmease.commons.git.command.GitCommand;
import com.pmease.commons.util.FileUtils;

public class ListChangedFilesCommandTest {

	@Test
	public void shouldListChangedFiles() {
	    Assert.assertTrue(GitCommand.checkError() == null);
	    Git git = new Git(FileUtils.createTempDir());
	    git.init(false);
	        
	    try {
    		FileUtils.touchFile(new File(git.repoDir(), "a"));
    		git.add("a");
    		git.commit("commit", false, false);
    		
    		FileUtils.touchFile(new File(git.repoDir(), "b"));
    		git.add("b");
    		git.commit("commit", false, false);
    		
    		FileUtils.touchFile(new File(git.repoDir(), "c"));
    		git.add("c");
    		git.commit("commit", false, false);
    		
    		FileUtils.touchFile(new File(git.repoDir(), "d"));
    		git.add("d");
    		git.commit("commit", false, false);
    		
    		FileUtils.writeFile(new File(git.repoDir(), "a"), "a");
    		git.add("a");
    		git.commit("commit", false, false);
    		
    		Collection<String> changedFiles = git.listChangedFiles("HEAD~4", "HEAD");
    		
    		Assert.assertTrue(changedFiles.containsAll(Sets.newHashSet("a", "b", "c", "d")));
	    } finally {
	        FileUtils.deleteDir(git.repoDir());
	    }
	}

}
