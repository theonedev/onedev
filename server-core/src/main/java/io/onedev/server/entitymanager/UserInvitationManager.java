package io.onedev.server.entitymanager;

import java.util.List;

import javax.annotation.Nullable;

import io.onedev.server.model.UserInvitation;
import io.onedev.server.persistence.dao.EntityManager;

public interface UserInvitationManager extends EntityManager<UserInvitation> {
	
	@Nullable
	UserInvitation findByEmailAddress(String emailAddress);
	
	@Nullable
	UserInvitation findByInvitationCode(String invitationCode);
	
	void sendInvitationEmail(UserInvitation invitation);

	int count(@Nullable String term);
	
	List<UserInvitation> query(@Nullable String term, int firstResult, int maxResults);

    void create(UserInvitation invitation);
}
