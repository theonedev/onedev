package io.onedev.server.manager;

import java.util.Set;

import org.eclipse.jgit.lib.ObjectId;

import io.onedev.server.model.Project;

public interface CodeCommentRelationInfoManager {

	public Set<String> getPullRequestUUIDs(Project project, ObjectId commitId);
	
}
