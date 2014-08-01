package com.pmease.commons.git.command;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import org.junit.Test;

import com.pmease.commons.git.AbstractGitTest;
import com.pmease.commons.git.Git;
import com.pmease.commons.util.FileUtils;

public class ListSubModulesCommandTest extends AbstractGitTest {

	@Test
	public void test() throws IOException {
		Git module1 = new Git(new File(tempDir, "module1"));
		Git module2 = new Git(new File(tempDir, "module2"));
		
		module1.init(false);
		module2.init(false);
		
		addFileAndCommit("readme", "readme", "initial commit");
		
		FileUtils.writeFile(new File(module1.repoDir(), "readme"), "readme");
		module1.add("readme").commit("initial commit", false, false);
		FileUtils.writeFile(new File(module2.repoDir(), "readme"), "readme");
		module2.add("readme").commit("initial commit", false, false);
		
		git.addSubModule(module1.repoDir().getAbsolutePath(), "module1");
		git.commit("add submodule1", false, false);
		
		Map<String, String> subModules = git.listSubModules("master");
		assertEquals(module1.repoDir().getCanonicalPath(), 
				new File(subModules.get("module1")).getCanonicalPath());

		createDir("dir");
		
		git.addSubModule(module2.repoDir().getAbsolutePath(), "dir/module2");
		commit("add submodule2");
		subModules = git.listSubModules("master");
		assertEquals(module1.repoDir().getCanonicalPath(), 
				new File(subModules.get("module1")).getCanonicalPath());
		assertEquals(module2.repoDir().getCanonicalPath(), 
				new File(subModules.get("dir/module2")).getCanonicalPath());
	}

}
