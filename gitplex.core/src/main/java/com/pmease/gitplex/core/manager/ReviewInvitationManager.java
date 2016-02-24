package com.pmease.gitplex.core.manager;

import com.pmease.commons.hibernate.dao.Dao;
import com.pmease.gitplex.core.entity.PullRequest;
import com.pmease.gitplex.core.entity.ReviewInvitation;
import com.pmease.gitplex.core.entity.User;

public interface ReviewInvitationManager extends Dao {
	
	ReviewInvitation find(User reviewer, PullRequest request);
	
	void save(ReviewInvitation reviewInvitation);
}
