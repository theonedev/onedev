package com.pmease.gitop.web.component.avatar;

import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.image.NonCachingImage;
import org.apache.wicket.model.IModel;

import com.pmease.gitop.web.util.Gravatar;

public class GravatarImage extends NonCachingImage {

	private static final long serialVersionUID = 1L;

	private static final int GRAVAR_IMAGE_SIZE = 256;

	public GravatarImage(String id, IModel<String> emailModel) {
		super(id, emailModel);
	}

	@Override
	protected void onComponentTag(ComponentTag tag) {
		String email = (String) getDefaultModelObject();
		if (email != null) {
			tag.put("src", Gravatar.getURL(email, GRAVAR_IMAGE_SIZE));
		} else {
			tag.put("src", "assets/img/empty-avatar.jpg");
		}
	}

	@Override
	protected boolean getStatelessHint() {
		return true;
	}
	
}
