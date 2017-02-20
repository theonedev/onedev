package com.gitplex.server.manager;

import java.util.Collection;
import java.util.Date;

import com.gitplex.server.model.Account;
import com.gitplex.server.model.PullRequest;
import com.gitplex.server.model.PullRequestReviewInvitation;
import com.gitplex.server.persistence.dao.EntityManager;

public interface PullRequestReviewInvitationManager extends EntityManager<PullRequestReviewInvitation> {
	
	PullRequestReviewInvitation find(Account reviewer, PullRequest request);
	
	void update(Collection<PullRequestReviewInvitation> invitations, Date since);
	
}
