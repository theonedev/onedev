package io.onedev.server.manager;

import io.onedev.server.model.ReviewInvitation;
import io.onedev.server.persistence.dao.EntityManager;

public interface ReviewInvitationManager extends EntityManager<ReviewInvitation> {
	
	boolean exclude(ReviewInvitation invitation);
	
	void invite(ReviewInvitation invitation);
	
}
