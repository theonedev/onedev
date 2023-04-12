package io.onedev.server.web.avatar;

import java.io.File;

import javax.annotation.Nullable;

import org.eclipse.jgit.lib.PersonIdent;

public interface AvatarManager {
	
	String getUserAvatarUrl(Long userId);
	
	String getPersonAvatarUrl(PersonIdent personIdent);
	
	String getProjectAvatarUrl(Long projectId);
	
	void useUserAvatar(Long userId, @Nullable String avatarData);
	
	void useProjectAvatar(Long projectId, @Nullable String avatarData);
	
	File getUserUploadedFile(Long userId);
	
	File getProjectUploadedFile(Long projectId);
	
	void copyProjectAvatar(Long fromProjectId, Long toProjectId);
	
}

