package io.onedev.server.entityreference;

import io.onedev.server.model.CodeComment;
import io.onedev.server.model.Issue;
import io.onedev.server.model.PullRequest;
import io.onedev.server.model.User;
import io.onedev.server.util.ProjectScopedCommit;

import javax.annotation.Nullable;

public interface EntityReferenceManager {

	void addReferenceChange(User user, Issue issue, @Nullable String markdown);
	
	void addReferenceChange(User user, PullRequest request, @Nullable String markdown);
	
	void addReferenceChange(User user, CodeComment comment, @Nullable String markdown);

	void addReferenceChange(ProjectScopedCommit commit);
	
}
