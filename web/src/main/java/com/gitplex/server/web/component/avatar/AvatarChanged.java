package com.gitplex.server.web.component.avatar;

import org.apache.wicket.ajax.AjaxRequestTarget;

import com.gitplex.server.model.Account;
import com.gitplex.server.web.util.AjaxPayload;

public class AvatarChanged extends AjaxPayload {

	private final Account account;
	
	public AvatarChanged(AjaxRequestTarget target, Account account) {
		super(target);
		
		this.account = account;
	}

	public Account getAccount() {
		return account;
	}

}