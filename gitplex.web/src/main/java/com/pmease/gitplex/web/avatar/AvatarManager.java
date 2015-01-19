package com.pmease.gitplex.web.avatar;

import javax.annotation.Nullable;

import org.apache.wicket.markup.html.form.upload.FileUpload;
import org.eclipse.jgit.lib.PersonIdent;

import com.pmease.gitplex.core.model.User;

public interface AvatarManager {
	
	String getAvatarUrl(@Nullable User user);
	
	String getAvatarUrl(PersonIdent person);

	void useAvatar(User user, FileUpload avatar);
	
	void resetAvatar(User user);
	
}

