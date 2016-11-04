package com.gitplex.server.web.component.confirmdelete;

import org.apache.wicket.ajax.AjaxRequestTarget;

import com.gitplex.server.core.GitPlex;
import com.gitplex.server.core.entity.Account;
import com.gitplex.server.core.manager.AccountManager;

@SuppressWarnings("serial")
public abstract class ConfirmDeleteAccountModal extends ConfirmDeleteModal {

	public ConfirmDeleteAccountModal(AjaxRequestTarget target) {
		super(target);
	}

	@Override
	protected void doDelete(AjaxRequestTarget target) {
		GitPlex.getInstance(AccountManager.class).delete(getAccount());
		getSession().success("Account has been deleted");
		
		onDeleted(target);
	}

	@Override
	protected String getConfirmInput() {
		return getAccount().getName();
	}

	@Override
	protected String getWarningMessage() {
		return "All repositories, pull requests and comments belonging to this account will also be deleted, "
				+ "please input account name below to confirm deletion.";
	}

	protected abstract void onDeleted(AjaxRequestTarget target);
	
	protected abstract Account getAccount();
}
