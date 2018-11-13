package io.onedev.server.web.util.avatar;

import java.io.File;

import javax.annotation.Nullable;

import io.onedev.server.util.facade.ProjectFacade;
import io.onedev.server.util.facade.UserFacade;
import io.onedev.server.util.userident.UserIdent;

public interface AvatarManager {
	
	String getAvatarUrl(UserIdent userIdent);
	
	String getAvatarUrl(ProjectFacade project);
	
	void useAvatar(UserFacade user, @Nullable String avatarData);
	
	void useAvatar(ProjectFacade project, @Nullable String avatarData);
	
	File getUploaded(UserFacade user);
	
	File getUploaded(ProjectFacade project);
	
	void copyAvatar(ProjectFacade from, ProjectFacade to);
	
}

