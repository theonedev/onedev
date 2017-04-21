package com.gitplex.server.manager;

import com.gitplex.server.model.ReviewInvitation;
import com.gitplex.server.persistence.dao.EntityManager;

public interface ReviewInvitationManager extends EntityManager<ReviewInvitation> {
	
	boolean exclude(ReviewInvitation invitation);
	
	void invite(ReviewInvitation invitation);
	
}
