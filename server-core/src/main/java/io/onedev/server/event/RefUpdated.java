package io.onedev.server.event;

import java.util.Date;

import org.eclipse.jgit.lib.ObjectId;

import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.UrlManager;
import io.onedev.server.model.Project;
import io.onedev.server.persistence.dao.Dao;
import io.onedev.server.util.CommitAware;
import io.onedev.server.util.ProjectScopedCommit;

public class RefUpdated extends ProjectEvent implements CommitAware {
	
	private final String refName;
	
	private final ObjectId oldCommitId;
	
	private final ObjectId newCommitId;
	
	private transient ProjectScopedCommit commit;
	
	public RefUpdated(Project project, String refName, ObjectId oldCommitId, ObjectId newCommitId) {
		super(null, new Date(), project);
		this.refName = refName;
		this.oldCommitId = oldCommitId;
		this.newCommitId = newCommitId;
	}

	public String getRefName() {
		return refName;
	}

	public ObjectId getOldCommitId() {
		return oldCommitId;
	}

	public ObjectId getNewCommitId() {
		return newCommitId;
	}

	@Override
	public ProjectScopedCommit getCommit() {
		if (commit == null)
			commit = new ProjectScopedCommit(getProject(), newCommitId);
		return commit;
	}

	@Override
	public String getActivity() {
		return "Git ref updated";
	}

	@Override
	public ProjectEvent cloneIn(Dao dao) {
		return new RefUpdated(dao.load(Project.class, getProject().getId()), refName, oldCommitId, newCommitId);
	}
	
	@Override
	public String getUrl() {
		if (newCommitId != null)
			return OneDev.getInstance(UrlManager.class).urlFor(getProject(), newCommitId);
		else
			throw new UnsupportedOperationException();
	}
	
}
