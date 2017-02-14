package com.gitplex.server.web.component.avatar;

import org.apache.wicket.ajax.AjaxRequestTarget;

import com.gitplex.commons.wicket.AjaxEvent;
import com.gitplex.server.core.entity.Account;

public class AvatarChanged extends AjaxEvent {

	private final Account account;
	
	public AvatarChanged(AjaxRequestTarget target, Account account) {
		super(target);
		
		this.account = account;
	}

	public Account getAccount() {
		return account;
	}

}