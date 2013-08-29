package com.pmease.commons.git;

import java.io.File;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import com.pmease.commons.util.FileUtils;
import com.pmease.commons.util.StringUtils;

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
		
		List<String> changedFiles = git.listChangedFiles().fromRev("HEAD~4").toRev("HEAD").call();
		
		Assert.assertEquals(StringUtils.join(changedFiles, ","), "a,b,c,d");
	}

}
