package com.pmease.gitplex.core.manager;

import java.util.Collection;
import java.util.Date;

import com.pmease.commons.hibernate.dao.EntityManager;
import com.pmease.gitplex.core.entity.Account;
import com.pmease.gitplex.core.entity.PullRequest;
import com.pmease.gitplex.core.entity.PullRequestReviewInvitation;

public interface PullRequestReviewInvitationManager extends EntityManager<PullRequestReviewInvitation> {
	
	PullRequestReviewInvitation find(Account reviewer, PullRequest request);
	
	void update(Collection<PullRequestReviewInvitation> invitations, Date since);
	
}
