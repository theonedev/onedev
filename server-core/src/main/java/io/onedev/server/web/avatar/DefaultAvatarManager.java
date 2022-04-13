package io.onedev.server.web.avatar;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.locks.Lock;

import javax.imageio.ImageIO;
import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.commons.codec.binary.Hex;
import org.eclipse.jgit.lib.PersonIdent;

import com.google.common.base.Splitter;
import com.google.common.collect.Iterables;

import io.onedev.commons.bootstrap.Bootstrap;
import io.onedev.commons.utils.FileUtils;
import io.onedev.commons.utils.LockUtils;
import io.onedev.commons.utils.StringUtils;
import io.onedev.server.entitymanager.EmailAddressManager;
import io.onedev.server.entitymanager.SettingManager;
import io.onedev.server.model.EmailAddress;
import io.onedev.server.model.Project;
import io.onedev.server.model.User;
import io.onedev.server.persistence.annotation.Sessional;
import io.onedev.server.web.component.avatarupload.AvatarUploadField;

@Singleton
public class DefaultAvatarManager implements AvatarManager {

	private static final int GRAVATAR_SIZE = 256;
	
	private static final String AVATARS_BASE_URL = "/site/avatars/";
	
	private final SettingManager settingManager;
	
	private final EmailAddressManager emailAddressManager;
	
	@Inject
	public DefaultAvatarManager(SettingManager settingManager, EmailAddressManager emailAddressManager) {
		this.settingManager = settingManager;
		this.emailAddressManager = emailAddressManager;
	}
	
	@Sessional
	@Override
	public String getAvatarUrl(User user) {
		if (user.isUnknown()) {
			return AVATARS_BASE_URL + "user.png";
		} else if (user.isSystem()) {
			return AVATARS_BASE_URL + "onedev.png";
		} else {
			File uploadedFile = getUploaded(user);
			if (uploadedFile.exists())
				return AVATARS_BASE_URL + "uploaded/users/" + user.getId() + ".jpg?version=" + uploadedFile.lastModified();
			
			EmailAddress emailAddress = user.getPrimaryEmailAddress();
			if (emailAddress == null || !emailAddress.isVerified()) 
				return generateAvatar(user.getName(), null);
			else if (settingManager.getSystemSetting().isGravatarEnabled()) 
				return Gravatar.getURL(emailAddress.getValue(), GRAVATAR_SIZE);
			else  
				return generateAvatar(user.getName(), emailAddress.getValue());
		}
	}
	
	@Sessional
	@Override
	public String getAvatarUrl(PersonIdent personIdent) {
		if (personIdent.getName().equals(User.ONEDEV_NAME)) { 
			return AVATARS_BASE_URL + "onedev.png";
		} else {
			EmailAddress emailAddress = emailAddressManager.findByValue(personIdent.getEmailAddress());
			if (emailAddress != null && emailAddress.isVerified()) { 
				return getAvatarUrl(emailAddress.getOwner());
			} else if (settingManager.getSystemSetting().isGravatarEnabled() 
					&& StringUtils.isNotBlank(personIdent.getEmailAddress())) {
				return Gravatar.getURL(personIdent.getEmailAddress(), GRAVATAR_SIZE);
			} else { 
				return generateAvatar(personIdent.getName(), personIdent.getEmailAddress());
			}
		}
	}
	
	private String generateAvatar(String primaryName, String secondaryName) {
		String encoded = Hex.encodeHexString((primaryName + ":" + AvatarGenerator.version()).getBytes());
		
		if (StringUtils.isBlank(primaryName))
			primaryName = "?";
		if (StringUtils.isBlank(secondaryName))
			secondaryName = primaryName;
		
		File avatarFile = new File(Bootstrap.getSiteDir(), "avatars/generated/" + encoded + ".png");
		if (!avatarFile.exists()) {
			Lock avatarLock = LockUtils.getLock("generated-avatar:" + encoded);
			avatarLock.lock();
			try {
				String letters = getLetter(primaryName);
				BufferedImage bi = AvatarGenerator.generate(letters, secondaryName);
				FileUtils.createDir(avatarFile.getParentFile());
				ImageIO.write(bi, "PNG", avatarFile);
			} catch (NoSuchAlgorithmException | IOException e) {
				throw new RuntimeException(e);
			} finally {
				avatarLock.unlock();
			}
		}
		
		return AVATARS_BASE_URL + "generated/" + encoded + ".png";
	}

	private String getLetter(String name) {
		String[] tokens = Iterables.toArray(Splitter.on(" ").split(name.trim()), String.class);

		char c = tokens[0].charAt(0);
		StringBuffer sb = new StringBuffer();
		sb.append(c);

		if (tokens.length > 1) {
			c = tokens[1].charAt(0);
			sb.append(c);
		}
		
		return sb.toString();
	}
	
	@Override
	public File getUploaded(User user) {
		return new File(Bootstrap.getSiteDir(), "avatars/uploaded/users/" + user.getId() + ".jpg");
	}

	@Sessional
	@Override
	public void useAvatar(User user, String avatarData) {
		Lock avatarLock = LockUtils.getLock("uploaded-user-avatar:" + user.getId());
		avatarLock.lock();
		try {
			File avatarFile = getUploaded(user);
			FileUtils.createDir(avatarFile.getParentFile());
			AvatarUploadField.writeToFile(avatarFile, avatarData);
		} finally {
			avatarLock.unlock();
		}
	}

	@Override
	public String getAvatarUrl(Long projectId) {
		File avatarFile = getUploaded(projectId);
		if (avatarFile.exists())  
			return AVATARS_BASE_URL + "uploaded/projects/" + projectId + ".jpg?version=" + avatarFile.lastModified();
		else
			return AVATARS_BASE_URL + "project.png";
	}

	@Override
	public void useAvatar(Project project, String avatarData) {
		Lock avatarLock = LockUtils.getLock("uploaded-project-avatar:" + project.getId());
		avatarLock.lock();
		try {
			File avatarFile = getUploaded(project.getId());
			FileUtils.createDir(avatarFile.getParentFile());
			AvatarUploadField.writeToFile(avatarFile, avatarData);
		} finally {
			avatarLock.unlock();
		}
	}

	@Override
	public File getUploaded(Long projectId) {
		return new File(Bootstrap.getSiteDir(), "avatars/uploaded/projects/" + projectId + ".jpg");
	}

	@Override
	public void copyAvatar(Project from, Project to) {
		Lock avatarLock = LockUtils.getLock("uploaded-project-avatar:" + from.getId());
		avatarLock.lock();
		try {
			File uploaded = getUploaded(from.getId());
			if (uploaded.exists()) {
				FileUtils.copyFile(uploaded, getUploaded(to.getId()));
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		} finally {
			avatarLock.unlock();
		}
	}

}
