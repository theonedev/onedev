package com.gitplex.server.web.component.avatar;

import org.apache.wicket.ajax.AjaxRequestTarget;

import com.gitplex.server.entity.Account;
import com.gitplex.server.web.util.AjaxEvent;

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