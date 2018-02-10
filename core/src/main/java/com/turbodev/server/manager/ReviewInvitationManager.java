package com.turbodev.server.manager;

import com.turbodev.server.model.ReviewInvitation;
import com.turbodev.server.persistence.dao.EntityManager;

public interface ReviewInvitationManager extends EntityManager<ReviewInvitation> {
	
	boolean exclude(ReviewInvitation invitation);
	
	void invite(ReviewInvitation invitation);
	
}
