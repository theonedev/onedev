package io.onedev.server.event.build2;

import java.util.Date;

import org.eclipse.jgit.lib.ObjectId;

import io.onedev.server.event.CommitAware;
import io.onedev.server.event.ProjectEvent;
import io.onedev.server.model.Build2;
import io.onedev.server.model.User;

public class BuildEvent extends ProjectEvent implements CommitAware {

	private Build2 build;
	
	public BuildEvent(User user, Date date, Build2 build) {
		super(user, date, build.getProject());
		this.build = build;
	}

	public Build2 getBuild() {
		return build;
	}

	public void setBuild(Build2 build) {
		this.build = build;
	}

	@Override
	public ObjectId getCommitId() {
		return ObjectId.fromString(build.getCommitHash());
	}

}
