package io.onedev.server.web.component.user.avatar;

import org.apache.wicket.ajax.AjaxRequestTarget;

import io.onedev.server.model.User;
import io.onedev.server.web.util.AjaxPayload;

public class UserAvatarChanged extends AjaxPayload {

	private final User user;
	
	public UserAvatarChanged(AjaxRequestTarget target, User user) {
		super(target);
		
		this.user = user;
	}

	public User getUser() {
		return user;
	}

}