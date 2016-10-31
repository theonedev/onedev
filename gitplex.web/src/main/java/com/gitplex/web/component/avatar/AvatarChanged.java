package com.gitplex.web.component.avatar;

import org.apache.wicket.ajax.AjaxRequestTarget;

import com.gitplex.core.entity.Account;
import com.gitplex.commons.wicket.AjaxEvent;

public class AvatarChanged extends AjaxEvent {

	private final Account user;
	
	public AvatarChanged(AjaxRequestTarget target, Account user) {
		super(target);
		
		this.user = user;
	}

	public Account getUser() {
		return user;
	}

}