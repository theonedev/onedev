package com.gitplex.web.component.avatar;

import org.apache.wicket.ajax.AjaxRequestTarget;

import com.gitplex.core.entity.Account;
import com.gitplex.commons.wicket.AjaxEvent;

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