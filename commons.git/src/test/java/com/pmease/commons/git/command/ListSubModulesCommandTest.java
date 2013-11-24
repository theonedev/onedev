package com.pmease.commons.git.command;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

import com.pmease.commons.git.Git;
import com.pmease.commons.util.FileUtils;

public class ListSubModulesCommandTest {

	@Test
	public void test() throws IOException {
	    Assert.assertTrue(GitCommand.checkError() == null);
		File tempDir = FileUtils.createTempDir();
		try {
			Git work = new Git(new File(tempDir, "work"));
			Git module1 = new Git(new File(tempDir, "module1"));
			Git module2 = new Git(new File(tempDir, "module2"));
			
			work.init(false);
			module1.init(false);
			module2.init(false);
			
			FileUtils.writeFile(new File(work.repoDir(), "readme"), "readme");
			work.add("readme").commit("initial commit", false);
			FileUtils.writeFile(new File(module1.repoDir(), "readme"), "readme");
			module1.add("readme").commit("initial commit", false);
			FileUtils.writeFile(new File(module2.repoDir(), "readme"), "readme");
			module2.add("readme").commit("initial commit", false);
			
			work.addSubModule(module1.repoDir().getAbsolutePath(), "module1");
			work.commit("add submodule1", false);
			
			Map<String, String> subModules = work.listSubModules("master");
			assertEquals(module1.repoDir().getCanonicalPath(), 
					new File(subModules.get("module1")).getCanonicalPath());

			File dir = new File(work.repoDir(), "dir");
			FileUtils.createDir(dir);
			
			work.addSubModule(module2.repoDir().getAbsolutePath(), "dir/module2");
			work.commit("add submodule2", false);
			subModules = work.listSubModules("master");
			assertEquals(module1.repoDir().getCanonicalPath(), 
					new File(subModules.get("module1")).getCanonicalPath());
			assertEquals(module2.repoDir().getCanonicalPath(), 
					new File(subModules.get("dir/module2")).getCanonicalPath());
			
		} finally {
			FileUtils.deleteDir(tempDir);
		}
	}

}
