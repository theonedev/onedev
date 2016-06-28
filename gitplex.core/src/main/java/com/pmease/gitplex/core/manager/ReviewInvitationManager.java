package com.pmease.gitplex.core.manager;

import com.pmease.commons.hibernate.dao.EntityManager;
import com.pmease.gitplex.core.entity.Account;
import com.pmease.gitplex.core.entity.PullRequest;
import com.pmease.gitplex.core.entity.ReviewInvitation;

public interface ReviewInvitationManager extends EntityManager<ReviewInvitation> {
	
	ReviewInvitation find(Account reviewer, PullRequest request);
	
	void save(ReviewInvitation reviewInvitation);
}
