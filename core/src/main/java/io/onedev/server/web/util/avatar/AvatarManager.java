package io.onedev.server.web.util.avatar;

import java.io.File;

import javax.annotation.Nullable;

import org.apache.wicket.markup.html.form.upload.FileUpload;
import org.eclipse.jgit.lib.PersonIdent;

import io.onedev.server.util.facade.ProjectFacade;
import io.onedev.server.util.facade.UserFacade;

public interface AvatarManager {
	
	/**
	 * Get URL of user avatar image. 
	 *  
	 * @param user
	 * 			user to get avatar for
	 * @return
	 * 			url of avatar image. This url will be relative to context root if gravatar is disabled
	 */
	String getAvatarUrl(@Nullable UserFacade user);
	
	String getAvatarUrl(ProjectFacade project);
	
	/**
	 * Get URL of avatar image of specified person. 
	 *  
	 * @param person
	 * 			person to get avatar for
	 * @return
	 * 			url of avatar image. This url will be relative to context root if gravatar is disabled
	 */
	String getAvatarUrl(PersonIdent person);

	void useAvatar(UserFacade user, @Nullable FileUpload upload);
	
	void useAvatar(ProjectFacade project, @Nullable FileUpload upload);
	
	File getUploaded(UserFacade user);
	
	File getUploaded(ProjectFacade project);
	
	void copyAvatar(ProjectFacade from, ProjectFacade to);
	
}

