package com.pmease.commons.git;

import java.io.File;

import org.eclipse.jgit.api.AddCommand;
import org.eclipse.jgit.api.CommitCommand;
import org.eclipse.jgit.api.RmCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Repository;
import org.junit.After;
import org.junit.Before;

import com.pmease.commons.util.FileUtils;

public abstract class AbstractJGitTest {

	protected File tempDir;
	
	protected File repoDir;
	
	protected org.eclipse.jgit.api.Git git;
	
	protected Repository repo;
	
	/**
	 * Subclass should call super.setup() as first statement if it wants to override this method.
	 */
	@Before
	public void before() {
		tempDir = FileUtils.createTempDir();
		repoDir = new File(tempDir, "repo");
		
		try {
			git = org.eclipse.jgit.api.Git.init().setBare(false).setDirectory(repoDir).call();
		} catch (IllegalStateException | GitAPIException e) {
			throw new RuntimeException(e);
		}
		repo = git.getRepository();
	}
	
	protected void createDir(String path) {
		FileUtils.createDir(new File(repoDir, path));
	}
	
	protected void writeFile(String path, String content) {
		File file = new File(repoDir, path);
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
		ci.setAuthor("foo", "foo@example.com");
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
	
	@After
	public void after() {
		git.close();
		FileUtils.deleteDir(tempDir);
	}

}
