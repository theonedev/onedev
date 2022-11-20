package io.onedev.server.event.project.build;

import java.util.Date;

import org.eclipse.jgit.lib.ObjectId;

import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.BuildManager;
import io.onedev.server.entitymanager.UrlManager;
import io.onedev.server.event.project.ProjectEvent;
import io.onedev.server.model.Build;
import io.onedev.server.model.User;
import io.onedev.server.util.CommitAware;
import io.onedev.server.util.ProjectScopedCommit;

public abstract class BuildEvent extends ProjectEvent implements CommitAware {

	private static final long serialVersionUID = 1L;
	
	private Long buildId;
	
	public BuildEvent(User user, Date date, Build build) {
		super(user, date, build.getProject());
		buildId = build.getId();
	}

	public Build getBuild() {
		return OneDev.getInstance(BuildManager.class).load(buildId);
	}

	@Override
	public ProjectScopedCommit getCommit() {
		Build build = getBuild();
		return new ProjectScopedCommit(build.getProject(), ObjectId.fromString(build.getCommitHash()));
	}

	@Override
	public String getActivity() {
		return getBuild().getStatus().toString();
	}

	@Override
	public String getLockName() {
		return Build.getSerialLockName(buildId);
	}

	@Override
	public String getUrl() {
		return OneDev.getInstance(UrlManager.class).urlFor(getBuild());
	}
	
}
