package com.pmease.gitplex.web.component.avatar;

import org.apache.wicket.ajax.AjaxRequestTarget;

import com.pmease.commons.wicket.AjaxEvent;
import com.pmease.gitplex.core.entity.Account;

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