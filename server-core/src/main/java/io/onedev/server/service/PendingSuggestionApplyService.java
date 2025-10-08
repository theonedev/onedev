package io.onedev.server.service;

import java.util.List;

import org.jspecify.annotations.Nullable;

import org.eclipse.jgit.lib.ObjectId;

import io.onedev.server.model.PendingSuggestionApply;
import io.onedev.server.model.PullRequest;
import io.onedev.server.model.User;

public interface PendingSuggestionApplyService extends EntityService<PendingSuggestionApply> {
	
	ObjectId apply(User user, PullRequest request, String commitMessage);

	void discard(@Nullable User user, PullRequest request);

	List<PendingSuggestionApply> query(User user, PullRequest request);

    void create(PendingSuggestionApply pendingApply);
}