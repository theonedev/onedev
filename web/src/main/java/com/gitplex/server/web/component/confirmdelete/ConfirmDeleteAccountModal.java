package com.gitplex.server.web.component.confirmdelete;

import org.apache.wicket.ajax.AjaxRequestTarget;

import com.gitplex.server.GitPlex;
import com.gitplex.server.manager.AccountManager;
import com.gitplex.server.model.Account;

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
		return "please input account name \"" + getAccount().getName() + "\" below to confirm deletion.";
	}

	protected abstract void onDeleted(AjaxRequestTarget target);
	
	protected abstract Account getAccount();
}
