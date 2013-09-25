package com.pmease.commons.git;

import java.io.File;
import java.util.Collection;

import org.junit.Assert;
import org.junit.Test;

import com.google.common.collect.Sets;
import com.pmease.commons.util.FileUtils;

public class ListChangedFilesCommandTest extends GitCommandTest {

	@Test
	public void shouldListChangedFiles() {
		Git git = new Git(repoDir).init().call();
		
		FileUtils.touchFile(new File(repoDir, "a"));
		git.add().addPath("a").call();
		git.commit().message("commit").call();
		
		FileUtils.touchFile(new File(repoDir, "b"));
		git.add().addPath("b").call();
		git.commit().message("commit").call();
		
		FileUtils.touchFile(new File(repoDir, "c"));
		git.add().addPath("c").call();
		git.commit().message("commit").call();
		
		FileUtils.touchFile(new File(repoDir, "d"));
		git.add().addPath("d").call();
		git.commit().message("commit").call();
		
		FileUtils.writeFile(new File(repoDir, "a"), "a");
		git.add().addPath("a").call();
		git.commit().message("commit").call();
		
		Collection<String> changedFiles = git.findChangedFiles().fromRev("HEAD~4").toRev("HEAD").call();
		
		Assert.assertTrue(changedFiles.containsAll(Sets.newHashSet("a", "b", "c", "d")));
	}

}
