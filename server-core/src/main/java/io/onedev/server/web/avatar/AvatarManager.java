package io.onedev.server.web.avatar;

import java.io.File;

import javax.annotation.Nullable;

import io.onedev.server.model.Project;
import io.onedev.server.model.User;
import io.onedev.server.util.userident.UserIdent;

public interface AvatarManager {
	
	String getAvatarUrl(UserIdent userIdent);
	
	String getAvatarUrl(Project project);
	
	void useAvatar(User user, @Nullable String avatarData);
	
	void useAvatar(Project project, @Nullable String avatarData);
	
	File getUploaded(User user);
	
	File getUploaded(Project project);
	
	void copyAvatar(Project from, Project to);
	
}

