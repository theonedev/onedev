package com.gitplex.server.git;

import java.io.File;
import java.io.IOException;

import javax.annotation.Nullable;

import org.eclipse.jgit.api.AddCommand;
import org.eclipse.jgit.api.CommitCommand;
import org.eclipse.jgit.api.RmCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.diff.DiffAlgorithm.SupportedAlgorithm;
import org.eclipse.jgit.lib.ConfigConstants;
import org.eclipse.jgit.lib.PersonIdent;
import org.eclipse.jgit.lib.RefUpdate;
import org.junit.Assert;
import org.mockito.Mockito;

import com.gitplex.launcher.loader.AppLoader;
import com.gitplex.launcher.loader.AppLoaderMocker;
import com.gitplex.server.git.command.GitCommand;
import com.gitplex.server.git.config.GitConfig;
import com.gitplex.utils.FileUtils;

public abstract class AbstractGitTest extends AppLoaderMocker {

	protected File gitDir;
	
	protected org.eclipse.jgit.api.Git git;
	
	protected PersonIdent user = new PersonIdent("foo", "foo@example.com");

	@Override
	protected void setup() {
		gitDir = FileUtils.createTempDir();
		
		try {
			git = org.eclipse.jgit.api.Git.init().setBare(false).setDirectory(gitDir).call();
		} catch (IllegalStateException | GitAPIException e) {
			throw new RuntimeException(e);
		}
		
		git.getRepository().getConfig().setEnum(ConfigConstants.CONFIG_DIFF_SECTION, null, 
				ConfigConstants.CONFIG_KEY_ALGORITHM, SupportedAlgorithm.HISTOGRAM);
		
		Mockito.when(AppLoader.getInstance(GitConfig.class)).thenReturn(new GitConfig() {

			private static final long serialVersionUID = 1L;

			@Override
			public String getExecutable() {
				return "git";
			}
			
		});
		
	    Assert.assertTrue(GitCommand.checkError("git") == null);
		
	}

	@Override
	protected void teardown() {
		git.close();
		FileUtils.deleteDir(gitDir);
	}

	protected void createDir(String path) {
		FileUtils.createDir(new File(gitDir, path));
	}
	
	protected void writeFile(String path, String content) {
		File file = new File(gitDir, path);
		FileUtils.writeFile(file, content);
	}
	
	protected void add(String...paths) {
		AddCommand add = git.add();
		for (String path: paths)
			add.addFilepattern(path);
		try {
			add.call();
		} catch (GitAPIException e) {
			throw new RuntimeException(e);
		}
	}
	
	protected void rm(String...paths) {
		RmCommand rm = git.rm();
		for (String path: paths)
			rm.addFilepattern(path);
		try {
			rm.call();
		} catch (GitAPIException e) {
			throw new RuntimeException(e);
		}
	}
	
	protected void commit(String comment) {
		CommitCommand ci = git.commit();
		ci.setMessage(comment);
		ci.setAuthor(user);
		ci.setCommitter(user);
		try {
			ci.call();
		} catch (GitAPIException e) {
			throw new RuntimeException(e);
		}
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

	protected void updateRef(String refName, String newValue, @Nullable String oldValue) {
		try {
			RefUpdate update = git.getRepository().updateRef(refName);
			update.setNewObjectId(git.getRepository().resolve(newValue));
			if (oldValue != null)
				update.setExpectedOldObjectId(git.getRepository().resolve(oldValue));
			update.setRefLogIdent(user);
			update.setRefLogMessage("update ref", false);
			GitUtils.updateRef(update);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
}
