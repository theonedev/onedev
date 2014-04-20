package com.pmease.gitop.web.component.avatar;

import java.io.IOException;

import org.apache.tika.mime.MediaType;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.request.resource.DynamicImageResource;

import com.google.common.io.Files;
import com.pmease.commons.loader.AppLoader;
import com.pmease.gitop.core.Gitop;
import com.pmease.gitop.core.manager.UserManager;
import com.pmease.gitop.model.User;
import com.pmease.gitop.web.GitopWebApp;
import com.pmease.gitop.web.service.FileTypes;

public class AvatarImageResource extends DynamicImageResource {

	private static final long serialVersionUID = 1L;

	@Override
	protected byte[] getImageData(Attributes attributes) {
		PageParameters params = attributes.getParameters();
		long id = params.get("id").toLong();

		User user = AppLoader.getInstance(UserManager.class).get(id);

		if (user.getLocalAvatar().exists()) {
			byte[] imageData;
			try {
				imageData = Files.toByteArray(user.getLocalAvatar());
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
			MediaType mediaType = Gitop.getInstance(FileTypes.class).getMediaType(
					user.getLocalAvatar().getAbsolutePath(), imageData);
			setFormat(mediaType.getType());
			return imageData;
		} else {
			setFormat("image/jpg");
			return GitopWebApp.get().getDefaultUserAvatar();
		}
	}

}
