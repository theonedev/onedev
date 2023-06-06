package io.onedev.server.git;

import io.onedev.commons.loader.AppLoader;
import io.onedev.commons.loader.AppLoaderMocker;
import io.onedev.commons.utils.FileUtils;
import io.onedev.server.git.location.GitLocation;
import org.eclipse.jgit.api.AddCommand;
import org.eclipse.jgit.api.CommitCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.RmCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.internal.storage.file.FileRepository;
import org.eclipse.jgit.lib.PersonIdent;
import org.eclipse.jgit.lib.RefUpdate;
import org.eclipse.jgit.storage.file.FileBasedConfig;
import org.eclipse.jgit.util.FS;
import org.junit.Assert;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;

public abstract class AbstractGitTest extends AppLoaderMocker {

	private static final Logger logger = LoggerFactory.getLogger(AbstractGitTest.class);
	
	protected File gitDir;
	
	protected Git git;
	
	protected PersonIdent user = new PersonIdent("foo", "foo@example.com");
	
	@Override
	protected void setup() {
		gitDir = FileUtils.createTempDir();
		
		try (var ignored = Git.init().setInitialBranch("main").setBare(false).setDirectory(gitDir).call()) {
		} catch (IllegalStateException | GitAPIException e) {
			throw new RuntimeException(e);
		}
		
		try {
			git = Git.wrap(new FileRepository(new File(gitDir, ".git")) {
				@Override
				public FileBasedConfig getConfig() {
					// avoid loading user config
					return new FileBasedConfig(new File(getDirectory(), "config"), FS.DETECTED);
				}
				
			});
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		
		Mockito.when(AppLoader.getInstance(GitLocation.class)).thenReturn(new GitLocation() {

			private static final long serialVersionUID = 1L;

			@Override
			public String getExecutable() {
				return "git";
			}
			
		});
		
		String gitError = CommandUtils.checkError("git");
	    Assert.assertTrue(gitError, gitError == null);
	}

	protected void deleteDir(File dir, int retries) {
		int retried = 0;
		while (dir.exists()) {
			try {
				FileUtils.deleteDir(dir);
				break;
			} catch (Exception e) {
				if (retried++ < retries) {
					logger.error("Error deleting directory '" + dir.getAbsolutePath() + "', will retry later...", e);
					try {
						Thread.sleep(5000);
					} catch (InterruptedException e2) {
					}
				} else {
					throw e;
				}
			}
		}
	}
	
	@Override
	protected void teardown() {
		git.getRepository().close();
		FileUtils.deleteDir(gitDir, 3);
	}

	protected void createDir(String path) {
		FileUtils.createDir(new File(gitDir, path));
	}
	
	protected void deleteDir(String path) {
		FileUtils.deleteDir(new File(gitDir, path));
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
	
	protected String commit(String comment) {
		CommitCommand ci = git.commit();
		ci.setMessage(comment);
		ci.setSign(false);
		ci.setAuthor(user);
		ci.setCommitter(user);
		try {
			return ci.call().name();
		} catch (GitAPIException e) {
			throw new RuntimeException(e);
		}
	}
	
	protected void addFile(String path, String content) {
		writeFile(path, content);
		add(path);
	}
	
	protected String addFileAndCommit(String path, String content, String comment) {
		addFile(path, content);
		return commit(comment);
	}
	
	protected String removeFileAndCommit(String path, String comment) {
		rm(path);
		return commit(comment);
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
