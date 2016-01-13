package com.pmease.gitplex.web.component.avatar;

import org.apache.wicket.ajax.AjaxRequestTarget;

import com.pmease.commons.wicket.AjaxEvent;
import com.pmease.gitplex.core.model.User;

public class AvatarChanged extends AjaxEvent {

	private final User user;
	
	public AvatarChanged(AjaxRequestTarget target, User user) {
		super(target);
		
		this.user = user;
	}

	public User getUser() {
		return user;
	}

}