package io.onedev.server.entityreference;

import javax.annotation.Nullable;

import io.onedev.server.model.CodeComment;
import io.onedev.server.model.Issue;
import io.onedev.server.model.PullRequest;
import io.onedev.server.model.User;

public interface ReferenceChangeManager {

	void addReferenceChange(User user, Issue issue, @Nullable String markdown);
	
	void addReferenceChange(User user, PullRequest request, @Nullable String markdown);
	
	void addReferenceChange(User user, CodeComment comment, @Nullable String markdown);
	
}
