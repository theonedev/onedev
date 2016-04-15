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
import com.google.common.base.Throwables;
import com.google.common.collect.Iterables;
import com.pmease.commons.bootstrap.Bootstrap;
import com.pmease.commons.hibernate.Transactional;
import com.pmease.commons.util.LockUtils;
import com.pmease.commons.util.StringUtils;
import com.pmease.gitplex.core.entity.Account;
import com.pmease.gitplex.core.manager.AccountManager;
import com.pmease.gitplex.core.manager.ConfigManager;

@Singleton
public class DefaultAvatarManager implements AvatarManager {

	private static final int GRAVATAR_SIZE = 256;
	
	private static final String AVATARS_BASE_URL = "/site/avatars/";
	
	private final ConfigManager configManager;
	
	private final AccountManager accountManager;
	
	@Inject
	public DefaultAvatarManager(ConfigManager configManager, AccountManager accountManager) {
		this.configManager = configManager;
		this.accountManager = accountManager;
	}
	
	@Transactional
	@Override
	public String getAvatarUrl(Account account) {
		if (account == null) 
			return AVATARS_BASE_URL + "default.png";
		
		if (account.getAvatarUploadDate() != null) { 
			File avatarFile = new File(Bootstrap.getSiteDir(), "avatars/" + account.getId());
			if (avatarFile.exists()) { 
				return AVATARS_BASE_URL + account.getId() + "?version=" + account.getAvatarUploadDate().getTime();
			} else {
				account.setAvatarUploadDate(null);
				accountManager.save(account, null);
			}
		} 
		
		if (configManager.getSystemSetting().isGravatarEnabled() && !account.isOrganization())
			return Gravatar.getURL(account.getEmail(), GRAVATAR_SIZE);
		else 
			return generateAvatar(account.getDisplayName(), account.getEmail());
	}
	
	private String generateAvatar(String primaryName, String secondaryName) {
		String encoded = Hex.encodeHexString((primaryName + ":" + AvatarGenerator.version()).getBytes());
		
		if (StringUtils.isBlank(primaryName))
			primaryName = "?";
		if (StringUtils.isBlank(secondaryName))
			secondaryName = primaryName;
		
		File avatarFile = new File(Bootstrap.getSiteDir(), "avatars/" + encoded);
		if (!avatarFile.exists()) {
			Lock avatarLock = LockUtils.getLock("avatars:" + encoded);
			avatarLock.lock();
			try {
				String letters = getLetter(primaryName);
				BufferedImage bi = AvatarGenerator.generate(letters, secondaryName);
				ImageIO.write(bi, "PNG", avatarFile);
			} catch (NoSuchAlgorithmException | IOException e) {
				throw new RuntimeException(e);
			} finally {
				avatarLock.unlock();
			}
		}
		
		return RequestCycle.get().getUrlRenderer().renderContextRelativeUrl(AVATARS_BASE_URL + encoded);
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
	public void useAvatar(Account account, FileUpload upload) {
		if (upload != null) {
			Lock avatarLock = LockUtils.getLock("avatars:" + account.getId());
			avatarLock.lock();
			try {
				upload.writeTo(new File(Bootstrap.getSiteDir(), "avatars/" + account.getId()));
			} catch (Exception e) {
				throw Throwables.propagate(e);
			} finally {
				avatarLock.unlock();
			}
			account.setAvatarUploadDate(new Date());
		} else {
			account.setAvatarUploadDate(null);
		}
		accountManager.save(account, null);
	}

}
