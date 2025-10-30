package io.onedev.server.web.avatar;

import com.google.common.base.Splitter;
import com.google.common.collect.Iterables;
import io.onedev.commons.bootstrap.Bootstrap;
import io.onedev.commons.loader.ManagedSerializedForm;
import io.onedev.commons.utils.FileUtils;
import io.onedev.commons.utils.StringUtils;
import io.onedev.server.cluster.ClusterService;
import io.onedev.server.service.EmailAddressService;
import io.onedev.server.service.SettingService;
import io.onedev.server.service.UserService;
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
public class DefaultAvatarService implements AvatarService, Serializable {

	private static final int GRAVATAR_SIZE = 256;
	
	private static final String AVATARS_BASE_URL = "/avatars/";
	
	private final SettingService settingService;
	
	private final UserService userService;
	
	private final EmailAddressService emailAddressService;
	
	private final ClusterService clusterService;
	
	@Inject
	public DefaultAvatarService(SettingService settingService, UserService userService,
								EmailAddressService emailAddressService, ClusterService clusterService) {
		this.settingService = settingService;
		this.userService = userService;
		this.emailAddressService = emailAddressService;
		this.clusterService = clusterService;
	}
	
	public Object writeReplace() throws ObjectStreamException {
		return new ManagedSerializedForm(AvatarService.class);
	}
	
	private String getFileExtension(File file) {
		return StringUtils.substringAfterLast(file.getName(), ".");
	}

	@Override
	public String getUserAvatarUrl(Long userId) {
		if (userId.equals(User.UNKNOWN_ID)) {
			return AVATARS_BASE_URL + "user.png";
		} else if (userId.equals(User.SYSTEM_ID)) {
			return AVATARS_BASE_URL + "onedev.png";
		} else {
			File uploadedFile = getUserUploadedFile(userId, null);
			if (uploadedFile.exists())
				return AVATARS_BASE_URL + "uploaded/users/" + userId + "." + getFileExtension(uploadedFile) + "?version=" + uploadedFile.lastModified();
			
			EmailAddressFacade emailAddress = emailAddressService.findPrimaryFacade(userId);
			UserFacade user = userService.findFacadeById(userId);
			if (emailAddress == null || !emailAddress.isVerified()) 
				return generateAvatar(user.getName(), null);
			else if (settingService.getSystemSetting().isUseAvatarService()) 
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
			EmailAddressFacade emailAddress = emailAddressService.findFacadeByValue(personIdent.getEmailAddress());
			if (emailAddress != null && emailAddress.isVerified()) { 
				return getUserAvatarUrl(emailAddress.getOwnerId());
			} else if (settingService.getSystemSetting().isUseAvatarService() 
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
	public File getUserUploadedFile(Long userId, String extension) {
		var pathSegments = "assets/avatars/uploaded/users/";
		if (extension != null) {
			return new File(Bootstrap.getSiteDir(), pathSegments + userId + "." + extension);
		} else {
			var file = new File(Bootstrap.getSiteDir(), pathSegments + userId + ".png");
			if (file.exists())
				return file;
			else
				return new File(Bootstrap.getSiteDir(), pathSegments + userId + ".jpg");
		}
	}

	private String getUserUploadedLockName(Long userId) {
		return "uploaded-user-avatar:" + userId;
	}
	
	@Override
	public void useUserAvatar(Long userId, String avatarData) {
		clusterService.runOnAllServers(() -> write(getUserUploadedLockName(userId), () -> {
			File avatarFile = getUserUploadedFile(userId, "png");
			createDir(avatarFile.getParentFile());
			if (avatarData != null) {
				writeToFile(avatarFile, avatarData);
			} else {
				if (avatarFile.exists())
					FileUtils.deleteFile(avatarFile);
				avatarFile = getUserUploadedFile(userId, "jpg");
				if (avatarFile.exists())
					FileUtils.deleteFile(avatarFile);
			}
			return null;
		}));
	}

	@Override
	public String getProjectAvatarUrl(Long projectId) {
		File avatarFile = getProjectUploadedFile(projectId, null);
		if (avatarFile.exists())  
			return AVATARS_BASE_URL + "uploaded/projects/" + projectId + "." + getFileExtension(avatarFile) + "?version=" + avatarFile.lastModified();
		else
			return AVATARS_BASE_URL + "project.png";
	}

	@Override
	public void useProjectAvatar(Long projectId, String avatarData) {
		clusterService.runOnAllServers(() -> write(getProjectUploadedLockName(projectId), () -> {
			File avatarFile = getProjectUploadedFile(projectId, "png");
			createDir(avatarFile.getParentFile());
			if (avatarData != null) {
				writeToFile(avatarFile, avatarData);
			} else {
				if (avatarFile.exists())
					FileUtils.deleteFile(avatarFile);
				avatarFile = getProjectUploadedFile(projectId, "jpg");
				if (avatarFile.exists())
					FileUtils.deleteFile(avatarFile);
			}
			return null;
		}));		
	}

	@Override
	public File getProjectUploadedFile(Long projectId, String extension) {
		var pathSegments = "assets/avatars/uploaded/projects/";
		if (extension != null) {
			return new File(Bootstrap.getSiteDir(), pathSegments + projectId + "." + extension);
		} else {
			var file = new File(Bootstrap.getSiteDir(), pathSegments + projectId + ".png");
			if (file.exists())
				return file;
			else
				return new File(Bootstrap.getSiteDir(), pathSegments + projectId + ".jpg");
		}
	}
	
	private String getProjectUploadedLockName(Long projectId) {
		return "uploaded-project-avatar:" + projectId;
	}
	
	@Override
	public void copyProjectAvatar(Long fromProjectId, Long toProjectId) {
		clusterService.runOnAllServers(() -> {
			var fromFile = getProjectUploadedFile(fromProjectId, null);
			var toFile = getProjectUploadedFile(toProjectId, getFileExtension(fromFile));
			var readLockName = getProjectUploadedLockName(fromProjectId);
			if (fromFile.exists()) {
				var tempFile = new File(toFile.getParentFile(), UUID.randomUUID().toString());
				try {
					read(readLockName, () -> {
						try {
							FileUtils.copyFile(fromFile, tempFile);
						} catch (IOException e) {
							throw new RuntimeException(e);
						}
					});
					write(getProjectUploadedLockName(toProjectId), () -> {
						try {
							FileUtils.moveFile(tempFile, toFile);
						} catch (IOException e) {
							throw new RuntimeException(e);
						}
					});
				} finally {
					FileUtils.deleteFile(tempFile);
				}
			}
			return null;
		});
	}

}
