package com.pmease.gitplex.core.manager;

import com.pmease.commons.hibernate.dao.EntityDao;
import com.pmease.gitplex.core.entity.Account;
import com.pmease.gitplex.core.entity.PullRequest;
import com.pmease.gitplex.core.entity.ReviewInvitation;

public interface ReviewInvitationManager extends EntityDao<ReviewInvitation> {
	
	ReviewInvitation find(Account reviewer, PullRequest request);
	
	void save(ReviewInvitation reviewInvitation);
}
