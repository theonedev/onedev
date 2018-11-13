package io.onedev.server.web.util.avatar;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.locks.Lock;

import javax.imageio.ImageIO;
import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.commons.codec.binary.Hex;

import com.google.common.base.Splitter;
import com.google.common.collect.Iterables;

import io.onedev.launcher.bootstrap.Bootstrap;
import io.onedev.server.manager.SettingManager;
import io.onedev.server.manager.UserManager;
import io.onedev.server.model.User;
import io.onedev.server.persistence.annotation.Sessional;
import io.onedev.server.util.facade.ProjectFacade;
import io.onedev.server.util.facade.UserFacade;
import io.onedev.server.util.userident.EmailAwareIdent;
import io.onedev.server.util.userident.RemovedUserIdent;
import io.onedev.server.util.userident.SystemUserIdent;
import io.onedev.server.util.userident.UserIdent;
import io.onedev.server.web.component.avatarupload.AvatarUploadField;
import io.onedev.utils.FileUtils;
import io.onedev.utils.LockUtils;
import io.onedev.utils.StringUtils;

@Singleton
public class DefaultAvatarManager implements AvatarManager {

	private static final int GRAVATAR_SIZE = 256;
	
	private static final String AVATARS_BASE_URL = "/site/avatars/";
	
	private final SettingManager settingManager;
	
	private final UserManager userManager;
	
	@Inject
	public DefaultAvatarManager(SettingManager settingManager, UserManager userManager) {
		this.settingManager = settingManager;
		this.userManager = userManager;
	}
	
	@Sessional
	@Override
	public String getAvatarUrl(UserIdent userIdent) {
		if (userIdent instanceof SystemUserIdent) {
			return AVATARS_BASE_URL + "onedev.png";
		} else if (userIdent instanceof RemovedUserIdent) {
			return AVATARS_BASE_URL + "user.png";
		} else {
			EmailAwareIdent emailAwareIdent = (EmailAwareIdent) userIdent;
			User user = userManager.findByEmail(emailAwareIdent.getEmail());
			if (user != null) {
				File uploadedFile = getUploaded(user.getFacade());
				if (uploadedFile.exists())
					return AVATARS_BASE_URL + "uploaded/users/" + user.getId() + ".jpg?version=" + uploadedFile.lastModified();
			}
			if (settingManager.getSystemSetting().isGravatarEnabled())
				return Gravatar.getURL(emailAwareIdent.getEmail(), GRAVATAR_SIZE);
			else 
				return generateAvatar(userIdent.getName(), emailAwareIdent.getEmail());
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
	public File getUploaded(UserFacade user) {
		return new File(Bootstrap.getSiteDir(), "avatars/uploaded/users/" + user.getId() + ".jpg");
	}

	@Sessional
	@Override
	public void useAvatar(UserFacade user, String avatarData) {
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
	public String getAvatarUrl(ProjectFacade project) {
		File avatarFile = getUploaded(project);
		if (avatarFile.exists())  
			return AVATARS_BASE_URL + "uploaded/projects/" + project.getId() + ".jpg?version=" + avatarFile.lastModified();
		else
			return AVATARS_BASE_URL + "project.png";
	}

	@Override
	public void useAvatar(ProjectFacade project, String avatarData) {
		Lock avatarLock = LockUtils.getLock("uploaded-project-avatar:" + project.getId());
		avatarLock.lock();
		try {
			File avatarFile = getUploaded(project);
			FileUtils.createDir(avatarFile.getParentFile());
			AvatarUploadField.writeToFile(avatarFile, avatarData);
		} finally {
			avatarLock.unlock();
		}
	}

	@Override
	public File getUploaded(ProjectFacade project) {
		return new File(Bootstrap.getSiteDir(), "avatars/uploaded/projects/" + project.getId() + ".jpg");
	}

	@Override
	public void copyAvatar(ProjectFacade from, ProjectFacade to) {
		Lock avatarLock = LockUtils.getLock("uploaded-project-avatar:" + from.getId());
		avatarLock.lock();
		try {
			File uploaded = getUploaded(from);
			if (uploaded.exists()) {
				FileUtils.copyFile(uploaded, getUploaded(to));
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		} finally {
			avatarLock.unlock();
		}
	}

}
