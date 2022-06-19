package io.onedev.server.entitymanager;

import javax.annotation.Nullable;

import io.onedev.server.model.PendingSuggestionApply;
import io.onedev.server.model.PullRequest;
import io.onedev.server.model.User;
import io.onedev.server.persistence.dao.EntityManager;

public interface PendingSuggestionApplyManager extends EntityManager<PendingSuggestionApply> {
	
	void apply(User user, PullRequest request, boolean resolveComment, String commitMessage);

	void discard(@Nullable User user, PullRequest request);

	int count(User user, PullRequest request);
	
}