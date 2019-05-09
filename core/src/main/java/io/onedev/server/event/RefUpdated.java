package io.onedev.server.event;

import java.util.Collection;

import org.eclipse.jgit.lib.ObjectId;

import com.google.common.collect.Sets;

import io.onedev.server.model.Project;

public class RefUpdated extends ProjectEvent implements BuildCommitsAware {
	
	private final String refName;
	
	private final ObjectId oldCommitId;
	
	private final ObjectId newCommitId;
	
	public RefUpdated(Project project, String refName, ObjectId oldCommitId, ObjectId newCommitId) {
		super(null, null, project);
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
	public Collection<ObjectId> getBuildCommits() {
		if (!newCommitId.equals(ObjectId.zeroId()))
			return Sets.newHashSet(newCommitId);
		else
			return Sets.newHashSet();
	}
	
}
