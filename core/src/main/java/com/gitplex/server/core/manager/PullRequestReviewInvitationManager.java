package com.gitplex.server.core.manager;

import java.util.Collection;
import java.util.Date;

import com.gitplex.commons.hibernate.dao.EntityManager;
import com.gitplex.server.core.entity.Account;
import com.gitplex.server.core.entity.PullRequest;
import com.gitplex.server.core.entity.PullRequestReviewInvitation;

public interface PullRequestReviewInvitationManager extends EntityManager<PullRequestReviewInvitation> {
	
	PullRequestReviewInvitation find(Account reviewer, PullRequest request);
	
	void update(Collection<PullRequestReviewInvitation> invitations, Date since);
	
}
