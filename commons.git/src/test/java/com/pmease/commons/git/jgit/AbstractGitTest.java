package com.pmease.commons.git.jgit;

import java.io.File;
import java.io.IOException;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.GitCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.PersonIdent;
import org.eclipse.jgit.transport.RefSpec;

import com.pmease.commons.git.GitUtils;
import com.pmease.commons.loader.AppLoaderMocker;
import com.pmease.commons.util.FileUtils;

public class AbstractGitTest extends AppLoaderMocker {

	protected File tempDir;
	
	protected Git workGit;
	
	protected Git bareGit;
	
	@Override
	protected void setup() {
		tempDir = FileUtils.createTempDir();
		try {
			File bareGitDir = new File(tempDir, "bare");
			Git.init().setDirectory(bareGitDir).setBare(true).call();
			bareGit = Git.open(bareGitDir);

			File workGitDir = new File(tempDir, "work");
			
			Git.cloneRepository()
				.setBare(false)
				.setDirectory(workGitDir)
				.setURI(bareGit.getRepository().getDirectory().getAbsolutePath())
				.call();
			workGit = Git.open(workGitDir);
			
			addFileAndCommit("initial", "hello git", "Initial commit");
			push(new RefSpec("master:master"));
		} catch (GitAPIException | IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	protected void push(RefSpec... specs) {
		call(workGit.push().setRemote("origin").setRefSpecs(specs));
	}
	
	protected void pull() {
		call(workGit.pull());
	}
	
	protected File getWorkDir() {
		return workGit.getRepository().getWorkTree();
	}
	
	protected void createDir(String path) {
		FileUtils.createDir(new File(getWorkDir(), path));
	}
	
	protected void writeFile(String path, String content) {
		File file = new File(getWorkDir(), path);
		FileUtils.writeFile(file, content);
	}
	
	protected void add(String filePatterns) {
		call(workGit.add().addFilepattern(filePatterns));
	}
	
	protected void rm(String filePattern) {
		call(workGit.rm().addFilepattern(filePattern));
	}
	
	protected void commit(String comment) {
		call(workGit.commit().setAuthor(getDefaultAuthor()).setCommitter(getDefaultCommitter()).setMessage(comment));
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
	
	protected PersonIdent getDefaultCommitter() {
		return new PersonIdent("A Tester", "atester@example.com");
	}
	
	protected PersonIdent getDefaultAuthor() {
		return new PersonIdent("A Tester", "atester@example.com");
	}
	
	protected <T> T call(GitCommand<T> command) {
		return GitUtils.call(command);
	}

	@Override
	protected void teardown() {
		if (bareGit != null)
			bareGit.close();
		if (workGit != null)
			workGit.close();
		
		FileUtils.deleteDir(tempDir);
	}
}
