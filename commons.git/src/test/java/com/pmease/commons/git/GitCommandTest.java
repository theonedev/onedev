package com.pmease.commons.git;

import java.io.File;

import org.junit.After;
import org.junit.Assume;
import org.junit.Before;

import com.pmease.commons.util.FileUtils;

public abstract class GitCommandTest {

	protected File repoDir;
	
	@Before
	public void before() {
		Assume.assumeTrue(Git.checkError() == null);
		
		repoDir = FileUtils.createTempDir();
	}
	
	@After
	public void after() {
		if (repoDir != null)
			FileUtils.deleteDir(repoDir);
	}

}
