package com.pmease.gitplex.core.manager;

import javax.annotation.Nullable;

import com.pmease.gitplex.core.model.PullRequest;
import com.pmease.gitplex.core.model.PullRequestUpdate;
import com.pmease.gitplex.core.model.PullRequestVote;
import com.pmease.gitplex.core.model.User;

public interface VoteManager {

	PullRequestVote find(User reviewer, PullRequestUpdate update);

	void vote(PullRequest request, User user, PullRequestVote.Result result, @Nullable String comment);
}
