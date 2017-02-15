package com.gitplex.server.manager;

import java.util.Collection;
import java.util.Date;

import com.gitplex.server.entity.Account;
import com.gitplex.server.entity.PullRequest;
import com.gitplex.server.entity.PullRequestReviewInvitation;
import com.gitplex.server.persistence.dao.EntityManager;

public interface PullRequestReviewInvitationManager extends EntityManager<PullRequestReviewInvitation> {
	
	PullRequestReviewInvitation find(Account reviewer, PullRequest request);
	
	void update(Collection<PullRequestReviewInvitation> invitations, Date since);
	
}
