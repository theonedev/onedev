package io.onedev.server.entityreference;

import org.jspecify.annotations.Nullable;

import io.onedev.server.model.CodeComment;
import io.onedev.server.model.Issue;
import io.onedev.server.model.PullRequest;
import io.onedev.server.model.User;

public interface ReferenceChangeService {

	void addReferenceChange(User user, Issue issue, @Nullable String markdown);
	
	void addReferenceChange(User user, PullRequest request, @Nullable String markdown);
	
	void addReferenceChange(User user, CodeComment comment, @Nullable String markdown);
	
}
