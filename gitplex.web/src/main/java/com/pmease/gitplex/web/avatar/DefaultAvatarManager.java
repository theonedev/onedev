package com.pmease.gitplex.web.avatar;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.concurrent.locks.Lock;

import javax.imageio.ImageIO;
import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.commons.codec.binary.Hex;
import org.apache.wicket.markup.html.form.upload.FileUpload;
import org.apache.wicket.request.cycle.RequestCycle;
import org.eclipse.jgit.lib.PersonIdent;

import com.google.common.base.Splitter;
import com.google.common.collect.Iterables;
import com.pmease.commons.bootstrap.Bootstrap;
import com.pmease.commons.hibernate.Transactional;
import com.pmease.commons.util.LockUtils;
import com.pmease.commons.util.StringUtils;
import com.pmease.gitplex.core.entity.Account;
import com.pmease.gitplex.core.manager.ConfigManager;
import com.pmease.gitplex.core.manager.AccountManager;

@Singleton
public class DefaultAvatarManager implements AvatarManager {

	private static final int GRAVATAR_SIZE = 256;
	
	private static final String AVATARS_BASE_URL = "/site/avatars/";
	
	private final ConfigManager configManager;
	
	private final AccountManager userManager;
	
	@Inject
	public DefaultAvatarManager(ConfigManager configManager, AccountManager userManager) {
		this.configManager = configManager;
		this.userManager = userManager;
	}
	
	@Transactional
	@Override
	public String getAvatarUrl(Account user) {
		if (user == null) 
			return AVATARS_BASE_URL + "default.png";
		
		if (user.getAvatarUploadDate() != null) { 
			File avatarFile = new File(Bootstrap.getSiteDir(), "avatars/" + user.getId());
			if (avatarFile.exists()) { 
				return AVATARS_BASE_URL + user.getId() + "?version=" + user.getAvatarUploadDate().getTime();
			} else {
				user.setAvatarUploadDate(null);
				userManager.save(user);
			}
		} 
		
		if (configManager.getSystemSetting().isGravatarEnabled())
			return Gravatar.getURL(user.getEmail(), GRAVATAR_SIZE);
		else 
			return generateAvatar(user.getDisplayName(), user.getEmail());
	}
	
	private String generateAvatar(String name, String email) {
		String encoded = encode(name, email, AvatarGenerator.version());
		
		File avatarFile = new File(Bootstrap.getSiteDir(), "avatars/" + encoded);
		if (!avatarFile.exists()) {
			Lock avatarLock = LockUtils.getLock("avatars:" + encoded);
			avatarLock.lock();
			try {
				String letters;
				if (StringUtils.isNotBlank(name))
					letters = getLetter(name);
				else if (StringUtils.isNotBlank(email))
					letters = getLetter(email);
				else
					letters = "?";
				
				BufferedImage bi = AvatarGenerator.generate(letters, email);
				ImageIO.write(bi, "PNG", avatarFile);
			} catch (NoSuchAlgorithmException | IOException e) {
				throw new RuntimeException(e);
			} finally {
				avatarLock.unlock();
			}
		}
		
		return RequestCycle.get().getUrlRenderer().renderContextRelativeUrl(AVATARS_BASE_URL + encoded);
	}
	
	private String encode(String name, String email, int version) {
		String concatenated = name + ":" + email + ":" + version;
		return Hex.encodeHexString(concatenated.getBytes());
	}
	
	@Override
	public String getAvatarUrl(PersonIdent person) {
		if (configManager.getSystemSetting().isGravatarEnabled())
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

	@Transactional
	@Override
	public void useAvatar(Account user, FileUpload upload) {
		if (upload != null) {
			Lock avatarLock = LockUtils.getLock("avatars:" + user.getId());
			avatarLock.lock();
			try {
				upload.writeTo(new File(Bootstrap.getSiteDir(), "avatars/" + user.getId()));
			} catch (IOException e) {
				throw new RuntimeException(e);
			} finally {
				avatarLock.unlock();
			}
			user.setAvatarUploadDate(new Date());
		} else {
			user.setAvatarUploadDate(null);
		}
		userManager.save(user);
	}

}
