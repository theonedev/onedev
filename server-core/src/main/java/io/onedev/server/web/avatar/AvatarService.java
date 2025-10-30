package io.onedev.server.web.avatar;

import java.io.File;

import org.jspecify.annotations.Nullable;

import org.eclipse.jgit.lib.PersonIdent;

public interface AvatarService {
	
	String getUserAvatarUrl(Long userId);
	
	String getPersonAvatarUrl(PersonIdent personIdent);
	
	String getProjectAvatarUrl(Long projectId);
	
	void useUserAvatar(Long userId, @Nullable String avatarData);
	
	void useProjectAvatar(Long projectId, @Nullable String avatarData);
	
	File getUserUploadedFile(Long userId, @Nullable String extension);
	
	File getProjectUploadedFile(Long projectId, @Nullable String extension);
	
	void copyProjectAvatar(Long fromProjectId, Long toProjectId);
	
}

