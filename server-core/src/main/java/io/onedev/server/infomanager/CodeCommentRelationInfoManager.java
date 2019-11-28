package io.onedev.server.infomanager;

import java.util.Collection;

import org.eclipse.jgit.lib.ObjectId;

import io.onedev.server.model.Project;

public interface CodeCommentRelationInfoManager {

	public Collection<Long> getPullRequestIds(Project project, ObjectId commitId);
	
}
