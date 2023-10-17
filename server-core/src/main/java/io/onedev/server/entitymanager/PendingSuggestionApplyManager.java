package io.onedev.server.entitymanager;

import java.util.List;

import javax.annotation.Nullable;

import org.eclipse.jgit.lib.ObjectId;

import io.onedev.server.model.PendingSuggestionApply;
import io.onedev.server.model.PullRequest;
import io.onedev.server.model.User;
import io.onedev.server.persistence.dao.EntityManager;

public interface PendingSuggestionApplyManager extends EntityManager<PendingSuggestionApply> {
	
	ObjectId apply(User user, PullRequest request, String commitMessage);

	void discard(@Nullable User user, PullRequest request);

	List<PendingSuggestionApply> query(User user, PullRequest request);

    void create(PendingSuggestionApply pendingApply);
}