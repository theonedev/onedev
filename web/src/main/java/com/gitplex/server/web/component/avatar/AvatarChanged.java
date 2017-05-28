package com.gitplex.server.web.component.avatar;

import org.apache.wicket.ajax.AjaxRequestTarget;

import com.gitplex.server.model.User;
import com.gitplex.server.web.util.AjaxPayload;

public class AvatarChanged extends AjaxPayload {

	private final User user;
	
	public AvatarChanged(AjaxRequestTarget target, User user) {
		super(target);
		
		this.user = user;
	}

	public User getUser() {
		return user;
	}

}