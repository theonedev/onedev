package com.gitplex.server.web.avatar;

import javax.annotation.Nullable;

import org.apache.wicket.markup.html.form.upload.FileUpload;
import org.eclipse.jgit.lib.PersonIdent;

import com.gitplex.server.core.entity.Account;

public interface AvatarManager {
	
	/**
	 * Get URL of user avatar image. 
	 *  
	 * @param user
	 * 			user to get avatar for
	 * @return
	 * 			url of avatar image. This url will be relative to context root if gravatar is disabled, or 
	 * 			a default avatar is returned when user is null 
	 */
	String getAvatarUrl(@Nullable Account user);
	
	/**
	 * Get URL of avatar image of specified person. 
	 *  
	 * @param person
	 * 			person to get avatar for
	 * @return
	 * 			url of avatar image. This url will be relative to context root if gravatar is disabled
	 */
	String getAvatarUrl(PersonIdent person);

	void useAvatar(Account user, @Nullable FileUpload avatar);
	
}

