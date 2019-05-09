package io.onedev.server.event.build;

import java.util.Collection;
import java.util.Date;

import org.eclipse.jgit.lib.ObjectId;

import com.google.common.collect.Sets;

import io.onedev.server.event.BuildCommitsAware;
import io.onedev.server.event.ProjectEvent;
import io.onedev.server.model.Build;
import io.onedev.server.model.User;

public class BuildEvent extends ProjectEvent implements BuildCommitsAware {

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
	public Collection<ObjectId> getBuildCommits() {
		return Sets.newHashSet(ObjectId.fromString(build.getCommitHash()));
	}

}
