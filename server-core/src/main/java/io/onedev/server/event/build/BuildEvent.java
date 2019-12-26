package io.onedev.server.event.build;

import java.util.Date;

import org.eclipse.jgit.lib.ObjectId;

import io.onedev.server.event.ProjectEvent;
import io.onedev.server.model.Build;
import io.onedev.server.model.User;
import io.onedev.server.util.CommitAware;
import io.onedev.server.util.ProjectAwareCommit;

public class BuildEvent extends ProjectEvent implements CommitAware {

	private Build build;
	
	public BuildEvent(User user, Date date, Build build) {
		super(user, date, build.getProject());
		this.build = build;
	}

	public Build getBuild() {
		return build;
	}

	public void setBuild(Build build) {
		this.build = build;
	}

	@Override
	public ProjectAwareCommit getCommit() {
		return new ProjectAwareCommit(build.getProject(), ObjectId.fromString(build.getCommitHash()));
	}

}
