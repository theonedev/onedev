package io.onedev.server.service;

import java.util.List;

import org.jspecify.annotations.Nullable;

import io.onedev.server.model.UserInvitation;

public interface UserInvitationService extends EntityService<UserInvitation> {
	
	@Nullable
	UserInvitation findByEmailAddress(String emailAddress);
	
	@Nullable
	UserInvitation findByInvitationCode(String invitationCode);
	
	void sendInvitationEmail(UserInvitation invitation);

	int count(@Nullable String term);
	
	List<UserInvitation> query(@Nullable String term, int firstResult, int maxResults);

    void create(UserInvitation invitation);
}
