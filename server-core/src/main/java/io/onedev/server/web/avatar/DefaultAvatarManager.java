package io.onedev.server.web.avatar;

import com.google.common.base.Splitter;
import com.google.common.collect.Iterables;
import io.onedev.commons.bootstrap.Bootstrap;
import io.onedev.commons.loader.ManagedSerializedForm;
import io.onedev.commons.utils.FileUtils;
import io.onedev.commons.utils.StringUtils;
import io.onedev.server.cluster.ClusterManager;
import io.onedev.server.entitymanager.EmailAddressManager;
import io.onedev.server.entitymanager.SettingManager;
import io.onedev.server.entitymanager.UserManager;
import io.onedev.server.model.User;
import io.onedev.server.util.facade.EmailAddressFacade;
import io.onedev.server.util.facade.UserFacade;
import org.apache.commons.codec.binary.Hex;
import org.eclipse.jgit.lib.PersonIdent;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.ObjectStreamException;
import java.io.Serializable;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;

import static io.onedev.commons.utils.FileUtils.createDir;
import static io.onedev.commons.utils.LockUtils.read;
import static io.onedev.commons.utils.LockUtils.write;
import static io.onedev.server.web.avatar.AvatarGenerator.generate;
import static io.onedev.server.web.component.avatarupload.AvatarUploadField.writeToFile;
import static javax.imageio.ImageIO.write;

@Singleton
public class DefaultAvatarManager implements AvatarManager, Serializable {

	private static final int GRAVATAR_SIZE = 256;
	
	private static final String AVATARS_BASE_URL = "/avatars/";
	
	private final SettingManager settingManager;
	
	private final UserManager userManager;
	
	private final EmailAddressManager emailAddressManager;
	
	private final ClusterManager clusterManager;
	
	@Inject
	public DefaultAvatarManager(SettingManager settingManager, UserManager userManager, 
			EmailAddressManager emailAddressManager, ClusterManager clusterManager) {
		this.settingManager = settingManager;
		this.userManager = userManager;
		this.emailAddressManager = emailAddressManager;
		this.clusterManager = clusterManager;
	}
	
	public Object writeReplace() throws ObjectStreamException {
		return new ManagedSerializedForm(AvatarManager.class);
	}
	
	@Override
	public String getUserAvatarUrl(Long userId) {
		if (userId.equals(User.UNKNOWN_ID)) {
			return AVATARS_BASE_URL + "user.png";
		} else if (userId.equals(User.SYSTEM_ID)) {
			return AVATARS_BASE_URL + "onedev.png";
		} else {
			File uploadedFile = getUserUploadedFile(userId);
			if (uploadedFile.exists())
				return AVATARS_BASE_URL + "uploaded/users/" + userId + ".jpg?version=" + uploadedFile.lastModified();
			
			EmailAddressFacade emailAddress = emailAddressManager.findPrimaryFacade(userId);
			UserFacade user = userManager.findFacadeById(userId);
			if (emailAddress == null || !emailAddress.isVerified()) 
				return generateAvatar(user.getName(), null);
			else if (settingManager.getSystemSetting().isGravatarEnabled()) 
				return Gravatar.getURL(emailAddress.getValue(), GRAVATAR_SIZE);
			else  
				return generateAvatar(user.getName(), emailAddress.getValue());
		}
	}
	
	@Override
	public String getPersonAvatarUrl(PersonIdent personIdent) {
		if (personIdent.getName().equals(User.SYSTEM_NAME)) { 
			return AVATARS_BASE_URL + "onedev.png";
		} else {
			EmailAddressFacade emailAddress = emailAddressManager.findFacadeByValue(personIdent.getEmailAddress());
			if (emailAddress != null && emailAddress.isVerified()) { 
				return getUserAvatarUrl(emailAddress.getOwnerId());
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
		
		String normalizedPrimaryName;
		if (StringUtils.isBlank(primaryName))
			normalizedPrimaryName = "?";
		else
			normalizedPrimaryName = primaryName;
		
		String normalizedSecondaryName;
		if (StringUtils.isBlank(secondaryName))
			normalizedSecondaryName = normalizedPrimaryName;
		else
			normalizedSecondaryName = secondaryName;

		var generatedPath = "generated/" + encoded + ".png";
		var avatarFile = new File(Bootstrap.getSiteDir(), "assets/avatars/" + generatedPath);		
		var lockName = getGeneratedLockName(encoded);
		if (!read(lockName, () -> avatarFile.exists())) {
			write(lockName, () -> {
				try {
					String letters = getLetter(normalizedPrimaryName);
					BufferedImage bi = generate(letters, normalizedSecondaryName);
					createDir(avatarFile.getParentFile());
					write(bi, "PNG", avatarFile);
				} catch(NoSuchAlgorithmException | IOException e){
					throw new RuntimeException(e);
				}
				return null;
			});
		}
		
		return AVATARS_BASE_URL + generatedPath;
	}

	private String getGeneratedLockName(String encoded) {
		return "generated-avatar:" + encoded;
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
	public File getUserUploadedFile(Long userId) {
		return new File(Bootstrap.getSiteDir(), "assets/avatars/uploaded/users/" + userId + ".jpg");
	}

	private String getUserUploadedLockName(Long userId) {
		return "uploaded-user-avatar:" + userId;
	}
	
	@Override
	public void useUserAvatar(Long userId, String avatarData) {
		clusterManager.runOnAllServers(() -> write(getUserUploadedLockName(userId), () -> {
			File avatarFile = getUserUploadedFile(userId);
			createDir(avatarFile.getParentFile());
			writeToFile(avatarFile, avatarData);
			return null;
		}));
	}

	@Override
	public String getProjectAvatarUrl(Long projectId) {
		File avatarFile = getProjectUploadedFile(projectId);
		if (avatarFile.exists())  
			return AVATARS_BASE_URL + "uploaded/projects/" + projectId + ".jpg?version=" + avatarFile.lastModified();
		else
			return AVATARS_BASE_URL + "project.png";
	}

	@Override
	public void useProjectAvatar(Long projectId, String avatarData) {
		clusterManager.runOnAllServers(() -> write(getProjectUploadedLockName(projectId), () -> {
			File avatarFile = getProjectUploadedFile(projectId);
			createDir(avatarFile.getParentFile());
			writeToFile(avatarFile, avatarData);
			return null;
		}));		
	}

	@Override
	public File getProjectUploadedFile(Long projectId) {
		return new File(Bootstrap.getSiteDir(), "assets/avatars/uploaded/projects/" + projectId + ".jpg");
	}
	
	private String getProjectUploadedLockName(Long projectId) {
		return "uploaded-project-avatar:" + projectId;
	}
	
	@Override
	public void copyProjectAvatar(Long fromProjectId, Long toProjectId) {
		clusterManager.runOnAllServers(() -> {
			var fromFile = getProjectUploadedFile(fromProjectId);
			var toFile = getProjectUploadedFile(toProjectId);
			var readLockName = getProjectUploadedLockName(fromProjectId);
			if (read(readLockName, () -> fromFile.exists())) {
				var tempFile = new File(toFile.getParentFile(), UUID.randomUUID().toString());
				try {
					read(readLockName, () -> {
						try {
							FileUtils.copyFile(fromFile, tempFile);
						} catch (IOException e) {
							throw new RuntimeException(e);
						}
						return null;
					});
					write(getProjectUploadedLockName(toProjectId), () -> {
						FileUtils.moveFile(tempFile, toFile);
						return null;
					});
				} finally {
					FileUtils.deleteFile(tempFile);
				}
			}
			return null;
		});
	}

}
