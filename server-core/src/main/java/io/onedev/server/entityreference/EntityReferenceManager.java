package io.onedev.server.entityreference;

import javax.annotation.Nullable;

import io.onedev.server.model.CodeComment;
import io.onedev.server.model.Issue;
import io.onedev.server.model.PullRequest;

public interface EntityReferenceManager {

	void addReferenceChange(Issue issue, @Nullable String markdown);
	
	void addReferenceChange(PullRequest request, @Nullable String markdown);
	
	void addReferenceChange(CodeComment comment, @Nullable String markdown);
	
}
