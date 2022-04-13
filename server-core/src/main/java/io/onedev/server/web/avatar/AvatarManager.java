package io.onedev.server.web.avatar;

import java.io.File;

import javax.annotation.Nullable;

import org.eclipse.jgit.lib.PersonIdent;

import io.onedev.server.model.Project;
import io.onedev.server.model.User;

public interface AvatarManager {
	
	String getAvatarUrl(User user);
	
	String getAvatarUrl(PersonIdent personIdent);
	
	String getAvatarUrl(Long projectId);
	
	void useAvatar(User user, @Nullable String avatarData);
	
	void useAvatar(Project project, @Nullable String avatarData);
	
	File getUploaded(User user);
	
	File getUploaded(Long projectId);
	
	void copyAvatar(Project from, Project to);
	
}

