package io.onedev.server.model.support.jobexecutor;

import java.io.File;
import java.io.IOException;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.ObjectId;

import io.onedev.commons.utils.ExceptionUtils;
import io.onedev.server.git.command.CheckoutCommand;
import io.onedev.server.git.command.FetchCommand;
import io.onedev.server.model.Project;

public class SourceSnapshot {

	private final Project project; 
	
	private final ObjectId commitId;
	
	public SourceSnapshot(Project project, ObjectId commitId) {
		this.project = project;
		this.commitId = commitId;
	}

	public Project getProject() {
		return project;
	}

	public ObjectId getCommitId() {
		return commitId;
	}

	private void fetchAndCheckout(File gitDir) {
		new FetchCommand(gitDir).depth(1).from(project.getGitDir().getAbsolutePath()).refspec(commitId.name()).call();
		new CheckoutCommand(gitDir).refspec(commitId.name()).call();
	}
	
	public void checkout(File dir) {
		if (new File(dir, ".git").exists()) {
			try (Git git = Git.open(dir)) {
				fetchAndCheckout(dir);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		} else {
	        try (Git git = Git.init().setDirectory(dir).call()) {
				fetchAndCheckout(dir);
			} catch (GitAPIException e) {
				throw ExceptionUtils.unchecked(e);
			}
		}
	}
	
}
