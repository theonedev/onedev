package com.pmease.gitplex.web.component.confirmdelete;

import com.pmease.gitplex.core.model.User;

@SuppressWarnings("serial")
public abstract class ConfirmDeleteAccountModalBehavior extends ConfirmDeleteModalBehavior {

	public ConfirmDeleteAccountModalBehavior(ConfirmDeleteAccountModal modalPanel) {
		super(modalPanel);
	}

	@Override
	protected String getWarningMessage() {
		return "All repositories belonging to this account will also be deleted, please input account name below to confirm deletion.";
	}

	@Override
	protected String getConfirmInput() {
		return getAccount().getName();
	}

	protected abstract User getAccount();
	
}
