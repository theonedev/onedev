package io.onedev.server.model.support.jobexecutor;

import java.io.File;

import org.eclipse.jgit.api.Git;
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
	
	public void checkout(File dir) {
        try (Git git = Git.init().setDirectory(dir).call()) {
			new FetchCommand(dir).depth(1).from(project.getGitDir().getAbsolutePath()).refspec(commitId.name()).call();
			new CheckoutCommand(dir).refspec(commitId.name()).call();
		} catch (Exception e) {
			throw ExceptionUtils.unchecked(e);
		}
	}
	
}
