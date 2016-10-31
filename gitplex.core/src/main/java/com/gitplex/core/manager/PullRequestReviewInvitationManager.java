package com.gitplex.core.manager;

import java.util.Collection;
import java.util.Date;

import com.gitplex.core.entity.Account;
import com.gitplex.core.entity.PullRequest;
import com.gitplex.core.entity.PullRequestReviewInvitation;
import com.gitplex.commons.hibernate.dao.EntityManager;

public interface PullRequestReviewInvitationManager extends EntityManager<PullRequestReviewInvitation> {
	
	PullRequestReviewInvitation find(Account reviewer, PullRequest request);
	
	void update(Collection<PullRequestReviewInvitation> invitations, Date since);
	
}
