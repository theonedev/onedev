package io.onedev.server.event.build;

import java.util.Date;

import org.eclipse.jgit.lib.ObjectId;

import io.onedev.server.event.ProjectEvent;
import io.onedev.server.model.Build;
import io.onedev.server.model.User;
import io.onedev.server.util.CommitAware;
import io.onedev.server.util.ProjectScopedCommit;

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
	public ProjectScopedCommit getCommit() {
		return new ProjectScopedCommit(build.getProject(), ObjectId.fromString(build.getCommitHash()));
	}

	@Override
	public String getActivity(boolean withEntity) {
		String activity = build.getStatus().getDisplayName();
		if (withEntity) {
			activity = " is " + activity;
			if (build.getVersion() != null)
				activity = "build #" + build.getNumber() + " (" + build.getVersion() + ")" + activity;
			else
				activity = "build #" + build.getNumber() + activity;
		} 
		return activity;
	}

}
