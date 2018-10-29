package io.onedev.server.web.util.avatar;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.locks.Lock;

import javax.annotation.Nullable;
import javax.imageio.ImageIO;
import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.commons.codec.binary.Hex;
import org.apache.wicket.markup.html.form.upload.FileUpload;
import org.eclipse.jgit.lib.PersonIdent;

import com.google.common.base.Splitter;
import com.google.common.collect.Iterables;

import io.onedev.launcher.bootstrap.Bootstrap;
import io.onedev.server.OneDev;
import io.onedev.server.manager.SettingManager;
import io.onedev.server.persistence.annotation.Sessional;
import io.onedev.server.util.facade.ProjectFacade;
import io.onedev.server.util.facade.UserFacade;
import io.onedev.utils.ExceptionUtils;
import io.onedev.utils.FileUtils;
import io.onedev.utils.LockUtils;
import io.onedev.utils.StringUtils;

@Singleton
public class DefaultAvatarManager implements AvatarManager {

	private static final int GRAVATAR_SIZE = 256;
	
	private static final String AVATARS_BASE_URL = "/site/avatars/";
	
	private final SettingManager configManager;
	
	@Inject
	public DefaultAvatarManager(SettingManager configManager) {
		this.configManager = configManager;
	}
	
	@Sessional
	@Override
	public String getAvatarUrl(@Nullable UserFacade user) {
		if (user == null) {
			return AVATARS_BASE_URL + "onedev.png";
		} else if (user.getId() == null) {
			return AVATARS_BASE_URL + "user.png";
		} else {
			File avatarFile = getUploaded(user);
			if (avatarFile.exists()) { 
				return AVATARS_BASE_URL + "uploaded/users/" + user.getId() + "?version=" + avatarFile.lastModified();
			}
			
			if (configManager.getSystemSetting().isGravatarEnabled())
				return Gravatar.getURL(user.getEmail(), GRAVATAR_SIZE);
			else 
				return generateAvatar(user.getDisplayName(), user.getEmail());
		}
	}
	
	private String generateAvatar(String primaryName, String secondaryName) {
		String encoded = Hex.encodeHexString((primaryName + ":" + AvatarGenerator.version()).getBytes());
		
		if (StringUtils.isBlank(primaryName))
			primaryName = "?";
		if (StringUtils.isBlank(secondaryName))
			secondaryName = primaryName;
		
		File avatarFile = new File(Bootstrap.getSiteDir(), "avatars/generated/" + encoded);
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
		
		return AVATARS_BASE_URL + "generated/" + encoded;
	}
	
	@Override
	public String getAvatarUrl(PersonIdent person) {
		if (person.getEmailAddress().length() == 0 && person.getName().equals(OneDev.NAME))
			return AVATARS_BASE_URL + "onedev.png";
		else if (configManager.getSystemSetting().isGravatarEnabled())
			return Gravatar.getURL(person.getEmailAddress(), GRAVATAR_SIZE);
		else 
			return generateAvatar(person.getName(), person.getEmailAddress());
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
	public File getUploaded(UserFacade user) {
		return new File(Bootstrap.getSiteDir(), "avatars/uploaded/users/" + user.getId());
	}

	@Sessional
	@Override
	public void useAvatar(UserFacade user, FileUpload upload) {
		Lock avatarLock = LockUtils.getLock("uploaded-user-avatar:" + user.getId());
		avatarLock.lock();
		try {
			File avatarFile = getUploaded(user);
			if (upload != null) {
				FileUtils.createDir(avatarFile.getParentFile());
				try {
					upload.writeTo(avatarFile);
				} catch (Exception e) {
					throw ExceptionUtils.unchecked(e);
				}
			} else {
				FileUtils.deleteFile(avatarFile);
			}
		} finally {
			avatarLock.unlock();
		}
	}

	@Override
	public String getAvatarUrl(ProjectFacade project) {
		File avatarFile = getUploaded(project);
		if (avatarFile.exists())  
			return AVATARS_BASE_URL + "uploaded/projects/" + project.getId() + "?version=" + avatarFile.lastModified();
		else
			return AVATARS_BASE_URL + "project.png";
	}

	@Override
	public void useAvatar(ProjectFacade project, FileUpload upload) {
		Lock avatarLock = LockUtils.getLock("uploaded-project-avatar:" + project.getId());
		avatarLock.lock();
		try {
			File avatarFile = getUploaded(project);
			if (upload != null) {
				FileUtils.createDir(avatarFile.getParentFile());
				try {
					upload.writeTo(avatarFile);
				} catch (Exception e) {
					throw ExceptionUtils.unchecked(e);
				}
			} else {
				FileUtils.deleteFile(avatarFile);
			}
		} finally {
			avatarLock.unlock();
		}
	}

	@Override
	public File getUploaded(ProjectFacade project) {
		return new File(Bootstrap.getSiteDir(), "avatars/uploaded/projects/" + project.getId());
	}

}
