package com.pmease.gitplex.core.manager;

import javax.annotation.Nullable;

import com.pmease.gitplex.core.model.PullRequest;
import com.pmease.gitplex.core.model.PullRequestUpdate;
import com.pmease.gitplex.core.model.Review;
import com.pmease.gitplex.core.model.User;

public interface ReviewManager {

	Review find(User reviewer, PullRequestUpdate update);

	void review(PullRequest request, User reviewer, Review.Result result, @Nullable String comment);
}
