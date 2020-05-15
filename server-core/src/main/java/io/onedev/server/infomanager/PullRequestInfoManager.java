package io.onedev.server.infomanager;

import java.util.Collection;

import javax.annotation.Nullable;

import org.eclipse.jgit.lib.ObjectId;

import io.onedev.server.model.Project;

public interface PullRequestInfoManager {

	Collection<Long> getPullRequestIds(Project project, ObjectId commitId);
	
	@Nullable
	ObjectId readMergeCommitId(Project project, ObjectId commitId1, ObjectId commitId2);
	
	void writeMergeCommitId(Project project, ObjectId commitId1, ObjectId commitId2, ObjectId mergeCommit);
	
}
