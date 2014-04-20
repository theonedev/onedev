package com.pmease.gitop.web.component.avatar;

import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.WebComponent;

import com.pmease.gitop.core.Gitop;
import com.pmease.gitop.model.User;
import com.pmease.gitop.web.service.AvatarManager;

@SuppressWarnings("serial")
public class AvatarImage extends WebComponent {

	private final String url;
	
	public AvatarImage(String id, User user) {
		super(id);
		
		url = Gitop.getInstance(AvatarManager.class).getAvatarUrl(user);
	}

	public AvatarImage(String id, String emailAddress) {
		super(id);
		
		url = Gitop.getInstance(AvatarManager.class).getAvatarUrl(emailAddress);
	}

	@Override
	protected void onComponentTag(ComponentTag tag) {
		super.onComponentTag(tag);
		
		tag.setName("img");
		tag.put("src", url);
	}

}
