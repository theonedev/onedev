package com.pmease.commons.git;

import java.io.File;

import org.junit.Assert;
import org.mockito.Mockito;

import com.pmease.commons.loader.AppLoader;
import com.pmease.commons.loader.AppLoaderMocker;
import com.pmease.commons.util.FileUtils;

public abstract class AbstractGitTest extends AppLoaderMocker {

	protected File tempDir;
	
	protected Git git;
	
	/**
	 * Subclass should call super.setup() as first statement if it wants to override this method.
	 */
	@SuppressWarnings("serial")
	@Override
	protected void setup() {
		Mockito.when(AppLoader.getInstance(GitConfig.class)).thenReturn(new GitConfig() {

			@Override
			public String getExecutable() {
				return "git";
			}
			
		});
		
	    Assert.assertTrue(com.pmease.commons.git.command.GitCommand.checkError("git") == null);
		
		tempDir = FileUtils.createTempDir();

		git = new Git(new File(tempDir, "repo"));
		git.init(false);
	}
	
	protected void createDir(String path) {
		FileUtils.createDir(new File(git.depotDir(), path));
	}
	
	protected void writeFile(String path, String content) {
		File file = new File(git.depotDir(), path);
		FileUtils.writeFile(file, content);
	}
	
	protected void add(String...paths) {
		git.add(paths);
	}
	
	protected void rm(String...paths) {
		git.rm(paths);
	}
	
	protected void commit(String comment) {
		git.commit(comment, false, false);
	}
	
	protected void addFile(String path, String content) {
		writeFile(path, content);
		add(path);
	}
	
	protected void addFileAndCommit(String path, String content, String comment) {
		addFile(path, content);
		commit(comment);
	}
	
	protected void removeFileAndCommit(String path, String comment) {
		rm(path);
		commit(comment);
	}
	
	protected void teardown() {
		FileUtils.deleteDir(tempDir);
	}

}
