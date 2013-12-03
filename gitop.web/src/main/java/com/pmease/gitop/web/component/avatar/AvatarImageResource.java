package com.pmease.gitop.web.component.avatar;

import java.io.File;
import java.io.IOException;

import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.request.resource.DynamicImageResource;

import com.google.common.base.Strings;
import com.google.common.base.Throwables;
import com.google.common.io.Files;
import com.pmease.commons.loader.AppLoader;
import com.pmease.gitop.core.manager.UserManager;
import com.pmease.gitop.model.User;
import com.pmease.gitop.web.GitopWebApp;
import com.pmease.gitop.web.SitePaths;
import com.pmease.gitop.web.component.avatar.AvatarImage.AvatarImageType;

public class AvatarImageResource extends DynamicImageResource {

	private static final long serialVersionUID = 1L;

	@Override
	protected byte[] getImageData(Attributes attributes) {
		PageParameters params = attributes.getParameters();
		AvatarImageType imageType = AvatarImageType.valueOf(params.get("type")
				.toString().toUpperCase());
		long id = params.get("id").toLong();
		File avatarFile = null;

		if (imageType == AvatarImageType.USER) {
			User user = AppLoader.getInstance(UserManager.class).get(id);

			if (!Strings.isNullOrEmpty(user.getAvatarUrl())) {
				avatarFile = new File(SitePaths.get().userAvatarDir(id),
						user.getAvatarUrl());
			}
		} else {
			// Repository project =
			// AppLoader.getInstance(RepositoryManager.class).get(id);
			// if (!Strings.isNullOrEmpty(project.getAvatarUrl())) {
			// avatarFile = new File(GitopWebApp.getProjectAvatarDir(id),
			// project.getAvatarUrl());
			// }
		}

		if (avatarFile != null && avatarFile.exists()) {
			setFormat("image/" + Files.getFileExtension(avatarFile.getName()));
			try {
				return Files.toByteArray(avatarFile);
			} catch (IOException e) {
				throw Throwables.propagate(e);
			}
		} else {
			setFormat("image/jpg");
			return GitopWebApp.get().getDefaultUserAvatar();
		}
	}

}
